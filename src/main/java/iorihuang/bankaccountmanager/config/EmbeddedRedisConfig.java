package iorihuang.bankaccountmanager.config;

import com.github.fppt.jedismock.RedisServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * if port is used,  find the process `netstat -ano|grep LISTEN | grep 6379` and kill the process
 */
@Configuration
@Slf4j
public class EmbeddedRedisConfig {

    /**
     * for RedisCacheManager to register redis client
     *
     * @throws IOException
     */
    public RedisServer redisServer(@Value("${spring.redis.port:19737}") int redisPort) throws IOException {
        int port = redisPort;
        if (!isPortAvailable(port)) {
            log.error("Embedded-Redis port {} is already in use, use port+1", port);
            port++;
        }
        log.info("start Embedded-Redis starting with port {} ", port);
        RedisServer jedisMock = RedisServer
                .newRedisServer(port)
//                .setOptions(ServiceOptions.defaultOptions().withClusterModeEnabled())
                .start();

        System.out.println(jedisMock.isRunning());
        System.out.println(jedisMock.getBindPort());
        return jedisMock;
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
