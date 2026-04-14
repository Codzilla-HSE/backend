
 FROM gradle:8-jdk23 AS builder
 WORKDIR /app
 COPY . .
 RUN gradle build -x test


 FROM eclipse-temurin:21-jre-alpine
 WORKDIR /app
 COPY --from=builder /app/build/libs/*.jar app.jar
 ENTRYPOINT ["java", "-jar", "app.jar"]