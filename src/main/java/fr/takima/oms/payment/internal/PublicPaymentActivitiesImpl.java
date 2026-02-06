package fr.takima.oms.payment.internal;

import fr.takima.oms.payment.api.PublicPaymentActivities;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Log
@Service
public class PublicPaymentActivitiesImpl implements PublicPaymentActivities {
    @Override
    public void cancelOrRefundPayment(CancelPaymentRequest request) {
        // NOOP
        log.info("Cancelling payment for order " + request.orderId());
    }
}
