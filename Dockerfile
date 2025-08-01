# 多阶段构建Dockerfile
FROM gradle:8.7.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/bank-account-manager.jar /app/lib/bank-account-manager.jar
EXPOSE 10086
ENV JAVA_OPTS_DEF="-Xms1g -Xmx1g -XX:NewRatio=2 -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-$JAVA_OPTS_DEF} -jar /app/lib/bank-account-manager.jar"]