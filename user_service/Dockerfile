# 빌드 단계
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :user_service:build -x test --no-daemon

# Frontend 빌드 단계
FROM node:18 AS frontend-build
WORKDIR /app/user_service/frontend
COPY user_service/frontend .
RUN npm install -g npm@latest && \
    npm install && \
    npm install --save axios @mui/material @mui/icons-material @emotion/react @emotion/styled \
                 react-router-dom react-error-boundary@latest recharts lodash @stomp/stompjs && \
    npm install --save-dev @babel/plugin-proposal-private-property-in-object && \
    npm run build

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/user_service/build/libs/*.jar /app/user_service.jar
COPY --from=frontend-build /app/user_service/frontend/build /usr/share/nginx/html/user
COPY user_service/src/main/resources/application.yml /app/user_service_application.yml
COPY user_service/start.sh /app/start.sh

RUN chmod +x /app/start.sh

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

EXPOSE 8086

CMD ["/app/start.sh"]