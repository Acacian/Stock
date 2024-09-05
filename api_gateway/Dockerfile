# 백엔드 빌드 단계
FROM gradle:7.6-jdk17 AS backend-build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :api_gateway:build -x test --no-daemon

# User Service Frontend 빌드 단계
FROM node:18 AS user-frontend-build
WORKDIR /app/user_service/frontend
COPY user_service/frontend .
RUN npm install -g npm@latest && \
    npm install && \
    npm install --save axios @mui/material @mui/icons-material @emotion/react @emotion/styled \
                 react-router-dom react-error-boundary@latest recharts lodash @stomp/stompjs && \
    npm install --save-dev @babel/plugin-proposal-private-property-in-object && \
    npm run build

# Stock Service Frontend 빌드 단계
FROM node:18 AS stock-frontend-build
WORKDIR /app/stock_service/frontend
COPY stock_service/frontend .
RUN npm install && \
    npm install --save axios @mui/material @mui/icons-material @emotion/react @emotion/styled \
                 react-router-dom react-error-boundary@latest recharts lodash && \
    npm install --save-dev @babel/plugin-proposal-private-property-in-object && \
    npm run build

# Social Service Frontend 빌드 단계
FROM node:18 AS social-frontend-build
WORKDIR /app/social_service/frontend
COPY social_service/frontend .
RUN npm install && \
    npm install --save-dev @babel/plugin-proposal-private-property-in-object && \
    npm install @stomp/stompjs && \
    npm run build

# Newsfeed Service Frontend 빌드 단계
FROM node:18 AS newsfeed-frontend-build
WORKDIR /app/newsfeed_service/frontend
COPY newsfeed_service/frontend .
RUN npm install && \
    npm install sockjs-client @stomp/stompjs \
    npm run build

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app
RUN apt-get update && apt-get install -y nginx openssl curl

# nginx 사용자와 그룹 생성
RUN groupadd -r nginx && useradd -r -g nginx nginx

COPY --from=backend-build /app/api_gateway/build/libs/*.jar /app/api_gateway.jar
COPY api_gateway/nginx.conf /etc/nginx/nginx.conf
COPY api_gateway/src/main/resources/application.yml /app/api_gateway_application.yml
COPY api_gateway/src/main/resources/apigateway.p12 /app/
COPY api_gateway/start.sh /app/start.sh

# Frontend 파일 복사
COPY --from=user-frontend-build /app/user_service/frontend/build /usr/share/nginx/html/user
COPY --from=stock-frontend-build /app/stock_service/frontend/build /usr/share/nginx/html/stock
COPY --from=social-frontend-build /app/social_service/frontend/build /usr/share/nginx/html/social
COPY --from=newsfeed-frontend-build /app/newsfeed_service/frontend/build /usr/share/nginx/html/newsfeed

RUN mkdir -p /etc/nginx/ssl
RUN chmod +x /app/start.sh

# Nginx 권한 설정
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
ENV SSL_KEY_STORE_PASSWORD=${SSL_KEY_STORE_PASSWORD}

EXPOSE 8081 443 3001 3002 3003 3004

CMD ["/app/start.sh"]