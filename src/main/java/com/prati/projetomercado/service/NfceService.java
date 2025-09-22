package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.NfceDataRequest;
import com.prati.projetomercado.dto.request.ProductRequest;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Item;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.DuplicateNfceException;
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
    public NfceDataRequest processNfceLink(String url, String accessToken) throws IOException {
        NfceDataRequest dto = scraper.getData(url);
        return saveNfce(dto, accessToken);
    }

    @Transactional(rollbackFor = Exception.class)
    public NfceDataRequest processNfceManual(NfceDataRequest dto, String accessToken) {
        return saveNfce(dto, accessToken);
    }

    private Supermarket createSupermarket(NfceDataRequest dto, AuthUser user) {
        var address = dto.address();
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

    private Catalog createCatalog(ProductRequest p, Supermarket market) {
        return catalogRepo.save(Catalog.builder()
                .supermarket(market)
                .code(p.code())
                .name(p.name())
                .unit(p.unit())
                .build());
    }

    private NfceDataRequest saveNfce(NfceDataRequest dto, String accessToken) {
        var email = jwtTokenServiceImpl.getSubjectFromToken(TokenUtils.recoveryToken(accessToken));
        AuthUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário não encontrado."));

        Supermarket market = supermarketRepo.findByCnpj(dto.cnpj())
                .orElseGet(() -> createSupermarket(dto, user));

        Purchase purchase = Purchase.builder()
                .user(user)
                .supermarket(market)
                .accessKey(dto.accessKey())
                .date(dto.date())
                .totalPrice(dto.totalPrice())
                .build();

        try {
            purchase = purchaseRepo.save(purchase);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateNfceException("Nota fiscal já existe.");
        }

        List<Item> itemsToSave = new ArrayList<>();
        for (ProductRequest p : dto.products()) {
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
