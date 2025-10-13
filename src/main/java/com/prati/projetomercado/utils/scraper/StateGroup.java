package com.prati.projetomercado.utils.scraper;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public enum StateGroup {
    GROUP1(new ScraperGroup1(), List.of("RS", "SC", "SP")),
    GROUP2(new ScraperGroup2(), List.of("MG"));

    private final Scraper scraper;
    @Getter
    private final List<String> states;

    StateGroup(Scraper scraper, List<String> states) {
        this.scraper = scraper;
        this.states = states;
    }

    public static Scraper getScraperByState(String state) {
        for (StateGroup group : values()) {
            if (group.states.contains(state)) {
                return group.scraper;
            }
        }
        throw new IllegalArgumentException("Estado " + state + " não implementado.");
    }

    public static List<String> getAllImplementedStates() {
        return Arrays.stream(values())
                .flatMap(group -> group.getStates().stream())
                .toList();
    }
}
