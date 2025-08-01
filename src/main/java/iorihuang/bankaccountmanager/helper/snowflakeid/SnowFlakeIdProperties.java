package iorihuang.bankaccountmanager.helper.snowflakeid;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "snowflakeid")
public class SnowFlakeIdProperties {
    /**
     * start time of snowflake id, default to 2025-07-19T00:00:00
     */
    private String startTime = "2025-07-31T00:00:00";
    /**
     * NANOSECONDS
     * MICROSECONDS
     * MILLISECONDS
     * SECONDS
     * MINUTES
     * HOURS
     * DAYS
     */
    private String timeUnit = "SECONDS";
    /**
     * tenant id and node id are used to generate distributed unique
     */
    private long tenantId = 0;
    /**
     * -1 means we will be set to last `bitsOfNode` bits of a long value of local ipv4 address
     */
    private long nodeId = -1;

    /**
     * recommended bits of snowflake id:
     * ------------------
     * sign bit(1)+
     * time bits(default 40 with max 34 years in millis)+
     * tenant bits(default 3 bits with max value 7)+
     * node bit(default 8 bits with max value 255)+
     * autoincrement(default 12 bits with max value 4095)
     * ------------------ or
     * sign bit(1)+
     * time bits(default 31 with max 68 years in seconds)+
     * tenant bits(default 3 bits with max value 7)+
     * node bit(default 8 bits with max value 255)+
     * autoincrement(default 21 bits with max value 2,097,151)
     */
    private int bitsOfTime = 31;
    private int bitsOfTenant = 3;
    private int bitsOfNode = 8;
    private int bitsOfAutoincrement = 21;

    /**
     * use loading-cache for recyclable long, default to true
     */
    private boolean useCache = true;
    private int maximumSize = 1024;//such as 1024 ms has 1024 sequence to generate recyclable long
    private boolean recordStats = true;
    /**
     * if useCache=false, set CAS try times while recyclable long fail reset to 0, use synchronized lock after max try
     */
    private int recyclableLongMaxTry = 1000;

}
