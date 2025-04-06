# AI Orchestration Layer MVP Documentation

## Overview

This document provides comprehensive documentation for the AI Orchestration Layer MVP implementation using Spring AI. The implementation follows the architectural design described in the provided paper, with a focus on resilience, performance, security, and event-driven architecture.

## Architecture

The AI Orchestration Layer serves as the operational brain of an AI-first enterprise, coordinating intelligent agents and systems across operations. The architecture consists of four core components:

1. **Agents Management**: Handles everything related to AI agents - autonomous, specialized models that perform specific tasks.
2. **Workflow Engine**: Defines workflow templates - structured sequences of tasks performed by one or more agents.
3. **Helper & Utility Functions**: Supporting modules that handle business logic and operational intelligence.
4. **UI Integration & Real-Time Control**: Exposes the orchestration layer's capabilities through APIs and WebSockets.

## Technology Stack

- **Spring Boot**: Application framework
- **Spring AI**: AI model integration with OpenAI
- **Spring Cloud**: Resilience, circuit breakers, configuration
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access
- **Spring WebFlux**: Reactive programming
- **Spring WebSocket**: Real-time communication
- **H2/PostgreSQL**: Database
- **Redis**: Caching
- **RabbitMQ**: Message broker
- **Docker**: Containerization

## Core Components

### Agents Management

The Agents Management component handles everything related to AI agents - autonomous, specialized models that perform specific tasks.

#### Key Features:
- Agent registration and validation
- Agent execution with Spring AI ChatClient
- Status monitoring and health checks
- Error handling and fallback mechanisms

#### Implementation:
- `Agent`: Entity model for agents
- `AgentRepository`: Data access for agents
- `AgentManagementService`: Business logic for agent operations
- `AgentController`: REST API for agent management
- `AgentEvent`: Event model for agent status changes

### Workflow Engine

The Workflow Engine defines and executes workflow templates - structured sequences of tasks performed by one or more agents.

#### Key Features:
- Workflow definition and validation
- Workflow execution with step sequencing
- Retry mechanism with exponential backoff
- Comprehensive audit trail

#### Implementation:
- `Workflow`: Entity model for workflow templates
- `WorkflowStep`: Entity model for workflow steps
- `WorkflowExecution`: Entity model for workflow executions
- `WorkflowService`: Business logic for workflow operations
- `WorkflowController`: REST API for workflow management
- `WorkflowEvent`: Event model for workflow status changes

### Helper & Utility Functions

Helper & Utility Functions handle business logic and operational intelligence supporting agents and workflows.

#### Key Features:
- Audit logging
- Risk score calculation
- Threshold determination

#### Implementation:
- `AuditLog`: Entity model for audit logs
- `AuditLogService`: Business logic for audit logging
- `RiskScoringService`: Business logic for risk scoring
- `ThresholdService`: Business logic for threshold determination
- `UtilityController`: REST API for utility functions

### UI Integration & Real-Time Control

UI Integration & Real-Time Control exposes the orchestration layer's capabilities through APIs and WebSockets.

#### Key Features:
- RESTful API for orchestration control
- WebSocket for real-time updates
- Security configuration

#### Implementation:
- `WebSocketConfig`: Configuration for WebSocket
- `WebSocketController`: Controller for WebSocket communication
- `SecurityConfig`: Security configuration

## Cloud Integration

The MVP includes cloud integration capabilities to enable deployment in cloud environments.

### Key Features:
- Containerization with Docker
- Service orchestration with Docker Compose
- Cloud-aware configuration profiles

### Implementation:
- `Dockerfile`: Container definition
- `docker-compose.yml`: Service orchestration
- `RabbitMQConfig`: Message broker configuration
- `RedisConfig`: Cache configuration
- `DatabaseConfig`: Database configuration with profiles

## Resilience Patterns

The MVP implements several resilience patterns to ensure robustness and reliability.

### Key Features:
- Circuit breaker pattern
- Retry mechanism with exponential backoff
- Bulkhead pattern for isolation
- Rate limiting
- Asynchronous processing

