package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.request.NfceDataRequest;
import com.prati.projetomercado.exceptions.DuplicateNfceException;
import com.prati.projetomercado.exceptions.EditNotAllowedException;
import com.prati.projetomercado.exceptions.NfceNotFoundException;
import com.prati.projetomercado.exceptions.UnauthorizedNfceAccessException;
import com.prati.projetomercado.service.NfceService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/nfce")
@RequiredArgsConstructor
public class NfceController {

    private final NfceService nfceService;

    @Setter
    @Getter
    public static class UrlRequest {
        private String url;
    }

    @Setter
    @Getter
    public static class AccessKeyRequest {
        private String accessKey;
    }

    // rota para cadastrar nfce pelo link
    @PostMapping("/scrape")
    public ResponseEntity<Map<String, Object>> scrapeNfce(@RequestHeader("Authorization") String authorization, @RequestBody UrlRequest request) throws IOException {
        NfceDataRequest data = nfceService.registerNfceLink(authorization, request.getUrl());
        return ResponseEntity.ok(
                Map.of(
                        "statusMessage", data,
                        "success", true
                )
        );
    }

    // rota para cadastrar nfce manualmente
    @PostMapping("/manual")
    public ResponseEntity<Map<String, Object>> manualNfce(@RequestHeader("Authorization") String authorization, @RequestBody NfceDataRequest manualData) {
        nfceService.registerNfceManual(authorization, manualData);
        return ResponseEntity.ok(
                Map.of(
                        "statusMessage", "Nota fiscal manual cadastrada com sucesso.",
                        "success", true
                )
        );
    }

    // rota para editar nota fiscal cadastrada manualmente
    @PutMapping("/edit")
    public ResponseEntity<Map<String, Object>> editNfce(@RequestHeader("Authorization") String authorization,
                                                          @RequestBody NfceDataRequest updatedNfce) {
        nfceService.updateNfce(authorization, updatedNfce);
        return ResponseEntity.ok(
                Map.of(
                        "statusMessage", "Nota fiscal editada com sucesso.",
                        "success", true
                )
        );
    }

    // rota para deletar nota pela chave de acesso
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteNfce(@RequestHeader("Authorization") String authorization,
                                                          @RequestBody AccessKeyRequest request)  {
        nfceService.deleteNfce(authorization, request.accessKey);
        return ResponseEntity.ok(
                Map.of(
                        "statusMessage", "Nota fiscal deletada com sucesso.",
                        "success", true
                )
        );
    }

    // EXCEPTION HANDLERS

    // usuário tentando editar ou deletar nota fiscal que não é dele
    // retorna status 403 forbidden
    @ExceptionHandler(UnauthorizedNfceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedNfceAccessException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of(
                        "statusMessage", e.getMessage(),
                        "success", false
                )
        );
    }

    // nota fiscal não encontrada no bando de dados
    // retorna status 404 not found
    @ExceptionHandler(NfceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NfceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "statusMessage", e.getMessage(),
                        "success", false
                )
        );
    }

    // nota fiscal já existe no bando de dados
    // retorna status 409 conflict
    @ExceptionHandler(DuplicateNfceException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DuplicateNfceException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "statusMessage", e.getMessage(),
                        "success", false
                )
        );
    }

    // fallback para outras exceptions
    // retorna status 400 bad request
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException e) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "statusMessage", e.getMessage(),
                        "success", false
                )
        );
    }
}
