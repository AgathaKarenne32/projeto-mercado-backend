package com.prati.projetomercado.service.nfce;

import com.prati.projetomercado.dto.request.NfceDataRequest;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Item;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NfceHelper {

    public NfceDataRequest createNfceDto(Purchase purchase) {
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

    public Supermarket buildSupermarket(NfceDataRequest nfceData, AuthUser user) {
        var address = nfceData.address();
        return Supermarket.builder()
                .name(nfceData.store())
                .cnpj(nfceData.cnpj())
                .street(address.street())
                .number(address.number())
                .complement(address.complement())
                .neighborhood(address.neighborhood())
                .city(address.city())
                .state(address.state())
                .createdByUser(user)
                .build();
    }

    public void updateSupermarket(NfceDataRequest nfceData, Purchase purchase) {
        Supermarket market = purchase.getSupermarket();
        market.setName(nfceData.store());
        market.setCnpj(nfceData.cnpj());
        market.setStreet(nfceData.address().street());
        market.setNumber(nfceData.address().number());
        market.setComplement(nfceData.address().complement());
        market.setNeighborhood(nfceData.address().neighborhood());
        market.setCity(nfceData.address().city());
        market.setState(nfceData.address().state());
    }

    public Catalog buildCatalog(NfceDataRequest.Item product, Supermarket market) {
        return Catalog.builder()
                .supermarket(market)
                .code(product.code())
                .name(product.name())
                .unit(product.unit())
                .build();
    }

    public Item buildItem(NfceDataRequest.Item product, Purchase purchase, Catalog catalog) {
        return Item.builder()
                .purchase(purchase)
                .catalog(catalog)
                .quantity(product.quantity())
                .unitPrice(product.price())
                .build();
    }
}
