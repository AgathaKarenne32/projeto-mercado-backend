package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.request.CreateRascunhoRequest;
import com.prati.projetomercado.dto.request.RascunhoFilterRequest;
import com.prati.projetomercado.dto.request.UpdateRascunhoRequest;
import com.prati.projetomercado.dto.response.PageResponse;
import com.prati.projetomercado.dto.response.RascunhoResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.RascunhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rascunhos") // Define o caminho base para todos os endpoints neste controller
@RequiredArgsConstructor
@Tag(name = "Rascunhos", description = "Endpoints para gerenciamento de rascunhos de usuários")
@SecurityRequirement(name = "bearerAuth") // Exige autenticação para TODOS os endpoints neste controller
public class RascunhoController {

    // Injeção de dependência do nosso service
    private final RascunhoService rascunhoService;

    @PostMapping
    @Operation(summary = "Cria um novo rascunho", description = "Cria um novo rascunho associado ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rascunho criado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<SuccessResponse<RascunhoResponse>> criarRascunho(@RequestBody CreateRascunhoRequest request) {
        RascunhoResponse response = rascunhoService.criarRascunho(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>("Rascunho criado com sucesso.", response));
    }

    @GetMapping
    @Operation(summary = "Lista todos os rascunhos do usuário", description = "Retorna uma lista de todos os rascunhos pertencentes ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de rascunhos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<SuccessResponse<List<RascunhoResponse>>> buscarRascunhosDoUsuario(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RascunhoResponse> response = rascunhoService.buscarRascunhosDoUsuario(page, size);
        PageResponse pageInfo = PageResponse.from(response);
        return ResponseEntity.ok(new SuccessResponse<>("Rascunhos encontrados com sucesso.", response.getContent(), pageInfo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um rascunho por ID", description = "Retorna os detalhes de um rascunho específico, se pertencer ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rascunho encontrado"),
            @ApiResponse(responseCode = "404", description = "Rascunho não encontrado"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<SuccessResponse<RascunhoResponse>> buscarRascunhoPorId(@PathVariable("id") Long rascunhoId) {
        RascunhoResponse response = rascunhoService.buscarRascunhoPorId(rascunhoId);
        return ResponseEntity.ok(new SuccessResponse<>("Rascunho encontrado com sucesso.", response));
    }

    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<List<RascunhoResponse>>> searchRascunhos(
            RascunhoFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RascunhoResponse> response = rascunhoService.searchRascunhos(filter, page, size);
        PageResponse pageInfo = PageResponse.from(response);

        return ResponseEntity.ok(new SuccessResponse<>("Rascunhos filtrados com sucesso.", response.getContent(), pageInfo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um rascunho", description = "Atualiza o título e o conteúdo de um rascunho existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rascunho atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Rascunho não encontrado"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<SuccessResponse<RascunhoResponse>> atualizarRascunho(@PathVariable("id") Long rascunhoId, @RequestBody UpdateRascunhoRequest request) {
        RascunhoResponse response = rascunhoService.atualizarRascunho(rascunhoId, request);
        return ResponseEntity.ok(new SuccessResponse<>("Rascunho editado com sucesso.", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Apaga um rascunho", description = "Remove um rascunho permanentemente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rascunho apagado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Rascunho não encontrado"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado")
    })
    public ResponseEntity<Void> apagarRascunho(@PathVariable("id") Long rascunhoId) {
        rascunhoService.apagarRascunho(rascunhoId);
        return ResponseEntity.noContent().build(); // Retorna status 204 No Content, que é o padrão para DELETE bem-sucedido
    }
}
