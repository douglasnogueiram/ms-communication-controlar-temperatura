FROM openjdk:8-jre
RUN mkdir app
ARG JAR_FILE
ADD /target/${JAR_FILE} /app/ms-communication-controlar-temperatura.jar
WORKDIR /app
ENTRYPOINT java -jar ms-communication-controlar-temperatura.jar