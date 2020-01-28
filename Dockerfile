FROM openjdk:13-alpine
EXPOSE 8080
ADD /target/rclone-watchdog.jar rclone-watchdog.jar
RUN apk update && apk --no-cache add ca-certificates
ENTRYPOINT ["java","-jar","rclone-watchdog.jar"]
