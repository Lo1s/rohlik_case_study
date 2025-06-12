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

app.put("/products/:id", (req, res) => {
  const productId = req.params.id;
  res.json({
    message: "Mock Update Product Endpoint",
    note: "This is a development mock. Run the actual Spring Boot app for real functionality.",
    status: "success",
    productId: productId,
    receivedData: req.body,
    mockResponse: {
      id: parseInt(productId),
      ...req.body,
      updatedAt: new Date().toISOString(),
    },
  });
});

app.delete("/products/:id", (req, res) => {
  const productId = req.params.id;
  res.json({
    message: "Mock Delete Product Endpoint",
    note: "This is a development mock. Run the actual Spring Boot app for real functionality.",
    status: "success",
    deletedProductId: parseInt(productId),
    deletedAt: new Date().toISOString(),
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
      items: req.body.items || [],
      totalPrice: calculateMockOrderTotal(req.body.items || []),
      createdAt: new Date().toISOString(),
      expirationTime: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
    },
  });
});

app.get("/orders/:id", (req, res) => {
  const orderId = parseInt(req.params.id);

  // Mock order data based on ID
  const mockOrder = {
    id: orderId,
    items: [{ id: 1, productId: 1, quantity: 2, price: 3.0 }],
    totalPrice: 3.0,
    createdAt: "2024-01-01T10:00:00",
    expirationTime: "2024-01-01T10:15:00",
  };

  res.json({
    message: "Mock Get Order Endpoint",
    note: "This is a development mock. Run the actual Spring Boot app for real functionality.",
    status: "success",
    data: mockOrder,
  });
});

// Helper function to calculate mock order total
function calculateMockOrderTotal(items) {
  const productPrices = { 1: 1.5, 2: 0.8, 3: 2.0, 4: 3.2, 5: 2.5 };
  return items.reduce((total, item) => {
    const price = productPrices[item.productId] || 1.0;
    return total + price * item.quantity;
  }, 0);
}

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

// Products management page (HTML)
app.get("/products-page", (req, res) => {
  res.sendFile(path.join(__dirname, "public", "products-page.html"));
});

// Orders management page (HTML)
app.get("/orders-page", (req, res) => {
  res.sendFile(path.join(__dirname, "public", "orders-page.html"));
});

// Java setup guide page (HTML)
app.get("/java-setup-page", (req, res) => {
  res.sendFile(path.join(__dirname, "public", "java-setup.html"));
});

// Health check page (HTML)
app.get("/health-page", (req, res) => {
  res.sendFile(path.join(__dirname, "public", "health.html"));
});

// System requirements check endpoints
app.get("/system-check", async (req, res) => {
  const { exec } = require("child_process");
  const { promisify } = require("util");
  const execAsync = promisify(exec);

  const results = {
    java: { status: "checking", version: null, path: null },
    maven: { status: "checking", version: null, path: null },
    springboot: { status: "checking", message: null },
  };

  try {
    // Check Java installation
    try {
      const javaResult = await execAsync("java -version 2>&1");
      const javaVersion = javaResult.stdout || javaResult.stderr;

      if (
        javaVersion.includes("17") ||
        javaVersion.includes('openjdk version "17')
      ) {
        results.java.status = "success";
        results.java.version = javaVersion.split("\n")[0].trim();
      } else if (
        javaVersion.includes("java version") ||
        javaVersion.includes("openjdk version")
      ) {
        results.java.status = "warning";
        results.java.version = javaVersion.split("\n")[0].trim();
        results.java.message = "Java found but not version 17";
      } else {
        results.java.status = "error";
        results.java.message = "Java not found";
      }
    } catch (error) {
      results.java.status = "error";
      results.java.message = "Java not installed or not in PATH";
    }

    // Check Maven installation (prioritize Maven Wrapper since it's included in the project)
    const fs = require("fs");
    let mavenWrapperAvailable = false;
    let mavenSystemAvailable = false;
    let wrapperExists = false;

    // Check if Maven Wrapper files exist
    try {
      wrapperExists = fs.existsSync("./mvnw") || fs.existsSync("./mvnw.cmd");
    } catch (error) {
      // File system check failed
    }

    // First check if Maven Wrapper is executable (preferred for this project)
    try {
      const mvnwResult = await execAsync("./mvnw -version 2>&1");
      const mvnwOutput = mvnwResult.stdout || mvnwResult.stderr;

      if (mvnwOutput.includes("Apache Maven")) {
        mavenWrapperAvailable = true;
        results.maven.status = "success";
        results.maven.version = mvnwOutput.split("\n")[0].trim();
        results.maven.message = "Maven Wrapper (mvnw) - Recommended ✅";
        results.maven.path = "./mvnw";
      }
    } catch (mvnwError) {
      // Check if wrapper exists but has permission issues
      if (wrapperExists) {
        results.maven.status = "warning";
        results.maven.message =
          "Maven Wrapper exists but not executable - Run: chmod +x mvnw";
        results.maven.path = "./mvnw (needs permissions)";
      }
    }

    // Also check if system Maven is available
    try {
      const mavenResult = await execAsync("mvn -version 2>&1");
      const mavenOutput = mavenResult.stdout || mavenResult.stderr;

      if (mavenOutput.includes("Apache Maven")) {
        mavenSystemAvailable = true;

        if (!mavenWrapperAvailable && !wrapperExists) {
          // Only use system Maven if wrapper is not available at all
          results.maven.status = "success";
          results.maven.version = mavenOutput.split("\n")[0].trim();
          results.maven.message = "System Maven (mvn) - Wrapper recommended";
          results.maven.path = "mvn";
        } else if (mavenWrapperAvailable) {
          // Both available - wrapper is preferred, but mention system Maven too
          results.maven.message =
            "Maven Wrapper (mvnw) + System Maven available ✅";
        } else if (wrapperExists) {
          // Wrapper exists but not executable, system Maven available as backup
          results.maven.status = "warning";
          results.maven.message =
            "Wrapper needs permissions (chmod +x mvnw) - Using system Maven as backup";
          results.maven.version = mavenOutput.split("\n")[0].trim();
          results.maven.path = "mvn (backup)";
        }
      }
    } catch (mavenError) {
      // System Maven not available
    }

    // Set error status only if neither wrapper nor system Maven is available
    if (!mavenWrapperAvailable && !mavenSystemAvailable && !wrapperExists) {
      results.maven.status = "error";
      results.maven.message = "Maven Wrapper and System Maven not found";
    } else if (
      !mavenWrapperAvailable &&
      !mavenSystemAvailable &&
      wrapperExists
    ) {
      // Wrapper exists but can't execute, no system Maven
      results.maven.status = "warning";
      results.maven.message =
        "Maven Wrapper exists but not executable - Run: chmod +x mvnw";
    }

    // Check Spring Boot (by checking if Java app is running)
    try {
      const response = await fetch("http://localhost:8080/actuator/health", {
        timeout: 2000,
      });
      if (response.ok) {
        results.springboot.status = "success";
        results.springboot.message = "Spring Boot application is running";
      } else {
        results.springboot.status = "warning";
        results.springboot.message =
          "Port 8080 responding but health endpoint unavailable";
      }
    } catch (error) {
      results.springboot.status = "error";
      results.springboot.message = "Spring Boot application not running";
    }
  } catch (error) {
    console.error("System check error:", error);
  }

  res.json(results);
});

