# Gerenciamento de Compras MaisPrati


**Versão 1.0 do projeto**

Contribuidores do projeto https://github.com/SamuelLimaCap/SamuelLimaCap
 ;

https://github.com/ViniAvemaria ;

https://github.com/ca-madureira ;

https://github.com/leonamsh ;

https://github.com/AgathaKarenne32 ;

https://github.com/LRLS-Srl.

Este projeto poderá ser editado, este será seu guia para que possa realizar as edições e alterações necessarias.

## Tabela de Conteúdos


.[1. Introdução do projeto](#1introdução-do-projeto)

.[2. Implementações](#Implementação)

.[3. Requisitos](#Requisitos)

.[4. Como rodar o projeto](#Como-rodar-projeto)

.[5. Mudança de banco de dados](#Mudança-de-banco-de-dados)

## 1.1 Introdução ao Gerenciador Compras

O projeto tem como objetivo gerenciar compras pessoais com base nas informações fornecidas pelo usuário. O sistema permite o registro manual de itens comprados, incluindo seus valores individuais, ou o cadastro automático das compras por meio da leitura do QR Code presente na Nota Fiscal de Serviço Eletrônica (NFS-e).

A cada compra registrada, o sistema realiza o cálculo automático e exibe ao usuário:

Despesas totais do mês.

Quantidade de compras realizadas

Total de itens adquiridos

Valor economizado em relação aos meses anteriores.

Além disso, o usuário pode criar um rascunho de compra, listando os itens que pretende adquirir em sua próxima compra. Com base nos valores registrados anteriormente, o sistema gera uma estimativa de custo, ajudando o usuário a planejar melhor os seus gastos. Caso os preços sejam atualizados, o valor estimado é ajustado automaticamente, garantindo um controle financeiro mais preciso e eficiente.

## 1.2 Implementações

  ### 1.2.1 Notas Fiscais 

 As notas fiscais servem para cadastrar as compras do usuário e também para registrar os mercados, permitindo uma visão mais ampla dos gastos mensais.

~~~~
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
        "isManual": true
      },
      "accessKey": "string",
      "date": "2025-10-31",
      "totalPrice": 0,
      "isManual": true,
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


~~~~

## 1.2.2 Rascunhos 

Os rascunhos são utilizados para que o usuário possa ter mais controle sobre os gastos, e ter uma breve visão de gastos e produtos a serem comprados.

~~~

{
  "statusMessage": "string",
  "success": true,
  "data": [
    {
      "id": 0,
      "mercado": "string",
      "conteudo": "string",
      "createdAt": "2025-10-31T22:15:49.949Z",
      "updatedAt": "2025-10-31T22:15:49.949Z"
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
~~~~

## 1.3 Requisitos

**Tenha instalado na sua máquina Intelij-idea.**

Linux 

Via Snap
~~~
sudo snap install intelij-idea-community --classic
~~~
Windows

Instalado via jetbrains selecionando arquivo .exe


*https://www.jetbrains.com/pt-br/idea/download/?section=windows]


MacOs


Instalado via jetbrains, selecione arquivo .dmg

*https://www.jetbrains.com/pt-br/idea/download/?section=mac]

**Documentações do Swagger**


Deve ser instalado também o swagger. 

Siga as instruções descritas na documentação ```SWAGGER_GUIDE.md``` que irá conter as anotações importantes, rotas a serem seguidas e todo passo a passo de como fazer edições do código.

## 1.4 Como rodar o projeto 

Para que possa acompanhar os processos de frontend e backend siga os passos a seguir:
 
### Antes de rodar o projeto acesso o link, contém informações do *.env* a ser criado

```bash
 https://github.com/maisprati-eng/projeto-mercado-backend?tab=readme-ov-file#gerenciamento-de-compras-backend
```
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




~~~

public record LoginUserRequest(


        String username,

        String email,


        String password,


        String confirmpassword


        String password

) {

}

~~~

Ao inserir as informações de login irá ser gerado um access token, e o  mesmo deve ser inserido na parte de login e assim irá ter acesso a tela do dashboard do projeto.


## 1.5 Mudança de banco de dados

Esta parte da documentação irá conter os passos para que possa ser feita a mudança do banco de dados.

### 1.5.1 Tenha instalado o postgreSQL

Linux

```bash
 sudo apt install postgresql postgresql-contrib
```
Windows

```bash
 https://www.postgresql.org/download/
```


### 1.5.2 Crie o banco do usuário acessando o terminal

Linux 
```bash
sudo -u postgres sql
```
Windowns
```bash
CREATE DATABASE mercado;
CREATE USER mercado_user WITH PASSWORD '1234';
GRANT ALL PRIVILEGES ON DATABASE mercado TO mercado_user;
````
### 1.5.3 Atualize o arquivo application.properties

~~~
spring.datasource.url=jdbc:postgresql://localhost:5432/mercado
spring.datasource.username=mercado_user
spring.datasource.password=1234
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
~~~
### 1.5.4 Adicione a dependência no pom.xml
~~~
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
~~~
## Reinicie o projeto 

Linux
```bash
mvn clean install
mvn spring-boot:run
```
**Após o restart o projeto passará a utilizar o postgreSQL**
