# 백엔드 빌드 단계
FROM gradle:7.6-jdk17 AS backend-build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :api_gateway:build -x test --no-daemon

# 실행 단계
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y nginx openssl curl

# nginx 사용자와 그룹 생성
RUN groupadd -r nginx && useradd -r -g nginx nginx

COPY --from=backend-build /app/api_gateway/build/libs/*.jar /app/api_gateway.jar
COPY api_gateway/nginx.conf /etc/nginx/nginx.conf
COPY api_gateway/src/main/resources/application.yml /app/api_gateway_application.yml
COPY api_gateway/start.sh /app/start.sh
COPY api_gateway/src/main/resources/static/ /usr/share/nginx/html/

RUN mkdir -p /etc/nginx/ssl
RUN chmod +x /app/start.sh

# Nginx 권한 설정
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
ENV SSL_KEY_STORE_PASSWORD=${SSL_KEY_STORE_PASSWORD}

EXPOSE 8081 443 3001 3002 3003 3004

CMD ["/app/start.sh"]
