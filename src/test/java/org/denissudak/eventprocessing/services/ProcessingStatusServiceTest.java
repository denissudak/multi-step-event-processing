package org.denissudak.eventprocessing.services;

import org.denissudak.eventprocessing.model.SaleEvent;
import org.denissudak.eventprocessing.model.context.EventProcessingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessingStatusServiceTest {

    private ProcessingContextService processingStateProcessingContextService;

    @Mock
    private Function<SaleEvent, String> stateIdFactory;

    @Mock
    private Function<SaleEvent, EventProcessingContext> newStateFactory;

    @Mock
    private ProcessingContextDAO stateDAO;

    private final String stateId = "RepurchaseEvent#sale-order-id";
    private final SaleEvent repurchaseEvent = new SaleEvent();

    @BeforeEach
    public void setUp() {
        lenient().when(stateIdFactory.apply(repurchaseEvent)).thenReturn(stateId);

        processingStateProcessingContextService = new ProcessingContextService(stateIdFactory, newStateFactory, stateDAO);
    }

    @Test
    public void shouldAcquireProcessingLockForTheFirstTime() {
        // given
        EventProcessingContext newState = new EventProcessingContext();
        when(newStateFactory.apply(repurchaseEvent)).thenReturn(newState);

        // when
        processingStateProcessingContextService.lockEventProcessing(repurchaseEvent);

        // then
        InOrder inOrder = Mockito.inOrder(stateDAO);
        inOrder.verify(stateDAO).saveNew(newState);
        inOrder.verify(stateDAO).acquireLock(newState);
    }

    @Test
    public void shouldAcquireProcessingLockForTheSecondTime() {
        // given
        SaleEvent repurchaseEvent = new SaleEvent();

        when(stateIdFactory.apply(repurchaseEvent)).thenReturn(stateId);
        EventProcessingContext existingState = new EventProcessingContext();
        when(stateDAO.fetchByEventId(stateId)).thenReturn(Optional.of(existingState));

        // when
        processingStateProcessingContextService.lockEventProcessing(repurchaseEvent);

        // then
        verify(stateDAO).acquireLock(existingState);
    }

    @Test
    public void shouldThrowExceptionIfProcessingStateIsLocked() {
        // given
        EventProcessingContext existingState = new EventProcessingContext();
        existingState.setLocked(true);
        when(stateDAO.fetchByEventId(stateId)).thenReturn(Optional.of(existingState));

        // when and then
        assertThatThrownBy(() -> processingStateProcessingContextService.lockEventProcessing(repurchaseEvent)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldUpdateProcessingState() {
        // given
        EventProcessingContext existingState = new EventProcessingContext();

        // when
        processingStateProcessingContextService.update(existingState);

        // then
        verify(stateDAO).update(existingState);
    }

    @Test
    public void shouldReleaseProcessingLock() {
        // given
        EventProcessingContext existingState = new EventProcessingContext();
        existingState.setLocked(true);

        // when
        processingStateProcessingContextService.releaseProcessingLock(existingState);

        // then
        verify(stateDAO).releaseLock(existingState);
    }
}
