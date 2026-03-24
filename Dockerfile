# --- ESTÁGIO 1: BUILD ---
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# --- ESTÁGIO 2: RUNTIME ---
# Usamos a imagem do Playwright que já tem as dependências de sistema
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy

# Instala o JRE 21 (necessário para rodar o seu Jar)
RUN apt-get update && apt-get install -y openjdk-21-jre-headless && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia o JAR gerado
COPY --from=build /app/target/*.jar app.jar

# COMANDO CORRIGIDO: Instala os navegadores usando o CLI do Playwright 
# que já vem embutido na imagem, sem depender do Maven.
RUN npx playwright install chromium --with-deps

# Configurações de ambiente
ENV PORT=8080
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]