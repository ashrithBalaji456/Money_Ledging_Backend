# Stage 1: Build
FROM maven:3.8.5-openjdk-17-slim AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B


# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/lending-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
