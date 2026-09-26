FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p logs uploads/posters && \
    apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -r spring && useradd -r -g spring spring && \
    chown -R spring:spring /app

USER spring:spring

ENV MALLOC_ARENA_MAX=2

EXPOSE 8080 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=40.0", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1", "-Xss512k", "-Djava.security.egd=file:/dev/urandom", "-jar", "app.jar"]