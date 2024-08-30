# Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :api_gateway:build -x test --no-daemon

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/api_gateway/build/libs/*.jar app.jar
COPY --from=build /app/api_gateway/src/main/resources/application.yml /app/
COPY --from=build /app/api_gateway/src/test/resources/application-test.yml /app/
COPY --from=build /app/api_gateway/src/main/resources/apigateway.p12 /app/apigateway.p12
COPY api_gateway/convert_cert.sh /app/convert_cert.sh
COPY api_gateway/start.sh /app/start.sh

# Install Nginx and OpenSSL without prompts
RUN mkdir -p /var/run/nginx /var/cache/nginx /var/log/nginx /etc/nginx/ssl /var/run
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y nginx openssl curl && \
    rm -rf /var/lib/apt/lists/* && \
    chmod +x /app/convert_cert.sh && \
    chmod +x /app/start.sh

# Copy nginx configuration after installation
COPY api_gateway/nginx.conf /etc/nginx/nginx.conf

ENV SSL_KEY_STORE_PASSWORD=${SSL_KEY_STORE_PASSWORD}

EXPOSE 8081 80 443

CMD ["/app/start.sh"]