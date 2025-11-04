package com.prati.projetomercado.utils.scraper;

import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.exceptions.NfceScrapeException;

import java.io.IOException;

public interface Scraper {
    NfceRequest scrape(String url) throws Exception;

    default NfceRequest getData(String url) throws NfceScrapeException {
        try {
            return scrape(url);
        } catch (IOException ex) {
            throw new NfceScrapeException("Erro ao buscar dados da página.");
        } catch (IllegalArgumentException ex) {
            throw new NfceScrapeException("URL inválida para fazer o scrape.");
        } catch (Exception ex) {
            throw new NfceScrapeException("A estrutura da página está diferente do esperado.");
        }
    }
}
