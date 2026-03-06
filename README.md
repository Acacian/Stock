# Stock

[![CI](https://github.com/Acacian/Stock/actions/workflows/ci.yml/badge.svg)](https://github.com/Acacian/Stock/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square)
![Spring Boot 3.1.5](https://img.shields.io/badge/Spring_Boot-3.1.5-6DB33F?style=flat-square)
![Kafka](https://img.shields.io/badge/Kafka-Event_Driven-231F20?style=flat-square)
![Redis](https://img.shields.io/badge/Redis-Cache_&_Token-DC382D?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square)

Kafka, Redis, Spring Batch를 조합해 주식 데이터 처리와 소셜 이벤트 흐름을 분리한 MSA 기반 개인 프로젝트.

## 프로젝트 개요

- 기간: 2024.08.07 - 2024.09.07
- 형태: 개인 프로젝트
- 목표: 주식 조회, 사용자 인증, 소셜 활동, 뉴스피드 생성을 독립 서비스로 분리하고 이벤트 기반으로 연결
- 초점: 확장 가능한 서비스 경계 설계, 비동기 메시징, 캐시 전략, 운영 자동화 경험 확보

## 문제 정의

일반적인 CRUD 중심 게시판 구조로는 주식 데이터 갱신, 사용자 인증, 팔로우/좋아요 이벤트, 뉴스피드 반영이 한 서비스에 결합되기 쉽습니다. 이 프로젝트에서는 아래 문제를 분리해 다뤘습니다.

- 인증과 사용자 프로필 변경이 다른 도메인 처리와 강하게 결합되는 문제
- 게시글, 좋아요, 팔로우 이벤트가 뉴스피드 생성 로직과 직접 엮이면서 확장성이 떨어지는 문제
- 주식 시세 수집과 보조지표 계산처럼 주기성 작업이 일반 API 요청 흐름을 방해하는 문제
- 토큰 관리와 반복 조회 데이터가 DB 부하로 이어지는 문제

## 아키텍처

![Architecture](./public/Architecture.png)

| 서비스 | 역할 |
| --- | --- |
| `api_gateway` | 라우팅, 인증 필터, Rate Limiting, 보안 헤더 처리 |
| `user_service` | 회원가입, JWT 로그인, 로그아웃, 프로필 관리, 이메일 인증 |
| `stock_service` | 주식 데이터 조회, 시세 저장, 기술적 지표 계산, 배치 처리 |
| `social_service` | 게시글, 댓글, 좋아요, 팔로우, 실시간 통신 진입점 |
| `newsfeed_service` | Kafka 이벤트 기반 뉴스피드 생성 및 개인화 피드 관리 |
| `eureka_server` | 서비스 디스커버리 |

## 핵심 기능

### 1. 인증과 사용자 관리

- JWT 기반 로그인/로그아웃
- Redis 기반 Refresh Token 저장과 블랙리스트 처리
- 사용자 프로필 조회 및 수정
- 이메일 인증 플로우

### 2. 주식 데이터 처리

- 주식 정보 조회 및 차트 데이터 제공
- 이동평균선, MACD, RSI, 볼린저 밴드 등 기술적 지표 계산
- Spring Batch 기반 주기성 시세 적재 작업

### 3. 소셜 기능

- 게시글 작성, 댓글, 좋아요, 팔로우
- WebSocket 기반 실시간 채팅
- WebRTC 시그널링 컴포넌트 실험

### 4. 뉴스피드 자동화

- 팔로우, 게시글 작성, 댓글, 좋아요 이벤트를 Kafka로 비동기 전파
- 이벤트 수신 후 사용자별 뉴스피드 반영
- Redis 기반 빠른 피드 조회

## 기술 선택 포인트

### Kafka

뉴스피드 서비스가 소셜 서비스와 직접 결합되지 않도록 이벤트를 기준으로 분리했습니다. 게시글 작성이나 좋아요 같은 액션이 발생해도 요청 응답 흐름과 뉴스피드 갱신 흐름을 분리할 수 있어 서비스 책임을 더 명확하게 가져갈 수 있었습니다.

### Redis

Refresh Token 저장, 로그아웃 토큰 블랙리스트, 캐시성 데이터 조회에 Redis를 사용했습니다. 토큰 만료와 블랙리스트 TTL을 함께 운영하면서 인증 상태를 빠르게 판별하는 구조를 구성했습니다.

### Spring Batch

주식 데이터 수집과 보조지표 계산은 요청 시점 처리보다 배치 작업이 적합했습니다. 배치 전용 흐름으로 분리해 API 응답 경로와 주기성 처리 경로를 나눴습니다.

### API Gateway + Eureka

서비스 간 직접 진입 대신 게이트웨이를 통해 라우팅과 공통 필터를 통합했습니다. 인증 필터, Rate Limiting, 보안 헤더 같은 횡단 관심사를 한 지점에서 관리할 수 있도록 구성했습니다.

## 기술 스택

- Backend: Java 17, Spring Boot 3.1.5, Spring Security, Spring Data JPA, Spring Cloud Gateway, OpenFeign, Eureka
- Data: MySQL, Redis
- Messaging: Apache Kafka, Kafka Streams
- Realtime: WebSocket, WebRTC
- Frontend: React 18
- Infra: Docker Compose, GitHub Actions

## ERD

![ERD Diagram](./public/ERD.png)

- 좋아요, 팔로우, 주식 가격 이력처럼 중복 제어가 필요한 영역은 복합 키를 사용
- 배치 처리 대상 데이터는 대량 입력과 조회 흐름을 고려해 분리

## 실행 방법

### 1. 로컬 실행

사전 준비

- Java 17
- Docker / Docker Compose
- 프로젝트 루트 `.env`

실행

```bash
docker compose up --build -d
```

주요 확인 포인트

- Eureka: `http://localhost:8761`
- Kafka UI: `http://localhost:8080`
- Jenkins: `http://localhost:8082`

### 2. 테스트 실행

일반 테스트

```bash
./gradlew test
```

통합 테스트

```bash
./gradlew integrationTest
```

## CI

GitHub Actions 기반 `CI` 워크플로를 추가했습니다.

- 트리거: `push`, `pull_request`
- 실행 환경: Java 17
- 의존 서비스: MySQL 8, Redis 7, Kafka
- 검증 범위: `build`, `test`

워크플로 파일: [`.github/workflows/ci.yml`](./.github/workflows/ci.yml)

## 트러블슈팅

### 로그아웃 이후에도 기존 토큰이 재사용되던 문제

문제

- 서버에서 로그아웃 처리를 마쳐도 이미 발급된 JWT가 만료 전까지 유효하게 남는 구간 존재

해결

- Redis에 블랙리스트 키를 저장하고 TTL을 토큰 만료 시간과 맞춤
- 인증 필터에서 블랙리스트 조회를 추가해 로그아웃 직후 재사용 차단

## 회고

- 서비스 분리는 단순히 개수를 늘리는 문제가 아니라 변경 이유와 데이터 흐름을 기준으로 경계를 나누는 작업이라는 점 확인
- 프로젝트 규모 대비 지나치게 세분화된 구조는 운영 복잡도를 키울 수 있어, 실제 운영 관점에서는 서비스 책임과 관리 비용의 균형이 중요하다는 점 학습
- 배치, 캐시, 메시징처럼 서로 다른 처리 모델을 한 프로젝트 안에서 조합하면서 설계 선택의 trade-off를 명확하게 체감
