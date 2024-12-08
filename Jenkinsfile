pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'talentexis-api'
        GITHUB_REPO = 'https://github.com/lovindy/Talentexis-Spring-Boot-APIs.git'
        DOCKER_CREDENTIALS = credentials('docker-credentials')
        ENV_FILE = credentials('env-file')
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_REGISTRY_URL = "https://${DOCKER_REGISTRY}"
        APP_PORT = '8081'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'develop', url: "${GITHUB_REPO}"
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh "echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USER --password-stdin \$DOCKER_REGISTRY_URL"
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    docker.build("${DOCKER_REGISTRY}/${DOCKER_USER}/${DOCKER_IMAGE}:${env.BUILD_NUMBER}")
                }
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh "docker push ${DOCKER_REGISTRY}/${DOCKER_USER}/${DOCKER_IMAGE}:${env.BUILD_NUMBER}"
                }
            }
        }

        stage('Prepare Deployment') {
            steps {
                withCredentials([file(credentialsId: 'env-file', variable: 'ENV_FILE')]) {
                    sh "cp \$ENV_FILE .env"
                    writeFile file: 'docker-compose.yml', text: """
                        version: '3.8'
                        services:
                          api:
                            image: ${DOCKER_REGISTRY}/${DOCKER_USER}/${DOCKER_IMAGE}:${env.BUILD_NUMBER}
                            container_name: talentexis-api
                            ports:
                              - "${APP_PORT}:8080"
                            env_file:
                              - .env
                            restart: unless-stopped
                            networks:
                              - app-network
                        networks:
                          app-network:
                            driver: bridge
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker-compose down || true
                    docker-compose up -d
                    docker system prune -f
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh """
                    for i in {1..30}; do
                        if curl -s http://localhost:${APP_PORT}/actuator/health | grep -q "UP"; then
                            echo "Application is healthy"
                            exit 0
                        fi
                        echo "Waiting for application to be ready..."
                        sleep 10
                    done
                    echo "Application failed to become healthy"
                    exit 1
                """
            }
        }
    }

    post {
        always {
            node {
                withCredentials([usernamePassword(credentialsId: 'docker-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh "docker logout \$DOCKER_REGISTRY_URL"
                }
                cleanWs()
            }
        }
        failure {
            node {
                script {
                    sh '''
                        if [ -f docker-compose.yml ]; then
                            docker-compose down
                            docker-compose up -d --no-deps api
                        fi
                    '''
                }
            }
        }
    }
}