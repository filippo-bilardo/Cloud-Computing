#───────────────────────────────────────────────────────────
# Multi-Stage Dockerfile: Node.js + Java
#───────────────────────────────────────────────────────────

#───────────────────────────────────────────────────────────
# STAGE 1: Build Node.js App
#───────────────────────────────────────────────────────────
FROM node:20-alpine AS node-builder
WORKDIR /app/nodejs

COPY esempi/nodejs-app/package*.json ./
RUN npm ci --only=production

COPY esempi/nodejs-app/ ./

#───────────────────────────────────────────────────────────
# STAGE 2: Build Java App
#───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS java-builder
WORKDIR /app/java

COPY esempi/java-app/pom.xml ./
RUN mvn dependency:go-offline

COPY esempi/java-app/src ./src
RUN mvn package -DskipTests

#───────────────────────────────────────────────────────────
# STAGE 3: Runtime (Node.js + Java)
#───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

# Install Node.js 20.x
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy Node.js app
WORKDIR /app/nodejs
COPY --from=node-builder --chown=appuser:appuser /app/nodejs ./

# Copy Java app
WORKDIR /app/java
COPY --from=java-builder --chown=appuser:appuser /app/java/target/*.jar app.jar

# Create startup script
WORKDIR /app
COPY <<'SCRIPT' /app/start.sh
#!/bin/bash
set -e

echo "🚀 Starting Multi-Runtime Container"
echo "=================================="

# Start Java app in background
echo "▶️  Starting Java app (port 8080)..."
cd /app/java
java -XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0 -jar app.jar &
JAVA_PID=$!

# Wait for Java app to be ready
sleep 10

# Start Node.js app in foreground
echo "▶️  Starting Node.js app (port 3000)..."
cd /app/nodejs
exec node server.js
SCRIPT

RUN chmod +x /app/start.sh && \
    chown appuser:appuser /app/start.sh

USER appuser

EXPOSE 3000 8080

CMD ["/app/start.sh"]
