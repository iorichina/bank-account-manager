# 银行账户管理系统

## 简介

本项目为基于Spring Boot的银行账户管理RESTful服务，支持账户的创建、查询、更新、删除（状态删除）、分页、转账等功能，数据存储于H2内存数据库，支持本地Caffeine缓存和嵌入式Redis。

## 技术栈

- Java 17
- Spring Boot 3
- Spring Data JPA
- H2 内存数据库
- Caffeine 本地缓存
- Embedded Redis
- Gradle
- Docker（多阶段构建）

## 核心功能设计

注： 按要求所有接口均不会做权限校验

### 账户

- 为确保金融账户安全，每个操作都会使用分布式锁（锁粒度为用户级别）来保证并发安全，转账会同时锁两个账号。
- 分布式锁仅限单集群操作，如果是多地多活，需要使用更复杂的分布式锁实现，如Redisson等。
- 所有操作均最小化事务范围，避免大事务、长事务带来的性能问题，比如长期占用连接池连接。

### 金额

- 金额使用Decimal存储，接口交互精度与数据库存储精度拆分，数据库存储精度较大，是为了后续扩展，如果没有此类需求精度可以保持一致；
- 接口交互精度为小数点后6位（比如股票软件里不同场景会显示不同的小数位，这里假设只有一种场景），数据库存储精度为小数点后10位；

### 异常处理

- 定义全局父异常类、父错误类，自定义各类操作异常、错误
- 所有异常捕捉都封装为自定义异常、错误
- 强依赖的异常，封装为“错误”，比如数据库获取不到连接、连接断开等
- 其余异常封装为“异常”
- Controller增加全局异常处理器，捕捉所有异常并返回结构化错误信息
- “错误”将返回5xx状态码及错误信息
- “异常”将返回200状态码及错误信息

### 缓存

- 只在读取账户信息时，对账户本身进行缓存，其他数据不缓存
- 使用本地缓存Caffeine
- 针对账户信息场景，一般来说是第一次读取后一段时间内，会频繁读取，所以第一次读取就产生缓存，能有效提高缓存命中率
- 为了更好地监控命中率，默认会开启缓存监控（有稍微性能影响），可通过 `http://localhost:10086/actuator/caches` 查看命中率

- 分布式锁使用Embedded-Redis，但由于Embedded-Redis不稳定，默认不开启Redis缓存
- 若需要使用Embedded-Redis缓存，可通过配置`embedded-redis.server.enabled=true`开启

embedded-redis 可能在某些环境下不稳定，比如我在arm架构的ubuntu测试时，Embedded-Redis会报如下错误：

```log
/tmp/1754197611799-0/redis-server-2.8.19: /tmp/1754197611799-0/redis-server-2.8.19: cannot execute binary file
```

### 监控

- 使用Spring Boot Actuator提供的监控端点，暴露应用健康状态、指标等信息
- 集成Micrometer，支持Prometheus等监控系统，由于是单进程项目，这里不做上报，仅写日志

### 单元测试&基准测试

单元测试覆盖领域服务，核心路径全量覆盖；

代码级基准测试启动springboot容器执行吞吐量压测；

Jmeter基准测试；

### 限流熔断

没有很明确的场景，这个待定 todo

## 目录结构

```
bankaccountmanager
├── BankAccountManagerApplication - 入口类
├── config - 自动配置，负责初始化监控、启动Embedded-Redis以及其他组件
├── constant - 常量类，定义系统中使用的常量
├── controller - 控制层，负责处理HTTP请求，并调用领域服务
├── dto - 数据传输对象，定义接口的输入输出数据结构
├── exception - 异常处理，定义全局异常处理器和自定义异常类
├── helper - 辅助工具，定义工具类，如雪花ID类
├── model - 实体类，定义账户等领域模型
├── repository - 数据访问层，定义JPA仓库接口
├── service - 领域服务，定义账户管理的业务逻辑
├── util - 工具类，提供通用的功能，如IP转换等
```

### 配置

### 敏感信息

