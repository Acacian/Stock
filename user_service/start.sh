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

# User Service 시작
java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
     -Dspring.datasource.url=$SPRING_DATASOURCE_URL \
     -Dspring.datasource.password=$SPRING_DATASOURCE_PASSWORD \
     -Dspring.config.location=/app/user_service_application.yml \
     /app/user_service.jar