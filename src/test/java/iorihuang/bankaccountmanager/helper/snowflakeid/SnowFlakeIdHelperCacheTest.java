package iorihuang.bankaccountmanager.helper.snowflakeid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class SnowFlakeIdHelperCacheTest {
    @Test
    void testGenIdWithCache() throws InterruptedException {
        SnowFlakeIdHelper helper = new SnowFlakeIdHelper(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                TimeUnit.MILLISECONDS,
                1, 1, 40, 3, 8, 12,
                1024, true
        );
        Set<Long> ids = new HashSet<>();
        ExecutorService executorService = Executors.newWorkStealingPool();
        for (int i = 0; i < 4000; i++) {
            executorService.submit(() -> {
                long id = helper.genId();
                if (!ids.add(id)) {
                    Assertions.fail("ID重复: " + id + "; " + helper.parseTimeInMillis(id));
                }
            });
        }
        long timeMillis = System.currentTimeMillis();
        System.out.println("timeMillis:" + timeMillis);
        for (int i = 0; i < 9000; i++) {
            executorService.submit(() -> {
                long id = helper.genId(timeMillis);
                if (!ids.add(id)) {
                    Assertions.fail("ID重复: " + id + "; " + helper.parseTimeInMillis(id));
                }
            });
        }
        // 生成1000个ID，检查唯一性
        for (int i = 0; i < 4000; i++) {
            long id = helper.genId();
            Assertions.assertTrue(ids.add(id), "ID重复: " + id + "; " + helper.parseTimeInMillis(id));
            long ts = helper.parseTimeInMillis(id);
            Assertions.assertTrue(ts > 0, "解析时间戳失败");
        }
        // 跨时间单位生成ID，sequence应重置
        long id1 = helper.genId();
        Thread.sleep(2); // 保证跨毫秒
        long id2 = helper.genId();
        Assertions.assertNotEquals(id1, id2);
        // 检查解析时间戳
        long t1 = helper.parseTimeInMillis(id1);
        long t2 = helper.parseTimeInMillis(id2);
        Assertions.assertTrue(Math.abs(t2 - t1) >= 1);
    }

    @Test
    void testGenIdWithCache_SecondsUnit() throws InterruptedException {
        SnowFlakeIdHelper helper = new SnowFlakeIdHelper(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                TimeUnit.SECONDS,
                1, 1, 31, 3, 8, 21,
                1024, true
        );
        long id1 = helper.genId();
        Thread.sleep(1100); // 跨秒
        long id2 = helper.genId();
        Assertions.assertNotEquals(id1, id2);
        long t1 = helper.parseTimeInMillis(id1);
        long t2 = helper.parseTimeInMillis(id2);
        Assertions.assertTrue(Math.abs(t2 - t1) >= 1000);
    }
}

