# -----------------------------
# Stage 1: Build the application
# -----------------------------
# Use Maven with Eclipse Temurin JDK 23 on Alpine Linux for a lightweight build stage
FROM maven:3.9.9-eclipse-temurin-23-alpine AS builder

# Set working directory inside the container
WORKDIR /app

# Copy Maven configuration and source code into the container
COPY . .

WORKDIR /app/backend/api-gateway

# Build the Spring Boot application and skip tests for faster builds
RUN mvn clean package -DskipTests

# -----------------------------
# Stage 2: Create a minimal runtime image
# -----------------------------
# Use Eclipse Temurin JRE 23 on Alpine for lightweight runtime
FROM eclipse-temurin:23-jre-alpine-3.21

# Set working directory for runtime container
WORKDIR /app

RUN apk add --no-cache bash

# Copy the built jar file from the builder stage
COPY --from=builder /app/backend/api-gateway/target/*.jar api-gateway.jar

COPY ../../wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Expose the port used by the API Gateway service
EXPOSE 8080

CMD /wait-for-it.sh mysql:3306 -- \
    /wait-for-it.sh redis:6379 -- \
    /wait-for-it.sh rabbitmq:5672 -- \
    /wait-for-it.sh nacos:8848 -- \
    /wait-for-it.sh minio:9000 -- \
    java -jar /app/api-gateway.jar
