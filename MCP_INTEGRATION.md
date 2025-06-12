# Model Control Protocol (MCP) Integration

This project now includes a Model Control Protocol (MCP) integration that allows AI agents to interact with the e-commerce system using natural language commands.

## Overview

The MCP integration enables users to perform complex operations through simple natural language commands. Instead of making multiple API calls, users can issue commands like:

- "create order for 5 bananas and 3 apples and pay for it"
- "list products"
- "pay for order 123"
- "cancel order 456"

## Architecture

The MCP system consists of several components:

### Backend Components

1. **McpController** (`/api/mcp/*`)

   - `/api/mcp/command` - Process natural language commands
   - `/api/mcp/help` - Get command examples and documentation
   - `/api/mcp/status` - Check MCP service status

2. **McpService**

   - Natural language processing and intent recognition
   - Command parsing and API orchestration
   - Integration with existing ProductService and OrderService

3. **DTOs**
   - `McpRequestDto` - Request structure for commands
   - `McpResponseDto` - Structured response with actions, results, and errors
   - `ParsedOrderItemDto` - Parsed product information from text

### Frontend Interface

- **MCP Web Interface** (`/mcp-page`)
  - Chat-like interface for natural language interaction
  - Example commands and help documentation
  - Real-time response display with actions and errors

## Supported Commands

### Order Management

```text
# Create Orders
"create order for 5 bananas and 3 apples"
"buy 2 milk and 1 bread"
"order 10 apples and pay for it"

# Payment
"pay for order 123"
"complete payment for order 456"

# Cancellation
"cancel order 123"
"cancel my order 456"
```

### Product Management

```text
# List Products
"list products"
"show available products"
"what products do you have"
```

## API Usage

### Command Processing

**Endpoint:** `POST /api/mcp/command`

**Request:**

```json
{
  "command": "create order for 5 bananas and 3 apples",
  "sessionId": "optional_session_id"
}
```

**Response:**

```json
{
  "status": "success",
  "message": "Actions completed: Added 5 Banana to order, Added 3 Apple to order, Order created successfully with ID: 123",
  "sessionId": "optional_session_id",
  "timestamp": "2024-01-01T12:00:00",
  "result": {
    "id": 123,
    "items": [...],
    "paid": false,
    "createdAt": "2024-01-01T12:00:00"
  },
  "actions": [
    "Added 5 Banana to order",
    "Added 3 Apple to order",
    "Order created successfully with ID: 123"
  ],
  "errors": []
}
```

### Getting Help

**Endpoint:** `GET /api/mcp/help`

Returns comprehensive documentation, examples, and supported intents.

### Service Status

**Endpoint:** `GET /api/mcp/status`

Returns current MCP service status and capabilities.

## Natural Language Processing

The MCP service includes basic natural language processing that can:

1. **Extract Product Names and Quantities**

   - Regex patterns to identify "5 bananas", "3 apples", etc.
   - Fuzzy matching against available products
   - Support for "and" connectors between items

2. **Intent Recognition**

   - Create orders: "create", "order", "buy"
   - List products: "list", "show", "available"
   - Payment: "pay", "complete payment"
   - Cancellation: "cancel"

3. **Command Orchestration**
   - Automatic API call sequencing
   - Error handling and rollback
   - Action logging and user feedback

## Development Setup

### Running the Development Server

The Node.js development server includes mock MCP endpoints:

```bash
npm run dev
```

Then visit: http://localhost:3000/mcp-page

### Running the Full Java Application

For full MCP functionality with real data persistence:

```bash
# Start Spring Boot application
npm run java:dev

# Or manually
./mvnw spring-boot:run
```

The Java application runs on port 8080 with full MCP integration.

## Testing

Run the MCP integration tests:

```bash
# Run all tests
npm run java:test

# Or run specifically MCP tests
./mvnw test -Dtest=McpControllerIntegrationTest
```

## Circuit Breaker Protection

The MCP service includes circuit breaker protection using Resilience4j:

- Failure rate threshold: 50%
- Sliding window size: 10 requests
- Wait duration in open state: 10 seconds

Configuration in `application.properties`:

```properties
resilience4j.circuitbreaker.instances.mcpService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.mcpService.sliding-window-size=10
resilience4j.circuitbreaker.instances.mcpService.wait-duration-in-open-state=10s
```

## Future Enhancements

1. **Advanced NLP**

   - Integration with external NLP services (OpenAI, Google NLP)
   - Better intent recognition and entity extraction
   - Support for more complex command structures

2. **Multi-step Workflows**

   - Shopping cart management
   - Product recommendations
   - Order history queries

3. **Voice Integration**

   - Speech-to-text input
   - Text-to-speech responses
   - Voice command processing

4. **AI Agent Integration**
   - OpenAI Assistant API integration
   - Custom AI model training
   - Context-aware conversations

## Security Considerations

- Input validation and sanitization
- Rate limiting for MCP endpoints
- Session management and authentication
- Command logging and audit trails

## Error Handling

The MCP system provides comprehensive error handling:

- Partial success responses when some actions fail
- Detailed error messages for debugging
- Graceful fallbacks for unrecognized commands
- Circuit breaker protection for service failures

## Integration with Existing System

The MCP integration is designed to be non-invasive:

- Reuses existing ProductService and OrderService
- Maintains existing API contracts
- Adds new endpoints without modifying existing ones
- Preserves all existing functionality and tests
