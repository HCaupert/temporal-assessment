package fr.takima.oms.pishofy.internal;

import fr.takima.oms.order.api.OrderProcessingWorkflow;
import fr.takima.oms.order.api.OrderProcessingWorkflow.Input;
import io.temporal.client.WorkflowClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static fr.takima.oms.order.api.OrderProcessingWorkflow.workflowId;

@RequestMapping("/api/storefront/webooks")
@RestController
@RequiredArgsConstructor
public class StoreFrontController {

    private final WorkflowClient workflowClient;

    @PostMapping
    void orderPlaced(@RequestBody StoreFrontWebhookDto dto) {
        var orderWorkflow = workflowClient
                .newWorkflowStub(OrderProcessingWorkflow.class, workflowId(dto.order().orderId()));

        var request = workflowClient.newSignalWithStartRequest();
        request.add(orderWorkflow::processOrder, new Input(dto.order().orderId()));
        request.add(orderWorkflow::triggerOrderProcessing);

        workflowClient.signalWithStart(request);
    }

    // Minimal parsing, never trust webhooks
    record StoreFrontWebhookDto(Order order) {
        record Order(String orderId) {
        }
    }
}
