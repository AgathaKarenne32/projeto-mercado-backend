package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.SupermarketRequest;
import com.prati.projetomercado.dto.response.SupermarketResponse;
import org.springframework.data.domain.Page;

public interface SupermarketService {
    SupermarketResponse findById(Long id);

    Page<SupermarketResponse> findAllByUser(int page, int size);

    SupermarketResponse create(SupermarketRequest supermarketData);

    SupermarketResponse update(Long id, SupermarketRequest supermarketData);

    void delete(Long id);
}
