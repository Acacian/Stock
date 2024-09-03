# 빌드 단계
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :newsfeed_service:build -x test --no-daemon

# Frontend 빌드 단계
FROM node:18 AS frontend-build
WORKDIR /app/newsfeed_service/frontend
COPY newsfeed_service/frontend .
RUN npm install && npm run build

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/newsfeed_service/build/libs/*.jar /app/newsfeed_service.jar
COPY --from=frontend-build /app/newsfeed_service/frontend/build /usr/share/nginx/html/newsfeed
COPY newsfeed_service/src/main/resources/application.yml /app/newsfeed_service_application.yml
COPY newsfeed_service/start.sh /app/start.sh

RUN chmod +x /app/start.sh

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

EXPOSE 8083

CMD ["/app/start.sh"]