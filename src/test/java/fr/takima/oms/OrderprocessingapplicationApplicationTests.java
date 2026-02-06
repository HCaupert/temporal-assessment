package fr.takima.oms;

import fr.takima.oms.order.api.OrderProcessingWorkflow;
import fr.takima.oms.order.internal.OrderActivities;
import fr.takima.oms.order.internal.OrderProcessingWorkflowImpl;
import fr.takima.oms.payment.api.PublicPaymentActivities;
import fr.takima.oms.pishofy.api.StoreFrontActivities;
import fr.takima.oms.temporal.TaskQueues;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderprocessingapplicationApplicationTests {


    @Mock
    PublicPaymentActivities paymentActivities;
    @Mock
    StoreFrontActivities storeFrontActivities;
    @Mock
    OrderActivities orderActivities;

    @RegisterExtension
    public static final TestWorkflowExtension testWorkflowExtension =
            TestWorkflowExtension.newBuilder()
                    .registerWorkflowImplementationTypes(OrderProcessingWorkflowImpl.class)
                    .setDoNotStart(true)
                    .build();


    @BeforeEach
    void setup(TestWorkflowEnvironment testEnv, Worker worker) {
        worker.registerActivitiesImplementations(paymentActivities);

        testEnv.newWorker(TaskQueues.ORDER)
                .registerActivitiesImplementations(orderActivities);
        testEnv.newWorker(TaskQueues.STORE_FRONT)
                .registerActivitiesImplementations(storeFrontActivities);
        testEnv.start();
    }


    @Test
    void contextLoads(OrderProcessingWorkflow workflow) {
        //workflow.processOrder();
    }

}
