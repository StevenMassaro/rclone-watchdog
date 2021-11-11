FROM openjdk:13-alpine
EXPOSE 8080
RUN apk update && apk --no-cache add ca-certificates && apk --no-cache add rclone
ADD /target/rclone-watchdog.jar rclone-watchdog.jar
ENTRYPOINT ["java","-jar","rclone-watchdog.jar"]
