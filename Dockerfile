# Этап 1 — сборка jar внутри Docker
 FROM gradle:8-jdk21-alpine AS builder
 WORKDIR /app
 COPY . .
 RUN gradle build -x test

 # Этап 2 — запуск
 FROM eclipse-temurin:21-jre-alpine
 WORKDIR /app
 COPY --from=builder /app/build/libs/*.jar app.jar
 ENTRYPOINT ["java", "-jar", "app.jar"]