package com.prati.projetomercado.controller;

import com.prati.projetomercado.service.NfceService;
import com.prati.projetomercado.utils.ScraperUtils.NfceData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nfce")
@RequiredArgsConstructor
public class NfceController {

    private final NfceService nfceService;

    @Setter
    @Getter
    public static class UrlRequest {
        private String url;
        private Long userId;
    }

    @PostMapping("/scrape")
    public ResponseEntity<?> scrapeNfce(@RequestBody UrlRequest request) {
        try {
            NfceData data = nfceService.processNfce(request.getUrl(), request.getUserId());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
