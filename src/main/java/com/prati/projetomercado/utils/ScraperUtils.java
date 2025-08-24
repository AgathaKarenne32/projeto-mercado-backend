package com.prati.projetomercado.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ScraperUtils {
    @Getter
    @AllArgsConstructor
    public static class NfceData {
        private String store;
        private String cnpj;
        private Address address;
        private String accessKey;
        private LocalDate date;
        private double totalPrice;
        private List<Product> products;

    }

    @Getter
    @AllArgsConstructor
    public static class Product {
        private String name;
        private String code;
        private Double quantity;
        private String unit;
        private double price;
    }

    @Getter
    @AllArgsConstructor
    public static class Address {
        private String street;
        private String number;
        private String complement;
        private String neighborhood;
        private String city;
        private String state;
    }

    private LocalDate extractDate(String text) {
        Matcher m = Pattern.compile("Emissão:\\s*(\\d{2}/\\d{2}/\\d{4})").matcher(text);
        String dateString = m.find() ? m.group(1) : "";
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public NfceData getData(String url) throws IOException {

        Document doc = Jsoup.connect(url).get();
        List<Product> products = new ArrayList<>();

        // gets store information
        Elements storeInfo = doc.select("div#conteudo div.txtCenter > div");
        String store = storeInfo.get(0).text();
        String cnpj = storeInfo.get(1).text().replaceFirst("CNPJ:\\s*", "");

        // splits address information into separate fields
        String addressString = storeInfo.get(2).text();
        String[] parts = addressString.split("\\s*,\\s*");
        Address address = new Address(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);

        // gets total price
        Element totalInfo = doc.selectFirst("div#totalNota > :nth-child(2) span");
        double totalPrice = Double.parseDouble(totalInfo.text().replace(",", "."));

        // gets access key
        Element keyInfo = doc.selectFirst("div#infos span.chave");
        String accessKey = keyInfo.text().trim().replace(" ", "");

        // gets NFC-e number, series and issue date. Then extracts date
        Element generalInfo = doc.selectFirst("div#infos > div ul li");
        String fullText = generalInfo.text();
        LocalDate date = extractDate(fullText);

        // gets all products information
        Elements rows = doc.select("table tbody tr");
        for (Element row : rows) {
            Elements spans = row.select("td span");

            String name = spans.get(0).text();
            String code = spans.get(1).text().replaceAll("\\D+", "").trim();
            String quantityStr = spans.get(2).text(); // "Qtde.: 1"
            quantityStr = quantityStr.replace("Qtde.:", "").trim().replace(",", ".");
            Double quantity = Double.parseDouble(quantityStr);

            String unit = spans.get(3).text().split(":")[1].trim();
            double price = Double.parseDouble(spans.get(4).text().replaceAll("[^\\d,]", "").replace(",", "."));

            products.add(new Product(name, code, quantity, unit, price));
        }

        return new NfceData(store, cnpj, address, accessKey, date, totalPrice, products);
    }
}
