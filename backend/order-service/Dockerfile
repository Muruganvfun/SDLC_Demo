# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY common ./common
COPY order-service/pom.xml ./order-service/pom.xml
COPY order-service/src ./order-service/src
RUN mvn -f pom.xml install -pl common -am -DskipTests
RUN mvn -f order-service/pom.xml package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/order-service/target/*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
