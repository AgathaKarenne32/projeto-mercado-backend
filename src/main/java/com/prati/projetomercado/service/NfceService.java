package com.prati.projetomercado.service;

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
import com.prati.projetomercado.utils.ScraperUtils.NfceData;
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
    private JwtTokenServiceImpl tokenService;

    private Supermarket createSupermarket(NfceData data, AuthUser user) {
        Supermarket newMarket = Supermarket.builder()
                .name(data.getStore())
                .cnpj(data.getCnpj())
                .street(data.getAddress().getStreet())
                .number(data.getAddress().getNumber())
                .complement(data.getAddress().getComplement())
                .neighborhood(data.getAddress().getNeighborhood())
                .city(data.getAddress().getCity())
                .state(data.getAddress().getState())
                .createdByUser(user)
                .build();

        return supermarketRepo.save(newMarket);
    }

    private Catalog createCatalog(ScraperUtils.Product p, Supermarket market) {
        Catalog newCatalog = Catalog.builder()
                .supermarket(market)
                .code(p.getCode())
                .name(p.getName())
                .unit(p.getUnit())
                .build();

        return catalogRepo.save(newCatalog);
    }

    // if an error occurs, the transaction is rolled back and nothing is sent to DB
    @Transactional(rollbackFor = Exception.class)
    public NfceData processNfce(String url, String accessToken) throws IOException {
        // scrapes nfc-e
        NfceData data = scraper.getData(url);

        // fetches user
        System.out.println(accessToken);
        var email = jwtTokenServiceImpl.getSubjectFromToken(TokenUtils.recoveryToken(accessToken));
        AuthUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário não encontrado."));

        // fetches supermarket or creates a new one
        Supermarket market = supermarketRepo.findByCnpj(data.getCnpj())
                .orElseGet(() -> createSupermarket(data, user));

        // creates/inserts purchase
        Purchase purchase = Purchase.builder()
                .user(user)
                .supermarket(market)
                .accessKey(data.getAccessKey())
                .date(data.getDate())
                .totalPrice(data.getTotalPrice())
                .build();

        try {
            purchase = purchaseRepo.save(purchase);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateNfceException("Nota fiscal já existe.");
        }

        List<Item> itemsToSave = new ArrayList<>();

        for (ScraperUtils.Product p : data.getProducts()) {
            // fetches catalog or creates a new one
            Catalog catalog = catalogRepo.findBySupermarketAndCode(market, p.getCode())
                    .orElseGet(() -> createCatalog(p, market));

            // creates item and adds to items array
            Item item = Item.builder()
                    .purchase(purchase)
                    .catalog(catalog)
                    .quantity(p.getQuantity())
                    .unitPrice(p.getPrice())
                    .build();

            itemsToSave.add(item);
        }
        // inserts all items in a single query
        itemRepo.saveAll(itemsToSave);

        return data;
    }
}