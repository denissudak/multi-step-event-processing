package org.denissudak.eventprocessing.model.context;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;


@Data
@DynamoDbBean
public class EventProcessingContext {
    private String eventId;
    private boolean locked;

    private String createdInvoiceId;

    private List<StepProcessingStatus> stepProcessingStatuses;

    public static final String KEY_ATTRIBUTE_NAME = "eventId";
    public static final String LOCKED_ATTRIBUTE_NAME = "locked";

    public void addStepProcessingStatus(StepProcessingStatus stepProcessingStatus) {
        checkNotNull(stepProcessingStatus);

        if (stepProcessingStatuses == null) {
            stepProcessingStatuses = newLinkedList();
        }

        stepProcessingStatuses.add(stepProcessingStatus);
    }

    public List<StepProcessingStatus> getStepProcessingStatuses() {
        if (stepProcessingStatuses == null) {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(stepProcessingStatuses);
    }

    @DynamoDbPartitionKey
    public String getEventId() {
        return eventId;
    }
}
