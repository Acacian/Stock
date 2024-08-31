# Backend 빌드 단계
FROM gradle:7.6-jdk17 AS backend-build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :api_gateway:build :eureka_server:build :user_service:build :stock_service:build :social_service:build :newsfeed_service:build -x test --no-daemon

# Frontend 빌드 단계
FROM node:18 AS frontend-build
WORKDIR /app

# User Service Frontend 빌드
COPY user_service/frontend /app/user_service/frontend
WORKDIR /app/user_service/frontend
RUN npm install -g npm@latest && npm install && npm install --save axios @mui/material @mui/icons-material @emotion/react @emotion/styled react-router-dom react-error-boundary@latest recharts lodash @stomp/stompjs && npm install --save-dev @babel/plugin-proposal-private-property-in-object && npm run build

# Stock Service Frontend 빌드
WORKDIR /app
COPY stock_service/frontend /app/stock_service/frontend
WORKDIR /app/stock_service/frontend
RUN npm install && npm install --save axios @mui/material @mui/icons-material @emotion/react @emotion/styled react-router-dom react-error-boundary@latest recharts lodash && npm install --save-dev @babel/plugin-proposal-private-property-in-object && npm run build

# Social Service Frontend 빌드
WORKDIR /app
COPY social_service/frontend /app/social_service/frontend
WORKDIR /app/social_service/frontend
RUN npm install && npm install --save-dev @babel/plugin-proposal-private-property-in-object && npm install @stomp/stompjs && npm run build

# Newsfeed Service Frontend 빌드
WORKDIR /app
COPY newsfeed_service/frontend /app/newsfeed_service/frontend
WORKDIR /app/newsfeed_service/frontend
RUN npm install && npm run build

# 최종 이미지
FROM openjdk:17-jdk-slim
WORKDIR /app
RUN apt-get update && apt-get install -y nginx openssl curl

# 백엔드 JAR 파일 복사
COPY --from=backend-build /app/api_gateway/build/libs/*.jar /app/api_gateway.jar
COPY --from=backend-build /app/eureka_server/build/libs/*.jar /app/eureka_server.jar
COPY --from=backend-build /app/user_service/build/libs/*.jar /app/user_service.jar
COPY --from=backend-build /app/stock_service/build/libs/*.jar /app/stock_service.jar
COPY --from=backend-build /app/social_service/build/libs/*.jar /app/social_service.jar
COPY --from=backend-build /app/newsfeed_service/build/libs/*.jar /app/newsfeed_service.jar

# 프론트엔드 빌드 결과 복사
COPY --from=frontend-build /app/user_service/frontend/build /usr/share/nginx/html/user
COPY --from=frontend-build /app/stock_service/frontend/build /usr/share/nginx/html/stock
COPY --from=frontend-build /app/social_service/frontend/build /usr/share/nginx/html/social
COPY --from=frontend-build /app/newsfeed_service/frontend/build /usr/share/nginx/html/newsfeed

# 설정 파일 복사
COPY api_gateway/nginx.conf /etc/nginx/nginx.conf
COPY api_gateway/src/main/resources/application.yml /app/api_gateway_application.yml
COPY eureka_server/src/main/resources/application.yml /app/eureka_server_application.yml
COPY user_service/src/main/resources/application.yml /app/user_service_application.yml
COPY stock_service/src/main/resources/application.yml /app/stock_service_application.yml
COPY social_service/src/main/resources/application.yml /app/social_service_application.yml
COPY newsfeed_service/src/main/resources/application.yml /app/newsfeed_service_application.yml
COPY api_gateway/src/main/resources/apigateway.p12 /app/
COPY .env /app/.env

# SSL 디렉토리 생성
RUN mkdir -p /etc/nginx/ssl

# start.sh 스크립트 복사 및 권한 설정
COPY api_gateway/start.sh /app/start.sh
RUN chmod +x /app/start.sh

# 환경 변수 설정
ENV MODE=${MODE:-prod}
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
ENV DB_NAME=${DB_NAME:-Stock}
ENV DB_PASSWORD_VAR=${DB_PASSWORD_VAR:-DB_PASSWORD}
ENV SSL_KEY_STORE_PASSWORD=${SSL_KEY_STORE_PASSWORD}

# 노출할 포트 설정
EXPOSE 8080 8081 8761 8086 8085 8084 8083 443

# 컨테이너 시작 시 start.sh 실행
CMD ["/app/start.sh"]