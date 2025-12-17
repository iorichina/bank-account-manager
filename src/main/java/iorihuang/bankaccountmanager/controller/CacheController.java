package iorihuang.bankaccountmanager.controller;

import io.micrometer.observation.annotation.Observed;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cache Information Query Interface
 */
@RestController
@RequestMapping("/info/api/cache/v1")
@RequiredArgsConstructor
@Slf4j
@Observed(name = "cache.controller")
public class CacheController extends BaseController {
    private final StringRedisTemplate redisTemplate;

    /**
     * Test cache set
     */
    @GetMapping("/set")
    public ResponseEntity<?> set() throws AccountError, AccountException {
        redisTemplate.opsForValue().set("test", String.valueOf(System.currentTimeMillis()));
        return buildResponse(null);
    }

    /**
     * Query redis test data
     */
    @GetMapping("/get")
    public ResponseEntity<?> get() throws AccountError, AccountException {
        Object o = redisTemplate.opsForValue().get("test");
        return buildResponse(o);
    }

}