package iorihuang.bankaccountmanager.helper.snowflakeid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SnowFlakeIdHelperTest {
    @Test
    void testGenIdAndParseTime_InMillis_seconds() {
        System.out.println(System.currentTimeMillis());
        System.out.println(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
        System.out.println(8 * 60 * 60 * 1000);
        System.out.println();

        LocalDateTime start = LocalDateTime.now();
        SnowFlakeIdHelper helper = new SnowFlakeIdHelper(start, TimeUnit.SECONDS, 1, 1, 32, 5, 5, 12, 10);
        long id = helper.genId();
        System.out.println(helper.getStartTime());
        System.out.println(helper.getMaxTimeNum());
        long timeMillis = helper.parseTimeInMillis(id);
        long now = System.currentTimeMillis(); // 精度为秒
        Assertions.assertTrue(Math.abs(timeMillis - now) < 2000, String.format("时间戳解析不正确: \n期望 %d(±2000), \n实际 %d", now, timeMillis));
    }

    @Test
    void testGenIdAndParseTimeInMillisMillis_() {
        LocalDateTime start = LocalDateTime.now();
        SnowFlakeIdHelper helper = new SnowFlakeIdHelper(start, TimeUnit.MILLISECONDS, 2, 3, 41, 4, 4, 10, 10);
        long id = helper.genId();
        long timeMillis = helper.parseTimeInMillis(id);
        long now = System.currentTimeMillis();
        Assertions.assertTrue(Math.abs(timeMillis - now) < 1000, "时间戳解析不正确");
    }

    @Test
    void testGenIdWithCustomTime() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime custom = LocalDateTime.of(2024, 7, 19, 12, 0);
        SnowFlakeIdHelper helper = new SnowFlakeIdHelper(start, TimeUnit.MILLISECONDS, 0, 0, 41, 4, 4, 10, 10);
        long id = helper.genId(custom);
        long timeMillis = helper.parseTimeInMillis(id);
        long expected = custom.toInstant(helper.getOffset()).toEpochMilli();
        Assertions.assertTrue(Math.abs(timeMillis - expected) < 10, "自定义时间戳解析不正确");
    }

    @Test
    void testDifferentConfigIdUniqueness() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        SnowFlakeIdHelper helper1 = new SnowFlakeIdHelper(start, TimeUnit.MILLISECONDS, 1, 1, 41, 4, 4, 10, 10);
        SnowFlakeIdHelper helper2 = new SnowFlakeIdHelper(start, TimeUnit.MILLISECONDS, 2, 2, 41, 4, 4, 10, 10);
        long id1 = helper1.genId();
        long id2 = helper2.genId();
        Assertions.assertNotEquals(id1, id2, "不同配置下ID应不同");
    }

    @Test
    void testParseTimeInMillisBoundary_seconds() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        SnowFlakeIdHelper helper = new SnowFlakeIdHelper(start, TimeUnit.SECONDS, 0, 0, 32, 5, 5, 12, 10);
        long id = helper.genId(start);
        long time = helper.parseTimeInMillis(id) / 1000;
        long expected = start.toEpochSecond(ZoneOffset.ofHours(8));
        Assertions.assertEquals(expected, time, "秒级边界时间戳解析不正确");
    }

    @Test
    void testParseTimeInMillisMillisBoundary_() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        SnowFlakeIdHelper helper = new SnowFlakeIdHelper(start, TimeUnit.MILLISECONDS, 0, 0, 41, 4, 4, 10, 10);
        long id = helper.genId(start);
        long time = helper.parseTimeInMillis(id);
        long expected = start.toInstant(helper.getOffset()).toEpochMilli();
        Assertions.assertEquals(expected, time, "毫秒级边界时间戳解析不正确");
    }

    @Test
    void testParseTimeInMillisFuture() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime future = LocalDateTime.of(2030, 1, 1, 0, 0);
        SnowFlakeIdHelper helper = new SnowFlakeIdHelper(start, TimeUnit.MILLISECONDS, 0, 0, 41, 4, 4, 10, 10);
        long id = helper.genId(future);
        long time = helper.parseTimeInMillis(id);
        long expected = future.toInstant(helper.getOffset()).toEpochMilli();
        Assertions.assertEquals(expected, time, "未来时间戳解析不正确");
    }

    @Test
    void testParseTimeInMillisReverse() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        SnowFlakeIdHelper helper = new SnowFlakeIdHelper(start, TimeUnit.MILLISECONDS, 1, 1, 41, 4, 4, 10, 10);
        long id = helper.genId();
        long time = helper.parseTimeInMillis(id);
        long id2 = helper.genId(LocalDateTime.ofEpochSecond(time / 1000, (int) (time % 1000) * 1000000, helper.getOffset()));
        Assertions.assertNotEquals(0, id2, "反向生成ID应有效");
    }

    /**
     * Test for parseTime method
     */
    @Test
    void testParseTime() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now();
        TimeUnit unit = TimeUnit.MILLISECONDS;
        long tenantId = 1;
        long nodeId = 1;
        long bitsOfTime = 40;
        long bitsOfTenant = 3;
        long bitsOfNode = 8;
        long bitsOfAutoincrement = 12;
        int recyclableLongMaxTry = 10;

        SnowFlakeIdHelper snowFlakeIdHelper = new SnowFlakeIdHelper(startTime, unit, tenantId, nodeId, bitsOfTime, bitsOfTenant, bitsOfNode, bitsOfAutoincrement, recyclableLongMaxTry);

        // Act
        long id = snowFlakeIdHelper.genId();
        LocalDateTime parsedTime = snowFlakeIdHelper.parseTime(id);

        // Assert
        assertNotNull(parsedTime);
        // Additional assertions can be added based on expected behavior
        assertEquals(startTime.toInstant(OffsetTime.now().getOffset()).toEpochMilli() / 10, parsedTime.toInstant(OffsetTime.now().getOffset()).toEpochMilli() / 10, "Parsed time should match the original time");
    }
}
