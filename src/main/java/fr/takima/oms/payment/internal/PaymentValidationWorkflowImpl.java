package fr.takima.oms.payment.internal;

import fr.takima.oms.payment.internal.PaymentActivities.PaymentDetails;
import fr.takima.oms.payment.internal.PaymentActivities.PaymentDetailsRequest;

import static io.temporal.workflow.Workflow.newActivityStub;

public class PaymentValidationWorkflowImpl implements PaymentValidationWorkflow {

    private boolean needsSync = true;
    private boolean orderNotified = false;
    private final PaymentActivities paymentActivities = newActivityStub(PaymentActivities.class);

    @Override
    public void validatePayment(Input input) {
        while (needsSync) {
            needsSync = false;
            var paymentDetails = paymentActivities
                    .getPaymentDetails(new PaymentDetailsRequest(input.rrn()));

            if (paymentDetails.status() != PaymentDetails.Status.PENDING && !orderNotified) {
                orderNotified = true;
                paymentActivities.signalProcessingOrderWorkflow(paymentDetails);
            }
        }
    }

    @Override
    public void statusChanged() {
        needsSync = true;
    }
}
