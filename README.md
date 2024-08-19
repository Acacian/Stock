# 📝 주식 토론 게시판 백엔드 프로젝트

이 프로젝트는 한 달간 진행된 개인 백엔드 프로젝트로, 주식 토론 게시판을 구현한 것입니다. Spring Boot와 Kafka, Redis, Eureka 등의 최신 백엔드 기술을 활용하여 개발되었습니다.

## 💻 사용된 프레임워크 및 아키텍처

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot) ![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white) ![MSA](https://img.shields.io/badge/MSA-00897B?style=for-the-badge) ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![Zookeeper](https://img.shields.io/badge/Zookeeper-FF4B4B?style=for-the-badge&logo=apache-zookeeper&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white) ![Eureka](https://img.shields.io/badge/Eureka-4DB33D?style=for-the-badge&logo=spring&logoColor=white)

## 🚪 포트 구성

다음은 각 서비스에서 사용되는 포트 정보입니다:

- **Eureka**: `8761`
- **MySQL**: `3306`
- **Redis**: `6379`
- **Kafka**: `9092`
- **Zookeeper**: `2181`
- **Jenkins**: `8080`
- **API Gateway**: `8081`
- **User Service**: `8082`
- **Newsfeed Service**: `8083`
- **Social Service**: `8084`
- **Stock Service**: `8085`
- **Frontend**: `3001`,`3002`,`3003`,`3004`

> **참고:** 비용 절감을 위해 기존의 `auth-service`는 `user-service`와 통합되었습니다.

## 🛠 문제 해결 (Troubleshooting)

- RestApi를 통한 통신 시, 요청과 응답이 즉시 이루어지지 않아 성능 저하가 발생  
  > Kafka를 사용한 이벤트 통신을 통해 비동기 처리로 성능을 개선

- 로그아웃을 RefreshToken을 제거하는 방식으로 구현했으나, 토큰을 재사용해서 로그인할 수 있었음
  > Redis를 사용한 블랙리스트 방식으로 개선하여 토큰 관리 효율성과 보안을 강화


## 🚀 설치 및 설정 방법

1. `.env` 파일을 생성한 후, 자신의 환경에 맞게 설정을 추가해 주세요.  
   특히 `MODE=prod` 여부를 확인해 주시고, 테스트 파일로 실행할 시 `test`로 변경해 주세요.

2. `eureka_server`에서 Self-Preservation Mode를 `True`로 설정하면 일시적인 네트워크 문제로 인한 서비스 손실을 방지할 수 있습니다.

## 📊 ERD

루트 디렉토리에 있는 `erd.sql` 및 `erd.vuerd.json` 파일을 참고해 주세요. 뉴스피드는 쿼리 수가 많아 Kafka와 Redis를 사용해 테이블 없이 구현되었습니다.

## 🐳 Docker 명령어

- **빌드**: `docker-compose -f docker-compose.yml build --progress=plain`
- **시작**: `docker-compose up -d`
- **상태 확인**: `docker-compose ps`
- **종료**: `docker-compose down`

### 🧪 테스트용 Docker 명령어

- **빌드**: `docker-compose -f docker-compose.test.yml build --progress=plain`
- **테스트 실행**: `docker-compose -f docker-compose.test.yml up`
- **테스트 종료**: `docker-compose -f docker-compose.test.yml down`
