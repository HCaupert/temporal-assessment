package fr.takima.oms.payment.internal;

import fr.takima.oms.payment.api.PublicPaymentActivities;
import fr.takima.oms.temporal.TaskQueues;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PaymentTemporalConfiguration {

    private final WorkerFactory workerFactory;
    private final PaymentActivities paymentActivities;
    private final PublicPaymentActivities publicPaymentActivities;

    @PostConstruct
    void initWorker() {
        var worker = workerFactory.newWorker(TaskQueues.ORDER);
        worker.registerActivitiesImplementations(paymentActivities, publicPaymentActivities);
    }
}
