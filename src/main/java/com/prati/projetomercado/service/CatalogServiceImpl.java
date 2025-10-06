package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.response.CatalogResponse;
import com.prati.projetomercado.exceptions.BadCredentialsException;
import com.prati.projetomercado.exceptions.FieldError;
import com.prati.projetomercado.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogRepository;

    @Override
    public List<CatalogResponse> getCatalogByMarket(Long marketID) {
        if (marketID == null) {
            throw new BadCredentialsException(List.of(
                    new FieldError("marketId", "Id do mercado está nulo"))
            );
        }

        var listCatalog = catalogRepository.findAllBySupermarket_Id(marketID);
        return listCatalog.stream().map(catalog -> {
            return new CatalogResponse(catalog.getCode(), catalog.getName(), marketID);
        }).toList();
    }
}
