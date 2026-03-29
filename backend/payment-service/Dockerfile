# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY common ./common
COPY payment-service/pom.xml ./payment-service/pom.xml
COPY payment-service/src ./payment-service/src
RUN mvn -f pom.xml install -pl common -am -DskipTests
RUN mvn -f payment-service/pom.xml package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
