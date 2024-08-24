# Backend Build Stage
FROM gradle:7.6-jdk17 AS backend-build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
# Add Gradle build cache
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :user_service:build -x test --no-daemon \
    --gradle-user-home /root/.gradle \
    -Dorg.gradle.internal.http.socketTimeout=120000 \
    -Dorg.gradle.internal.http.connectionTimeout=120000 \
    || (sleep 10 && ./gradlew :user_service:build -x test --no-daemon \
    --gradle-user-home /root/.gradle \
    -Dorg.gradle.internal.http.socketTimeout=120000 \
    -Dorg.gradle.internal.http.connectionTimeout=120000)

# Frontend Build Stage
FROM node:14 AS frontend-build
WORKDIR /app/frontend
COPY newsfeed_service/frontend/package*.json ./
RUN npm install
RUN npm install react-router-dom
RUN npm install react-error-boundary 
RUN npm install @material-ui/core
RUN npm install --save-dev @babel/plugin-proposal-private-property-in-object
COPY newsfeed_service/frontend .
RUN npm run build

# Final Stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Install dependencies
RUN apt-get update && apt-get install -y default-mysql-client wget unzip nginx

# Copy built backend and frontend artifacts
COPY --from=backend-build /app/newsfeed_service/build/libs/*.jar app.jar
COPY --from=frontend-build /app/frontend/build /usr/share/nginx/html

# Copy configuration files
COPY newsfeed_service/src/main/resources/application.yml /app/
COPY newsfeed_service/src/test/resources/application-test.yml /app/
COPY .env /app/.env

# Copy Nginx configuration
COPY newsfeed_service/nginx.conf /etc/nginx/nginx.conf

# Environment Variables
ENV MODE=${MODE:-prod}
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
ENV DB_NAME=${DB_NAME:-Stock}
ENV DB_PASSWORD_VAR=${DB_PASSWORD_VAR:-DB_PASSWORD}

# Expose necessary ports
EXPOSE 8083 80

# Command to run the application
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
    java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE app.jar
