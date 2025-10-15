package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.response.CatalogResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.exceptions.BadCredentialsException;
import com.prati.projetomercado.exceptions.EntityNotFoundException;
import com.prati.projetomercado.exceptions.FieldError;
import com.prati.projetomercado.repository.CatalogRepository;
import com.prati.projetomercado.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
            return new CatalogResponse(catalog.getId(), catalog.getCode(), catalog.getUnit(), catalog.getName(), marketID);
        }).toList();
    }

    public void deleteCatalog(Long id) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var catalog = catalogRepository.findCatalogoByIdAndSupermarket_CreatedByUser_Email(id, email)
                .orElseThrow(() -> new EntityNotFoundException("catalogo não encontrado"));

        catalogRepository.delete(catalog);
    }

    public CatalogResponse editCatalog(Long id, String newName) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var catalog = catalogRepository.findCatalogoByIdAndSupermarket_CreatedByUser_Email(id, email)
                .orElseThrow(() -> new EntityNotFoundException("catalogo não encontrado"));

        catalog.setName(newName);
        catalogRepository.save(catalog);

        return new CatalogResponse(catalog.getId(), catalog.getCode(), catalog.getUnit(), catalog.getName(), catalog.getSupermarket().getId());
    }

}
