package fr.takima.oms.order.internal;

import fr.takima.oms.order.api.OrderProcessingWorkflow;
import fr.takima.oms.order.internal.OrderActivities.OrderAllocateRequest;
import fr.takima.oms.order.internal.OrderActivities.OrderEnrichRequest;
import fr.takima.oms.order.internal.OrderActivities.OrderUpdate;
import fr.takima.oms.order.internal.OrderActivities.OrderUpdate.PaymentStatus;
import fr.takima.oms.order.internal.OrderActivities.OrderUpdate.Status;
import fr.takima.oms.payment.api.PublicPaymentActivities;
import fr.takima.oms.payment.api.PublicPaymentActivities.CancelPaymentRequest;
import fr.takima.oms.pishofy.api.StoreFrontActivities;
import fr.takima.oms.pishofy.internal.StoreFrontOrder;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;

import static io.temporal.workflow.Workflow.newActivityStub;

public class OrderProcessingWorkflowImpl implements OrderProcessingWorkflow {

    private final Promise<Void> paymentExpirationTimer = Workflow.newTimer(Duration.ofMinutes(30));
    private final PublicPaymentActivities paymentActivities = newActivityStub(PublicPaymentActivities.class);


    @Nullable
    private PaymentSignal.Status paymentStatus = null;
    @Nullable
    private StoreFrontOrder order = null;
    private boolean orderChanged = false;
    private boolean orderCancelled = false;

    private final StoreFrontActivities storeFrontActivities = newActivityStub(StoreFrontActivities.class);
    private final OrderActivities orderActivities = newActivityStub(OrderActivities.class);

    public void processOrder(Input input) {

        while (!orderValid() || orderCancelled) {
            Workflow.await(() -> orderChanged || orderCancelled);
            if (orderCancelled) continue;
            orderChanged = false;

            order = storeFrontActivities.getOrder(input.orderId());
            // Give visibility to customer & support team
            orderActivities.upsertOrder(order);
        }

        if (paymentExpirationTimer.isCompleted() || orderCancelled || paymentFailed()) {
            cancelOrder(input);
            return;
        }


        if (order.order().needsEnrichment()) {
            orderActivities.enrichOrder(new OrderEnrichRequest(input.orderId()));
        }

        orderActivities.allocateOrder(new OrderAllocateRequest(input.orderId()));
    }

    private void cancelOrder(Input input) {
        paymentActivities.cancelOrRefundPayment(new CancelPaymentRequest(input.orderId()));

        var update = OrderUpdate.builder()
                .orderId(input.orderId())
                .status(Status.CANCELLED)
                .paymentStatus(PaymentStatus.CANCELLED)
                .build();
        orderActivities.updateOrder(update);
    }

    @Override
    public void updatePaymentStatus(PaymentSignal paymentSignal) {
        paymentStatus = paymentSignal.status();
    }

    @Override
    public void triggerOrderProcessing() {
        orderChanged = true;
    }

    private boolean paymentCompleted() {
        return Objects.nonNull(paymentStatus);
    }

    private boolean paymentFailed() {
        return Objects.nonNull(paymentStatus) && paymentStatus.equals(PaymentSignal.Status.FAILED);
    }

    private boolean orderValid() {
        return Objects.nonNull(order) && order.order().valid();
    }

    public void cancelOrder() {
        if (paymentCompleted()) return; // Too late!
        orderCancelled = true;
    }
}
