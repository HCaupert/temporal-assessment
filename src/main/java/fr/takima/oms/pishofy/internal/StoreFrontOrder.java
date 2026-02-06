package fr.takima.oms.pishofy.internal;

import java.util.List;

public record StoreFrontOrder(
        String customerId,
        Order order
) {
    public record Order(String id, List<String> items, boolean valid, boolean needsEnrichment) {
    }
}
