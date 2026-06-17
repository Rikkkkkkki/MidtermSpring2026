# Build stage
FROM maven:3.9.4-eclipse-temurin-11 AS builder

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:11-jre

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/uno-cli.jar .

# Set default command: run with 3 bots, 1 game, quiet mode
ENTRYPOINT ["java", "-jar", "uno-cli.jar"]
CMD ["--bots", "3", "--games", "1", "--quiet"]

# Allow overriding the command
# Usage: docker run <image> --bots 2 --games 3 --human