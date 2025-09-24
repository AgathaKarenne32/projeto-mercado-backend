package com.prati.projetomercado.utils;

import com.prati.projetomercado.dto.request.NfceDataRequest;
import com.prati.projetomercado.dto.request.ItemRequest;
import com.prati.projetomercado.dto.request.AddressRequest;
import com.prati.projetomercado.exceptions.NfceFetchException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ScraperUtils {

    private LocalDate extractDate(String text) {
        Matcher m = Pattern.compile("Emissão:\\s*(\\d{2}/\\d{2}/\\d{4})").matcher(text);
        String dateString = m.find() ? m.group(1) : "";
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public NfceDataRequest getData(String url) {

        try {
            Document doc = Jsoup.connect(url).get();
            List<ItemRequest> products = new ArrayList<>();

            // gets store information
            Elements storeInfo = doc.select("div#conteudo div.txtCenter > div");
            String store = storeInfo.get(0).text();
            String cnpj = storeInfo.get(1).text().replaceFirst("CNPJ:\\s*", "");

            // splits address information into separate fields
            String addressString = storeInfo.get(2).text();
            String[] parts = addressString.split("\\s*,\\s*");
            AddressRequest address = new AddressRequest(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);

            // gets total price
            Element totalInfo = doc.selectFirst("div#totalNota > :nth-child(2) span");
            String totalPriceString = totalInfo.text().replace(",", ".");
            BigDecimal totalPrice = new BigDecimal(totalPriceString);

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

                String quantityString = spans.get(2).text()
                        .replace("Qtde.:", "")
                        .trim()
                        .replace(",", ".");
                BigDecimal quantity = new BigDecimal(quantityString);

                String unit = spans.get(3).text().split(":")[1].trim();

                String priceString = spans.get(4).text()
                        .replaceAll("[^\\d,]", "")
                        .replace(",", ".");

                BigDecimal price = new BigDecimal(priceString);

                products.add(new ItemRequest(name, code, quantity, unit, price));
            }

            return new NfceDataRequest(store, cnpj, address, accessKey, date, totalPrice, products);
        } catch (IOException e) {
            throw new NfceFetchException("Erro ao buscar dados da página");
        } catch (IllegalArgumentException e) {
            throw new NfceFetchException ("URL inválida.");
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            throw new NfceFetchException("A estrutura da página está diferente do esperado.");
        }
    }
}
