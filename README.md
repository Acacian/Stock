임시 readme

# Setup
.env를 만든 후, 아래와 같이 추가해주세요.
DB_PASSWORD = " 여러분의 DB Password "
SERVERS = " 로컬에서 할 거라면 Localhost,아니면 AWS 등의 IP "
KAFKA_SERVERS = " Kafka Server, 위와 동일 "
REDIS_HOST = " Redis Server, 위와 동일 "
JWT_SECRET = " JWT 키 뭘로 세팅할지 정하면 됩니다. 배포할 거면 어렵게 정하세요. "
EMAIL = " 유저에게 인증 이메일 보낼 메일 "
EMAIL_PASSWORD = " 인증 비밀번호 "

# Erd
check erd.vuerd.json file and erd.sql

# Docker 명령어
start : docker-compose up -d
check :docker-compose ps
end : docker-compose down