package fr.takima.oms.payment.internal;

import fr.takima.oms.order.api.OrderProcessingWorkflow;
import fr.takima.oms.order.api.OrderProcessingWorkflow.Input;
import fr.takima.oms.order.api.OrderProcessingWorkflow.PaymentSignal;
import fr.takima.oms.temporal.TaskQueues;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ApplicationFailure;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static fr.takima.oms.order.api.OrderProcessingWorkflow.workflowId;

@Log
@Service
public class PaymentActivitiesImpl implements PaymentActivities {
    private final WorkflowClient workflowClient;

    public PaymentActivitiesImpl(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @Override
    public PaymentDetails getPaymentDetails(PaymentDetailsRequest request) {
        // Call external PSP API, returning mock here...
        log.info("Getting payment details for RRN: " + request.rrn());
        return new PaymentDetails(request.rrn(), "123456789", PaymentDetails.Status.VALIDATED);
    }

    @Override
    public void signalProcessingOrderWorkflow(PaymentDetails paymentDetails) {

        var workflowOptions = WorkflowOptions.newBuilder()
                .setTaskQueue(TaskQueues.ORDER)
                .setWorkflowExecutionTimeout(Duration.ofDays(31))
                .setWorkflowId(workflowId(paymentDetails.orderId()))
                .build();

        var orderProcessingWorkflow = workflowClient
                .newWorkflowStub(OrderProcessingWorkflow.class, workflowOptions);

        var request = workflowClient.newSignalWithStartRequest();
        request.add(orderProcessingWorkflow::processOrder, new Input(paymentDetails.orderId()));

        var paymentSignal = switch (paymentDetails.status()) {
            case FAILED -> PaymentSignal.Status.FAILED;
            case VALIDATED -> PaymentSignal.Status.SUCCESS;
            // Should never happen
            case PENDING -> throw ApplicationFailure
                    .newNonRetryableFailure("Unable to notify order processing of a non terminal status", "INVALID_PAYMENT_STATUS");
        };

        request.add(orderProcessingWorkflow::updatePaymentStatus, new PaymentSignal(paymentSignal));

        workflowClient.signalWithStart(request);
    }
}
