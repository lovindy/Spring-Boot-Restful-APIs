pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'talentexis-api'
        GITHUB_REPO = 'https://github.com/lovindy/Talentexis-Spring-Boot-APIs.git'
        DOCKER_CREDENTIALS = credentials('docker-credentials')
        ENV_FILE = credentials('env-file')
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

        stage('Docker Build') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${env.BUILD_NUMBER}")
                }
            }
        }

        stage('Prepare Deployment') {
            steps {
                script {
                    // Copy environment file
                    sh "cp \$ENV_FILE .env"

                    // Create or update docker-compose file
                    writeFile file: 'docker-compose.yml', text: '''
                        version: '3.8'
                        services:
                          api:
                            image: ${DOCKER_IMAGE}:${BUILD_NUMBER}
                            container_name: talentexis-api
                            ports:
                              - "8080:8080"
                            env_file:
                              - .env
                            restart: unless-stopped
                            networks:
                              - app-network
                        networks:
                          app-network:
                            driver: bridge
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    // Deploy using docker-compose
                    sh '''
                        docker-compose down || true
                        docker-compose up -d
                        docker system prune -f
                    '''
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    // Wait for application to be ready
                    sh '''
                        for i in {1..30}; do
                            if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
                                echo "Application is healthy"
                                exit 0
                            fi
                            echo "Waiting for application to be ready..."
                            sleep 10
                        done
                        echo "Application failed to become healthy"
                        exit 1
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            script {
                // Rollback to previous version if deployment fails
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