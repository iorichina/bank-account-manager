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
docker run -e JAVA_OPTS="-Xmx512m -XX:+UseG1GC" -p 10086:10086 bank-account-manager
```

### 2. 访问接口

- 账户管理接口前缀：`/op/api/accounts/v1`
- H2控制台：`http://localhost:10086/h2-console`，JDBC URL: `jdbc:h2:mem:bankdb`

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

```bash
./gradlew test
```

### 5. 数据库建表

见`src/main/resources/schema.sql`

## 其他说明

- 默认端口：10086
- 字符集：UTF-8
- 支持本地缓存和嵌入式Redis
- 详细异常处理，返回结构化错误信息

---

