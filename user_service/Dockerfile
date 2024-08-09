# 1. Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY gradle /app/gradle
COPY gradlew /app/
COPY build.gradle /app/
COPY settings.gradle /app/
COPY src /app/src

# Build the application using the Gradle Wrapper
RUN ./gradlew clean
RUN ./gradlew build --no-daemon

# 2. Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
