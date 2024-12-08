pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'talentexis-api'
        GITHUB_REPO = 'https://github.com/lovindy/Talentexis-Spring-Boot-APIs.git'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: "${GITHUB_REPO}"
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

        stage('Deploy') {
            steps {
                script {
                    // Stop and remove existing container
                    sh 'docker stop talentexis-api || true'
                    sh 'docker rm talentexis-api || true'

                    // Run new container
                    sh """
                        docker run -d \
                        --name talentexis-api \
                        -p 8080:8080 \
                        ${DOCKER_IMAGE}:${env.BUILD_NUMBER}
                    """
                }
            }
        }
    }
}