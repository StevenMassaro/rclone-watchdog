pipeline {
   agent {
      label 'master'
   }

   tools {
      maven 'M3'
   }

   environment {
      pom = readMavenPom().getVersion()
   }

   stages {
      stage('Build') {
         steps {
            sh 'mvn clean package -P ui'
         }
      }
      stage('Docker') {
         steps {
            script {
               image = docker.build("stevenmassaro/rclone-watchdog:latest")
               docker.withRegistry('', 'DockerHub') {
                  image.push()
                  image.push(pom)
               }
            }
         }
      }
      stage('Results') {
         steps {
            junit '**/target/surefire-reports/TEST-*.xml'
            archiveArtifacts 'target/*.jar'
         }
      }
   }
}
