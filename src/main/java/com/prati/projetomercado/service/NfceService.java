package com.prati.projetomercado.service;

import com.prati.projetomercado.entity.*;
import com.prati.projetomercado.repository.*;
import com.prati.projetomercado.utils.ScraperUtils;
import com.prati.projetomercado.utils.ScraperUtils.NfceData;
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
        Supermarket newMarket = new Supermarket();
        newMarket.setName(data.getStore());
        newMarket.setCnpj(data.getCnpj());
        newMarket.setStreet(data.getAddress().getStreet());
        newMarket.setNumber(data.getAddress().getNumber());
        newMarket.setComplement(data.getAddress().getComplement());
        newMarket.setNeighborhood(data.getAddress().getNeighborhood());
        newMarket.setCity(data.getAddress().getCity());
        newMarket.setState(data.getAddress().getState());
        newMarket.setCreatedByUser(user);
        return supermarketRepo.save(newMarket);
    }

    private Catalog createCatalog(ScraperUtils.Product p, Supermarket market) {
        Catalog newCatalog = new Catalog();
        newCatalog.setSupermarket(market);
        newCatalog.setCode(p.getCode());
        newCatalog.setName(p.getName());
        newCatalog.setUnit(p.getUnit());
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
        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setSupermarket(market);
        purchase.setAccessKey(data.getAccessKey());
        purchase.setDate(data.getDate());
        purchase.setTotalPrice(data.getTotalPrice());
        purchase = purchaseRepo.save(purchase);

        List<Item> itemsToSave = new ArrayList<>();

        // creates/inserts catalog and items
        for (ScraperUtils.Product p : data.getProducts()) {
            Catalog catalog = catalogRepo.findBySupermarketAndCode(market, p.getCode())
                    .orElseGet(() -> createCatalog(p, market));

            Item item = new Item();
            item.setPurchase(purchase);
            item.setCatalog(catalog);
            item.setQuantity(p.getQuantity());
            item.setUnitPrice(p.getPrice());

            itemsToSave.add(item);
        }
        // saves all items in a single query
        itemRepo.saveAll(itemsToSave);

        return data;
    }
}