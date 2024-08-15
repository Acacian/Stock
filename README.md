# 개요
1달간의 개인 백엔드 프로젝트로, 주식 토론 게시판을 구현합니다.

# Port
MySQL : 3306
Redis : 6379
Kafka : 9092
Zookeeper : 2181
user-service : 8082
newsfeed-service : 8083
social-service : 8084
(기존에 auth-service가 있었으나, 비용 절약 및 큰 기능 차이가 없어 user-service와 통합)

# Used Framework & Architecture
Java, Spring Boot, Kafka, Redis

# TroubleShooting


# How to Setup
.env를 만든 후, 여러분의 환경에 맞게 추가해주세요.

# Erd
Root에 있는 erd.sql 및 erd.vuerd.json을 참고해주세요.
뉴스피드는 쿼리가 너무 많아, Kafka와 Redis를 사용해 Table 없이 구현했습니다.

# Docker 명령어
start : docker-compose up -d
check : docker-compose ps
end : docker-compose down