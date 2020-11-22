node {
   def mvnHome
   properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10')), disableConcurrentBuilds()])
   stage('Preparation') { // for display purposes
      checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'GitBucketReadOnly', url: 'http://192.168.0.117:8084/git/rclone-watchdog/rclone-watchdog.git']]])
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.           
      mvnHome = tool 'M3'
   }
   stage('Build') {
      // Run the maven build
      withEnv(["MVN_HOME=$mvnHome"]) {
         if (isUnix()) {
            sh '"$MVN_HOME/bin/mvn" clean package -P ui dockerfile:build'
         } else {
            bat(/"%MVN_HOME%\bin\mvn" clean package -P ui dockerfile:build/)
         }
      }
   }
   stage('Results') {
      junit '**/target/surefire-reports/TEST-*.xml'
      archiveArtifacts 'target/*.jar'
   }
   stage('Publish docker image') {
       sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
       sh 'docker push stevenmassaro/rclone-watchdog:latest'
       pom = readMavenPom file: 'pom.xml'
       sh "docker push stevenmassaro/rclone-watchdog:${pom.version}"
   }
}