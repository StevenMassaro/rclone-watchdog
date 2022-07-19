FROM eclipse-temurin:17-jre
EXPOSE 8080
RUN apt-get update && apt-get install ca-certificates openjdk17 -y && \
    curl https://rclone.org/install.sh | bash
ADD /target/rclone-watchdog.jar rclone-watchdog.jar
HEALTHCHECK CMD wget --no-verbose --tries=1 --spider http://localhost:8080/admin/health || exit 1
ENTRYPOINT ["java","-jar","rclone-watchdog.jar"]
