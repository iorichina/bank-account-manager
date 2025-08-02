package iorihuang.bankaccountmanager.helper.snowflakeid;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import iorihuang.bankaccountmanager.util.NetworkUtils;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * recommended bits of snowflake id:
 * ------------------
 * sign bit(1)+
 * time bits(default 40 with max 34 years in millis)+
 * tenant bits(default 3 bits with max value 7)+
 * node bit(default 8 bits with max value 255)+
 * autoincrement(default 12 bits with max value 4095)
 * ------------------
 * sign bit(1)+
 * time bits(default 31 with max 68 years in seconds)+
 * tenant bits(default 3 bits with max value 7)+
 * node bit(default 8 bits with max value 255)+
 * autoincrement(default 21 bits with max value 2,097,151)
 */
@Getter
public class SnowFlakeIdHelper {
    private final long startTime;
    private final TimeUnit unit;
    private final long tenantId;
    private final long nodeId;

    private final long bitsOfTime;
    private final long bitsOfTenant;
    private final long bitsOfNode;
    private final long bitsOfAutoincrement;

    private final long maxTimeNum;
    private final long maxTenantNum;
    private final long maxNodeNum;
    private final long maxAutoincrementNum;

    private final long leftOfNode;
    private final long leftOfTenant;
    private final long leftOfTime;

    private final Cache<Long, AtomicLong> sequenceCache;
    private final ZoneOffset offset;
    private final RecyclableAtomicLong sequence;

    /**
     * using loading-cache to store sequence
     */
    public SnowFlakeIdHelper(LocalDateTime startTime, TimeUnit unit, long tenantId, long nodeId, long bitsOfTime, long bitsOfTenant, long bitsOfNode, long bitsOfAutoincrement, int maximumSize, boolean recordStats) {
        this.offset = OffsetDateTime.now().getOffset();
        Instant instant = startTime.toInstant(this.offset);
        this.startTime = unit.convert(instant.getEpochSecond() * 1_000_000_000L + instant.getNano(), TimeUnit.NANOSECONDS);
        this.unit = unit;
        this.bitsOfTime = bitsOfTime;
        this.bitsOfTenant = bitsOfTenant;
        this.bitsOfNode = bitsOfNode;
        this.bitsOfAutoincrement = bitsOfAutoincrement;

        this.maxTimeNum = -1L ^ (-1L << bitsOfTime);
        this.maxTenantNum = -1L ^ (-1L << bitsOfTenant);
        this.maxNodeNum = -1L ^ (-1L << bitsOfNode);
        this.maxAutoincrementNum = -1L ^ (-1L << bitsOfAutoincrement);

        this.tenantId = tenantId & this.maxTenantNum;
        this.nodeId = genNodeId(nodeId, this.maxNodeNum);

        this.leftOfNode = bitsOfAutoincrement;
        this.leftOfTenant = this.leftOfNode + bitsOfNode;
        this.leftOfTime = this.leftOfTenant + bitsOfTenant;

        this.sequence = null;
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().expireAfterWrite(maximumSize, unit).maximumSize(maximumSize);
        if (recordStats) {
            caffeine.recordStats();
        }
        this.sequenceCache = caffeine.build();
    }

