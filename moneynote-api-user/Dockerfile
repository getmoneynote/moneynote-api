#FROM arm64v8/openjdk:17.0.1-slim
FROM openjdk:17-slim
COPY build/libs/moneynote-api-user-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]