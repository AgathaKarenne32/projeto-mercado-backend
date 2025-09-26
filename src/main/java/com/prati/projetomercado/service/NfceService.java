package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.NfceDataRequest;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Item;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.DuplicateNfceException;
import com.prati.projetomercado.exceptions.EditNotAllowedException;
import com.prati.projetomercado.exceptions.NfceNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    @Transactional(readOnly = true)
    public NfceDataRequest getOne(String accessToken, String accessKey) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Purchase purchase = purchaseRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new NfceNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedNfceAccessException("Você não tem permissão para acessar esta nota fiscal.");
        }

        return createNfceRequest(purchase);
    }

    @Transactional(readOnly = true)
    public List<NfceDataRequest> getAll(String accessToken) {
        AuthUser user = getAuthenticatedUser(accessToken);

        List<Purchase> purchases = purchaseRepo.findAllByUserId(user.getId());

        List<NfceDataRequest> nfceList = new ArrayList<>();

        for (Purchase purchase : purchases) {
            NfceDataRequest nfce = createNfceRequest(purchase);
            nfceList.add(nfce);
        }

        return nfceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public NfceDataRequest registerLink(String accessToken, String url) {
        NfceDataRequest nfceData = scraper.getData(url);
        return saveNfce(nfceData, accessToken, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void registerManual(String accessToken, NfceDataRequest nfceData) {
        saveNfce(nfceData, accessToken, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void edit(String accessToken, NfceDataRequest nfceData) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Purchase purchase = purchaseRepo.findByAccessKey(nfceData.accessKey())
                .orElseThrow(() -> new NfceNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.isManual()) {
            throw new EditNotAllowedException("Notas fiscais cadastradas pelo QR code não podem ser editadas.");
        }

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedNfceAccessException("Você não tem permissão para acessar esta nota fiscal.");
        }

        Supermarket updatedMarket = updateSupermarket(nfceData, purchase);
        supermarketRepo.save(updatedMarket);

        purchase.setSupermarket(updatedMarket);
        purchase.setAccessKey(nfceData.accessKey());
        purchase.setDate(nfceData.date());
        purchase.setTotalPrice(nfceData.totalPrice());

        purchase.getItems().clear();
        for (NfceDataRequest.Item product : nfceData.products()) {
            Catalog catalog = catalogRepo.findBySupermarketAndCode(updatedMarket, product.code())
                    .orElseGet(() -> createCatalog(product, updatedMarket));

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

    @Transactional(rollbackFor = Exception.class)
    public void delete(String accessToken, String accessKey) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Purchase purchase = purchaseRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new NfceNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedNfceAccessException("Você não tem permissão para editar esta nota fiscal.");
        }

        purchaseRepo.delete(purchase);
    }

    private NfceDataRequest saveNfce(NfceDataRequest nfceData, String accessToken, boolean isManual) {
        AuthUser user = getAuthenticatedUser(accessToken);

        if (purchaseRepo.findByAccessKey(nfceData.accessKey()).isPresent()) {
            throw new DuplicateNfceException("Nota fiscal já existe.");
        }

        Supermarket market;

        if(isManual) {
            market = supermarketRepo.findByCnpjAndManualAndCreatedByUser(nfceData.cnpj(), isManual, user)
                    .orElseGet(() -> createSupermarket(nfceData, user, true));
        } else {
            market = supermarketRepo.findByCnpjAndManual(nfceData.cnpj(), isManual)
                    .orElseGet(() -> createSupermarket(nfceData, user, false));
        }

        Purchase purchase = Purchase.builder()
                .user(user)
                .supermarket(market)
                .accessKey((nfceData.accessKey() == null || nfceData.accessKey().isEmpty())
                        ? generateAccessKey(): nfceData.accessKey()
                )
                .date(nfceData.date())
                .totalPrice(nfceData.totalPrice())
                .manual(isManual)
                .build();

        for (NfceDataRequest.Item product : nfceData.products()) {
            Catalog catalog = catalogRepo.findBySupermarketAndCode(market, product.code())
                    .orElseGet(() -> createCatalog(product, market));

            Optional<Item> existingItem = purchase.getItems().stream()
                    .filter(i -> i.getCatalog().getCode().equals(product.code()))
                    .findFirst();

            if (existingItem.isPresent()) {
                Item item = existingItem.get();
                item.setQuantity(item.getQuantity().add(product.quantity()));
            } else {
                Item item = Item.builder()
                        .purchase(purchase)
                        .catalog(catalog)
                        .quantity(product.quantity())
                        .unitPrice(product.price())
                        .build();

                purchase.getItems().add(item);
            }
        }

        purchaseRepo.save(purchase);
        return nfceData;
    }

    private NfceDataRequest createNfceRequest(Purchase purchase) {
        Supermarket supermarket = purchase.getSupermarket();
        NfceDataRequest.Address address = new NfceDataRequest.Address(
                supermarket.getStreet(),
                supermarket.getNumber(),
                supermarket.getComplement(),
                supermarket.getNeighborhood(),
                supermarket.getCity(),
                supermarket.getState()
        );

        List<NfceDataRequest.Item> items = purchase.getItems().stream()
                .map(item -> new NfceDataRequest.Item(
                        item.getCatalog().getName(),
                        item.getCatalog().getCode(),
                        item.getQuantity(),
                        item.getCatalog().getUnit(),
                        item.getUnitPrice()
                ))
                .toList();

        return new NfceDataRequest(
                supermarket.getName(),
                supermarket.getCnpj(),
                address,
                purchase.getAccessKey(),
                purchase.getDate(),
                purchase.getTotalPrice(),
                items
        );
    }

    private Supermarket createSupermarket(NfceDataRequest nfceData, AuthUser user, boolean isManual) {
        var address = nfceData.address();
        return supermarketRepo.save(Supermarket.builder()
                .name(nfceData.store())
                .cnpj(nfceData.cnpj())
                .street(address.street())
                .number(address.number())
                .complement(address.complement())
                .neighborhood(address.neighborhood())
                .city(address.city())
                .state(address.state())
                .createdByUser(isManual ? user : null)
                .manual(isManual)
                .build());
    }

    private Supermarket updateSupermarket(NfceDataRequest nfceData, Purchase purchase) {
        Supermarket updatedMarket = purchase.getSupermarket();
        updatedMarket.setName(nfceData.store());
        updatedMarket.setCnpj(nfceData.cnpj());
        updatedMarket.setStreet(nfceData.address().street());
        updatedMarket.setNumber(nfceData.address().number());
        updatedMarket.setComplement(nfceData.address().complement());
        updatedMarket.setNeighborhood(nfceData.address().neighborhood());
        updatedMarket.setCity(nfceData.address().city());
        updatedMarket.setState(nfceData.address().state());
        return updatedMarket;
    }

    private Catalog createCatalog(NfceDataRequest.Item p, Supermarket market) {
        return catalogRepo.save(Catalog.builder()
                        .supermarket(market)
                        .code(p.code())
                        .name(p.name())
                        .unit(p.unit())
                        .build());
    }

    private AuthUser getAuthenticatedUser(String accessToken) {
        var email = jwtTokenServiceImpl.getSubjectFromToken(TokenUtils.recoveryToken(accessToken));
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário não encontrado."));
    }

    private String generateAccessKey() {
        Random random = new Random();
        int TOTAL_LENGTH = 44;
        String PREFIX = "MA";
        String key;

        do {
            StringBuilder sb = new StringBuilder(TOTAL_LENGTH);
            sb.append(PREFIX);
            for (int i = 0; i < TOTAL_LENGTH - PREFIX.length(); i++) {
                sb.append(random.nextInt(10));
            }
            key = sb.toString();
        } while (purchaseRepo.findByAccessKey(key).isPresent());

        return key;
    }
}
