package iorihuang.bankaccountmanager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

import java.net.ServerSocket;

/**
 * if port is used,  find the process `netstat -ano|grep LISTEN | grep 6379` and kill the process
 */
@Configuration
@Slf4j
public class EmbeddedRedisConfig {
    @Value("${spring.redis.port:6379}")
    private int redisPort;

    /**
     * warning: sometimes the embedded redis destroy without port release
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = "embedded-redis.server.enabled", havingValue = "true")
    public RedisServer redisServer() {
        int port = redisPort;
        if (!isPortAvailable(port)) {
            log.error("Embedded-Redis port {} is already in use, skip", port);
            return null;
        }
        log.info("start Embedded-Redis starting with port {} ", port);
        return new RedisServerBuilder().port(port).setting("maxmemory 32M").build();
    }

    /**
     * Check if the port is available
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
