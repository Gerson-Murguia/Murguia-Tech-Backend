# Etapa 1: Construcción
FROM maven:3.8.4-openjdk-8 AS builder

# Establece el directorio de trabajo en /app
WORKDIR /app

# Copia el archivo pom.xml y el archivo de configuración (si es necesario)
COPY pom.xml .
# Copia toddo el código fuente de la aplicación
COPY src ./src

# Compila la aplicación y genera el archivo JAR
RUN mvn clean package


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