### Implementation:
- `ResilienceConfig`: Circuit breaker configuration
- `RetryConfig`: Retry configuration
- `BulkheadConfig`: Bulkhead configuration
- `RateLimiterConfig`: Rate limiter configuration
- `AsyncConfig`: Asynchronous execution configuration

## Security Implementation

The MVP includes security features to protect the system and data.

### Key Features:
- Authentication and authorization
- CORS configuration
- HTTP security configuration

### Implementation:
- `SecurityConfig`: Security configuration

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- Docker and Docker Compose (for local development with infrastructure)

### Local Development

1. Clone the repository
2. Set up required environment variables:
   ```
   export OPENAI_API_KEY=your_openai_api_key
   export JWT_SECRET=your_jwt_secret
   ```
3. Start the required infrastructure using Docker Compose:
   ```
   docker-compose up -d postgres rabbitmq redis
   ```
4. Build and run the application:
   ```
   ./mvnw clean package
   ./mvnw spring-boot:run
   ```

### Docker Deployment

To run the entire application stack using Docker Compose:

```
docker-compose up -d
```

This will start the application along with PostgreSQL, RabbitMQ, and Redis.

## API Documentation

### Agents API

- `POST /api/agents`: Register a new agent
- `GET /api/agents`: Get all agents
- `GET /api/agents/{id}`: Get agent by ID
- `PUT /api/agents/{id}`: Update agent
- `DELETE /api/agents/{id}`: Delete agent
- `POST /api/agents/{id}/execute`: Execute agent
- `POST /api/agents/{id}/health-check`: Perform health check

### Workflows API

- `POST /api/workflows`: Create a new workflow
- `GET /api/workflows`: Get all workflows
- `GET /api/workflows/{id}`: Get workflow by ID
- `PUT /api/workflows/{id}`: Update workflow
- `DELETE /api/workflows/{id}`: Delete workflow
- `POST /api/workflows/{id}/execute`: Execute workflow
- `GET /api/workflows/{id}/executions`: Get workflow executions
- `GET /api/workflows/executions/{executionId}`: Get workflow execution
- `POST /api/workflows/executions/{executionId}/cancel`: Cancel workflow execution
- `POST /api/workflows/executions/{executionId}/steps/{stepId}/human-review`: Complete human review

### Utility API

- `GET /api/utility/audit-logs/type/{type}`: Get audit logs by type
- `GET /api/utility/audit-logs/entity/{entityType}/{entityId}`: Get audit logs by entity
- `GET /api/utility/audit-logs/time-range`: Get audit logs by time range
- `GET /api/utility/audit-logs/user/{userId}`: Get audit logs by user
- `POST /api/utility/risk-score`: Calculate risk score
- `GET /api/utility/threshold/{context}`: Get threshold
- `PUT /api/utility/threshold/{context}`: Update threshold

## WebSocket API

- `/ws`: WebSocket endpoint
- `/topic/agents`: All agent events
- `/topic/agents/{agentId}`: Agent-specific events
- `/topic/workflows`: All workflow events
- `/topic/workflows/{workflowId}`: Workflow-specific events
- `/topic/executions/{executionId}`: Execution-specific events

## Best Practices

### Resilience

- Use circuit breakers to prevent cascading failures
- Implement retry with exponential backoff for transient failures
- Use bulkheads to isolate failures
- Implement rate limiting to prevent overload
- Use asynchronous processing for non-blocking operations

### Performance

- Use caching for frequently accessed data
- Implement asynchronous processing for concurrent operations
- Configure connection pooling for database and HTTP clients
- Use appropriate thread pools for different types of operations

### Security

- Implement authentication and authorization
- Validate all inputs
- Use HTTPS for all communications
- Implement audit logging for security events

## Conclusion

This MVP implementation provides a solid foundation for an AI Orchestration Layer using Spring AI. It incorporates best practices for resilience, performance, security, and event-driven architecture, making it suitable for cloud deployment and enterprise use.
