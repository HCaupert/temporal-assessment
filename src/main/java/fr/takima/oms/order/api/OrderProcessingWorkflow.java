package fr.takima.oms.order.api;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface OrderProcessingWorkflow {

    static String workflowId(String orderId) {
        return "order-processing-" + orderId;
    }

    @WorkflowMethod
    void processOrder(Input input);

    @SignalMethod
    void updatePaymentStatus(PaymentSignal paymentSignal);

    @SignalMethod
    void triggerOrderProcessing();

    @SignalMethod
    void cancelOrder();


    record Order() {
    }

    record Input(String orderId) {
    }

    record PaymentSignal(Status status) {
        public enum Status {
            SUCCESS,
            FAILED
        }
    }
}
