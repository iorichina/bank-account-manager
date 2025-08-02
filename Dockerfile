# 多阶段构建Dockerfile
FROM gradle:8-jdk17-ubi-minimal AS build
WORKDIR /app
COPY . .
RUN gradle bootJar -x test --no-daemon


FROM eclipse-temurin:17-jre-ubi9-minimal
WORKDIR /app
RUN mkdir -p /app/lib
RUN mkdir -p /app/etc
RUN mkdir -p /app/log
COPY --from=build /app/build/libs/bank-account-manager.jar /app/lib/
#COPY --from=build /app/build/libs/lib/* /app/lib/
#COPY --from=build /app/src/main/resources/application.properties /app/etc/
#COPY --from=build /app/src/main/resources/logback.xml /app/etc/
#COPY --from=build /app/src/main/resources/schema.sql /app/etc/
EXPOSE 10086
ENV JAVA_OPTS_DEF="-server -Xms1g -Xmx1g -XX:NewRatio=2 -XX:+UseG1GC -Xlog:gc* -XX:MaxGCPauseMillis=200"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-$JAVA_OPTS_DEF} -jar /app/lib/bank-account-manager.jar"]