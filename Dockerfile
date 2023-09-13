# Usa una imagen base con Java 8
FROM openjdk:8-jre-slim

# Copia el archivo JAR de tu aplicación al contenedor
COPY target/securityjwt-0.0.1-SNAPSHOT.jar /app.jar

# Puerto en el que escucha tu aplicación Spring Boot
EXPOSE 8080

# Comando para ejecutar tu aplicación cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "/app.jar"]