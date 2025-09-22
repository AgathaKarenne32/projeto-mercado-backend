package com.prati.projetomercado.controller;

import com.prati.projetomercado.exceptions.DuplicateNfceException;
import com.prati.projetomercado.service.NfceService;
import com.prati.projetomercado.utils.ScraperUtils.NfceData;
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
    public ResponseEntity<?> scrapeNfce(@RequestHeader("Authorization") String authorization,@RequestBody UrlRequest request) throws IOException {
        NfceData data = nfceService.processNfce(request.getUrl(), authorization);
        return ResponseEntity.ok(data);
    }

    @ExceptionHandler(DuplicateNfceException.class)
    public ResponseEntity<?> handleDuplicateKey(DuplicateNfceException e) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "statusMessage", e.getMessage(),
                        "success", false
                )
        );
    }
}
