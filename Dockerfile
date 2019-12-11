FROM openjdk:13-alpine
EXPOSE 8080
ADD /target/rclone-watchdog.jar rclone-watchdog.jar
ENTRYPOINT ["java","-jar","rclone-watchdog.jar"]
