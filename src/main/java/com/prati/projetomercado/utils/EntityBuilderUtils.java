package com.prati.projetomercado.utils;

import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.dto.request.SupermarketRequest;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Item;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EntityBuilderUtils {

    public Supermarket buildSupermarket(SupermarketRequest supermarket, AuthUser user, Boolean isManual) {
        return Supermarket.builder()
                .name(supermarket.store())
                .cnpj(supermarket.cnpj())
                .city(supermarket.city())
                .state(supermarket.state())
                .createdByUser(user)
                .manual(isManual)
                .build();
    }

    public Catalog buildCatalog(NfceRequest.Item product, Supermarket market) {
        return Catalog.builder()
                .supermarket(market)
                .code(product.code() == null || product.code().isEmpty()
                        ? UUID.randomUUID().toString() : product.code()
                )
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
