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

    @Transactional(rollbackFor = Exception.class)
    public NfceDataRequest registerNfceLink(String url, String accessToken) throws IOException {
        NfceDataRequest dto = scraper.getData(url);
        return saveNfce(dto, accessToken, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void registerNfceManual(NfceDataRequest dto, String accessToken) {
        saveNfce(dto, accessToken, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateNfce(NfceDataRequest dto, String accessToken) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Purchase purchase = purchaseRepo.findByAccessKey(dto.accessKey())
                .orElseThrow(() -> new RuntimeException("Nota fiscal não encontrada."));

        if (!purchase.isManual()) {
            throw new RuntimeException("Notas fiscais cadastradas pelo QR code não podem ser editadas.");
        }

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Você não tem permissão para editar esta nota fiscal.");
        }

        Supermarket existingSupermarket = purchase.getSupermarket();
        Supermarket updatedSupermarket = Supermarket.builder()
                .id(existingSupermarket.getId())
                .name(dto.store())
                .cnpj(dto.cnpj())
                .street(dto.address().street())
                .number(dto.address().number())
                .complement(dto.address().complement())
                .neighborhood(dto.address().neighborhood())
                .city(dto.address().city())
                .state(dto.address().state())
                .createdByUser(existingSupermarket.getCreatedByUser())
                .creationDate(existingSupermarket.getCreationDate())
                .build();

        supermarketRepo.save(updatedSupermarket);

        purchase.setSupermarket(updatedSupermarket);
        purchase.setAccessKey(dto.accessKey());
        purchase.setDate(dto.date());
        purchase.setTotalPrice(dto.totalPrice());

        purchase.getItems().clear();
        for (ItemRequest product : dto.products()) {
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

    private Supermarket createSupermarket(NfceDataRequest dto, AuthUser user) {
        AddressRequest address = dto.address();
        return supermarketRepo.save(Supermarket.builder()
                .name(dto.store())
                .cnpj(dto.cnpj())
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

    private NfceDataRequest saveNfce(NfceDataRequest dto, String accessToken, boolean isManual) {

        AuthUser user = getAuthenticatedUser(accessToken);

        Supermarket market = supermarketRepo.findByCnpj(dto.cnpj())
                .orElseGet(() -> createSupermarket(dto, user));

        Purchase purchase = Purchase.builder()
                .user(user)
                .supermarket(market)
                .accessKey(dto.accessKey())
                .date(dto.date())
                .totalPrice(dto.totalPrice())
                .manual(isManual)
                .build();

        try {
            purchase = purchaseRepo.save(purchase);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Nota fiscal já existe.");
        }

        List<Item> itemsToSave = new ArrayList<>();
        for (ItemRequest p : dto.products()) {
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

        return dto;
    }
}
