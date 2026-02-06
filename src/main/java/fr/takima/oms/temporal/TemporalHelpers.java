package fr.takima.oms.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import lombok.experimental.UtilityClass;

import java.time.Duration;

@UtilityClass
public class TemporalHelpers {
    public static <T> T simpleActivityStub(Class<T> clazz, String taskQueue) {
        var options = ActivityOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setStartToCloseTimeout(Duration.ofSeconds(15))
                // COuld set retry options eventually
                .build();
        return Workflow.newActivityStub(clazz, options);
    }
}
