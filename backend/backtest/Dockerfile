# Stage 1: Build the application
FROM gradle:8.8.0-jdk21-alpine AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle bootJar

# Stage 2: Create the final image
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/backtest-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "backtest-0.0.1-SNAPSHOT.jar"]
