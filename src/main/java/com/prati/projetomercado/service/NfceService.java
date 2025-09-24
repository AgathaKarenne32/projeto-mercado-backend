package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.AddressRequest;
import com.prati.projetomercado.dto.request.NfceDataRequest;
import com.prati.projetomercado.dto.request.ItemRequest;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Item;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.DuplicateNfceException;
import com.prati.projetomercado.exceptions.EditNotAllowedException;
import com.prati.projetomercado.exceptions.UnauthorizedNfceAccessException;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.CatalogRepository;
import com.prati.projetomercado.repository.ItemRepository;
import com.prati.projetomercado.repository.PurchaseRepository;
import com.prati.projetomercado.repository.SupermarketRepository;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.utils.ScraperUtils;
import com.prati.projetomercado.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NfceService {

    private final ScraperUtils scraper;
    private final AuthUserRepository userRepo;
    private final SupermarketRepository supermarketRepo;
    private final PurchaseRepository purchaseRepo;
    private final CatalogRepository catalogRepo;
    private final ItemRepository itemRepo;
    private final JwtTokenServiceImpl jwtTokenServiceImpl;

    // cadastrar nota fiscal pelo link
    @Transactional(rollbackFor = Exception.class)
    public NfceDataRequest registerNfceLink(String accessToken, String url) {
        NfceDataRequest nfceData = scraper.getData(url);
        return saveNfce(nfceData, accessToken, false);
    }

    // cadastrar nota fiscal manualmente
    @Transactional(rollbackFor = Exception.class)
    public void registerNfceManual(String accessToken, NfceDataRequest nfceData) {
        saveNfce(nfceData, accessToken, true);
    }

    // editar nota fiscal
    @Transactional(rollbackFor = Exception.class)
    public void updateNfce(String accessToken, NfceDataRequest nfceData) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Purchase purchase = purchaseRepo.findByAccessKey(nfceData.accessKey())
                .orElseThrow(() -> new RuntimeException("Nota fiscal não encontrada."));

        if (!purchase.isManual()) {
            throw new EditNotAllowedException("Notas fiscais cadastradas pelo QR code não podem ser editadas.");
        }

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedNfceAccessException("Você não tem permissão para editar esta nota fiscal.");
        }

        Supermarket existingSupermarket = purchase.getSupermarket();
        Supermarket updatedSupermarket = Supermarket.builder()
                .id(existingSupermarket.getId())
                .name(nfceData.store())
                .cnpj(nfceData.cnpj())
                .street(nfceData.address().street())
                .number(nfceData.address().number())
                .complement(nfceData.address().complement())
                .neighborhood(nfceData.address().neighborhood())
                .city(nfceData.address().city())
                .state(nfceData.address().state())
                .createdByUser(existingSupermarket.getCreatedByUser())
                .creationDate(existingSupermarket.getCreationDate())
                .build();

        supermarketRepo.save(updatedSupermarket);

        purchase.setSupermarket(updatedSupermarket);
        purchase.setAccessKey(nfceData.accessKey());
        purchase.setDate(nfceData.date());
        purchase.setTotalPrice(nfceData.totalPrice());

        purchase.getItems().clear();
        for (ItemRequest product : nfceData.products()) {
            Catalog catalog = catalogRepo.findBySupermarketAndCode(updatedSupermarket, product.code())
                    .orElseGet(() -> createCatalog(product, updatedSupermarket));

            Item item = Item.builder()
                    .purchase(purchase)
                    .catalog(catalog)
                    .quantity(product.quantity())
                    .unitPrice(product.price())
                    .build();

            purchase.getItems().add(item);
        }

        purchaseRepo.save(purchase);
    }

    // deletar nota fiscal pela chave de acesso
    @Transactional(rollbackFor = Exception.class)
    public void deleteNfce(String accessToken, String accessKey) {

        AuthUser user = getAuthenticatedUser(accessToken);

        Purchase purchase = purchaseRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new RuntimeException("Nota fiscal não encontrada."));

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedNfceAccessException("Você não tem permissão para editar esta nota fiscal.");
        }

        itemRepo.deleteAllByPurchase(purchase);
        purchaseRepo.delete(purchase);
    }

    private Supermarket createSupermarket(NfceDataRequest nfceData, AuthUser user) {
        AddressRequest address = nfceData.address();
        return supermarketRepo.save(Supermarket.builder()
                .name(nfceData.store())
                .cnpj(nfceData.cnpj())
                .street(address.street())
                .number(address.number())
                .complement(address.complement())
                .neighborhood(address.neighborhood())
                .city(address.city())
                .state(address.state())
                .createdByUser(user)
                .build());
    }

    private Catalog createCatalog(ItemRequest p, Supermarket market) {
        return catalogRepo.findBySupermarketAndCode(market, p.code())
                .orElseGet(() -> catalogRepo.save(Catalog.builder()
                        .supermarket(market)
                        .code(p.code())
                        .name(p.name())
                        .unit(p.unit())
                        .build()));
    }

    private AuthUser getAuthenticatedUser(String accessToken) {
        var email = jwtTokenServiceImpl.getSubjectFromToken(TokenUtils.recoveryToken(accessToken));
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário não encontrado."));
    }

    private NfceDataRequest saveNfce(NfceDataRequest nfceData, String accessToken, boolean isManual) {

        AuthUser user = getAuthenticatedUser(accessToken);

        Supermarket market = supermarketRepo.findByCnpj(nfceData.cnpj())
                .orElseGet(() -> createSupermarket(nfceData, user));

        Purchase purchase = Purchase.builder()
                .user(user)
                .supermarket(market)
                .accessKey(nfceData.accessKey())
                .date(nfceData.date())
                .totalPrice(nfceData.totalPrice())
                .manual(isManual)
                .build();

        try {
            purchase = purchaseRepo.save(purchase);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateNfceException("Nota fiscal já existe.");
        }

        List<Item> itemsToSave = new ArrayList<>();
        for (ItemRequest p : nfceData.products()) {
            Catalog catalog = catalogRepo.findBySupermarketAndCode(market, p.code())
                    .orElseGet(() -> createCatalog(p, market));

            Item item = Item.builder()
                    .purchase(purchase)
                    .catalog(catalog)
                    .quantity(p.quantity())
                    .unitPrice(p.price())
                    .build();

            itemsToSave.add(item);
        }
        itemRepo.saveAll(itemsToSave);

        return nfceData;
    }
}
