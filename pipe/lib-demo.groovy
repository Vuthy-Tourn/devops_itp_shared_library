@Library("itp_shared_lib@main") _
pipeline {
    agent any
    environment{
        CHAT_ID="777493627"
        CHAT_TOKEN="8477902807:AAGdQkcWqEb-gtJ19Efup07VDjSeMOIuWB4"
    }

    stages {
        stage('Send Message') {
            steps {
                script{
                    def message= """
                    Welcome to jenkin bot \\
                    You are ready to use a nortification \\!
                    """
                    sendTelegramMessage("${message}","${CHAT_TOKEN}","${CHAT_ID}")
                }
            }
        }
    }
}
