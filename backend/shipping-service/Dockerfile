# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY common ./common
COPY shipping-service/pom.xml ./shipping-service/pom.xml
COPY shipping-service/src ./shipping-service/src
RUN mvn -f pom.xml install -pl common -am -DskipTests
RUN mvn -f shipping-service/pom.xml package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/shipping-service/target/*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
