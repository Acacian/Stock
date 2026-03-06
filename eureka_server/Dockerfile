# 빌드 단계
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :eureka_server:build -x test --no-daemon

# 실행 단계
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/eureka_server/build/libs/*.jar /app/eureka_server.jar
COPY eureka_server/src/main/resources/application.yml /app/eureka_server_application.yml
COPY eureka_server/start.sh /app/start.sh

RUN chmod +x /app/start.sh

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

EXPOSE 8761

CMD ["/app/start.sh"]
