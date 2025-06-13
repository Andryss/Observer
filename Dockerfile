FROM eclipse-temurin:17-jre-alpine

ARG JAR_FILE=target/observer*.jar

WORKDIR /opt/app

COPY ${JAR_FILE} observer_bot.jar

ENTRYPOINT ["java","-jar","observer_bot.jar"]
