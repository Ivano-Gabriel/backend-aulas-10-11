# Etapa 1: compilação e testes com Maven e Java 21.
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package

# Etapa 2: imagem menor, só com o runtime Java.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/backend-aulas-10-11-1.0.0.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
