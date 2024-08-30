#!/bin/sh

# P12 파일을 CRT와 KEY 파일로 변환
openssl pkcs12 -in /app/apigateway.p12 -out /etc/nginx/ssl/nginx.crt -clcerts -nokeys -password pass:${SSL_KEY_STORE_PASSWORD}
openssl pkcs12 -in /app/apigateway.p12 -out /etc/nginx/ssl/nginx.key -nocerts -nodes -password pass:${SSL_KEY_STORE_PASSWORD}

# 권한 설정
chmod 644 /etc/nginx/ssl/nginx.crt
chmod 600 /etc/nginx/ssl/nginx.key

# Nginx 재시작
nginx -s reload || nginx