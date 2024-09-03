# 빌드 단계
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :social_service:build -x test --no-daemon

# Frontend 빌드 단계
FROM node:18 AS frontend-build
WORKDIR /app/social_service/frontend
COPY social_service/frontend .
RUN npm install && \
    npm install --save-dev @babel/plugin-proposal-private-property-in-object && \
    npm install @stomp/stompjs && \
    npm run build

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/social_service/build/libs/*.jar /app/social_service.jar
COPY --from=frontend-build /app/social_service/frontend/build /usr/share/nginx/html/social
COPY social_service/src/main/resources/application.yml /app/social_service_application.yml
COPY social_service/start.sh /app/start.sh

RUN chmod +x /app/start.sh

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

EXPOSE 8084

CMD ["/app/start.sh"]