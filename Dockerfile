# --- ESTÁGIO 1: BUILD (Compilação) ---
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# --- ESTÁGIO 2: RUNTIME (Execução) ---
# Usamos a imagem oficial do Playwright como base final
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy

# Instala o OpenJDK 21 e limpa o cache para reduzir o tamanho
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk-headless && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Variáveis de Ambiente
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
# O Playwright baixará o necessário na primeira vez que o código rodar, 
# mas como estamos na imagem oficial, as dependências de sistema já estão lá.

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]