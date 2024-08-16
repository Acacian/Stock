# Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :eureka_server:build -x test --no-daemon

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/eureka_server/build/libs/*.jar app.jar
COPY --from=build /app/eureka_server/src/main/resources/application.yml /app/
COPY --from=build /app/eureka_server/src/test/resources/application-test.yml /app/

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

CMD java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE app.jar