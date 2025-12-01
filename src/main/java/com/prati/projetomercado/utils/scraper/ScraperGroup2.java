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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ScraperGroup2 implements Scraper {
    private SupermarketRequest getSupermarketRequest(String cnpjAndAddressText, String store, String cnpj) {
        String[] parts = cnpjAndAddressText.split("Inscrição Estadual: \\d+");
        String fullAddress = parts.length > 1 ? parts[1].trim() : null;
        String[] addressParts = fullAddress.split(" - ");

        String leftPart = addressParts[0];
        String rightPart = addressParts[1];

        String[] leftTokens = leftPart.split(",");
        String[] rightTokens = rightPart.split(",");

        return new SupermarketRequest(
                null,
                store,
                cnpj,
                rightTokens[0].trim(), // city
                rightTokens[1].trim() // state
        );
    }

    @Override
    public NfceRequest scrape(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        List<NfceRequest.Item> products = new ArrayList<>();

        // gets store name
        Elements storeInfo = doc.select("div.container h4 b");
        String store = storeInfo.text();

        // gets cnpj and address
        Elements cnpjAndAddress = doc.select("div.container table.table.text-center tbody");
        String cnpjAndAddressText = cnpjAndAddress.text();

        Pattern pattern = Pattern.compile("CNPJ:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(cnpjAndAddressText);
        String cnpj = matcher.find() ? matcher.group(1) : "";

        // splits address information into separate fields
        SupermarketRequest supermarket = getSupermarketRequest(cnpjAndAddressText, store, cnpj);

        // gets total price
        Elements totalInfo = doc.select("div.container div.row strong");
        BigDecimal totalPrice = new BigDecimal(totalInfo.get(3).text());

        // gets access key
        Element keyInfo = doc.selectFirst("div#collapseTwo table.table.table-hover tbody td");
        String accessKey = keyInfo.text().replaceAll("\\D", "");

        // gets date
        Elements generalInfo = doc.select("div#collapse4 table.table-hover tbody td:nth-child(4)");
        String dateString = generalInfo.get(1).text().split(" ")[0];
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        System.out.println("test date");

        // gets all products information
        Elements items = doc.select("tbody#myTable tr");
        for (Element item : items) {
            Elements info = item.select("td");

            pattern = Pattern.compile("^(.*?)\\s*\\(Código:\\s*(\\d+)\\)$");
            matcher = pattern.matcher(info.getFirst().text());

            String name = matcher.find() ? matcher.group(1).trim() : "";
            String code = matcher.group(2).trim();
            BigDecimal quantity = new BigDecimal(info.get(1).text().replaceAll("[^0-9.]", ""));
            String unit = info.get(2).text().split(":")[1].trim();
            BigDecimal lineTotalPrice = new BigDecimal(info.get(3).text().replaceAll("[^\\d,]", "").replace(",", "."));
            BigDecimal price = lineTotalPrice.divide(quantity, 4, RoundingMode.HALF_UP);

            products.add(new NfceRequest.Item(name, code, quantity, unit, price));
        }

        return new NfceRequest(supermarket, accessKey, date, totalPrice, products);
    }
}
