# Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# 전체 프로젝트를 복사합니다
COPY . .

# Gradle Wrapper 권한 설정
RUN chmod +x ./gradlew

# social_service 서비스 빌드
RUN ./gradlew :social_service:build -x test --no-daemon

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
COPY --from=build /app/social_service/build/libs/*.jar app.jar
COPY --from=build /app/social_service/src/main/resources/application.properties /app/
COPY --from=build /app/social_service/src/test/resources/application-test.properties /app/
COPY --from=build /app/gradlew /app/
COPY --from=build /app/gradle /app/gradle

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
ENV SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/Stock_Test
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=${TEST_DB_PASSWORD}

CMD ["java", "-jar", "-Dspring.profiles.active=test", "app.jar"]