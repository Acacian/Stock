pipeline {
    agent any

    environment {
        DB_PASSWORD = credentials('db-password')
        TEST_DB_PASSWORD = credentials('test-db-password')
    }

    stages {
        stage('Build') {
            steps {
                sh 'docker-compose -f docker-compose.test.yml build'
            }
        }
        stage('Test') {
            steps {
                sh 'docker-compose -f docker-compose.test.yml run --rm auth-service ./gradlew test'
                sh 'docker-compose -f docker-compose.test.yml run --rm user-service ./gradlew test'
                sh 'docker-compose -f docker-compose.test.yml run --rm newsfeed-service ./gradlew test'
                sh 'docker-compose -f docker-compose.test.yml run --rm social-service ./gradlew test'
            }
        }
        stage('Deploy') {
            steps {
                sh 'docker-compose up -d'
            }
        }
    }

    post {
        always {
            sh 'docker-compose -f docker-compose.test.yml down'
            sh 'docker-compose down'
        }
    }
}