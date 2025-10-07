package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.request.SupermarketRequest;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.dto.response.SupermarketResponse;
import com.prati.projetomercado.service.supermarket.SupermarketService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/supermarkets")
@RequiredArgsConstructor
public class SupermarketController {

    private final SupermarketService marketService;

    @GetMapping("/")
    public ResponseEntity<SuccessResponse<List<SupermarketResponse>>> getAll(@RequestHeader("Authorization") String authorization) {
        List<SupermarketResponse> data = marketService.getAll(authorization);

        if (data.isEmpty()) {
            return ResponseEntity.ok(new SuccessResponse<>("Nenhum mercado encontrado."));
        }

        return ResponseEntity.ok(new SuccessResponse<>("Supermercados encontrados com sucesso.", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<SupermarketResponse>> getOne(@RequestHeader("Authorization") String authorization, @PathVariable long id) {
        SupermarketResponse data = marketService.getOne(authorization, id);
        return ResponseEntity.ok(new SuccessResponse<>("Supermercado encontrado com sucesso.", data));
    }

    @PostMapping("/")
    public ResponseEntity<SuccessResponse<SupermarketResponse>> register(@RequestHeader("Authorization") String authorization, @RequestBody SupermarketRequest supermarketData) {
        SupermarketResponse data = marketService.saveSupermarket(authorization, supermarketData);
        return ResponseEntity.ok(new SuccessResponse<>("Supermercado cadastrado com sucesso.", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<SupermarketResponse>> edit(@RequestHeader("Authorization") String authorization,
                                                                     @PathVariable long id,
                                                                     @RequestBody SupermarketRequest supermarketData) {
        SupermarketResponse data = marketService.edit(authorization, id, supermarketData);

        return ResponseEntity.ok(new SuccessResponse<>("Supermercado editado com sucesso.", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> delete(@RequestHeader("Authorization") String authorization,
                                                        @PathVariable long id) {
        marketService.delete(authorization, id);
        return ResponseEntity.ok(new SuccessResponse<>("Supermercado deletado com sucesso."));
    }
}