    public SnowFlakeIdHelper(LocalDateTime startTime, TimeUnit unit, long tenantId, long nodeId, long bitsOfTime, long bitsOfTenant, long bitsOfNode, long bitsOfAutoincrement, int recyclableLongMaxTry) {
        this.offset = OffsetDateTime.now().getOffset();
        Instant instant = startTime.toInstant(this.offset);
        this.startTime = unit.convert(instant.getEpochSecond() * 1_000_000_000L + instant.getNano(), TimeUnit.NANOSECONDS);
        this.unit = unit;
        this.bitsOfTime = bitsOfTime;
        this.bitsOfTenant = bitsOfTenant;
        this.bitsOfNode = bitsOfNode;
        this.bitsOfAutoincrement = bitsOfAutoincrement;

        this.maxTimeNum = -1L ^ (-1L << bitsOfTime);
        this.maxTenantNum = -1L ^ (-1L << bitsOfTenant);
        this.maxNodeNum = -1L ^ (-1L << bitsOfNode);
        this.maxAutoincrementNum = -1L ^ (-1L << bitsOfAutoincrement);

        this.tenantId = tenantId & this.maxTenantNum;
        this.nodeId = genNodeId(nodeId, this.maxNodeNum);

        this.leftOfNode = bitsOfAutoincrement;
        this.leftOfTenant = this.leftOfNode + bitsOfNode;
        this.leftOfTime = this.leftOfTenant + bitsOfTenant;

        this.sequence = new RecyclableAtomicLong(this.maxAutoincrementNum, recyclableLongMaxTry);
        this.sequenceCache = null;
    }

    private long genNodeId(long nodeId, long maxNodeNum) {
        if (-1 != nodeId) {
            return nodeId & maxNodeNum;
        }
        //use last bitsOfNode of long value of ipv4 as nodeId
        String ipv4 = NetworkUtils.getLocalIpV4();
        if (null != ipv4) {
            nodeId = NetworkUtils.ipV4ToLong(ipv4);
        } else {
            //fallback if we can't get ipv4
            nodeId = ThreadLocalRandom.current().nextLong(maxNodeNum + 1);
        }
        return nodeId & maxNodeNum;
    }

    /**
     * get sequence by unitTime in current unit
     */
    private long getSequence(long unitTime) {
        if (null == sequenceCache) {
            return sequence.getAndIncrementWithRecycle();
        }
        return sequenceCache.get(unitTime, k -> loadSequence(k)).getAndIncrement();
    }

    /**
     * load sequence cache with initial value 0
     */
    private AtomicLong loadSequence(long unitTime) {
        return new AtomicLong(0);
    }

    /**
     * generate next ID in current timestamp
     */
    public long genId() {
        return genId(System.currentTimeMillis());
    }

    /**
     * generate next ID with special time in millis
     *
     * @param timeInMillis time in milliseconds since epoch in local timezone
     */
    public long genId(long timeInMillis) {
        long time = unit.convert(timeInMillis, TimeUnit.MILLISECONDS);
        long sequence = getSequence(time);
        // if sequence overflow, we need to wait for next time
        while (sequence > maxAutoincrementNum) {
            time++;
            sequence = getSequence(time);
        }
        return genId(time, sequence);
    }

    /**
     * generate next ID with special local date time
     */
    public long genId(LocalDateTime localDateTime) {
        Instant instant = localDateTime.toInstant(offset);
        return genId(instant.getEpochSecond() * 1_000_000_000L + instant.getNano(), TimeUnit.NANOSECONDS);
    }

    /**
     * generate next ID with special time and time unit
     */
    public long genId(long time, TimeUnit unit) {
        time = this.unit.convert(time, unit);
        long sequence = getSequence(time);
        // if sequence overflow, we need to wait for next time
        while (sequence > maxAutoincrementNum) {
            time++;
            sequence = getSequence(time);
        }
        return genId(time, sequence);
    }

    /**
     * generate next ID with special time and sequence
     */
    private long genId(long time, long sequence) {
        return ((time - startTime) << leftOfTime)
                | (tenantId << leftOfTenant)
                | (nodeId << leftOfNode)
                | (sequence & maxAutoincrementNum);
    }

    /**
     * parse timestamp from ID
     */
    public long parseTimeInMillis(long id) {
        return unit.toMillis(startTime + (id >>> leftOfTime));
    }

    /**
     * parse localdatetime from ID
     */
    public LocalDateTime parseTime(long id) {
        long nanos = unit.toNanos(startTime + (id >>> leftOfTime));
        return LocalDateTime.ofEpochSecond(TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS),
                (int) (nanos % 1_000_000_000), offset);
    }

}
