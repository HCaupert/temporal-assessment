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
import fr.takima.oms.temporal.TaskQueues;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;

import static fr.takima.oms.temporal.TemporalHelpers.simpleActivityStub;

public class OrderProcessingWorkflowImpl implements OrderProcessingWorkflow {

    private final Promise<Void> paymentExpirationTimer = Workflow.newTimer(Duration.ofDays(30));
    private final PublicPaymentActivities paymentActivities = simpleActivityStub(PublicPaymentActivities.class, TaskQueues.PAYMENT);
    private final StoreFrontActivities storeFrontActivities = simpleActivityStub(StoreFrontActivities.class, TaskQueues.STORE_FRONT);
    private final OrderActivities orderActivities = simpleActivityStub(OrderActivities.class, TaskQueues.ORDER);

    @Nullable
    private PaymentSignal.Status paymentStatus = null;
    @Nullable
    private StoreFrontOrder order = null;
    private boolean orderChanged = false;
    private boolean orderCancelled = false;

    public void processOrder(Input input) {

        while (!orderValid() || !orderCancelled || !paymentExpired()) {
            Workflow.await(() -> orderChanged || orderCancelled || paymentExpired());
            if (orderCancelled || paymentExpired()) continue;
            orderChanged = false;

            order = storeFrontActivities.getOrder(input.orderId());
            // Give visibility to customers & support team
            orderActivities.upsertOrder(order);
            // Might as well notify the support team here
        }

        if (paymentExpired() || orderCancelled || paymentFailed()) {
            cancelOrder(input);
            return;
        }


        if (order.order().needsEnrichment()) {
            orderActivities.enrichOrder(new OrderEnrichRequest(input.orderId()));
        }

        orderActivities.allocateOrder(new OrderAllocateRequest(input.orderId()));
    }

    public void requestOrderCancellation() {
        if (paymentCompleted()) return; // Too late!
        orderCancelled = true;
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

    private boolean paymentExpired() {
        return Objects.isNull(paymentStatus) && paymentExpirationTimer.isCompleted();
    }
}
