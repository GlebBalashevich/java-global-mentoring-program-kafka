FROM openjdk:17

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} palmetto-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/palmetto-service.jar"]
