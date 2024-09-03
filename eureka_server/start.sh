#!/bin/bash

# 실행 모드 설정
if [ "$MODE" = "test" ]; then
    echo "Running in TEST mode"
    export SPRING_PROFILES_ACTIVE=test
else
    echo "Running in PRODUCTION mode"
    export SPRING_PROFILES_ACTIVE=prod
fi

# Eureka Server 시작
java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
     -Dspring.config.location=/app/eureka_server_application.yml \
     /app/eureka_server.jar