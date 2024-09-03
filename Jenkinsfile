pipeline {
    agent any

    triggers {
        cron('0 1 * * *') // 매일 새벽 1시에 실행
    }

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
                sh 'docker-compose run --rm wait-for-it user-service:8086 -t 30'
                sh 'docker-compose run --rm wait-for-it newsfeed-service:8083 -t 30'
                sh 'docker-compose run --rm wait-for-it social-service:8084 -t 30'
                sh 'docker-compose run --rm wait-for-it api-gateway:8081 -t 30'
            }
        }

        stage('Check Service Health') {
            steps {
                script {
                    def services = [
                        [name: 'Eureka Server', port: 8761],
                        [name: 'API Gateway', port: 8081],
                        [name: 'User Service', port: 8086],
                        [name: 'Newsfeed Service', port: 8083],
                        [name: 'Social Service', port: 8084],
                        [name: 'Stock Service', port: 8085]
                    ]
                    
                    services.each { service ->
                        try {
                            sh "curl -f http://localhost:${service.port}/actuator/health || exit 1"
                            echo "${service.name} is healthy"
                        } catch (Exception e) {
                            error "${service.name} is not healthy"
                        }
                    }
                }
            }
        }

        stage('Run Integration Tests') {
            steps {
                script {
                    try {
                        sh 'docker-compose run --rm integration-test'
                        echo "Integration tests passed successfully"
                    } catch (Exception e) {
                        error "Integration tests failed: ${e.message}"
                    }
                }
            }
        }

        stage('Run Batch Job') {
            steps {
                script {
                    try {
                        sh 'docker-compose exec -T stock-service java -jar app.jar --spring.batch.job.names=updateStockPricesJob'
                        echo "Batch job completed successfully"
                    } catch (Exception e) {
                        error "Batch job failed: ${e.message}"
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Stopping services and cleaning up resources..."
            sh 'docker-compose down'
            echo "Cleanup completed"
        }
    }
}