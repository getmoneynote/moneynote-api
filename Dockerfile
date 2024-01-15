#FROM arm64v8/openjdk:17.0.1-slim
#FROM openjdk:17-slim
FROM openjdk:17-oracle
COPY ./build/libs/moneynote-api-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
