#!/bin/bash

# 실행 모드 설정
if [ "$MODE" = "test" ]; then
    echo "Running in TEST mode"
    export SPRING_PROFILES_ACTIVE=test
    export SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/Stock_Test
    export SPRING_DATASOURCE_PASSWORD=$TEST_DB_PASSWORD
else
    echo "Running in PRODUCTION mode"
    export SPRING_PROFILES_ACTIVE=prod
    export SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/Stock
    export SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD
fi

# SSL 인증서 변환: P12 파일을 CRT와 KEY 파일로 변환
openssl pkcs12 -in /app/apigateway.p12 -out /etc/nginx/ssl/nginx.crt -clcerts -nokeys -password pass:${SSL_KEY_STORE_PASSWORD}
openssl pkcs12 -in /app/apigateway.p12 -out /etc/nginx/ssl/nginx.key -nocerts -nodes -password pass:${SSL_KEY_STORE_PASSWORD}

# 권한 설정
chmod 644 /etc/nginx/ssl/nginx.crt
chmod 600 /etc/nginx/ssl/nginx.key

# Nginx 시작
nginx

# Eureka Server 시작
java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dspring.config.location=/app/eureka_server_application.yml /app/eureka_server.jar &

# 다른 서비스들이 Eureka Server가 완전히 시작될 때까지 기다리도록 함
sleep 30

# API Gateway 시작
java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dserver.ssl.key-store=/app/apigateway.p12 -Dserver.ssl.key-store-password=$SSL_KEY_STORE_PASSWORD -Dserver.ssl.key-store-type=PKCS12 -Dspring.config.location=/app/api_gateway_application.yml /app/api_gateway.jar &

# User Service 시작
java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dspring.config.location=/app/user_service_application.yml /app/user_service.jar &

# Stock Service 시작
java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dspring.config.location=/app/stock_service_application.yml /app/stock_service.jar &

# Social Service 시작
java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dspring.config.location=/app/social_service_application.yml /app/social_service.jar &

# Newsfeed Service 시작
java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dspring.config.location=/app/newsfeed_service_application.yml /app/newsfeed_service.jar &

# 모든 프로세스가 종료되지 않도록 대기
wait