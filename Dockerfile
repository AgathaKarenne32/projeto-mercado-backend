# --- ESTÁGIO 1: BUILD ---
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
# Copia apenas o pom.xml primeiro para aproveitar o cache das dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código e gera o jar
COPY src ./src
RUN mvn clean package -DskipTests

# --- ESTÁGIO 2: RUNTIME ---
# Usamos a imagem oficial do Playwright que já vem com as dependências do sistema
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy

# Instala o JDK 21 para rodar sua aplicação (Ubuntu base)
RUN apt-get update && apt-get install -y openjdk-21-jdk-headless && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# COMANDO CRUCIAL: Instala os navegadores necessários dentro da imagem
# Sem isso, o DriverException continuará ocorrendo
RUN mvn com.microsoft.playwright:playwright-maven-plugin:1.40.0:install-browsers

# Configurações de porta e ambiente
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]