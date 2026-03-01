# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 3900

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3900/actuator/health || exit 1

# Run application
# GCP credentials should be provided via:
# - GCP_CREDENTIALS_JSON environment variable (JSON as string), OR
# - GCP_CREDENTIALS_PATH environment variable (path to mounted secret file), OR
# - GOOGLE_APPLICATION_CREDENTIALS (standard GCP env var)
ENTRYPOINT ["java", "-jar", "app.jar"]
