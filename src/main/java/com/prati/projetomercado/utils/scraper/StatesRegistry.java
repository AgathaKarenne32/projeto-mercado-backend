package com.prati.projetomercado.utils.scraper;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StatesRegistry {

    private final Map<String, Scraper> stateToScraper = new HashMap<>();

    public StatesRegistry(
            ScraperGroup1 scraperGroup1,
            ScraperGroup2 scraperGroup2,
            ScraperGroup3 scraperGroup3,
            ScraperGroup4 scraperGroup4
    ) {
        register(scraperGroup1, List.of("RS", "SC", "SP"));
        register(scraperGroup2, List.of("MG"));
        register(scraperGroup3, List.of("CE", "RJ"));
        register(scraperGroup4, List.of("PE"));
    }

    private void register(Scraper scraper, List<String> states) {
        for (String state : states) {
            stateToScraper.put(state, scraper);
        }
    }

    public Scraper getScraperByState(String state) {
        Scraper scraper = stateToScraper.get(state);
        if (scraper == null) {
            throw new IllegalArgumentException("Estado " + state + " não implementado.");
        }
        return scraper;
    }

    public List<String> getAllImplementedStates() {
        return List.copyOf(stateToScraper.keySet());
    }
}
