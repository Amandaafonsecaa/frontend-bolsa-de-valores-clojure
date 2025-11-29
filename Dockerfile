FROM openjdk:8-alpine

COPY target/uberjar/bolsa-front.jar /bolsa-front/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/bolsa-front/app.jar"]
