# 📝 주식 토론 게시판 백엔드 프로젝트

- 이 프로젝트는 한 달간 진행된 개인 백엔드 프로젝트로, 주식 토론 게시판을 구현한 것입니다. 간단하지만 프론트엔드 역시 혼자서 구현했습니다.
- Java 및 Spring Boot를 메인으로 사용하였고 Kafka,Redis,Eureka,WebSocket,WebRTC 등의 최신 백엔드 기술을 활용하여 개발되었습니다.

## 💻 사용된 프레임워크 및 아키텍처

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white) ![MSA](https://img.shields.io/badge/MSA-00897B?style=for-the-badge) ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![Zookeeper](https://img.shields.io/badge/Zookeeper-FF4B4B?style=for-the-badge&logo=apache-zookeeper&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white) ![Eureka](https://img.shields.io/badge/Eureka-4DB33D?style=for-the-badge&logo=spring&logoColor=white) ![WebRTC](https://img.shields.io/badge/WebRTC-333333?style=for-the-badge&logo=webrtc&logoColor=white) ![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=websocket&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white) ![SSL](https://img.shields.io/badge/SSL-3A9B35?style=for-the-badge&logo=let's-encrypt&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white) ![RESTful API](https://img.shields.io/badge/RESTful-02569B?style=for-the-badge&logo=restful-api&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white) ![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white) ![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white) 

## 🚪 포트 구성

다음은 각 서비스에서 사용되는 포트 정보입니다:

- **Eureka**: `8761`
- **MySQL**: `3306`
- **Redis**: `6379`
- **Kafka**: `9092`
- **Zookeeper**: `2181`
- **API Gateway**: `8080` , `8081`(SSL 적용)
- **Jenkins**: `8082`
- **User Service**: `8086`
- **Newsfeed Service**: `8083`
- **Social Service**: `8084`
- **Stock Service**: `8085`
- **Frontend**: `3000`(api-gateway에서 관리)

> **참고:** 비용 절감을 위해 기존의 `auth-service`는 `user-service`와 통합되었습니다. 또한 시간적 문제로 WebRTC, WebSocket을 위한 서비스를 따로 분리하지 못하고 Social Service와 통합시켰습니다.

## 🛠 문제 해결 (Troubleshooting)

- SSL 적용 시, 기존의 Eureka Service Discovery와 충돌 현상 발생 
  > API Gateway에서 SSL을 처리하고, 응답을 HTTP로 변환 후 내부 서비스 통신은 Eureka를 통해 관리
  > 이를 통해 타 서비스는 HTTP로, 보안이 중요한 User-Service는 HTTPS로 통신

- 로그아웃을 RefreshToken을 제거하는 방식으로 구현했으나, 토큰을 재사용해서 로그인할 수 있었음
  > Redis를 사용한 블랙리스트 방식으로 개선하여 토큰 관리 효율성과 보안을 강화


## 🚀 설치 및 설정 방법

1. `.env` 파일을 생성한 후, 자신의 환경에 맞게 설정을 추가해 주세요.  
   특히 `MODE=prod` 여부를 확인해 주시고, 테스트 파일로 실행할 시 `test`로 변경해 주세요. 개인정보인 부분은 제외시켰으므로, 그 부분만 채워넣으시면 됩니다.

```
# Test여부(prod / test에 따라 dockerfile 세팅이 바뀜)
MODE=prod

# DB
DB_PASSWORD=
SPRING_DATASOURCE_USERNAME=root
DB_NAME=Stock

# Port
SERVERS=db
KAFKA_SERVERS=kafka
KAFKA_PORT=9092
REDIS_HOST=redis
REDIS_PORT=6379
EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
JWT_SECRET=

# Email Verification (env와 별개로, 구글에서 따로 설정해주셔야 합니다.)
APP_DOMAIN=localhost
SENDING_EMAIL=
EMAIL_PASSWORD=

# Test Script
DOCKER_GROUP_ID=
TEST_EMAIL=
TEST_DB_PASSWORD=

# Frontend
REACT_APP_AUTH_URL=https://localhost/api/auth
REACT_APP_SOCIAL_API_URL=http://localhost/api/social
REACT_APP_NEWSFEED_API_URL=http://localhost/api/newsfeed
REACT_APP_SOCKET_URL=http://localhost/ws
REACT_APP_API_GATEWAY_URL=https://localhost/api

# HTTPS
SSL_KEY_STORE_PASSWORD=

# Upload Path
APP_UPLOAD_DIR=/path/to/upload/directory
APP_UPLOAD_URL=https://stock/uploads
```

2. `eureka_server`에서 Self-Preservation Mode를 `True`로 설정하면 일시적인 네트워크 문제로 인한 서비스 손실을 방지할 수 있습니다.

3. 본 프로젝트 중 일부는 SSL(HTTPS)이 적용되어 있습니다. 따라서, keystore.p12 파일을 직접 만든 후 비밀번호를 env 파일에, user_service의 resources에 넣어 주셔야 정상적으로 작동합니다.

## 📊 ERD

루트 디렉토리에 있는 `erd.sql` 및 `erd.vuerd.json` 파일을 참고해 주세요. 뉴스피드는 쿼리 수가 많아 Kafka와 Redis를 사용해 테이블 없이 구현되었습니다.

## 🐳 Docker 명령어

- 우선, Docker를 설치해주세요.
- **빌드**: `docker-compose -f docker-compose.yml build --progress=plain`
- **시작**: `docker-compose up -d`
- **상태 확인**: `docker-compose ps`
- **종료**: `docker-compose down`

### 🧪 테스트용 Docker 명령어

- **빌드**: `docker-compose -f docker-compose.test.yml build --progress=plain`
- **테스트 실행**: `docker-compose -f docker-compose.test.yml up`
- **테스트 종료**: `docker-compose -f docker-compose.test.yml down`
