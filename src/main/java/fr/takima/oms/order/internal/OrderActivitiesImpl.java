package fr.takima.oms.order.internal;

import fr.takima.oms.pishofy.internal.StoreFrontOrder;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Log
@Service
public class OrderActivitiesImpl implements OrderActivities {
    @Override
    public void upsertOrder(StoreFrontOrder order) {
        log.info("Upserting order " + order.order().id());
    }

    @Override
    public void updateOrder(OrderUpdate update) {
        log.info("Updating order " + update.orderId());
    }

    @Override
    public void enrichOrder(OrderEnrichRequest request) {
        log.info("Enriching order " + request.orderId());
    }

    @Override
    public void allocateOrder(OrderAllocateRequest request) {
        log.info("Allocating order " + request.orderId());
    }
}
