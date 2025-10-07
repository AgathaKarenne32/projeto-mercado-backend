package com.prati.projetomercado.service.nfce;

import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.dto.response.NfceResponse;
import com.prati.projetomercado.dto.response.SupermarketResponse;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Item;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NfceHelper {

    public NfceResponse createNfceDto(Purchase purchase) {
        Supermarket market = purchase.getSupermarket();
        SupermarketResponse supermarket = SupermarketResponse.toDto(market);

        List<NfceRequest.Item> items = purchase.getItems().stream()
                .map(item -> new NfceRequest.Item(
                        item.getCatalog().getName(),
                        item.getCatalog().getCode(),
                        item.getQuantity(),
                        item.getCatalog().getUnit(),
                        item.getUnitPrice()
                ))
                .toList();

        return new NfceResponse(
                supermarket,
                purchase.getAccessKey(),
                purchase.getDate(),
                purchase.getTotalPrice(),
                purchase.isManual(),
                items
        );
    }

    public Supermarket buildSupermarket(NfceRequest nfceData, AuthUser user, Boolean isManual) {
        return Supermarket.builder()
                .name(nfceData.supermarket().store())
                .cnpj(nfceData.supermarket().cnpj())
                .city(nfceData.supermarket().city())
                .state(nfceData.supermarket().state())
                .createdByUser(user)
                .manual(isManual)
                .build();
    }

    public Catalog buildCatalog(NfceRequest.Item product, Supermarket market) {
        return Catalog.builder()
                .supermarket(market)
                .code(product.code())
                .name(product.name())
                .unit(product.unit())
                .build();
    }

    public Item buildItem(NfceRequest.Item product, Purchase purchase, Catalog catalog) {
        return Item.builder()
                .purchase(purchase)
                .catalog(catalog)
                .quantity(product.quantity())
                .unitPrice(product.price())
                .build();
    }
}
