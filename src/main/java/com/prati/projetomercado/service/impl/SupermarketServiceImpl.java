package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.dto.request.SupermarketRequest;
import com.prati.projetomercado.dto.response.SupermarketResponse;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Supermarket;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.EntityDeletionException;
import com.prati.projetomercado.exceptions.EntityNotFoundException;
import com.prati.projetomercado.exceptions.NotManualEntityException;
import com.prati.projetomercado.exceptions.UnauthorizedAccessException;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.PurchaseRepository;
import com.prati.projetomercado.repository.SupermarketRepository;
import com.prati.projetomercado.service.SupermarketService;
import com.prati.projetomercado.utils.EntityBuilderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupermarketServiceImpl implements SupermarketService {

    private final EntityBuilderUtils builder;
    private final AuthUserRepository userRepo;
    private final SupermarketRepository supermarketRepo;
    private final PurchaseRepository purchaseRepo;

    private AuthUser getAuthenticatedUser() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário autenticado não encontrado no banco de dados."));
    }

    @Override
    @Transactional(readOnly = true)
    public SupermarketResponse findById(Long id) {
        AuthUser user = getAuthenticatedUser();
        Supermarket supermarket = supermarketRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));

        if (!supermarket.getCreatedByUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para acessar este supermercado");
        }

        return SupermarketResponse.toDto(supermarket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupermarketResponse> findAllByUser() {
        AuthUser user = getAuthenticatedUser();
        List<Supermarket> supermarkets = supermarketRepo.findAllByCreatedByUser(user);
        List<SupermarketResponse> supermarketList = new ArrayList<>();

        for (Supermarket supermarket : supermarkets) {
            SupermarketResponse marketDto = SupermarketResponse.toDto(supermarket);
            supermarketList.add(marketDto);
        }

        return supermarketList;
    }

    @Override
    @Transactional()
    public SupermarketResponse create(SupermarketRequest supermarketData) {
        AuthUser user = getAuthenticatedUser();

        Supermarket market = supermarketRepo.save(builder.buildSupermarket(supermarketData, user, true));

        return SupermarketResponse.toDto(market);
    }

    @Override
    @Transactional()
    public SupermarketResponse update(Long id, SupermarketRequest supermarketData) {
        AuthUser user = getAuthenticatedUser();

        Supermarket supermarket = supermarketRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));

        if (!supermarket.isManual()) {
            throw new NotManualEntityException("Supermercados registrados pelo QR code não podem ser editados");
        }

        if (!supermarket.getCreatedByUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para editar este supermercado");
        }

        supermarket.setName(supermarketData.store());
        supermarket.setCnpj(supermarketData.cnpj());
        supermarket.setCity(supermarketData.city());
        supermarket.setState(supermarketData.state());

        return SupermarketResponse.toDto(supermarket);
    }

    @Override
    @Transactional()
    public void delete(Long id) {
        AuthUser user = getAuthenticatedUser();

        Supermarket supermarket = supermarketRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));

        if (!supermarket.isManual()) {
            throw new NotManualEntityException("Supermercados registrados pelo QR code não podem ser deletados");
        }

        if (!supermarket.getCreatedByUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para deletar este supermercado");
        }

        if (purchaseRepo.findBySupermarket(supermarket).isPresent()) {
            throw new EntityDeletionException("Não é possível deletar este supermercado porque existem notas fiscais associadas a ele.");
        }

        supermarketRepo.delete(supermarket);
    }
}
