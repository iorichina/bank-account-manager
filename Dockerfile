# 多阶段构建Dockerfile
FROM gradle:8-jdk17-ubi-minimal AS build
WORKDIR /app

# 1. 先复制Gradle配置文件（不常变动）
COPY build.gradle settings.gradle ./

# 2. 创建空的源代码结构，执行依赖解析（仅配置文件变动时重新执行）
# 仅下载依赖，不执行构建 只要 build.gradle或settings.gradle没有变动，就不会重新下载依赖
RUN mkdir -p src/main/java && \
    mkdir -p src/main/resources && \
    gradle dependencies --no-daemon  

# 3. 复制实际源代码（频繁变动，但不影响依赖层缓存）
COPY src ./src

#COPY . . #利用前面缓存，加快构建
RUN gradle bootJar -x test --no-daemon


FROM eclipse-temurin:17-jre-ubi9-minimal
WORKDIR /app
RUN mkdir -p /app/lib && \
    mkdir -p /app/etc && \
    mkdir -p /app/log 
COPY --from=build /app/build/libs/bank-account-manager.jar /app/lib/
#COPY --from=build /app/build/libs/lib/* /app/lib/
#COPY --from=build /app/src/main/resources/application.properties /app/etc/
#COPY --from=build /app/src/main/resources/logback.xml /app/etc/
#COPY --from=build /app/src/main/resources/schema.sql /app/etc/
EXPOSE 10086
ENV JAVA_OPTS_DEF="-server -Xms1g -Xmx1g -XX:NewRatio=2 -XX:+UseG1GC -Xlog:gc* -XX:MaxGCPauseMillis=200"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-$JAVA_OPTS_DEF} -jar /app/lib/bank-account-manager.jar"]