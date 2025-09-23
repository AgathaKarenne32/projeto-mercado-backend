package com.prati.projetomercado.controller;

import com.prati.projetomercado.service.NfceService;
import com.prati.projetomercado.utils.ScraperUtils.NfceData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nfce")
@RequiredArgsConstructor
@Tag(name = "NFC-e", description = "Endpoints para processamento de Notas Fiscais de Consumidor Eletrônicas") // NOVO
public class NfceController {

    private final NfceService nfceService;

    @Setter
    @Getter
    public static class UrlRequest {
        private String url;
        private Long userId;
    }

    @Operation(summary = "Extrai dados de uma NFC-e a partir de uma URL", description = "Recebe a URL de uma NFC-e, faz a extração dos dados dos produtos e os associa ao usuário autenticado.") // NOVO
    @ApiResponses(value = { // NOVO
            @ApiResponse(responseCode = "200", description = "Dados da nota fiscal extraídos com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NfceData.class)) }),
            @ApiResponse(responseCode = "400", description = "URL inválida ou erro no processamento da nota", content = @Content),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado, token inválido", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth") // NOVO: Indica que este endpoint requer autenticação
    @PostMapping("/scrape")
    public ResponseEntity<?> scrapeNfce(@RequestHeader("Authorization") String authorization, @RequestBody UrlRequest request) {
        try {
            NfceData data = nfceService.processNfce(request.getUrl(), authorization);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
