package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.dto.response.MarketResponse;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.SupermarketRepository;
import com.prati.projetomercado.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MarketServiceImpl implements MarketService {

    private final SupermarketRepository supermarketRepository;
    private final AuthUserRepository authUserRepository;

    @Override
    public List<MarketResponse> getAll() {
        var email= SecurityContextHolder.getContext().getAuthentication().getName();
        // Ja está sendo validado no filter.
        // Se o accessToken não estiver bem formatado ou não existir, a função getAll() nem será chamada.
        // Por isso não precisa fazer essa validação no service
        var user = authUserRepository.findByEmail(email).get();

        var markets = supermarketRepository.findAllByCreatedByUser(user);
        return markets.stream().map(market -> {
            return new MarketResponse(
                    market.getId(),
                    market.getName(),
                    market.getCity(),
                    market.getState()
            );
        }).toList();
    }




}
