# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -N install -DskipTests
RUN mvn -pl api-gateway package -DskipTests -am

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/api-gateway/target/*.war app.jar
RUN apk add --no-cache curl
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
