package org.denissudak.eventprocessing.services;

import org.denissudak.eventprocessing.model.SaleEvent;
import org.denissudak.eventprocessing.model.context.EventProcessingContext;
import org.denissudak.eventprocessing.model.context.StepProcessingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaleEventProcessorTest {

    @Mock
    private EventProcessingContext eventProcessingState;

    @Mock
    private Function<String, StepProcessingStatus> notStartedStepProcessingStatusFactory;

    @Mock
    private ProcessingContextService processingContextService;

    private SaleEventProcessor processor;

    @Mock
    private SaleEvent saleEvent;

    @Mock
    private ProcessingStep processingStep1, processingStep2;

    private final String step1Code = "step1", step2Code = "step2";

    @Mock
    private StepProcessingStatus step1Status, step2Status;

    @BeforeEach
    public void setUp() {
        processor = new SaleEventProcessor(
                newArrayList(processingStep1, processingStep2),
                processingContextService,
                notStartedStepProcessingStatusFactory
        );

        lenient().when(processingStep1.getCode()).thenReturn(step1Code);
        lenient().when(processingStep2.getCode()).thenReturn(step2Code);
        lenient().when(step1Status.getStepCode()).thenReturn(step1Code);
        lenient().when(step2Status.getStepCode()).thenReturn(step2Code);
    }

    @Test
    public void shouldSuccessfullyProcessEvent() {
        // given
        when(processingContextService.lockEventProcessing(any())).thenReturn(eventProcessingState);
        when(processingStep1.process(saleEvent, eventProcessingState)).thenReturn(true);
        when(processingStep2.process(saleEvent, eventProcessingState)).thenReturn(true);
        when(notStartedStepProcessingStatusFactory.apply(step1Code)).thenReturn(step1Status);
        when(notStartedStepProcessingStatusFactory.apply(step2Code)).thenReturn(step2Status);

        // when
        processor.process(saleEvent);

        // then
        verify(processingContextService).lockEventProcessing(saleEvent);

        // and
        InOrder inOrder = Mockito.inOrder(step1Status, processingContextService, eventProcessingState, processingStep1);
        inOrder.verify(eventProcessingState).addStepProcessingStatus(step1Status); // new step
        inOrder.verify(step1Status).setStatus(StepProcessingStatus.Status.PROCESSING);
        inOrder.verify(processingContextService).update(eventProcessingState);
        inOrder.verify(processingStep1).process(saleEvent, eventProcessingState); // actual processing
        inOrder.verify(step1Status).setStatus(StepProcessingStatus.Status.SUCCESS);
        inOrder.verify(processingContextService).update(eventProcessingState);

        // and the same for the second step
        inOrder = Mockito.inOrder(step2Status, processingContextService, eventProcessingState, processingStep2);
        inOrder.verify(eventProcessingState).addStepProcessingStatus(step2Status); // new step
        inOrder.verify(processingContextService).update(eventProcessingState);
        inOrder.verify(processingStep2).process(saleEvent, eventProcessingState);
        inOrder.verify(step2Status).setStatus(StepProcessingStatus.Status.SUCCESS);
        inOrder.verify(processingContextService).update(eventProcessingState);

        // and finally
        verify(processingContextService).releaseProcessingLock(eventProcessingState);
    }


    @Test
    public void shouldSuccessfullyRetry() {
        // given
        when(processingContextService.lockEventProcessing(any())).thenReturn(eventProcessingState);
        when(eventProcessingState.getStepProcessingStatuses()).thenReturn(newArrayList(step1Status, step2Status));
        when(step1Status.getStepCode()).thenReturn(step1Code);
        when(step1Status.getStatus()).thenReturn(StepProcessingStatus.Status.SUCCESS);
        when(step2Status.getStepCode()).thenReturn(step2Code);
        when(step2Status.getStatus()).thenReturn(StepProcessingStatus.Status.TRY_AGAIN); // failed the last time
        when(processingStep2.process(saleEvent, eventProcessingState)).thenReturn(true); // but will succeed this time

        // when
        processor.process(saleEvent);

        // then
        verify(processingContextService).lockEventProcessing(saleEvent);

        // and skip to the second step
        InOrder inOrder = Mockito.inOrder(step2Status, processingContextService, eventProcessingState, processingStep2);
        inOrder.verify(step2Status).setStatus(StepProcessingStatus.Status.PROCESSING);
        inOrder.verify(processingContextService).update(eventProcessingState);
        inOrder.verify(processingStep2).process(saleEvent, eventProcessingState); // actual processing
        inOrder.verify(step2Status).setStatus(StepProcessingStatus.Status.SUCCESS);
        inOrder.verify(processingContextService).update(eventProcessingState);

        // and let's make sure the first step was never called
        verify(processingStep1, never()).process(any(), any());

        // and finally
        verify(processingContextService).releaseProcessingLock(eventProcessingState);
    }

    @Test
    public void shouldRecordStepFailure() {
        // given
        when(processingStep1.process(saleEvent, eventProcessingState)).thenReturn(false); // will fail
        when(notStartedStepProcessingStatusFactory.apply(step1Code)).thenReturn(step1Status);
        when(processingContextService.lockEventProcessing(any())).thenReturn(eventProcessingState);

        // when
        assertThatThrownBy(() -> processor.process(saleEvent)).isInstanceOf(RuntimeException.class);

        // then
        verify(processingContextService).lockEventProcessing(saleEvent);

        // and
        InOrder inOrder = Mockito.inOrder(step1Status, processingContextService, eventProcessingState, processingStep1);
        inOrder.verify(step1Status).setStatus(StepProcessingStatus.Status.PROCESSING);
        inOrder.verify(processingContextService).update(eventProcessingState);
        inOrder.verify(processingStep1).process(saleEvent, eventProcessingState); // attempting to process the event
        inOrder.verify(step1Status).setStatus(StepProcessingStatus.Status.TRY_AGAIN);
        inOrder.verify(processingContextService).update(eventProcessingState);

        // and finally
        verify(processingContextService).releaseProcessingLock(eventProcessingState);
    }
}
