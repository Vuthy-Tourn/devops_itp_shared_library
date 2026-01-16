@Library('itp_shared_lib@main') _

pipeline {
    agent any
    environment {
        REPO_NAME = 'vuthytourn'
        IMAGE_NAME = 'jenkins-itp-springboot'
        TAG = 'latest'
        CHAT_ID = '777493627'
        CHAT_TOKEN = '8477902807:AAGdQkcWqEb-gtJ19Efup07VDjSeMOIuWB4'
    }

    stages {
         stage('Clean Workspace') {
        steps { deleteDir() }
    }

        stage('Checkout') {
             steps {
                git branch: 'main',
                    url: 'https://github.com/Vuthy-Tourn/Rest-API.git'
            }
        }
        stage('Check Dockerfile') {
            steps {
                script {
                     if (!fileExists('Dockerfile')) {
                        echo 'Dockerfile not found, loading from Shared Library...'
                        def dockerfile = libraryResource 'spring/dev.Dockerfile'
                        writeFile file: 'Dockerfile', text: dockerfile
                    } else {
                        echo 'Dockerfile found in project'
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${REPO_NAME}/${IMAGE_NAME}:${TAG} .'
            }
        }

        stage('Push Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'DOCKERHUB-CREDENTIAL',
                    usernameVariable: 'DH_USERNAME',
                    passwordVariable: 'DH_PASSWORD'
                )]) {
                    sh '''
                    echo "$DH_PASSWORD" | docker login -u "$DH_USERNAME" --password-stdin
                    docker push ${REPO_NAME}/${IMAGE_NAME}:${TAG}
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker stop springboot-cont || true
                    docker rm springboot-cont || true

                   docker run -d -p 8081:8080 \
                  --name springboot-cont \
                    --network app-network \
                  ${REPO_NAME}/${IMAGE_NAME}:${TAG}
                '''
            }
        }
    }

    post {
        success {
            sendTelegramMessage( '✅ Spring Boot Deploy Successful', CHAT_TOKEN, CHAT_ID )
        }
        failure {
            sendTelegramMessage( '❌ Spring Boot Pipeline Failed', CHAT_TOKEN, CHAT_ID )
        }
    }
}