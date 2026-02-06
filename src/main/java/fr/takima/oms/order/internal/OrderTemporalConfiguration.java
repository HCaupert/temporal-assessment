package fr.takima.oms.order.internal;

import fr.takima.oms.temporal.TaskQueues;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OrderTemporalConfiguration {

    private final WorkerFactory workerFactory;
    private final OrderActivities orderActivities;

    @PostConstruct
    void initWorker() {
        var worker = workerFactory.newWorker(TaskQueues.ORDER);
        worker.registerWorkflowImplementationTypes(OrderProcessingWorkflowImpl.class);
        worker.registerActivitiesImplementations(orderActivities);
    }
}
