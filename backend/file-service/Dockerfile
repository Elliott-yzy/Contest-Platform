# -----------------------------
# Stage 1: Build the application
# -----------------------------
# Use Maven with Eclipse Temurin JDK 23 on Alpine Linux for a lightweight build stage
FROM maven:3.9.9-eclipse-temurin-23-alpine AS builder

# Set the working directory for the entire project
WORKDIR /app

# Copy the whole project (includes parent pom and modules)
COPY . .

# Enter the specific microservice module directory
WORKDIR /app/backend/file-service

# Build the Spring Boot application and skip tests for faster builds
RUN mvn clean package -DskipTests

# -----------------------------
# Stage 2: Create a minimal runtime image
# -----------------------------
FROM eclipse-temurin:23-jre-alpine-3.21

# Set working directory in the runtime container
WORKDIR /app
RUN apk add --no-cache bash

# Copy the built JAR file from builder stage and rename it to file-service.jar
COPY --from=builder /app/backend/file-service/target/*.jar file-service.jar

COPY ../../wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8083

CMD /wait-for-it.sh mysql:3306 -- \
    /wait-for-it.sh redis:6379 -- \
    /wait-for-it.sh rabbitmq:5672 -- \
    /wait-for-it.sh nacos:8848 -- \
    /wait-for-it.sh minio:9000 -- \
    java -jar /app/file-service.jar