`application.properties`里面的数据库账号密钥，优先使用了环境变量的值，通常适合于大部分密钥管理要求；
也可以通过`application-prod.properties`这种形式覆盖，但需要启动命令指定环境`prod`；

但如果想要更安全，就需要配置`spring cloud config`或者aws加密管理服务等配置中心来管理敏感信息。

## 快速开始

### 1. 构建与运行

#### 本地运行

```bash
./gradlew bootJar
java -jar build/libs/bank-account-manager.jar
```

#### Docker运行

```bash
docker build -t bank-account-manager .
# 使用默认配置
docker run -p 10086:10086 bank-account-manager
# 使用自定义配置
docker run -e JAVA_OPTS="-server -Xms2g -Xmx2g -XX:NewRatio=2 -XX:+UseG1GC -Xlog:gc* -XX:MaxGCPauseMillis=200" -p 10086:10086 bank-account-manager
```

### 2. 访问接口

- 账户管理接口前缀：`/op/api/accounts/v1`
- H2控制台：`http://localhost:10086/h2-console`，JDBC URL: `jdbc:h2:mem:bankdb`
- 监控缓存命中率： `http://localhost:10086/actuator/metrics/cache.gets?tag=cache:account&tag=result:hit` /
  `http://localhost:10086/actuator/metrics/cache.gets?tag=cache:account`

### 3. 主要API

| 功能     | 方法     | 路径                                           | 说明         |
|--------|--------|----------------------------------------------|------------|
| 创建账户   | POST   | `/op/api/accounts/v1/create`                 | 创建新账户      |
| 删除账户   | DELETE | `/op/api/accounts/v1/delete/{accountNumber}` | 删除（状态删除）账户 |
| 修改账户   | PUT    | `/op/api/accounts/v1/update/{accountNumber}` | 修改账户信息     |
| 查询账户详情 | GET    | `/op/api/accounts/v1/{accountNumber}`        | 查询单个账户详情   |
| 分页查询账户 | GET    | `/op/api/accounts/v1/list?page=0&size=10`    | 分页查询账户列表   |
| 账户转账   | POST   | `/op/api/accounts/v1/transfer`               | 账户间转账      |

> 各接口请求与响应示例：
>
> **创建账户**
> - 路径：POST `/op/api/accounts/v1/create`
> - 请求体：
    >   ```json
    > {
    > "accountNumber": "10001",
    > "ownerId": "u001",
    > "ownerName": "张三",
    > "contactInfo": "13800000000",
    > "accountType": 1,
    > "initialBalance": "1000.00"
    > }
    >   ```
> - 响应体：
    >   ```json
    > {
    > "id": 1,
    > "accountNumber": "10001",
    > "accountType": 1,
    > "ownerId": "u001",
    > "ownerName": "张三",
    > "contactInfo": "13800000000",
    > "balance": 1000.00,
    > "state": 1
    > }
    >   ```
> - 示例请求：
    >   ```shell
    > curl 'http://127.0.0.1:10086/op/api/accounts/v1/create' \
    > -X POST \
    > -H 'Content-Type: application/json' \
    > -d '{
    >     "account_number":"A002",
    >     "account_type":1,
    >     "owner_id":"2L",
    >     "owner_name":"A2L",
    >     "contact_info":"A2LL",
    >     "initial_balance":"23.506"
    > }'
    >   ```
>
> **删除账户**
> - 路径：DELETE `/op/api/accounts/v1/delete/{accountNumber}`
> - 响应体：
    >   ```json
    > {
    > "id": 1,
    > "accountNumber": "10001"
    > }
    >   ```
> - 示例请求：
    >   ```shell
    >   curl 'http://127.0.0.1:10086/op/api/accounts/v1/delete/A003' \
    >   -X DELETE 
    >   ```
>
> **修改账户**
> - 路径：PUT `/op/api/accounts/v1/update/{accountNumber}`
> - 请求体：
    >   ```json
    > {
    > "ownerName": "李四",
    > "contactInfo": "13900000000"
    > }
    >   ```
