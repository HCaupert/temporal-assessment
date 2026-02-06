package fr.takima.oms.pishofy.internal;

import fr.takima.oms.pishofy.api.StoreFrontActivities;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreFrontActivitiesImpl implements StoreFrontActivities {
    @Override
    public StoreFrontOrder getOrder(String orderId) {
        // NOOP
        // call rate limited storefront api here
        // Do mapping and validation on the fly
        return new StoreFrontOrder("cust_123", new StoreFrontOrder.Order(orderId, List.of(), true, true));
    }
}
