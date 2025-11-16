# Gerenciamento de Compras MaisPrati

**Versão 1.0 do projeto**

Contribuidores do projeto

-   [Agatha Karenne](https://github.com/AgathaKarenne32)

-   [Camila Madureira](https://github.com/ca-madureira)

-   [Leonam Monteiro](https://github.com/bashln)

-   [Leonardo Sales](https://github.com/LRLS-Srl)

-   [Samuel Capusesera](https://github.com/SamuelLimaCap)

-   [Vinicius Avemaria](https://github.com/ViniAvemaria)

Este projeto poderá ser editado, este será seu guia para que possa realizar as edições e alterações necessarias.

## Tabela de Conteúdos

-   [1. Introdução](#1-introdução)

-   [2. Implementações](#2-implementações)

-   [3. Requisitos](#3-requisitos)

-   [4. Como rodar o projeto](#4-como-rodar-o-projeto)

-   [5. Mudança de banco de dados](#5-mudança-de-banco-de-dados)

## 1. Introdução

O projeto tem como objetivo gerenciar compras pessoais com base nas informações fornecidas pelo usuário. O sistema permite o registro manual de itens comprados, incluindo seus valores individuais, ou o cadastro automático das compras por meio da leitura do QR Code presente na Nota Fiscal de Serviço Eletrônica (NFS-e).

A cada compra registrada, o sistema realiza o cálculo automático e exibe ao usuário:

Despesas totais do mês.

Quantidade de compras realizadas

Total de itens adquiridos

Valor economizado em relação aos meses anteriores.

Além disso, o usuário pode criar um rascunho de compra, listando os itens que pretende adquirir em sua próxima compra. Com base nos valores registrados anteriormente, o sistema gera uma estimativa de custo, ajudando o usuário a planejar melhor os seus gastos. Caso os preços sejam atualizados, o valor estimado é ajustado automaticamente, garantindo um controle financeiro mais preciso e eficiente.

## 2. Implementações

### Notas Fiscais

As notas fiscais servem para cadastrar as compras do usuário e também para registrar os mercados, permitindo uma visão mais ampla dos gastos mensais.

```json
{
    "statusMessage": "string",
    "success": true,
    "data": [
        {
            "supermarket": {
                "id": 0,
                "store": "string",
                "cnpj": "string",
                "city": "string",
                "state": "string",
                "isManual": true,
                "createdAt": "2025-11-11T23:37:19.218Z",
                "updatedAt": "2025-11-11T23:37:19.219Z"
            },
            "accessKey": "string",
            "date": "2025-11-11",
            "totalPrice": 0,
            "isManual": true,
            "createdAt": "2025-11-11T23:37:19.219Z",
            "updatedAt": "2025-11-11T23:37:19.219Z",
            "products": [
                {
                    "name": "string",
                    "code": "string",
                    "quantity": 0,
                    "unit": "string",
                    "price": 0
                }
            ]
        }
    ],
    "page": {
        "pageNumber": 0,
        "pageSize": 0,
        "totalElements": 0,
        "totalPages": 0,
        "last": true
    }
}
```

### Rascunhos

Os rascunhos são utilizados para que o usuário possa ter mais controle sobre os gastos, e ter uma breve visão de gastos e produtos a serem comprados.

```json
{
    "statusMessage": "string",
    "success": true,
    "data": [
        {
            "id": 0,
            "mercado": "string",
            "conteudo": "string",
            "totalPrice": 0,
            "createdAt": "2025-11-11T23:37:52.863Z",
            "updatedAt": "2025-11-11T23:37:52.863Z"
        }
    ],
    "page": {
        "pageNumber": 0,
        "pageSize": 0,
        "totalElements": 0,
        "totalPages": 0,
        "last": true
    }
}
```

## 3. Requisitos

**Tenha instalado na sua máquina Intelij-idea.**

Linux

Via Snap

```
sudo snap install intelij-idea-community --classic
```

Windows

Instalado via jetbrains selecionando arquivo .exe

https://www.jetbrains.com/pt-br/idea/download/?section=windows

MacOs

Instalado via jetbrains, selecione arquivo .dmg

https://www.jetbrains.com/pt-br/idea/download/?section=mac

**Documentações do Swagger**

Deve ser instalado também o swagger.

Siga as instruções descritas na documentação [SWAGGER_GUIDE](https://github.com/maisprati-eng/projeto-mercado-backend/blob/develop/docs/SWAGGER_GUIDE.md) que irá conter as anotações importantes, rotas a serem seguidas e todo passo a passo de como fazer edições do código.

## 4. Como rodar o projeto

Para que possa acompanhar os processos de frontend e backend siga os passos a seguir:

### Antes de rodar o projeto do backend

Crie um arquivo chamado `.env` na raiz do projeto. 

Nesse arquivo, adicione esta chave e valor:

`GOOGLE_OAUTH2_ID:54212427633-321lush0fmhlt0bbkp0us22r258b1fnjapps.googleusercontent.com`

Copie a raiz do projeto

**Para rodar o frontend de um:**

```bash
 git clone https://github.com/maisprati-eng/projeto-mercado-frontend.git
```

**Faça o mesmo processo para backend:**

```bash
git clone https://github.com/maisprati-eng/projeto-mercado-backend.git
```

**Rode as dependências do frontend**

```bash
 npm install
```

**Caso apresente algum erro**

```bash
npm audit fix
```

**Ainda no frontend rode com:**

```bash
npm run dev
```

**Agora em backend iremos subir o servidor**

```bash
mvn spring-boot:run
```

A partir deste ponto, você já deve conseguir acessar o projeto em http://localhost:8080

Caso as rotas de login não estejam acessíveis, mantenha o Swagger aberto, acesse a rota de login e edite as strings de autenticação conforme necessário.

```java
public record LoginUserRequest(

        String username,

        String email,

        String password,

        String confirmpassword,

        String password
) {

}
```

Ao inserir as informações de login irá ser gerado um access token, e o mesmo deve ser inserido na parte de login e assim irá ter acesso a tela do dashboard do projeto.

## 5. Mudança de banco de dados

Esta parte da documentação irá conter os passos para que possa ser feita a mudança do banco de dados.

### Tenha instalado o PostgreSQL

Linux

```bash
 sudo apt install postgresql postgresql-contrib
```

Windows

```bash
 https://www.postgresql.org/download/
```

### Crie o banco do usuário

Linux

```bash
sudo -u postgres sql
```

Windowns

```bash
CREATE DATABASE mercado;
CREATE USER mercado_user WITH PASSWORD '1234';
GRANT ALL PRIVILEGES ON DATABASE mercado TO mercado_user;
```

### Atualize o arquivo `application.properties`

```
spring.datasource.url=jdbc:postgresql://localhost:5432/mercado
spring.datasource.username=mercado_user
spring.datasource.password=1234
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

### Adicione a dependência no `pom.xml`

```
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Reinicie o projeto

Linux

```bash
mvn clean install
mvn spring-boot:run
```

**Após o restart o projeto passará a utilizar o postgreSQL**
