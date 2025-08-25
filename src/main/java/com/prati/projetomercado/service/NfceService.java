package com.prati.projetomercado.service;

import com.prati.projetomercado.entity.*;
import com.prati.projetomercado.repository.*;
import com.prati.projetomercado.utils.ScraperUtils;
import com.prati.projetomercado.utils.ScraperUtils.NfceData;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NfceService {

    private final ScraperUtils scraper;
    private final UserRepository userRepo;
    private final SupermarketRepository supermarketRepo;
    private final PurchaseRepository purchaseRepo;
    private final CatalogRepository catalogRepo;
    private final ItemRepository itemRepo;

    private Supermarket createSupermarket(NfceData data, User user) {
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
    public NfceData processNfce(String url, Long userId) throws IOException {
        // scrapes nfc-e
        NfceData data = scraper.getData(url);

        // fetches user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // creates/inserts supermarket
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

        purchase = purchaseRepo.save(purchase);

        List<Item> itemsToSave = new ArrayList<>();

        // creates/inserts catalog and items
        for (ScraperUtils.Product p : data.getProducts()) {
            Catalog catalog = catalogRepo.findBySupermarketAndCode(market, p.getCode())
                    .orElseGet(() -> createCatalog(p, market));

            Item item = Item.builder()
                    .purchase(purchase)
                    .catalog(catalog)
                    .quantity(p.getQuantity())
                    .unitPrice(p.getPrice())
                    .build();

            itemsToSave.add(item);
        }
        // saves all items in a single query
        itemRepo.saveAll(itemsToSave);

        return data;
    }
}