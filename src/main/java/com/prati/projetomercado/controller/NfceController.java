package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.request.NfceDataRequest;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.nfce.NfceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/nfce")
@RequiredArgsConstructor
@Tag(name = "NFC-e",
        description = "Endpoints para processamento de Notas Fiscais de Consumidor Eletrônicas")
public class NfceController {
    
    private final NfceService nfceService;

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<List<NfceDataRequest>>> getAll(@RequestHeader("Authorization") String authorization) {
        List<NfceDataRequest> data = nfceService.getAll(authorization);

        if (data.isEmpty()) {
            return ResponseEntity.ok(new SuccessResponse<>("Nenhuma nota fiscal encontrada."));
        }

        return ResponseEntity.ok(new SuccessResponse<>("Notas fiscais encontradas com sucesso.", data));
    }

    @GetMapping("/{accessKey}")
    public ResponseEntity<SuccessResponse<NfceDataRequest>> getOne(@RequestHeader("Authorization") String authorization, @PathVariable String accessKey) {
        NfceDataRequest data = nfceService.getOne(authorization, accessKey);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal encontrada com sucesso.", data));
    }

    @PostMapping("/register-link")
    public ResponseEntity<SuccessResponse<NfceDataRequest>> registerLink(@RequestHeader("Authorization") String authorization,
                                                                         @RequestBody UrlRequest request) {
        NfceDataRequest data = nfceService.registerLink(authorization, request.getUrl());
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal pelo link cadastrada com sucesso.", data));
    }

    @PostMapping("/register-manual")
    public ResponseEntity<SuccessResponse<Void>> registerManual(@RequestHeader("Authorization") String authorization,
                                                                @RequestBody NfceDataRequest manualData) {
        nfceService.registerManual(authorization, manualData);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal manual cadastrada com sucesso."));
    }

    @PutMapping("/edit")
    public ResponseEntity<SuccessResponse<Void>> edit(@RequestHeader("Authorization") String authorization,
                                                      @RequestBody NfceDataRequest updatedNfce) {
        nfceService.edit(authorization, updatedNfce);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal editada com sucesso."));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse<Void>> delete(@RequestHeader("Authorization") String authorization,
                                                        @RequestBody AccessKeyRequest request) {
        nfceService.delete(authorization, request.accessKey);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal deletada com sucesso."));
    }

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
}

