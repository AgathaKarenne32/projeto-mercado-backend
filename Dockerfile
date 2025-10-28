# --- ESTÁGIO 1: O "CONSTRUTOR" ---
# Usamos uma imagem completa do Java 21 (JDK) para construir nosso projeto.
# Usamos a mesma versão do Java do seu projeto [  restartedMain] c.p.p.ProjetomercadoApplication          : Starting ProjetomercadoApplication using Java 21.0.8...].
FROM eclipse-temurin:21-jdk-jammy AS builder

# Define o diretório de trabalho dentro da "caixa"
WORKDIR /app

# Copia todo o código-fonte do seu projeto para dentro da "caixa"
COPY . .

# Dá permissão de execução para o Maven Wrapper no ambiente Linux
RUN chmod +x mvnw

# Executa o comando do Maven para compilar o projeto e gerar o arquivo .jar
# Usamos -DskipTests para pular os testes, pois eles devem ser feitos em outra etapa.
RUN ./mvnw clean package -DskipTests


# --- ESTÁGIO 2: O "EXECUTOR" ---
# Agora, usamos uma imagem muito menor, que contém apenas o Java para *rodar* (JRE).
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copia *apenas* o arquivo .jar que foi gerado no Estágio 1 (o "builder")
# para dentro da nossa "caixa" final.
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta 8080, que é a porta que o seu Tomcat usa [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)].
EXPOSE 8080

# O comando que será executado quando a "caixa" for ligada.
ENTRYPOINT ["java", "-jar", "app.jar"]