// Java status check endpoint
app.get("/java-status", async (req, res) => {
  try {
    // Try to check Spring Boot health endpoint
    const response = await fetch("http://localhost:8080/actuator/health", {
      timeout: 3000,
    });

    if (response.ok) {
      const data = await response.json();
      res.json({
        status: "running",
        message: "Spring Boot application is running",
        health: data,
        port: 8080,
        endpoints: {
          main: "http://localhost:8080",
          health: "http://localhost:8080/actuator/health",
          swagger: "http://localhost:8080/swagger-ui.html",
          h2Console: "http://localhost:8080/h2-console",
        },
      });
    } else {
      throw new Error("Health endpoint returned non-200 status");
    }
  } catch (error) {
    // Try basic connectivity check
    try {
      const response = await fetch("http://localhost:8080/", {
        timeout: 2000,
      });

      if (response.ok || response.status === 404) {
        res.json({
          status: "partial",
          message:
            "Spring Boot application is responding but health endpoint unavailable",
          port: 8080,
          endpoints: {
            main: "http://localhost:8080",
            swagger: "http://localhost:8080/swagger-ui.html",
            h2Console: "http://localhost:8080/h2-console",
          },
        });
      } else {
        throw new Error("No response from port 8080");
      }
    } catch (secondError) {
      res.json({
        status: "not_running",
        message: "Spring Boot application is not running",
        error: secondError.message,
        expectedPort: 8080,
        setupGuide: "http://localhost:3000/java-setup-page",
      });
    }
  }
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
      {
        path: "/api-docs-page",
        method: "GET",
        description: "API documentation page",
      },
      {
        path: "/api-docs",
        method: "GET",
        description: "API documentation JSON",
      },
      { path: "/products", method: "GET", description: "Products list" },
      { path: "/orders", method: "GET", description: "Orders list" },
      { path: "/health-page", method: "GET", description: "Health check page" },
      { path: "/health", method: "GET", description: "Health check API" },
      {
        path: "/java-status",
        method: "GET",
        description: "Java application status",
      },
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

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Development Server running on http://localhost:${PORT}`);
  console.log(`📚 API Documentation: http://localhost:${PORT}/api-docs-page`);
  console.log(`🛒 Products Management: http://localhost:${PORT}/products-page`);
  console.log(`📦 Orders Management: http://localhost:${PORT}/orders-page`);
  console.log(`❤️  Health Check: http://localhost:${PORT}/health-page`);
  console.log(`☕ Java Setup Guide: http://localhost:${PORT}/java-setup-page`);
  console.log(`\n📊 API Endpoints:`);
  console.log(`   Products API: http://localhost:${PORT}/products`);
  console.log(`   Orders API: http://localhost:${PORT}/orders`);
  console.log(`   Java Commands JSON: http://localhost:${PORT}/java-commands`);
  console.log(`   API Docs JSON: http://localhost:${PORT}/api-docs`);
  console.log(
    `\n📝 This is a development proxy for the Spring Boot application`,
  );
  console.log(
    `   To run the actual Java app: npm run java:dev (requires Java 17)`,
  );
  console.log(`   Spring Boot would run on: http://localhost:8080`);
  console.log(`\n✅ Development environment ready!`);
});
