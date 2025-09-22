package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.request.NfceDataRequest;
import com.prati.projetomercado.exceptions.DuplicateNfceException;
import com.prati.projetomercado.service.NfceService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

    @PostMapping("/scrape")
    public ResponseEntity<Map<String, Object>> scrapeNfce(
            @RequestHeader("Authorization") String authorization,
            @RequestBody UrlRequest request
    ) throws IOException {
        NfceDataRequest data = nfceService.processNfceLink(request.getUrl(), authorization);
        return ResponseEntity.ok(
                Map.of(
                        "statusMessage", data,
                        "success", true
                )
        );
    }

    @PostMapping("/manual")
    public ResponseEntity<Map<String, Object>> manualNfce(
            @RequestHeader("Authorization") String authorization,
            @RequestBody NfceDataRequest manualData
    ) {
        NfceDataRequest data = nfceService.processNfceManual(manualData, authorization);
        return ResponseEntity.ok(
                Map.of(
                        "statusMessage", "Nota fiscal cadastrada com sucesso.",
                        "success", true
                )
        );
    }

    @ExceptionHandler(DuplicateNfceException.class)
    public ResponseEntity<?> handleDuplicateNfce(DuplicateNfceException e) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "statusMessage", e.getMessage(),
                        "success", false
                )
        );
    }
}
