package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.response.MarketResponse;

import java.util.List;

public interface MarketService {

    public List<MarketResponse> getAll();
}
