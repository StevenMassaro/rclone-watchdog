# rclone-watchdog

## Building docker image
https://spring.io/guides/gs/spring-boot-docker/
```
mvn install dockerfile:build
```

## Running docker image
```
docker run -p 8080:8080 -t stevenmassaro/rclone-watchdog
```