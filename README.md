# 개요
1달간의 개인 백엔드 프로젝트로, 주식 토론 게시판에

# Used Framework


# TroubleShooting




# How to Setup
.env를 만든 후, 여러분의 환경에 맞게 추가해주세요.

# Erd
Root에 있는 erd.sql 및 erd.vuerd.json을 참고해주세요.
뉴스피드는 쿼리로 처리할 수 있지만, 시간복잡도 증가 및 서버 부하를 고려해
데이터가 보존되고 이벤트로 Pub/Sub 처리가 되는 Kafka 방식을 채택하였습니다.

# Docker 명령어
start : docker-compose up -d
check : docker-compose ps
end : docker-compose down