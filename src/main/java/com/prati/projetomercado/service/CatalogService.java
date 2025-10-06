package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.response.CatalogResponse;

import java.util.List;

public interface CatalogService {
    public List<CatalogResponse> getCatalogByMarket(Long marketID);
}
