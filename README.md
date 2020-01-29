# rclone-watchdog

## Environment variables

|Variable|Required|Description|
|---|---|---|
|`RCLONE_BANDWIDTH_SCHEDULE`|no|If specified, this will be used as the scheduled bandwidth limit for all jobs. See https://rclone.org/docs/#bwlimit-bandwidth-spec|

## Building docker image
https://spring.io/guides/gs/spring-boot-docker/
```
mvn install dockerfile:build
```

## Running docker image
```
docker run -p 8080:8080 -t stevenmassaro/rclone-watchdog
```