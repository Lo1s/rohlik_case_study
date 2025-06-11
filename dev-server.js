const express = require("express");
const cors = require("cors");
const path = require("path");

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static("public"));

// API Documentation endpoint
app.get("/", (req, res) => {
  res.json({
    message: "Spring Boot Case Study - Development Server",
    description:
      "This is a development proxy for the Java Spring Boot application",
    note: "To run the actual Java application, you need Java 17 and Maven installed",
    status: "✅ Development server running successfully",
    timestamp: new Date().toISOString(),
    endpoints: {
      "API Documentation": "GET /api-docs",
      Products: "GET /products",
      Orders: "GET /orders",
      "Create Product": "POST /products",
      "Create Order": "POST /orders",
      "Java Commands": "GET /java-commands",
      "Health Check": "GET /health",
    },
    actualSpringBootEndpoints: {
      "API Documentation": "http://localhost:8080/swagger-ui.html",
      "H2 Database Console": "http://localhost:8080/h2-console",
      Products: "http://localhost:8080/products",
      Orders: "http://localhost:8080/orders",
    },
  });
});

// API Documentation
app.get("/api-docs", (req, res) => {
  res.json({
    title: "Rohlik Case Study API Documentation",
    description: "E-commerce API for managing products and orders",
    version: "1.0.0",
    baseUrl: "http://localhost:8080",
    proxyUrl: "http://localhost:3000",
    endpoints: {
      products: {
        "GET /products": "List all products",
        "POST /products": "Create a new product",
        "PUT /products/{id}": "Update a product",
        "DELETE /products/{id}": "Delete a product",
      },
      orders: {
        "GET /orders": "List all orders",
        "POST /orders": "Create a new order",
        "GET /orders/{id}": "Get order details",
      },
    },
    models: {
      Product: {
        id: "Long",
        name: "String",
        price: "BigDecimal",
        stock: "Integer",
      },
      Order: {
        id: "Long",
        items: "List<OrderItem>",
        totalPrice: "BigDecimal",
        createdAt: "LocalDateTime",
        expirationTime: "LocalDateTime",
      },
      OrderItem: {
        id: "Long",
        product: "Product",
        quantity: "Integer",
        price: "BigDecimal",
      },
    },
  });
});

// Mock Products endpoints
app.get("/products", (req, res) => {
  res.json({
    message: "Mock Products Endpoint",
    note: "This is a development mock. Run the actual Spring Boot app for real data.",
    status: "success",
    count: 5,
    data: [
      { id: 1, name: "Apple", price: 1.5, stock: 100 },
      { id: 2, name: "Banana", price: 0.8, stock: 150 },
      { id: 3, name: "Orange", price: 2.0, stock: 75 },
      { id: 4, name: "Milk", price: 3.2, stock: 50 },
      { id: 5, name: "Bread", price: 2.5, stock: 80 },
    ],
  });
});

app.post("/products", (req, res) => {
  res.json({
    message: "Mock Create Product Endpoint",
    note: "This is a development mock. Run the actual Spring Boot app for real functionality.",
    status: "success",
    receivedData: req.body,
    mockResponse: {
      id: Math.floor(Math.random() * 1000),
      ...req.body,
      createdAt: new Date().toISOString(),
    },
  });
});

// Mock Orders endpoints
app.get("/orders", (req, res) => {
  res.json({
    message: "Mock Orders Endpoint",
    note: "This is a development mock. Run the actual Spring Boot app for real data.",
    status: "success",
    count: 2,
    data: [
      {
        id: 1,
        items: [{ id: 1, productId: 1, quantity: 2, price: 3.0 }],
        totalPrice: 3.0,
        createdAt: "2024-01-01T10:00:00",
        expirationTime: "2024-01-01T10:15:00",
      },
      {
        id: 2,
        items: [
          { id: 2, productId: 2, quantity: 3, price: 2.4 },
          { id: 3, productId: 3, quantity: 1, price: 2.0 },
        ],
        totalPrice: 4.4,
        createdAt: "2024-01-01T11:00:00",
        expirationTime: "2024-01-01T11:15:00",
      },
    ],
  });
});

app.post("/orders", (req, res) => {
  res.json({
    message: "Mock Create Order Endpoint",
    note: "This is a development mock. Run the actual Spring Boot app for real functionality.",
    status: "success",
    receivedData: req.body,
    mockResponse: {
      id: Math.floor(Math.random() * 1000),
      ...req.body,
      createdAt: new Date().toISOString(),
      expirationTime: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
    },
  });
});

