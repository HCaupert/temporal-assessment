package fr.takima.oms.payment.api;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface PublicPaymentActivities {
    void cancelOrRefundPayment(CancelPaymentRequest request);

    record CancelPaymentRequest(String orderId) {
    }
}
