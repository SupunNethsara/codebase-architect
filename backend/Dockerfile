# Multi-stage build for Spring Boot application
# Stage 1: Build artifact using Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom.xml first for efficient layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies offline
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal lightweight runtime using Eclipse Temurin JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

EXPOSE 8080

# Configure JVM for container memory limits
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
