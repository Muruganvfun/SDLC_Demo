# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -pl common install -DskipTests
RUN mvn -pl shipping-service package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/shipping-service/target/*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
