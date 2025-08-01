package iorihuang.bankaccountmanager.helper;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis distributed lock that supports automatic release via try-with-resources
 */
public class RedisLock implements AutoCloseable {
    private final StringRedisTemplate redisTemplate;
    private final String lockKey;
    private final String lockValue;
    private final long expireSeconds;
    private boolean locked;

    public RedisLock(StringRedisTemplate redisTemplate, String lockKey, long expireSeconds) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.lockValue = UUID.randomUUID().toString();
        this.expireSeconds = expireSeconds;
        this.locked = tryLock();
    }

    /**
     * Acquire the lock
     */
    private boolean tryLock() {
        if (null == redisTemplate) {
            return true;
        }
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Boolean success = ops.setIfAbsent(lockKey, lockValue, Duration.ofSeconds(expireSeconds));
        return Boolean.TRUE.equals(success);
    }

    /**
     * Check if the lock is acquired successfully
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Release the lock
     */
    @Override
    public void close() {
        if (null == redisTemplate) {
            return;
        }
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String val = ops.get(lockKey);
        if (locked && lockValue.equals(val)) {
            redisTemplate.delete(lockKey);
        }
    }
}

