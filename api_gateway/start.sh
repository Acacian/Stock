#!/bin/bash

# SSL 인증서 설정
/app/convert_cert.sh

# Nginx 시작
nginx

# Java 애플리케이션 시작
java -jar app.jar