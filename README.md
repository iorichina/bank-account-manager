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

- API文档见`/api/accounts`相关接口
- H2控制台：`http://localhost:10086/h2-console`，JDBC URL: `jdbc:h2:mem:bankdb`

### 3. 主要API

- 创建账户：POST `/api/accounts`
- 删除账户：DELETE `/api/accounts/{accountNumber}`
- 修改账户：PUT `/api/accounts/{accountNumber}`
- 查询账户详情：GET `/api/accounts/{accountNumber}`
- 分页查询账户：GET `/api/accounts?page=0&size=10`
- 账户转账：POST `/api/accounts/transfer`

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
如需Kubernetes部署，可参考Docker镜像并编写相应yaml。

