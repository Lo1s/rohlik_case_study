# MCP (Model Context Protocol) Integration Guide

This guide explains how to integrate MCP with your e-commerce application to enable AI agents to perform complex operations using natural language.

## 🎯 What is MCP?

Model Context Protocol (MCP) is a standardized way for AI agents to interact with applications through well-defined tools and functions. It enables AI models to:

- Execute specific functions in your application
- Access real-time data
- Perform complex multi-step operations
- Maintain context across interactions

## 🚀 Features Implemented

### 1. **Natural Language Order Processing**
AI agents can process requests like:
- "create order for 5 bananas and 4 apples and pay for it"
- "order 3 oranges and 2 grapes"
- "I want 10 strawberries and 1 pineapple, please pay for it"

### 2. **MCP Tools Available**

#### `search_products`
- **Purpose**: Find products by name or description
- **Input**: Search query (e.g., "banana", "apple")
- **Output**: List of matching products with ID, name, price, and stock

#### `create_order`
- **Purpose**: Create orders with specific product IDs and quantities
- **Input**: Array of items with productId and quantity
- **Output**: Order details including ID, total price, and status

#### `pay_order`
- **Purpose**: Process payment for existing orders
- **Input**: Order ID
- **Output**: Payment confirmation and updated order status

#### `natural_language_order`
- **Purpose**: Process complex natural language requests
- **Input**: Natural language request
- **Output**: Complete order processing including creation and optional payment

### 3. **Integration Endpoints**

#### WebSocket Endpoint
```
ws://localhost:8080/mcp
```
- Full MCP protocol implementation
- Real-time bidirectional communication
- Tool discovery and execution

#### REST API Endpoints
```
GET  /api/mcp/tools                    # List available tools
POST /api/mcp/tools/{toolName}/execute # Execute specific tool
POST /api/mcp/order                    # Quick natural language order processing
```

## 🛠️ How It Works

### 1. **Product Name Resolution**
The system automatically resolves natural language product names to database IDs:
```
"bananas" → searches for products containing "banana" → finds "Banana" (ID: 1)
"apples"  → searches for products containing "apple"  → finds "Apple" (ID: 2)
```

### 2. **Quantity Extraction**
Uses regex patterns to extract quantities and product names:
```
"5 bananas and 4 apples" → [(5, "banana"), (4, "apple")]
```

### 3. **Order Orchestration**
1. Parse natural language request
2. Extract items and quantities
3. Resolve product names to IDs
4. Create order with resolved items
5. Optionally process payment if requested

### 4. **Error Handling**
- Product not found → suggests available products
- Insufficient stock → returns stock availability
- Invalid quantities → validation errors
- Payment failures → detailed error messages

## 📋 Usage Examples

### Using the Web Interface
1. Navigate to `http://localhost:8080/mcp-demo.html`
2. Enter natural language requests
3. View real-time results

### Using REST API
```bash
# Process natural language order
curl -X POST http://localhost:8080/api/mcp/order \
  -H "Content-Type: application/json" \
  -d '{"request": "create order for 5 bananas and 4 apples and pay for it"}'

# Search for products
curl -X POST http://localhost:8080/api/mcp/tools/search_products/execute \
  -H "Content-Type: application/json" \
  -d '{"query": "banana"}'

# Create order manually
curl -X POST http://localhost:8080/api/mcp/tools/create_order/execute \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 1, "quantity": 5},
      {"productId": 2, "quantity": 4}
    ]
  }'
```

### Using WebSocket (MCP Protocol)
```javascript
const ws = new WebSocket('ws://localhost:8080/mcp');

// List available tools
ws.send(JSON.stringify({
  jsonrpc: "2.0",
  id: "1",
  method: "tools/list"
}));

// Execute natural language order
ws.send(JSON.stringify({
  jsonrpc: "2.0",
  id: "2",
  method: "tools/call",
  params: {
    name: "natural_language_order",
    arguments: {
      request: "create order for 5 bananas and 4 apples and pay for it"
    }
  }
}));
```

## 🔧 Technical Architecture

### Components

1. **MCPServer** - WebSocket handler implementing MCP protocol
2. **MCPController** - REST API endpoints for HTTP access
3. **MCP Tools** - Individual tool implementations
4. **DTOs** - Data transfer objects for MCP protocol
5. **DataInitializer** - Sample product data for testing

### Tool Interface
```java
public interface MCPTool {
    String getName();
    MCPToolDefinition getDefinition();
    MCPToolResult execute(Map<String, Object> arguments) throws Exception;
}
```

### Adding New Tools
1. Implement the `MCPTool` interface
2. Add `@Component` annotation
3. Define tool schema in `getDefinition()`
4. Implement business logic in `execute()`

## 🚦 Getting Started

### 1. Start the Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using npm scripts
npm run java:dev
```

### 2. Access the Demo
- Web Interface: `http://localhost:8080/mcp-demo.html`
- API Documentation: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`

### 3. Test Natural Language Orders
Try these example requests:
- "create order for 5 bananas and 4 apples and pay for it"
- "order 3 oranges and 2 grapes"
- "I want 10 strawberries and 1 pineapple, please pay for it"
- "buy 2 mangos and 5 kiwis"

## 🔮 AI Agent Integration

### Claude/ChatGPT Integration
```python
import requests

def create_order_with_ai(natural_request):
    response = requests.post(
        'http://localhost:8080/api/mcp/order',
        json={'request': natural_request}
    )
    return response.json()

# Example usage
result = create_order_with_ai("create order for 5 bananas and 4 apples and pay for it")
print(result)
```

### Custom AI Agent
```javascript
class EcommerceAgent {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    async processOrder(request) {
        const response = await fetch(`${this.baseUrl}/api/mcp/order`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ request })
        });
        return response.json();
    }
    
    async searchProducts(query) {
        const response = await fetch(`${this.baseUrl}/api/mcp/tools/search_products/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ query })
        });
        return response.json();
    }
}

// Usage
const agent = new EcommerceAgent('http://localhost:8080');
const result = await agent.processOrder('order 5 bananas and pay for it');
```

## 🔒 Security Considerations

1. **Input Validation** - All inputs are validated and sanitized
2. **Error Handling** - Detailed error messages without exposing internals
3. **Rate Limiting** - Consider implementing rate limiting for production
4. **Authentication** - Add authentication for production deployments
5. **CORS** - Configure CORS policies appropriately

## 📈 Extending the System

### Adding New Product Categories
1. Update the `DataInitializer` with new products
2. Enhance the search algorithm for better matching
3. Add category-based filtering

### Advanced Natural Language Processing
1. Integrate with NLP libraries for better parsing
2. Add support for complex queries
3. Implement intent recognition

### Multi-step Workflows
1. Add order modification tools
2. Implement inventory management
3. Add customer management tools

## 🐛 Troubleshooting

### Common Issues

1. **WebSocket Connection Failed**
   - Check if port 8080 is available
   - Verify Spring Boot application is running
   - Check firewall settings

2. **Product Not Found**
   - Verify product names in database
   - Check search algorithm sensitivity
   - Review available products via `/api/products`

3. **Order Creation Failed**
   - Check product stock availability
   - Verify product IDs exist
   - Review validation errors

### Debug Mode
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.rohlik.case_study.mcp: DEBUG
```

## 📚 Additional Resources

- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [OpenAPI/Swagger Documentation](http://localhost:8080/swagger-ui.html)

---

This MCP integration enables powerful AI agent interactions with your e-commerce application, making it possible to handle complex operations through simple natural language requests.