package iorihuang.bankaccountmanager.config;

import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(SnowFlakeIdProperties.class)
public class SnowFlakeIdAutoConfig {
    @Bean(name = "idHelper")
    @ConditionalOnMissingBean
    public SnowFlakeIdHelper idHelper(SnowFlakeIdProperties properties) {
        LocalDateTime startTime = LocalDateTime.parse(properties.getStartTime());
        TimeUnit unit = switch (properties.getTimeUnit()) {
            case "NANOSECONDS" -> TimeUnit.NANOSECONDS;
            case "MICROSECONDS" -> TimeUnit.MICROSECONDS;
            case "SECONDS" -> TimeUnit.SECONDS;
            case "MINUTES" -> TimeUnit.MINUTES;
            case "HOURS" -> TimeUnit.HOURS;
            case "DAYS" -> TimeUnit.DAYS;
            default -> TimeUnit.MILLISECONDS;
        };
        long zoneId = properties.getTenantId();
        long nodeId = properties.getNodeId();
        long bitsOfTime = properties.getBitsOfTime();
        long bitsOfZone = properties.getBitsOfTenant();
        long bitsOfNode = properties.getBitsOfNode();
        long bitsOfAutoincrementMax = properties.getBitsOfAutoincrement();

        if (properties.isUseCache()) {
            return new SnowFlakeIdHelper(startTime, unit, zoneId, nodeId, bitsOfTime, bitsOfZone, bitsOfNode, bitsOfAutoincrementMax, properties.getMaximumSize(), properties.isRecordStats());
        }
        return new SnowFlakeIdHelper(startTime, unit, zoneId, nodeId, bitsOfTime, bitsOfZone, bitsOfNode, bitsOfAutoincrementMax, properties.getRecyclableLongMaxTry());
    }

    @Bean(name = "verHelper")
    @ConditionalOnMissingBean
    public SnowFlakeIdHelper verHelper(SnowFlakeIdProperties properties) {
        LocalDateTime startTime = LocalDateTime.parse(properties.getStartTime());
        TimeUnit unit = switch (properties.getTimeUnit()) {
            case "NANOSECONDS" -> TimeUnit.NANOSECONDS;
            case "MICROSECONDS" -> TimeUnit.MICROSECONDS;
            case "SECONDS" -> TimeUnit.SECONDS;
            case "MINUTES" -> TimeUnit.MINUTES;
            case "HOURS" -> TimeUnit.HOURS;
            case "DAYS" -> TimeUnit.DAYS;
            default -> TimeUnit.MILLISECONDS;
        };
        long zoneId = properties.getTenantId();
        long nodeId = properties.getNodeId();
        long bitsOfTime = properties.getBitsOfTime();
        long bitsOfZone = properties.getBitsOfTenant();
        long bitsOfNode = properties.getBitsOfNode();
        long bitsOfAutoincrementMax = properties.getBitsOfAutoincrement();

        if (properties.isUseCache()) {
            return new SnowFlakeIdHelper(startTime, unit, zoneId, nodeId, bitsOfTime, bitsOfZone, bitsOfNode, bitsOfAutoincrementMax, properties.getMaximumSize(), properties.isRecordStats());
        }
        return new SnowFlakeIdHelper(startTime, unit, zoneId, nodeId, bitsOfTime, bitsOfZone, bitsOfNode, bitsOfAutoincrementMax, properties.getRecyclableLongMaxTry());
    }
}
