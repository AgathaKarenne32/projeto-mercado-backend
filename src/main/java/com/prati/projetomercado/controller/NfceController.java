package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.request.NfceDataRequest;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.NfceService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/register-link")
    public ResponseEntity<Object> registerLink(@RequestHeader("Authorization") String authorization,
                                             @RequestBody UrlRequest request) {
        NfceDataRequest data = nfceService.registerLink(authorization, request.getUrl());
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal pelo link cadastrada com sucesso.", data));
    }

    @PostMapping("/register-manual")
    public ResponseEntity<Object> registerManual(@RequestHeader("Authorization") String authorization,
                                                 @RequestBody NfceDataRequest manualData) {
        nfceService.registerManual(authorization, manualData);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal manual cadastrada com sucesso."));
    }

    @PutMapping("/edit")
    public ResponseEntity<Object> edit(@RequestHeader("Authorization") String authorization,
                                                          @RequestBody NfceDataRequest updatedNfce) {
        nfceService.edit(authorization, updatedNfce);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal editada com sucesso."));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> delete(@RequestHeader("Authorization") String authorization,
                                         @RequestBody AccessKeyRequest request)  {
        nfceService.delete(authorization, request.accessKey);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal deletada com sucesso."));
    }
}
