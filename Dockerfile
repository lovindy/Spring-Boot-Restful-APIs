# Multi-stage build for smaller, faster image
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copy only necessary files for dependency resolution
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests \
    && mv target/*.jar app.jar

# Final runtime stage
FROM eclipse-temurin:17-jre-alpine

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copy only the jar from build stage
COPY --from=build /app/app.jar .

# Use exec form for signal handling
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]