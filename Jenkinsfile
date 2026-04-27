pipeline {
    agent any

    environment {
        IMAGE_NAME = 'oscarfndez/users-service'
        IMAGE_TAG = "build-${env.BUILD_NUMBER}"
        SONAR_PROJECT_KEY = 'users-service'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn -B clean verify -Djkube.skip=true'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                      mvn -B sonar:sonar \
                        -DskipTests \
                        -Djkube.skip=true \
                        -Dsonar.projectKey=users-service \
                        -Dsonar.organization=oscarfndez
                    '''
                }
            }
        }

        stage('Publish to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_PASSWORD'
                )]) {
                    sh '''
                      mvn -B jib:build \
                        -DskipTests \
                        -Djkube.skip=true \
                        -Djib.to.image=${IMAGE_NAME}:${IMAGE_TAG} \
                        -Djib.to.auth.username=$DOCKERHUB_USERNAME \
                        -Djib.to.auth.password=$DOCKERHUB_PASSWORD
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
    }
}
