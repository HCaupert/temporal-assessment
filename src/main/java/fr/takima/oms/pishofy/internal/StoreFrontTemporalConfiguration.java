package fr.takima.oms.pishofy.internal;

import fr.takima.oms.pishofy.api.StoreFrontActivities;
import fr.takima.oms.pishofy.internal.StoreFrontTemporalConfiguration.StoreFrontProperties;
import fr.takima.oms.temporal.TaskQueues;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(StoreFrontProperties.class)
public class StoreFrontTemporalConfiguration {

    private final WorkerFactory workerFactory;
    private final StoreFrontActivities storeFrontActivities;
    private final StoreFrontProperties storeFrontProperties;

    @PostConstruct
    void initWorker() {
        var options = WorkerOptions.newBuilder()
                .setMaxConcurrentActivityExecutionSize(storeFrontProperties.allowedConcurrency())
                .build();
        var worker = workerFactory.newWorker(TaskQueues.ORDER, options);
        worker.registerActivitiesImplementations(storeFrontActivities);
    }

    @ConfigurationProperties("storefront")
    record StoreFrontProperties(int storeFrontRateLimitPerSec, int instanceDeployed) {
        int allowedConcurrency() {
            return storeFrontRateLimitPerSec / instanceDeployed;
        }
    }
}
