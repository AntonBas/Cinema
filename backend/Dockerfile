FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests -Pdocker

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p logs
RUN mkdir -p uploads/posters

COPY uploads/posters /app/uploads/posters

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -r spring && useradd -r -g spring spring

RUN chown -R spring:spring logs
RUN chown -R spring:spring uploads

USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]