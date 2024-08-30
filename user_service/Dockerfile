# Backend Build Stage
FROM gradle:7.6-jdk17 AS backend-build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN --mount=type=cache,target=/home/gradle/.gradle,sharing=private \
    ./gradlew :user_service:build -x test --no-daemon \
    --gradle-user-home /home/gradle/.gradle \
    -Dorg.gradle.internal.http.socketTimeout=120000 \
    -Dorg.gradle.internal.http.connectionTimeout=120000

# Frontend Build Stage
FROM node:18 AS frontend-build
WORKDIR /app/frontend
ENV NODE_OPTIONS=--max-old-space-size=4096
RUN npm config set legacy-peer-deps true

COPY user_service/frontend/package*.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --prefer-offline --network-timeout=100000

COPY user_service/frontend .
RUN npm run build

# Final stage
FROM openjdk:17-jdk-slim
WORKDIR /app
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y default-mysql-client nginx openssl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=backend-build /app/user_service/build/libs/*.jar app.jar
COPY --from=frontend-build /app/frontend/build /usr/share/nginx/html
COPY user_service/src/main/resources/application.yml /app/
COPY user_service/src/test/resources/application-test.yml /app/
COPY .env /app/.env
COPY user_service/nginx.conf /etc/nginx/nginx.conf
COPY user_service/src/main/resources/keystore.p12 /app/keystore.p12

ENV MODE=${MODE:-prod}
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
ENV DB_NAME=${DB_NAME:-Stock}
ENV DB_PASSWORD_VAR=${DB_PASSWORD_VAR:-DB_PASSWORD}

EXPOSE 8444 443

CMD mkdir -p /etc/nginx/ssl && \
    openssl pkcs12 -in /app/keystore.p12 -out /etc/nginx/ssl/nginx.crt -clcerts -nokeys -passin pass:${SSL_KEY_STORE_PASSWORD} && \
    openssl pkcs12 -in /app/keystore.p12 -out /etc/nginx/ssl/nginx.key -nocerts -nodes -passin pass:${SSL_KEY_STORE_PASSWORD} && \
    if [ "$MODE" = "test" ]; then \
        echo "Running in TEST mode"; \
        export SPRING_PROFILES_ACTIVE=test; \
        export SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/Stock_Test; \
        export SPRING_DATASOURCE_PASSWORD=$TEST_DB_PASSWORD; \
    else \
        echo "Running in PRODUCTION mode"; \
        export SPRING_PROFILES_ACTIVE=prod; \
        export SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/Stock; \
        export SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD; \
    fi && \
    nginx && \
    java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dserver.port=8444 app.jar