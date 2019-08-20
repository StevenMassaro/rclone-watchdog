FROM java:8
EXPOSE 8080
ADD /target/rclone-watchdog.jar demo.jar
ENTRYPOINT ["java","-jar","demo.jar"]
