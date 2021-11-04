pipeline {
    agent any
    environment {
        registry = "{account}.dkr.ecr.eu-west-1.amazonaws.com/ecr_repo"
    }
   
    stages {
        stage('Cloning Git') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '', url: 'Angular_APP_URL']]])     
            }
        }
  
    // Building Docker images
    stage('Building image') {
      steps{
        script {
          dockerImage = docker.build registry
        }
      }
    }
   
    // Uploading Docker images into AWS ECR
    stage('Pushing to ECR') {
     steps{  
         script {
                sh 'aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin account.ecr.eu-west-1.amazonaws.com'
                sh 'docker push account.dkr.ecr.eu-west-1.amazonaws.com/ecr_repo:latest'
         }
        }
      }
   
         // Stopping Docker containers for cleaner Docker run
     stage('stop previous containers') {
         steps {
            sh 'docker ps -f name=Angular-App -q | xargs --no-run-if-empty docker container stop'
            sh 'docker container ls -a -fname=Angular-App -q | xargs -r docker container rm'
         }
       }
      
    stage('Docker Run') {
     steps{
         script {
                sh 'docker run -d -p 80:80 --rm --name Angular-App account.dkr.ecr.eu-west-1.amazonaws.com/ecr_repo:latest'
            }
      }
    }
    }
}
