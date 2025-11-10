package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.dto.request.NfceFilterRequest;
import com.prati.projetomercado.dto.request.NfcePatchRequest;
import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.dto.response.NfceResponse;
import com.prati.projetomercado.dto.response.StatesResponse;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Item;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.DuplicateEntityException;
import com.prati.projetomercado.exceptions.EntityNotFoundException;
import com.prati.projetomercado.exceptions.NfceUrlParseException;
import com.prati.projetomercado.exceptions.NotManualEntityException;
import com.prati.projetomercado.exceptions.UnauthorizedAccessException;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.CatalogRepository;
import com.prati.projetomercado.repository.PurchaseRepository;
import com.prati.projetomercado.repository.SupermarketRepository;
import com.prati.projetomercado.repository.spec.PurchaseSpecification;
import com.prati.projetomercado.service.NfceService;
import com.prati.projetomercado.utils.EntityBuilderUtils;
import com.prati.projetomercado.utils.scraper.Scraper;
import com.prati.projetomercado.utils.scraper.StatesRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NfceServiceImpl implements NfceService {

    private final EntityBuilderUtils builder;
    private final StatesRegistry statesRegistry;
    private final AuthUserRepository userRepo;
    private final SupermarketRepository supermarketRepo;
    private final PurchaseRepository purchaseRepo;
    private final CatalogRepository catalogRepo;

    @Override
    @Transactional(readOnly = true)
    public NfceResponse findByAccessKey(String accessKey) {
        AuthUser user = getAuthenticatedUser();
        Purchase purchase = purchaseRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new EntityNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para acessar esta nota fiscal.");
        }

        return NfceResponse.from(purchase);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NfceResponse> findAllByUser(int page, int size) {
        AuthUser user = getAuthenticatedUser();

        Pageable pageable = PageRequest.of(page, size);
        Page<Purchase> purchasesPage = purchaseRepo.findAllByUser(user, pageable);

        return purchasesPage.map(NfceResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NfceResponse> search(NfceFilterRequest filter, int page, int size) {
        AuthUser user = getAuthenticatedUser();

        Pageable pageable = PageRequest.of(page, size);

        Specification<Purchase> spec = Specification.allOf(
                PurchaseSpecification.hasSupermarket(filter.supermarketId()),
                PurchaseSpecification.hasDate(filter.date()),
                PurchaseSpecification.hasUpdatedDate(filter.updatedDate()),
                PurchaseSpecification.hasTotalBetween(filter.minTotal(), filter.maxTotal()),
                (root, query, cb) -> cb.equal(root.get("user"), user)
        );

        Page<Purchase> purchasesPage = purchaseRepo.findAll(spec, pageable);

        return purchasesPage.map(NfceResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public StatesResponse getAvailableStates() {
        getAuthenticatedUser();
        return new StatesResponse(statesRegistry.getAllImplementedStates());
    }

    @Override
    @Transactional()
    public NfceResponse createFromLink(String url) {
        String state = getStateFromUrl(url);
        Scraper scraper = statesRegistry.getScraperByState(state);
        NfceRequest nfceData = scraper.getData(url);
        return savePurchase(nfceData, false);
    }

    @Override
    @Transactional()
    public NfceResponse createManually(NfceRequest nfceData) {
        return savePurchase(nfceData, true);
    }

    @Override
    @Transactional()
    public NfceResponse update(String accessKey, NfceRequest nfceData) {
        AuthUser user = getAuthenticatedUser();

        Purchase purchase = purchaseRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new EntityNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.isManual()) {
            throw new NotManualEntityException("Notas fiscais cadastradas pelo QR code não podem ser editadas.");
        }

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para editar esta nota fiscal.");
        }

        purchase.setDate(nfceData.date());
        purchase.setTotalPrice(nfceData.totalPrice());
        purchase.getItems().clear();

        for (NfceRequest.Item product : nfceData.products()) {
            Catalog catalog = catalogRepo.findBySupermarketAndCode(purchase.getSupermarket(), product.code())
                    .orElseGet(() -> catalogRepo.save(builder.buildCatalog(product, purchase.getSupermarket())));
            purchase.getItems().add(builder.buildItem(product, purchase, catalog));
        }

        return NfceResponse.from(purchase);
    }

    @Override
    @Transactional()
    public NfceResponse partialUpdate(String accessKey, NfcePatchRequest patchData) {
        AuthUser user = getAuthenticatedUser();

        Purchase purchase = purchaseRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new EntityNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.isManual()) {
            throw new NotManualEntityException("Notas fiscais cadastradas pelo QR code não podem ser editadas.");
        }

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para editar esta nota fiscal.");
        }

        if (patchData.date() != null) purchase.setDate(patchData.date());
        if (patchData.totalPrice() != null) purchase.setTotalPrice(patchData.totalPrice());
        if (patchData.products() != null) {
            purchase.getItems().clear();
            for (NfceRequest.Item item : patchData.products()) {
                Catalog catalog = catalogRepo.findBySupermarketAndCode(purchase.getSupermarket(), item.code())
                        .orElseGet(() -> catalogRepo.save(builder.buildCatalog(item, purchase.getSupermarket())));
                purchase.getItems().add(builder.buildItem(item, purchase, catalog));
            }
        }

        return NfceResponse.from(purchase);
    }

    @Override
    @Transactional()
    public void delete(String accessKey) {
        AuthUser user = getAuthenticatedUser();

        Purchase purchase = purchaseRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new EntityNotFoundException("Nota fiscal não encontrada."));

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Você não tem permissão para deletar esta nota fiscal.");
        }

        purchaseRepo.delete(purchase);
    }

    private NfceResponse savePurchase(NfceRequest nfceData, boolean isManual) {
        AuthUser user = getAuthenticatedUser();

        purchaseRepo.findByAccessKey(nfceData.accessKey())
                .ifPresent(p -> {
                    throw new DuplicateEntityException("Nota fiscal já existe.");
                });

        Supermarket market;

        if (nfceData.supermarket().id() != null) {
            market = supermarketRepo.findById(nfceData.supermarket().id())
                    .orElseThrow(() -> new EntityNotFoundException("Supermercado não encontrado."));
        } else if (!isManual) {
            market = supermarketRepo.findByCnpjAndManual(nfceData.supermarket().cnpj(), false)
                    .orElseGet(() -> supermarketRepo.save(builder.buildSupermarket(nfceData.supermarket(), user, false)));
        } else {
            market = supermarketRepo.save(builder.buildSupermarket(nfceData.supermarket(), user, true));
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
                    .orElseGet(() -> catalogRepo.save(builder.buildCatalog(product, market)));

            Optional<Item> existingItem = purchase.getItems().stream()
                    .filter(i -> i.getCatalog().getCode().equals(product.code()))
                    .findFirst();

            if (existingItem.isPresent()) {
                Item item = existingItem.get();
                item.setQuantity(item.getQuantity().add(product.quantity()));
            } else {
                Item item = builder.buildItem(product, purchase, catalog);
                purchase.getItems().add(item);
            }
        }

        purchaseRepo.save(purchase);
        return NfceResponse.from(purchase);
    }

    private AuthUser getAuthenticatedUser() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("Usuário autenticado não encontrado no banco de dados."));
    }

    public String getStateFromUrl(String url) {
        try {
            String safeUrl = url.replace("|", "%7C");
            URI uri = new URI(safeUrl);
            String host = uri.getHost();

            if (host == null) {
                throw new NfceUrlParseException("Host inválido.");
            }

            String[] parts = host.split("\\.");

            if (parts.length >= 3 && "gov".equals(parts[parts.length - 2]) && "br".equals(parts[parts.length - 1])) {
                return parts[parts.length - 3].toUpperCase();
            }

            throw new NfceUrlParseException("Estado não encontrado na URL");

        } catch (URISyntaxException ex) {
            throw new NfceUrlParseException("URL inválida para extrair o estado");
        }
    }
}
