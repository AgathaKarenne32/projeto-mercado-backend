# Guia de Documentação de API com Swagger

Este documento explica como documentar os endpoints da nossa API usando as anotações do `springdoc-openapi`.

## Como Visualizar a Documentação

1.  Rode a aplicação localmente.
2.  Acesse a seguinte URL no seu navegador: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Anotações Essenciais

-   `@Tag`: Usada no topo da classe `Controller` para agrupar todos os seus endpoints sob um título (ex: "Autenticação", "Produtos").
-   `@Operation`: Usada acima de cada método (endpoint) para dar um resumo (`summary`) e uma descrição (`description`) do que ele faz.
-   `@ApiResponses` e `@ApiResponse`: Usadas para descrever cada possível código de retorno HTTP (ex: 200 para sucesso, 404 para não encontrado, 401 para não autorizado). É importante documentar tanto os casos de sucesso quanto os de erro.
-   `@Parameter`: Usada para descrever parâmetros específicos, como um `{id}` na URL.


## Como Testar Rotas Protegidas (Com Login)

Muitos endpoints da nossa API exigem que o usuário esteja autenticado. Para testá-los no Swagger, siga estes passos:

1.  **Faça Register:** Use o endpoint `POST /auth/register` para seu usúario ser reconhecido.

2.  **Faça Login:** Use o endpoint `POST /auth/login` para obter um `accessToken`. Copie o valor completo do token da resposta.

3.  **Autorize o Swagger:** No topo da página, clique no botão verde **"Authorize"**. Na janela que abrir, cole o token no campo "Value", lembrando que não precisa adicionar a palavra Bearer somente o token já serve.

4.  **Execute os Testes:** Agora você pode testar qualquer endpoint protegido. O Swagger enviará automaticamente o seu token de autorização.

## Exemplo Completo

Use o código abaixo como um modelo para documentar novos endpoints. Este é o exemplo do endpoint de login:

```java
@Operation(summary = "Realiza o login de um usuário", description = "Autentica um usuário com nome de usuário e senha, retornando um token de acesso e um refresh token.")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Login bem-sucedido",
            content = { @Content(mediaType = "application/json",
            schema = @Schema(implementation = JwtToken.class)) }),
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content)
})
@PostMapping("/login")
public ResponseEntity<JwtToken> login(@RequestBody LoginUserRequest userRequest) throws Exception {
   var jwtToken = userService.login(userRequest);
   return new ResponseEntity<>(jwtToken, HttpStatus.OK);
}

