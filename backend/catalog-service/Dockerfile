# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY common ./common
COPY catalog-service/pom.xml ./catalog-service/pom.xml
COPY catalog-service/src ./catalog-service/src
RUN mvn -f pom.xml install -pl common -am -DskipTests
RUN mvn -f catalog-service/pom.xml package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/catalog-service/target/*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
