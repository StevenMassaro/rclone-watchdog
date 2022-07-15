FROM eclipse-temurin:17-jre
EXPOSE 8080
RUN apt-get update && apt-get install ca-certificates -y && \
    wget https://downloads.rclone.org/rclone-current-linux-amd64.zip && \
    unzip rclone-current-linux-amd64.zip && \
    cd rclone-*-linux-amd64 && \
    cp rclone /usr/bin/ && \
    chown root:root /usr/bin/rclone && \
    chmod 755 /usr/bin/rclone && \
    cd .. && \
    rm -rf rclone-current-linux-amd64.zip rclone-*-linux-amd64
ADD /target/rclone-watchdog.jar rclone-watchdog.jar
HEALTHCHECK CMD wget --no-verbose --tries=1 --spider http://localhost:8080/admin/health || exit 1
ENTRYPOINT ["java","-jar","rclone-watchdog.jar"]
