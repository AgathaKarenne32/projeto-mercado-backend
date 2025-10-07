package com.prati.projetomercado.service.nfce;

import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.dto.response.NfceResponse;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Item;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.DuplicateNfceException;
import com.prati.projetomercado.exceptions.EditNotAllowedException;
import com.prati.projetomercado.exceptions.EntityNotFoundException;
import com.prati.projetomercado.exceptions.UnauthorizedAccessException;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.CatalogRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NfceService {

    private final ScraperUtils scraper;
    private final NfceHelper helper;
    private final AuthUserRepository userRepo;
    private final SupermarketRepository supermarketRepo;
    private final PurchaseRepository purchaseRepo;
    private final CatalogRepository catalogRepo;
    private final JwtTokenServiceImpl jwtTokenServiceImpl;

    @Transactional(readOnly = true)
    public NfceResponse getOne(String accessToken, String accessKey) {
        AuthUser user = getAuthenticatedUser(accessToken);
        Purchase purchase = purchaseRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new EntityNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para acessar esta nota fiscal.");
        }

        return helper.createNfceDto(purchase);
    }

    @Transactional(readOnly = true)
    public List<NfceResponse> getAll(String accessToken) {
        AuthUser user = getAuthenticatedUser(accessToken);
        List<Purchase> purchases = purchaseRepo.findAllByUser(user);
        List<NfceResponse> nfceList = new ArrayList<>();

        for (Purchase purchase : purchases) {
            NfceResponse nfceDto = helper.createNfceDto(purchase);
            nfceList.add(nfceDto);
        }

        return nfceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public NfceResponse registerLink(String accessToken, String url) {
        NfceRequest nfceData = scraper.getData(url);
        return savePurchase(nfceData, accessToken, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public NfceResponse registerManual(String accessToken, NfceRequest nfceData) {
        return savePurchase(nfceData, accessToken, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void edit(String accessToken, NfceRequest nfceData) {
        AuthUser user = getAuthenticatedUser(accessToken);
        Purchase purchase = purchaseRepo.findByAccessKey(nfceData.accessKey())
                .orElseThrow(() -> new EntityNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.isManual()) {
            throw new EditNotAllowedException("Notas fiscais cadastradas pelo QR code não podem ser editadas.");
        }

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para editar esta nota fiscal.");
        }

        purchase.setSupermarket(purchase.getSupermarket());
        purchase.setAccessKey(nfceData.accessKey());
        purchase.setDate(nfceData.date());
        purchase.setTotalPrice(nfceData.totalPrice());
        purchase.getItems().clear();

        for (NfceRequest.Item product : nfceData.products()) {
            Catalog catalog = catalogRepo.findBySupermarketAndCode(purchase.getSupermarket(), product.code())
                    .orElseGet(() -> catalogRepo.save(helper.buildCatalog(product, purchase.getSupermarket())));

            Item item = helper.buildItem(product, purchase, catalog);
            purchase.getItems().add(item);
        }

        purchaseRepo.save(purchase);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String accessToken, String accessKey) {
        AuthUser user = getAuthenticatedUser(accessToken);

        Purchase purchase = purchaseRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new EntityNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para deletar esta nota fiscal.");
        }

        purchaseRepo.delete(purchase);
    }

    private NfceResponse savePurchase(NfceRequest nfceData, String accessToken, boolean isManual) {
        AuthUser user = getAuthenticatedUser(accessToken);

        purchaseRepo.findByAccessKey(nfceData.accessKey())
                .ifPresent(p -> {
                    throw new DuplicateNfceException("Nota fiscal já existe.");
                });

        Supermarket market;

        if (nfceData.supermarket().id() != null) {
            market = supermarketRepo.findById(nfceData.supermarket().id())
                    .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));
        } else if (!isManual) {
            market = supermarketRepo.findByCnpjAndManual(nfceData.supermarket().cnpj(), false)
                    .orElseGet(() -> supermarketRepo.save(helper.buildSupermarket(nfceData, user, false)));
        } else {
            market = supermarketRepo.save(helper.buildSupermarket(nfceData, user, true));
        }

        Purchase purchase = Purchase.builder()
                .user(user)
                .supermarket(market)
                .accessKey((nfceData.accessKey() == null || nfceData.accessKey().isEmpty())
                        ? UUID.randomUUID().toString() : nfceData.accessKey()
                )
                .date(nfceData.date())
                .totalPrice(nfceData.totalPrice())
                .manual(isManual)
                .build();

        for (NfceRequest.Item product : nfceData.products()) {
            Catalog catalog = catalogRepo.findBySupermarketAndCode(market, product.code())
                    .orElseGet(() -> catalogRepo.save(helper.buildCatalog(product, market)));

            Optional<Item> existingItem = purchase.getItems().stream()
                    .filter(i -> i.getCatalog().getCode().equals(product.code()))
                    .findFirst();

            if (existingItem.isPresent()) {
                Item item = existingItem.get();
                item.setQuantity(item.getQuantity().add(product.quantity()));
            } else {
                Item item = helper.buildItem(product, purchase, catalog);
                purchase.getItems().add(item);
            }
        }

        purchaseRepo.save(purchase);
        return helper.createNfceDto(purchase);
    }

    private AuthUser getAuthenticatedUser(String accessToken) {
        var email = jwtTokenServiceImpl.getSubjectFromToken(TokenUtils.recoveryToken(accessToken));
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário não encontrado."));
    }
}
