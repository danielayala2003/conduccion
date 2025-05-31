FROM openjdk:17
COPY "./target/conduccion-1.jar" "app.jar"
EXPOSE 8115
ENTRYPOINT [ "java", "-jar","app.jar" ]