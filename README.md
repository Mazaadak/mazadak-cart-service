# Cart Service
## Overview
- The Cart Service is responsible for managing shopping cart operations, including adding items, updating quantities, removing items, and cart persistence.

- It exposes a REST API, persists cart state, and integrates with other services in the Mazadak platform for inventory validation and order processing.

- The Cart Service is the owner of shopping cart state within the platform.

## API Endpoints
- See [Cart Service Wiki Page](https://github.com/Mazaadak/.github/wiki/Cart-Service) for a detailed breakdown of the service's API endpoints
- Swagger UI available at `http://localhost:18087/swagger-ui/index.html` when running locally

## How to Run
You can run it via [Docker Compose](https://github.com/Mazaadak/mazadak-infrastructure) <!-- or [Kubernetes](https://github.com/Mazaadak/mazadak-k8s/) -->

## Tech Stack
- **Spring Boot 3.5.6** (Java 21) 
- **PostgreSQL**
- **Netflix Eureka** - Service Discovery
- **Docker & Kubernetes** - Deployment & Containerization
- **Micrometer, OpenTelemetry, Alloy, Loki, Prometheus, Tempo, Grafana** - Observability
- **OpenAPI/Swagger** - API Documentation

## For Further Information
Refer to [Cart Service Wiki Page](https://github.com/Mazaadak/.github/wiki/Cart-Service).
