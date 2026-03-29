# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY common ./common
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY notification-service/src ./notification-service/src
RUN mvn -f pom.xml install -pl common -am -DskipTests
RUN mvn -f notification-service/pom.xml package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
