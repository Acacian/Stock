pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'docker-compose build'
            }
        }

        stage('Start Services') {
            steps {
                sh 'docker-compose up -d'
            }
        }

        stage('Wait for Services') {
            steps {
                // 서비스들이 완전히 시작될 때까지 대기
                sh 'docker-compose run --rm wait-for-it db:3306 -t 30'
                sh 'docker-compose run --rm wait-for-it eureka-server:8761 -t 30'
                sh 'docker-compose run --rm wait-for-it user-service:8082 -t 30'
                sh 'docker-compose run --rm wait-for-it newsfeed-service:8083 -t 30'
                sh 'docker-compose run --rm wait-for-it social-service:8084 -t 30'
                sh 'docker-compose run --rm wait-for-it api-gateway:8081 -t 30'
            }
        }

        stage('Run Integration Tests') {
            steps {
                // 통합 테스트 실행
                sh 'docker-compose run --rm integration-test'
            }
        }
    }

    post {
        always {
            // 모든 작업이 끝나면 서비스 중지 및 리소스 정리
            sh 'docker-compose down'
        }
    }
}