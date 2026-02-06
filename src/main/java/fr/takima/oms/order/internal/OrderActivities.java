package fr.takima.oms.order.internal;

import fr.takima.oms.pishofy.internal.StoreFrontOrder;
import io.temporal.activity.ActivityInterface;
import lombok.Builder;

import javax.annotation.Nullable;

@ActivityInterface
public interface OrderActivities {
    void upsertOrder(StoreFrontOrder order);

    void updateOrder(OrderUpdate update);

    void enrichOrder(OrderEnrichRequest request);

    void allocateOrder(OrderAllocateRequest request);

    @Builder
    record OrderUpdate(
            String orderId,
            @Nullable Status status,
            @Nullable PaymentStatus paymentStatus
    ) {
        public enum Status {
            PENDING,
            VALIDATED,
            CANCELLED
        }

        public enum PaymentStatus {
            PENDING,
            VALIDATED,
            CANCELLED
        }
    }

    record OrderEnrichRequest(String orderId) {
    }

    record OrderAllocateRequest(String orderId) {
    }

}
