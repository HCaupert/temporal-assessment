package fr.takima.oms.payment.internal;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PaymentValidationWorkflow {

    static String workflowId(String rrn) {
        return "payment-validation-" + rrn;
    }

    @WorkflowMethod
    void validatePayment(Input input);

    @SignalMethod
    void statusChanged();

    record Input(
            String rrn
    ) {
    }
}