> - 响应体：同“删除账户”响应体
> - 示例请求：
    >   ```shell
    >   curl 'http://127.0.0.1:10086/op/api/accounts/v1/update/A002' \
    >    -X PUT \
    >   -H 'Content-Type: application/json' \
    >   -d '{
    >       "owner_name":"A22L",
    >       "contact_info":"A22LL"
    >   }'
    >   ```
>
> **查询账户详情**
> - 路径：GET `/op/api/accounts/v1/{accountNumber}`
> - 响应体：同“创建账户”响应体
> - 示例请求：
    >   ```shell
    >   curl 'http://127.0.0.1:10086/info/api/accounts/v1/A002'
    >   ```
>
> **分页查询账户**
> - 路径：GET `/op/api/accounts/v1/list?last_id=920682284187648&size=10`
> - 请求参数：
    - `last_id`：上次查询的最后一个账户ID，用于分页
    - `size`：每页返回的账户数量，默认为10
> - 响应体：
    >   ```json
    > {
    > "elements": [
    > { "id": 1, "accountNumber": "10001", ... },
    > { "id": 2, "accountNumber": "10002", ... }
    > ],
    > "hasMore": false,
    > "lastId": 920682284187648
    > }
    >   ```
> - 示例请求：
    >   ```shell
    >   curl 'http://127.0.0.1:10086/info/api/accounts/v1/list?last_id=920682284187648'
    >   curl 'http://127.0.0.1:10086/info/api/accounts/v1/list'
    >   ```
>
> **账户转账**
> - 路径：POST `/op/api/accounts/v1/transfer`
> - 请求体：
    >   ```json
    > {
    > "fromAccountNumber": "10001",
    > "toAccountNumber": "10002",
    > "amount": "100.00"
    > }
    >   ```
> - 响应体：
    >   ```json
    > {
    > "from": { "accountNumber": "10001", "balance": 900.00 },
    > "to": { "accountNumber": "10002", "balance": 1100.00 }
    > }
    >   ```

### 4. 测试

#### 单元测试

核心路径全覆盖

```bash
./gradlew test
```

#### 代码级基准测试

当前是通过JMH实现的，直接执行任务即可完成基准测试。

```bash
./gradlew jmh
```

通过修改`build.gradle`中的`jmh`任务配置，可以调整基准测试的参数，如线程数、循环次数等。

```config
jmh {
    iterations = 1
    warmupIterations = 1
    timeOnIteration = '10m'
}
```

通过修改`benchmark/BankAccountServiceBenchmarkIface.java`文件中的常量控制每次执行测试需要测试的账户数量，默认10000。

#### JMeter基准测试

在`jmx`目录下，提供了JMeter测试脚本，可以直接导入JMeter进行基准测试。

### 5. 数据库

使用H2内存数据库，数据在应用运行时存储于内存中，重启后数据将丢失。
可通过H2控制台访问数据库，默认地址为`http://localhost:10086/h2-console`，JDBC URL为`jdbc:h2:mem:bankdb`。

建表DDL见`src/main/resources/schema.sql`

- 账户信息表，存储账户简单信息，如账户号、账户类型、持有人信息、地址等。
- 账户信息变更记录表，存储账户的变更记录，主要是不涉及账户余额的变更的操作记录，如开户、修改地址、删除等操作。
- 账户余额变更记录表，记录每个账户余额的变动，一次转账就是两条记录。
- 账户转账记录表，存储账户转账记录，在这里也是主要记录转账操作。

#### 账户信息表

本案例核心表，所有的功能都是围绕这个表进行

#### 账户信息变更记录表

这个信息表并不是很重要，可以考虑异步提交到分布式队列，定时任务消费的方式来实现记录。 todo

#### 账户余额变更记录表

账户余额的变更记录，对于期初期末的对账、旁路监控等都至关重要，

#### 账户转账记录表

理论上转账记录就可以导出用户余额变动记录，所以在转账事务中，其实可以仅保存账户信息表、账户转账记录表，然后通过异步提交、定时任务补偿的形式，将转账记录推送到分布式队列，再分布式消费，提高转账的性能。

## 其他说明

- 默认端口：10086
- 字符集：UTF-8
- 支持本地缓存和嵌入式Redis
- 详细异常处理，返回结构化错误信息

---

