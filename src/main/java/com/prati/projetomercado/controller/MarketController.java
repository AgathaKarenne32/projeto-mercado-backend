package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.response.MarketResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.MarketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Mercado",
        description = "Endpoints para mercados criados através do cadastro da NFC-e"
)
@RestController
@RequestMapping("/api/mercado")
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<MarketResponse>>> getAll() {
       var markets = marketService.getAll();
       return ResponseEntity.ok(
               new SuccessResponse<>(
                       "Lista de mercados retornado com sucesso",
                       markets)
       );
    }
}
