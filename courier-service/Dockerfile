FROM openjdk:17

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} courier-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/courier-service.jar"]
