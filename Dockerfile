# Stage 1: Build stage using Maven and JDK 17
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copy pom.xml first to fetch and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage using a slim OpenJDK 17 image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the packaged jar from the build stage
COPY --from=build /app/target/lending-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Command to execute the application jar
ENTRYPOINT ["java", "-jar", "app.jar"]
