# 빌드 단계
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :stock_service:build -x test --no-daemon

# Frontend 빌드 단계
FROM node:18 AS frontend-build
WORKDIR /app/stock_service/frontend
COPY stock_service/frontend .
RUN npm install && \
    npm install --save axios @mui/material @mui/icons-material @emotion/react @emotion/styled \
                 react-router-dom react-error-boundary@latest recharts lodash && \
    npm install --save-dev @babel/plugin-proposal-private-property-in-object && \
    npm run build

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/stock_service/build/libs/*.jar /app/stock_service.jar
COPY --from=frontend-build /app/stock_service/frontend/build /usr/share/nginx/html/stock
COPY stock_service/src/main/resources/application.yml /app/stock_service_application.yml
COPY stock_service/start.sh /app/start.sh

RUN chmod +x /app/start.sh

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

EXPOSE 8085

CMD ["/app/start.sh"]