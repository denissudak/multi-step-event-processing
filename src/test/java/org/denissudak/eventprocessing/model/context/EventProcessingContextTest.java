package org.denissudak.eventprocessing.model.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EventProcessingContextTest {

    @Test
    public void shouldGetStepProcessingStatuses() {
        // given
        EventProcessingContext context = new EventProcessingContext();

        // when and then
        assertThat(context.getStepProcessingStatuses()).isNotNull().isEmpty();

    }

    @Test
    public void shouldAddStepProcessingStatus() {
        // given
        EventProcessingContext context = new EventProcessingContext();
        StepProcessingStatus status1 = mock(StepProcessingStatus.class);

        // when
        context.addStepProcessingStatus(status1);

        // then
        assertThat(context.getStepProcessingStatuses()).hasSize(1).contains(status1);

    }
}
