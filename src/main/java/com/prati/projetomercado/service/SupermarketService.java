package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.SupermarketRequest;
import com.prati.projetomercado.dto.response.SupermarketResponse;

import java.util.List;

public interface SupermarketService {
    SupermarketResponse findById(String accessToken, long id);

    List<SupermarketResponse> findAllByUser(String accessToken);

    SupermarketResponse create(String accessToken, SupermarketRequest supermarketData);

    SupermarketResponse update(String accessToken, long id, SupermarketRequest supermarketData);

    void delete(String accessToken, long id);
}
