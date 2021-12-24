FROM openjdk:13-alpine
EXPOSE 8080
RUN apk update && apk --no-cache add ca-certificates && \
    wget https://downloads.rclone.org/rclone-current-linux-amd64.zip && \
    unzip rclone-current-linux-amd64.zip && \
    cd rclone-*-linux-amd64 && \
    cp rclone /usr/bin/ && \
    chown root:root /usr/bin/rclone && \
    chmod 755 /usr/bin/rclone
ADD /target/rclone-watchdog.jar rclone-watchdog.jar
ENTRYPOINT ["java","-jar","rclone-watchdog.jar"]
