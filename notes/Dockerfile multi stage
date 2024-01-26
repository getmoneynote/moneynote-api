# Stage 1: Build the application
FROM gradle:8.5-jdk17 as build
WORKDIR /workspace/app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle clean build

# Stage 2: Create the runtime image
FROM openjdk:17-oracle
WORKDIR /app

COPY --from=build /workspace/app/build/libs/*.jar /app/app.jar
EXPOSE 9092
ENTRYPOINT ["java", "-jar", "app.jar"]