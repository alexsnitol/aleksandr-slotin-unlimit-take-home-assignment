# Build stage
FROM maven:3.9-eclipse-temurin-26 AS builder

WORKDIR /build

# Copy project files
COPY pom.xml .
COPY mvnw* ./
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:26-jre-jammy

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/target/issue-analyzer-*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

