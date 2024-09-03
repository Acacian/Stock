#!/bin/bash

# SSL 인증서 변환 및 권한 설정
openssl pkcs12 -in /app/apigateway.p12 -out /etc/nginx/ssl/nginx.crt -clcerts -nokeys -password pass:${SSL_KEY_STORE_PASSWORD}
openssl pkcs12 -in /app/apigateway.p12 -out /etc/nginx/ssl/nginx.key -nocerts -nodes -password pass:${SSL_KEY_STORE_PASSWORD}
chmod 644 /etc/nginx/ssl/nginx.crt
chmod 600 /etc/nginx/ssl/nginx.key

# Nginx 시작
nginx

# 실행 모드 설정
if [ "$MODE" = "test" ]; then
    echo "Running in TEST mode"
    export SPRING_PROFILES_ACTIVE=test
else
    echo "Running in PRODUCTION mode"
    export SPRING_PROFILES_ACTIVE=prod
fi

# API Gateway 시작
java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
     -Dserver.port=8081 \
     -Dserver.ssl.key-store=/app/apigateway.p12 \
     -Dserver.ssl.key-store-password=$SSL_KEY_STORE_PASSWORD \
     -Dserver.ssl.key-store-type=PKCS12 \
     -Dspring.config.location=/app/api_gateway_application.yml \
     /app/api_gateway.jar

# 프로세스가 종료되지 않도록 대기
wait