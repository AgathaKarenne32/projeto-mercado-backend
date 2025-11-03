package com.prati.projetomercado.utils.scraper;

import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.dto.request.SupermarketRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScraperGroup4 implements IScraper {
    @Override
    public NfceRequest scrape(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        List<NfceRequest.Item> products = new ArrayList<>();

        Element storeInfo = doc.selectFirst("emit");
        String store = storeInfo.selectFirst("xNome").text();
        String cnpj = storeInfo.selectFirst("CNPJ").text();

        Element address = doc.selectFirst("enderEmit");
        String city = address.selectFirst("xMun").text();
        String state = address.selectFirst("UF").text();

        SupermarketRequest supermarket = new SupermarketRequest(
                null,
                store,
                cnpj,
                city,
                state
        );

        Element priceInfo = doc.selectFirst("ICMSTot");
        String totalPriceString = priceInfo.selectFirst("vProd").text();
        BigDecimal totalPrice = new BigDecimal(totalPriceString);

        String accessKey = doc.selectFirst("chNFe").text();
        System.out.println(accessKey);

        String dateString = doc.selectFirst("dhEmi").text();
        OffsetDateTime odt = OffsetDateTime.parse(dateString);
        LocalDate date = odt.toLocalDate();

        Elements items = doc.select("det");
        for (Element item : items) {
            Element prod = item.selectFirst("prod");

            String name = prod.selectFirst("xProd").text();
            String code = prod.selectFirst("cProd").text();

            String quantityString = prod.selectFirst("qCom").text();
            BigDecimal quantity = new BigDecimal(quantityString);

            String unit = prod.selectFirst("uCom").text();

            String priceString = prod.selectFirst("vUnCom").text();
            BigDecimal price = new BigDecimal(priceString);

            products.add(new NfceRequest.Item(name, code, quantity, unit, price));
        }

        return new NfceRequest(supermarket, accessKey, date, totalPrice, products);
    }
}
