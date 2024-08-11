FROM gradle:7.6-jdk17 AS build
WORKDIR /app

COPY . /app
RUN gradle build -x test --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/authentication/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

CMD ["./gradlew", "test"]