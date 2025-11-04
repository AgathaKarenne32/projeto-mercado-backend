package com.prati.projetomercado.service.supermarket;

import com.prati.projetomercado.dto.request.SupermarketRequest;
import com.prati.projetomercado.dto.response.SupermarketResponse;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Supermarket;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.EditNotAllowedException;
import com.prati.projetomercado.exceptions.EntityNotFoundException;
import com.prati.projetomercado.exceptions.SupermarketDeletionException;
import com.prati.projetomercado.exceptions.UnauthorizedAccessException;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.PurchaseRepository;
import com.prati.projetomercado.repository.SupermarketRepository;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.utils.EntityBuilderUtils;
import com.prati.projetomercado.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupermarketService {

    private final EntityBuilderUtils builder;
    private final AuthUserRepository userRepo;
    private final SupermarketRepository supermarketRepo;
    private final PurchaseRepository purchaseRepo;
    private final JwtTokenServiceImpl jwtTokenServiceImpl;

    @Transactional(readOnly = true)
    public SupermarketResponse getOne(String accessToken, long id) {
        Supermarket supermarket = supermarketRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));
        return SupermarketResponse.from(supermarket);
    }

    @Transactional(readOnly = true)
    public Page<SupermarketResponse> getAll(String accessToken, int page, int size) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Pageable pageable = PageRequest.of(page, size);
        Page<Supermarket> supermarketsPage = supermarketRepo.findAllByCreatedByUser(user, pageable);

        return supermarketsPage.map(SupermarketResponse::from);
    }

    @Transactional(rollbackFor = Exception.class)
    public SupermarketResponse saveSupermarket(String accessToken, SupermarketRequest supermarketData) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Supermarket market = supermarketRepo.save(builder.buildSupermarket(supermarketData, user, true));

        return SupermarketResponse.from(market);
    }

    @Transactional(rollbackFor = Exception.class)
    public SupermarketResponse edit(String accessToken, long id, SupermarketRequest supermarketData) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Supermarket supermarket = supermarketRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));

        if (!supermarket.isManual()) {
            throw new EditNotAllowedException("Supermercados registrados por link não podem ser editados");
        }

        if (!supermarket.getCreatedByUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para editar este supermercado");
        }

        supermarket.setName(supermarketData.store());
        supermarket.setCnpj(supermarketData.cnpj());
        supermarket.setCity(supermarketData.city());
        supermarket.setState(supermarketData.state());

        return SupermarketResponse.from(supermarket);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String accessToken, long id) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Supermarket supermarket = supermarketRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));

        if (!supermarket.isManual()) {
            throw new UnauthorizedAccessException("Supermercados registrados por link não podem ser deletados");
        }

        if (!supermarket.getCreatedByUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para deletar este supermercado");
        }

        if (purchaseRepo.findBySupermarket(supermarket).isPresent()) {
            throw new SupermarketDeletionException("Não é possível deletar este supermercado porque existem notas fiscais associadas a ele.");
        }

        supermarketRepo.delete(supermarket);
    }

    private AuthUser getAuthenticatedUser(String accessToken) {
        var email = jwtTokenServiceImpl.getSubjectFromToken(TokenUtils.recoveryToken(accessToken));
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário não encontrado."));
    }
}
