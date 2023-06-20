FROM rclone/rclone:1.62.2
EXPOSE 8080
RUN apk add --update --no-cache wget openjdk17-jre-headless
ADD /target/rclone-watchdog.jar rclone-watchdog.jar
HEALTHCHECK CMD wget --no-verbose --tries=1 --spider http://localhost:8080/admin/health || exit 1
ENTRYPOINT ["java","-jar","rclone-watchdog.jar"]
