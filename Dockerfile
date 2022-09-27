FROM eclipse-temurin:19-jre
EXPOSE 8080
RUN apt-get update && apt-get install unzip ca-certificates -y && \
    curl https://rclone.org/install.sh | bash && \
    apt-get remove unzip -y
ADD /target/rclone-watchdog.jar rclone-watchdog.jar
HEALTHCHECK CMD wget --no-verbose --tries=1 --spider http://localhost:8080/admin/health || exit 1
ENTRYPOINT ["java","-jar","rclone-watchdog.jar"]
