# 빌드 단계
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :api_gateway:build -x test --no-daemon

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app
RUN apt-get update && apt-get install -y nginx openssl curl

COPY --from=build /app/api_gateway/build/libs/*.jar /app/api_gateway.jar
COPY api_gateway/nginx.conf /etc/nginx/nginx.conf
COPY api_gateway/src/main/resources/application.yml /app/api_gateway_application.yml
COPY api_gateway/src/main/resources/apigateway.p12 /app/
COPY api_gateway/start.sh /app/start.sh

# Frontend 파일 복사
COPY user_service/frontend/build /usr/share/nginx/html/user
COPY stock_service/frontend/build /usr/share/nginx/html/stock
COPY social_service/frontend/build /usr/share/nginx/html/social
COPY newsfeed_service/frontend/build /usr/share/nginx/html/newsfeed

RUN mkdir -p /etc/nginx/ssl
RUN chmod +x /app/start.sh

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
ENV SSL_KEY_STORE_PASSWORD=${SSL_KEY_STORE_PASSWORD}

EXPOSE 8081 443 3001 3002 3003 3004

CMD ["/app/start.sh"]