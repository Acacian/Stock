# 📝 주식 토론 게시판 프로젝트

## 📌 프로젝트 소개

<h2 align="center">대규모 커뮤니티와 주식 데이터를 처리하는 통합 백엔드 시스템</h2>

- 이 프로젝트는 24.08.07 ~ 24.09.04간 진행된 개인 백엔드 프로젝트로, 주식 토론 게시판과 관련된 기능을 구현하였습니다. 프론트엔드 개발도 함께 진행했습니다.
- Java 17 및 Spring Boot 3.1.5를 메인으로 사용하였고, Microservice Architecture를 적용하여 확장성과 유지보수성을 고려한 구조로 설계되었습니다.
- Kafka(대규모 메시징 처리), Redis(캐싱), Eureka(서비스 디스커버리), WebSocket(실시간 통신), WebRTC(영상 및 음성 통신) 등의 최신 백엔드 기술을 사용하였습니다.
- 프론트엔드는 React 18.02(Node.js 18)를 기반으로 구현되었습니다.

## 🎥 데모 영상 (Demo Video)

- 추후 첨부 예정

## 🏛️ Architecture

![Architecture](./public/Architecture.png)

## 🗂️ ERD

![ERD Diagram](./public/ERD.png)

- 대규모 데이터 처리에서 작업의 안정성을 보장하고 관리하기 위해 Spring Batch를 사용했습니다.
- likes, follows, stock prices 등에 복합 키를 사용하여 데이터 중복을 방지하고 메모리 공간을 절약하였습니다.

## 💻 사용된 프레임워크 및 아키텍처

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![Spring Batch](https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white) ![MSA](https://img.shields.io/badge/MSA-00897B?style=for-the-badge) ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![Zookeeper](https://img.shields.io/badge/Zookeeper-FF4B4B?style=for-the-badge&logo=apache-zookeeper&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white) ![Eureka](https://img.shields.io/badge/Eureka-4DB33D?style=for-the-badge&logo=spring&logoColor=white) ![WebRTC](https://img.shields.io/badge/WebRTC-333333?style=for-the-badge&logo=webrtc&logoColor=white) ![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=websocket&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white) ![SSL](https://img.shields.io/badge/SSL-3A9B35?style=for-the-badge&logo=let's-encrypt&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white) ![RESTful API](https://img.shields.io/badge/RESTful-02569B?style=for-the-badge&logo=restful-api&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white) ![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white) ![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white) 

## 🛠 문제 해결 (Troubleshooting)

- SSL 적용 시, 기존의 Eureka Service Discovery와 충돌 현상 발생 
  > API Gateway에서 SSL을 처리하고, 응답을 HTTP로 변환 후 내부 서비스 통신은 Eureka를 통해 관리
  > 이를 통해 타 서비스는 HTTP로, 보안이 중요한 User-Service는 HTTPS로 통신

- 로그아웃을 RefreshToken을 제거하는 방식으로 구현했으나, 토큰을 재사용해서 로그인할 수 있었음
  > Redis를 사용한 블랙리스트 방식으로 개선하여 토큰 관리 효율성과 보안을 강화


