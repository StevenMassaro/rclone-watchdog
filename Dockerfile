FROM alpine:3.17
EXPOSE 8080
RUN apk add --update unzip ca-certificates && \
    curl https://rclone.org/install.sh | ash && \
    apk del unzip
ADD /target/rclone-watchdog.jar rclone-watchdog.jar
HEALTHCHECK CMD wget --no-verbose --tries=1 --spider http://localhost:8080/admin/health || exit 1
ENTRYPOINT ["java","-jar","rclone-watchdog.jar"]