// Java commands information
app.get("/java-commands", (req, res) => {
  res.json({
    title: "Java Spring Boot Commands",
    description: "Commands to run the actual Java application",
    requirements: "Java 17 and Maven",
    setupSteps: [
      "1. Install Java 17: apt-get install openjdk-17-jdk",
      "2. Verify installation: java -version",
      "3. Compile project: ./mvnw clean compile",
      "4. Run application: ./mvnw spring-boot:run",
    ],
    commands: {
      "Install Dependencies": "./mvnw clean compile",
      "Run Application": "./mvnw spring-boot:run",
      "Run Tests": "./mvnw test",
      "Build JAR": "./mvnw clean package",
      "Clean Build": "./mvnw clean",
    },
    endpoints: {
      Application: "http://localhost:8080",
      "API Documentation": "http://localhost:8080/swagger-ui.html",
      "H2 Database Console": "http://localhost:8080/h2-console",
    },
    notes: [
      "The Spring Boot application runs on port 8080 by default",
      "H2 database console available for testing",
      "API documentation via Swagger UI",
      "This development server runs on port 3000",
      "Use 'npm run java:dev' to start Java app (if Java is installed)",
    ],
  });
});

// API documentation page (HTML)
app.get("/api-docs-page", (req, res) => {
  res.sendFile(path.join(__dirname, "public", "api-docs.html"));
});

// Health check page (HTML)
app.get("/health-page", (req, res) => {
  res.sendFile(path.join(__dirname, "public", "health.html"));
});

// Health check API (JSON)
app.get("/health", (req, res) => {
  const memory = process.memoryUsage();
  const uptime = process.uptime();

  res.json({
    status: "UP",
    service: "Development Proxy Server",
    timestamp: new Date().toISOString(),
    environment: "development",
    port: PORT,
    uptime: uptime,
    memory: {
      rss: memory.rss,
      heapTotal: memory.heapTotal,
      heapUsed: memory.heapUsed,
      external: memory.external,
      arrayBuffers: memory.arrayBuffers,
    },
    version: "1.0.0",
    nodeVersion: process.version,
    platform: process.platform,
    arch: process.arch,
    loadAverage: process.platform === "linux" ? require("os").loadavg() : null,
    endpoints: [
      { path: "/", method: "GET", description: "Landing page" },
      { path: "/api-docs", method: "GET", description: "API documentation" },
      { path: "/products", method: "GET", description: "Products list" },
      { path: "/orders", method: "GET", description: "Orders list" },
      { path: "/health", method: "GET", description: "Health check API" },
      { path: "/health-page", method: "GET", description: "Health check page" },
      {
        path: "/java-commands",
        method: "GET",
        description: "Java setup guide",
      },
    ],
  });
});

// 404 handler
app.use("*", (req, res) => {
  res.status(404).json({
    error: "Endpoint not found",
    message: "This is a development proxy for the Spring Boot application",
    path: req.originalUrl,
    method: req.method,
    availableEndpoints: [
      "GET /",
      "GET /api-docs",
      "GET /products",
      "POST /products",
      "GET /orders",
      "POST /orders",
      "GET /java-commands",
      "GET /health",
    ],
    suggestion:
      "Visit / for the main landing page or /api-docs for API documentation",
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error("Error:", err);
  res.status(500).json({
    error: "Internal Server Error",
    message: "Something went wrong on the server",
    timestamp: new Date().toISOString(),
  });
});

app.listen(PORT, () => {
  console.log(`🚀 Development Server running on http://localhost:${PORT}`);
  console.log(`📚 API Documentation: http://localhost:${PORT}/api-docs-page`);
  console.log(`📊 API JSON: http://localhost:${PORT}/api-docs`);
  console.log(`🔧 Java Commands: http://localhost:${PORT}/java-commands`);
  console.log(`❤️  Health Check: http://localhost:${PORT}/health-page`);
  console.log(`🛒 Products: http://localhost:${PORT}/products`);
  console.log(`📦 Orders: http://localhost:${PORT}/orders`);
  console.log(
    `\n📝 This is a development proxy for the Spring Boot application`,
  );
  console.log(
    `   To run the actual Java app: npm run java:dev (requires Java 17)`,
  );
  console.log(`   Spring Boot would run on: http://localhost:8080`);
  console.log(`\n✅ Development environment ready!`);
});
