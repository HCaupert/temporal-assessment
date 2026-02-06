package fr.takima.oms.payment.internal;

import fr.takima.oms.payment.internal.PaymentValidationWorkflow.Input;
import io.temporal.client.WorkflowClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static fr.takima.oms.payment.internal.PaymentValidationWorkflow.workflowId;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final WorkflowClient workflowClient;

    /**
     * Receive notifications - parse as little as possible to avoid serialization issues
     * Webhooks are replayed, delayed etc. so pretty not up to date anyway...
     */
    @PostMapping("/webhooks")
    void receivePayment(@RequestBody PaymentProcessorNotification notification) {
        var rrn = notification.paymentDetails().rrn();

        var workflow = workflowClient
                .newWorkflowStub(PaymentValidationWorkflow.class, workflowId(rrn));

        var signalWithStart = workflowClient.newSignalWithStartRequest();
        var input = new Input(rrn);
        signalWithStart.add(workflow::validatePayment, input);
        signalWithStart.add(workflow::statusChanged);

        workflowClient.signalWithStart(signalWithStart);
    }
}

