# 빌드 단계
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :user_service:build -x test --no-daemon

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/user_service/build/libs/*.jar /app/user_service.jar
COPY user_service/src/main/resources/application.yml /app/user_service_application.yml
COPY user_service/start.sh /app/start.sh

RUN chmod +x /app/start.sh

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

EXPOSE 8086

CMD ["/app/start.sh"]