package fr.takima.oms.payment.internal;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface PaymentActivities {

    PaymentDetails getPaymentDetails(PaymentDetailsRequest request);

    void signalProcessingOrderWorkflow(PaymentDetails paymentDetails);

    record PaymentDetailsRequest(String rrn) {
    }

    record PaymentDetails(String rrn, String orderId, Status status) {
        public enum Status {
            PENDING,
            VALIDATED,
            FAILED,
        }
    }
}
