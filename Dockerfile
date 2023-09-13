# syntax = docker/dockerfile:1.2

FROM maven:3.8.4-openjdk-8 AS builder

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN --mount=type=secret,id=_env,target=/etc/secrets/.env mvn -e -B clean package -Dmaven.test.skip=true



# Usa una imagen base con Java 8
FROM openjdk:8-jre-alpine

# Establece el directorio de trabajo en /app
WORKDIR /app

# Copia el archivo JAR generado desde la etapa 1 al directorio actual
COPY --from=builder /app/target/securityjwt-0.0.1-SNAPSHOT.jar /app/app.jar

# Puerto en el que escucha tu aplicación Spring Boot
EXPOSE 8080

# Comando para ejecutar tu aplicación cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]