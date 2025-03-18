pipeline {
    agent any

    environment {
        DOCKER_IMAGE = credentials('DOCKER_IMAGE')
        DOCKER_CONTAINER = credentials('DOCKER_CONTAINER')
        DOCKER_PORT = credentials('DOCKER_PORT')
        DOCKER_PATH = '/var/lib/docker' // EC2 서버의 Docker 저장 경로

        EC2_USER = credentials('EC2_USER')
        EC2_IP = credentials('EC2_IP')
        SSH_KEY = credentials('SSH_KEY')

        SPRING_PROFILES_ACTIVE = 'prod'
        DB_URL = credentials('DB_URL')
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')

        JAVA_HOME = '/opt/java/openjdk'
        GRADLE_HOME = '/opt/gradle/gradle-8.13'
        PATH = "${JAVA_HOME}/bin:${GRADLE_HOME}/bin:${env.PATH}"
    }

    tools {
        jdk 'JDK17'
        gradle 'Gradle 8.13'
    }

    stages {
        stage('Clone Repository') {
            steps {
                echo 'Cloning the repository...'
                git branch: 'BE/dev',
                    url: 'https://lab.ssafy.com/s12-blockchain-nft-sub1/S12P21D210.git',
                    credentialsId: 'GitLab-PAT'
            }
        }
        stage('Build Application') {
            steps {
                echo 'Building the application with Gradle Wrapper...'
                dir('Backend') {
                    sh 'gradle clean build'
                    sh 'ls -al $(pwd)/build/libs'
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                echo 'Building the Docker image...'
                dir('Backend') {
                    sh 'cp build/libs/backend-0.0.1-SNAPSHOT.jar .'
                    sh 'docker build -t ${DOCKER_IMAGE}:latest .'
                }
            }
        }
        stage('Save and Transfer Docker Image') {
            steps {
                echo 'Saving and transferring Docker image to EC2...'
                sh """
                docker save ${DOCKER_IMAGE}:latest | gzip > backend-0.0.1-SNAPSHOT.tar.gz
                """
                sshPublisher(publishers: [
                    sshPublisherDesc(
                        configName: 'EC2-Server',
                        transfers: [
                            sshTransfer(
                                sourceFiles: 'backend-0.0.1-SNAPSHOT.tar.gz'
                            )
                        ]
                    )
                ])
            }
        }
        stage('Deploy to EC2') {
            steps {
                echo 'Deploying the application on EC2...'
                sshPublisher(publishers: [
                    sshPublisherDesc(
                        configName: 'EC2-Server',
                        transfers: [
                            sshTransfer(
                                execCommand: """
                                    mkdir -p ${DOCKER_PATH}
                                    docker stop ${DOCKER_CONTAINER} || true
                                    docker rm ${DOCKER_CONTAINER} || true
                                    docker rmi ${DOCKER_IMAGE}:latest || true
                                    docker load < ${DOCKER_PATH}/my-project-0.0.1-SNAPSHOT.tar.gz
                                    docker run -d --name ${DOCKER_CONTAINER} \
                                      -p ${DOCKER_PORT}:${DOCKER_PORT} \
                                      -e SPRING_PROFILES_ACTIVE=dev \
                                      -e SERVER_PORT=${DOCKER_PORT} \
                                      -e DB_URL=${DB_URL} \
                                      -e DB_USERNAME=${DB_USERNAME} \
                                      -e DB_PASSWORD=${DB_PASSWORD} \
                                      ${DOCKER_IMAGE}:latest
                                """.stripIndent()
                            )
                        ]
                    )
                ])
            }
        }
    }

    post {
        always {
            echo 'Cleaning workspace...'
            cleanWs()
        }
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}