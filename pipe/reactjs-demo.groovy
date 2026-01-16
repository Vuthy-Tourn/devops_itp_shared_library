@Library("itp_shared_lib@main") _

pipeline {
    agent any

    environment {
        REPO_NAME='vuthytourn'
        IMAGE_NAME='jenkins-itp-reactjs'
        TAG='latest'
        CHAT_ID='777493627'
        CHAT_TOKEN='8477902807:AAGdQkcWqEb-gtJ19Efup07VDjSeMOIuWB4'
    }

    stages {
         stage('Clean Workspace') {
        steps { deleteDir() }
     }

        stage('Clone Code') {
            steps {
                git 'https://github.com/Vuthy-Tourn/reactjs-fullstack-template.git'
            }
        }

        stage('Check Dockerfile') {
            steps {
                script {
                    if (!fileExists('Dockerfile')) {
                        echo 'Dockerfile not found, loading from Shared Library...'
                        def dockerfile = libraryResource 'reactjs/dev.Dockerfile'
                        writeFile file: 'Dockerfile', text: dockerfile
                    } else {
                        echo 'Dockerfile found in project'
                    }
                }
            }
        }

        stage('Build Image') {
            steps {
                sh '''
                docker build -t ${REPO_NAME}/${IMAGE_NAME}:${TAG} .
                '''
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

        stage('Run Service') {
            steps {
                sh '''
                docker stop react-app || true
                docker rm react-app || true
                docker run -dp 3000:80 --name react-app ${REPO_NAME}/${IMAGE_NAME}:${TAG}
                '''
            }
        }

        stage('Send Message') {
            steps {
                script {
                    sendTelegramMessage(
                        "Deploy Successfully 🟢",
                        CHAT_TOKEN,
                        CHAT_ID
                    )
                }
            }
        }
    }
}