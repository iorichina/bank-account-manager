# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a Spring Boot-based bank account management RESTful service that supports account creation, querying, updating, deletion (soft delete), pagination, and transfers. Data is stored in an H2 in-memory database with support for local Caffeine caching and embedded Redis.

## Architecture

### Core Components

1. **Controllers** - Handle HTTP requests:
   - `BankAccountController` - Main operations (create, delete, update, transfer)
   - `BankAccountInfoController` - Read operations (get, list)
   - `BankAccountBalanceController` - Balance-related operations

2. **Service Layer**:
   - `BankAccountService` - Interface defining account operations
   - `BankAccountServiceImpl` - Implementation with business logic including distributed locking, validation, and transaction management

3. **Repository Layer**:
   - `BankAccountRepository` - JPA repository for account data access
   - `BankAccountTrans` - Transactional operations for complex operations involving multiple entities

4. **Data Model**:
   - `BankAccount` - Main account entity
   - Related entities: `BankAccountBalanceLog`, `BankAccountChangeLog`, `BankAccountTransferLog`

5. **Configuration**:
   - `EmbeddedRedisConfig` - Embedded Redis setup
   - `MonitoringConfig` - Micrometer tracing configuration
   - `SnowFlakeIdAutoConfig` - Snowflake ID generator configuration

### Key Features

1. **Concurrency Control**:
   - Distributed locking using Redis for account operations
   - Optimistic locking with version numbers
   - Repeatable read isolation level for transactions

2. **Caching**:
   - Caffeine local cache for individual account queries
   - Cache eviction on account updates/deletes

3. **Error Handling**:
   - Global exception handler in `GlobalExceptionHandler`
   - Custom exceptions for different error scenarios
   - Distinction between "errors" (5xx) and "exceptions" (200 with error payload)

4. **Monitoring**:
   - Micrometer tracing annotations
   - Actuator endpoints for cache monitoring

## Common Development Tasks

### Building and Running

```bash
# Build the application
./gradlew bootJar

# Run the application
java -jar build/libs/bank-account-manager.jar

# Run unit tests
./gradlew test

# Run JMH benchmarks
./gradlew jmh
```

### Docker

```bash
# Build Docker image
docker build -t bank-account-manager .

# Run with default configuration
docker run -p 10086:10086 bank-account-manager
```

### Testing Individual Components

Unit tests are located in `src/test/java/iorihuang/bankaccountmanager/service/` and use JUnit 5 with Mockito for mocking dependencies.

### Benchmark Testing

JMH benchmarks are implemented in `src/test/java/iorihuang/bankaccountmanager/benchmark/`:
- Individual operation benchmarks (create, get, update, delete, transfer, list)
- Shared Spring context to avoid multiple Spring Boot startups
- Run with `./gradlew jmh`

## Important Notes

1. **Port**: Application runs on port 10086 by default
2. **Database**: H2 in-memory database accessible at `/h2-console`
3. **Embedded Redis**: Disabled by default due to stability issues on some platforms
4. **Caching**: Account queries are cached, updates trigger cache eviction
5. **Transaction Management**: All operations use repeatable read isolation level
6. **Distributed Locking**: Redis-based locking for concurrent account operations