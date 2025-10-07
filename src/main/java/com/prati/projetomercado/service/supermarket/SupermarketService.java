package com.prati.projetomercado.service.supermarket;

import com.prati.projetomercado.dto.request.SupermarketRequest;
import com.prati.projetomercado.dto.response.SupermarketResponse;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Supermarket;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.EditNotAllowedException;
import com.prati.projetomercado.exceptions.EntityNotFoundException;
import com.prati.projetomercado.exceptions.UnauthorizedAccessException;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.SupermarketRepository;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupermarketService {

    private final AuthUserRepository userRepo;
    private final SupermarketRepository supermarketRepo;
    private final JwtTokenServiceImpl jwtTokenServiceImpl;

    @Transactional(readOnly = true)
    public SupermarketResponse getOne(String accessToken, Long id) {
        Supermarket supermarket = supermarketRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));
        return SupermarketResponse.toDto(supermarket);
    }

    @Transactional(readOnly = true)
    public List<SupermarketResponse> getAll(String accessToken) {
        AuthUser user = getAuthenticatedUser(accessToken);
        List<Supermarket> supermarkets = supermarketRepo.findAllByCreatedByUser(user);
        List<SupermarketResponse> supermarketList = new ArrayList<>();

        for (Supermarket supermarket : supermarkets) {
            SupermarketResponse marketDto = SupermarketResponse.toDto(supermarket);
            supermarketList.add(marketDto);
        }

        return supermarketList;
    }

    @Transactional(rollbackFor = Exception.class)
    public SupermarketResponse saveSupermarket(String accessToken, SupermarketRequest supermarketData) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Supermarket market = supermarketRepo.save(
                Supermarket.builder()
                        .name(supermarketData.store())
                        .cnpj(supermarketData.cnpj())
                        .city(supermarketData.city())
                        .state(supermarketData.state())
                        .createdByUser(user)
                        .manual(true)
                        .build()
        );

        return SupermarketResponse.toDto(market);
    }

    @Transactional(rollbackFor = Exception.class)
    public SupermarketResponse edit(String accessToken, SupermarketRequest supermarketData) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Supermarket supermarket = supermarketRepo.findById(supermarketData.id())
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

        supermarketRepo.save(supermarket);

        return SupermarketResponse.toDto(supermarket);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String accessToken, Long id) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Supermarket supermarket = supermarketRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));

        if (!supermarket.isManual()) {
            throw new UnauthorizedAccessException("Supermercados registrados por link não podem ser deletados");
        }

        if (!supermarket.getCreatedByUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para deletar este supermercado");
        }

        supermarketRepo.delete(supermarket);
    }

    private AuthUser getAuthenticatedUser(String accessToken) {
        var email = jwtTokenServiceImpl.getSubjectFromToken(TokenUtils.recoveryToken(accessToken));
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário não encontrado."));
    }
}
