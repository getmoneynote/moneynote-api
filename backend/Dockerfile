FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# 容器内保护机制:entrypoint.sh 作为 PID 1 循环拉起 java,
# 处理"配置保存后退出码 88 自动重启"与"被 kill/崩溃自动拉起"
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 9096