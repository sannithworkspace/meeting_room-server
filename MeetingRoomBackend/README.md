# MeetingRoomBackend - Production Microservices Infrastructure

Production-grade Spring Cloud microservices backend for Meeting Room Booking System built with Java 21 and Spring Boot 3.3.x.

## Microservices Architecture

- **Eureka Server (`eureka-server`)**: Service Registry running on port `8761` with basic auth security.
- **API Gateway (`api-gateway`)**: Ingress gateway running on port `8080` with Resilience4j circuit breakers, CORS WebFilter, request correlation tracing, and unified Swagger UI aggregation.

## Centralized Swagger UI Documentation

All registered microservices' OpenAPI specifications are dynamically discovered and aggregated into a single unified interactive dashboard:

- **Swagger UI URL**: `http://localhost:8080/swagger-ui.html`
- Select any microservice from the top-right dropdown in Swagger UI to view and interact with its API endpoints.

## Build and Run Locally

### Prerequisites
- JDK 21 LTS
- Apache Maven 3.9+
- Docker & Docker Compose (optional for containerized execution)

### 1. Build Multi-Module Project
```bash
mvn clean package -DskipTests
```

### 2. Run Services Individually
- Start Eureka Server:
  ```bash
  java -jar eureka-server/target/eureka-server-1.0.0-SNAPSHOT.jar
  ```
  Access Dashboard: `http://localhost:8761` (Credentials: `admin` / `admin123`)

- Start API Gateway:
  ```bash
  java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar
  ```
  Access Gateway: `http://localhost:8080`
  Access Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Run with Docker Compose
```bash
docker-compose up --build -d
```
