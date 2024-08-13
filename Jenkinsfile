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