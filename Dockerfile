# 1) Build do frontend (React)
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npx vite build --outDir /app/static

# 2) Build do backend (Spring Boot) com o frontend dentro
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /app/static ./src/main/resources/static
RUN mvn -q -B -DskipTests package

# 3) Imagem final
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend /app/target/area-membros-*.jar app.jar
EXPOSE 8080
# Cabe em servidores pequenos (ex.: 512 MB do plano gratuito do Render)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
ENTRYPOINT ["java", "-jar", "app.jar"]
