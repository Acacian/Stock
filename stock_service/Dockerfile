# Backend build stage
FROM gradle:7.6-jdk17 AS backend-build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :stock_service:build -x test --no-daemon

# Frontend build stage
FROM node:16 AS frontend-build
WORKDIR /app/frontend
COPY stock_service/frontend/package*.json ./
RUN npm install
RUN npm install react-router-dom @mui/material @mui/icons-material recharts axios
RUN npm install --save-dev @babel/plugin-proposal-private-property-in-object
COPY stock_service/frontend .
RUN npm run build

# Final stage
FROM openjdk:17-jdk-slim
WORKDIR /app

RUN apt-get update && apt-get install -y default-mysql-client wget unzip nginx

COPY --from=backend-build /app/stock_service/build/libs/*.jar app.jar
COPY --from=frontend-build /app/frontend/build /app/static

COPY stock_service/src/main/resources/application.yml /app/
COPY stock_service/src/test/resources/application-test.yml /app/
COPY .env /app/.env

COPY stock_service/nginx.conf /etc/nginx/nginx.conf

ENV MODE=${MODE:-prod}
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
ENV DB_NAME=${DB_NAME:-Stock}
ENV DB_PASSWORD_VAR=${DB_PASSWORD_VAR:-DB_PASSWORD}

EXPOSE 8082 80

CMD if [ "$MODE" = "test" ]; then \
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
    java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -Dspring.web.resources.static-locations=file:/app/static \
    app.jar