# ===========
# Build stage
# ===========
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom + source
COPY pom.xml .
COPY src ./src

# Build the jar (skip tests for faster image build)
RUN mvn -q -DskipTests clean package

# ============
# Runtime stage
# ============
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Render injects PORT at runtime; default to 8080 for local runs
ENV PORT=8080
EXPOSE 8080

# Start Spring Boot on the port Render provides
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
