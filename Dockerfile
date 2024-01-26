FROM openjdk:17-slim
WORKDIR /app
COPY ./build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 9092