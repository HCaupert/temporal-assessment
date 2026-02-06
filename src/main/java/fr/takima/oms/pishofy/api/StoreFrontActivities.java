package fr.takima.oms.pishofy.api;

import fr.takima.oms.pishofy.internal.StoreFrontOrder;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface StoreFrontActivities {
    StoreFrontOrder getOrder(String orderId);
}

