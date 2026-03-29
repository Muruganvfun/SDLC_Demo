# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY common ./common
COPY inventory-service/pom.xml ./inventory-service/pom.xml
COPY inventory-service/src ./inventory-service/src
RUN mvn -f pom.xml install -pl common -am -DskipTests
RUN mvn -f inventory-service/pom.xml package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/inventory-service/target/*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
