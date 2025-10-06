package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.response.CatalogResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.CatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catalogo", description = "Rotas de manipulação do catálogo dos mercados cadastrados")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/{id}")
    public SuccessResponse<List<CatalogResponse>> getCatalogByMarket(@PathVariable("id") Long id) {
        return new SuccessResponse<>("Catalogo retornado com sucesso", catalogService.getCatalogByMarket(id));
    }

}
