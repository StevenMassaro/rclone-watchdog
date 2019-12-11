FROM openjdk:13-alpine
EXPOSE 8080
ADD /target/rclone-watchdog.jar demo.jar
ENTRYPOINT ["java","-jar","demo.jar"]
