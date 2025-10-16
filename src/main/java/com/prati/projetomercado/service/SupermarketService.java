package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.SupermarketRequest;
import com.prati.projetomercado.dto.response.SupermarketResponse;

import java.util.List;

public interface SupermarketService {
    SupermarketResponse findById(Long id);

    List<SupermarketResponse> findAllByUser();

    SupermarketResponse create(SupermarketRequest supermarketData);

    SupermarketResponse update(Long id, SupermarketRequest supermarketData);

    void delete(Long id);
}
