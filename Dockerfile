FROM gcr.io/distroless/java17
WORKDIR /app
COPY ./build/libs/*.jar app.jar
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
EXPOSE 9092