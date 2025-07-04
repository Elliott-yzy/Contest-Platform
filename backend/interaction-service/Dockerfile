# -----------------------------
# Stage 1: Build the application
# -----------------------------
# Use Maven with Eclipse Temurin JDK 23 on Alpine Linux for a lightweight build stage
FROM maven:3.9.9-eclipse-temurin-23-alpine AS builder

# Set root working directory inside the container
WORKDIR /app

# Copy the entire project into the container (including parent pom)
COPY . .

# Move into the module's directory to build
WORKDIR /app/backend/interaction-service

# Build the Spring Boot application and skip tests for faster builds
RUN mvn clean package -DskipTests

# -----------------------------
# Stage 2: Create a minimal runtime image
# -----------------------------
FROM eclipse-temurin:23-jre-alpine-3.21

# Set working directory for the runtime container
WORKDIR /app
RUN apk add --no-cache bash

# Copy the built JAR file from builder stage
COPY --from=builder /app/backend/interaction-service/target/*.jar interaction-service.jar

COPY ../../wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8085

CMD /wait-for-it.sh mysql:3306 -- \
    /wait-for-it.sh redis:6379 -- \
    /wait-for-it.sh rabbitmq:5672 -- \
    /wait-for-it.sh nacos:8848 -- \
    /wait-for-it.sh minio:9000 -- \
    java -jar /app/interaction-service.jar
