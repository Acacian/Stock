# Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# 전체 프로젝트를 복사합니다
COPY . .

# Gradle Wrapper 권한 설정
RUN chmod +x ./gradlew

# newsfeed_service 서비스 빌드
RUN ./gradlew :newsfeed_service:build -x test --no-daemon

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# MySQL 클라이언트 및 필요한 도구 설치
RUN apt-get update && apt-get install -y default-mysql-client wget unzip

# Gradle 설치
RUN wget https://services.gradle.org/distributions/gradle-7.6-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-*.zip \
    && ln -s /opt/gradle/gradle-7.6 /opt/gradle/latest

ENV GRADLE_HOME=/opt/gradle/latest
ENV PATH=${GRADLE_HOME}/bin:${PATH}

# 빌드 결과물 및 필요한 파일 복사
COPY --from=build /app/newsfeed_service/build/libs/*.jar app.jar
COPY --from=build /app/newsfeed_service/src/main/resources/application.yml /app/
COPY --from=build /app/newsfeed_service/src/test/resources/application-test.yml /app/
COPY --from=build /app/gradlew /app/
COPY --from=build /app/gradle /app/gradle
COPY --from=build /app/.env /app/.env

# 환경 변수 설정
ENV MODE=${MODE:-prod}
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
ENV DB_NAME=${DB_NAME:-Stock}
ENV DB_PASSWORD_VAR=${DB_PASSWORD_VAR:-DB_PASSWORD}

# 시작 명령어
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
    java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE app.jar