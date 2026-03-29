# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY common ./common
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY auth-service/src ./auth-service/src
RUN mvn -f pom.xml install -pl common -am -DskipTests
RUN mvn -f auth-service/pom.xml package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/auth-service/target/*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
