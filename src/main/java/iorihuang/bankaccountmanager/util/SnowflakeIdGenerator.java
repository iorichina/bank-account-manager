package iorihuang.bankaccountmanager.util;

/**
 * Snowflake ID generator
 * 
 * Snowflake ID format:
 * 1 bit sign (0)
 * 41 bits timestamp
 * 10 bits machine ID (5 bits datacenter ID + 5 bits worker ID)
 * 12 bits sequence number
 */
public class SnowflakeIdGenerator {
    // Start timestamp (2020-01-01)
    private static final long START_TIMESTAMP = 1577836800000L;
    
    // Bit widths
    private static final long SEQUENCE_BIT = 12;
    private static final long MACHINE_BIT = 10;
    private static final long DATACENTER_BIT = 5;
    private static final long WORKER_BIT = 5;
    
    // Maximum values
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private static final long MAX_WORKER_NUM = ~(-1L << WORKER_BIT);
    
    // Bit shifts
    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;
    
    private final long datacenterId;  // Datacenter ID
    private final long workerId;      // Worker ID
    private long sequence = 0L;       // Sequence number
    private long lastTimestamp = -1L; // Last timestamp
    
    /**
     * Constructor
     * 
     * @param datacenterId Datacenter ID (0-31)
     * @param workerId Worker ID (0-31)
     */
    public SnowflakeIdGenerator(long datacenterId, long workerId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (workerId > MAX_WORKER_NUM || workerId < 0) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + MAX_WORKER_NUM + " or less than 0");
        }
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }
    
    /**
     * Generate next ID
     * 
     * @return Snowflake ID
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();
        
        // Clock moved backwards
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - currentTimestamp) + " milliseconds");
        }
        
        // Same millisecond
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // Sequence overflow
            if (sequence == 0) {
                currentTimestamp = getNextMillis(lastTimestamp);
            }
        } else {
            // Next millisecond, sequence starts from 0
            sequence = 0L;
        }
        
        lastTimestamp = currentTimestamp;
        
        return (currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT
                | datacenterId << DATACENTER_LEFT
                | workerId << MACHINE_LEFT
                | sequence;
    }
    
    /**
     * Get next millisecond
     * 
     * @param lastTimestamp Last timestamp
     * @return Next millisecond
     */
    private long getNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}