package org.denissudak.eventprocessing.services;

import lombok.RequiredArgsConstructor;
import org.denissudak.eventprocessing.model.SaleEvent;
import org.denissudak.eventprocessing.model.context.EventProcessingContext;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


@RequiredArgsConstructor
public class ProcessingContextService {
    private final Function<SaleEvent, String> eventToProcessingContextIdMapper;
    private final Function<SaleEvent, EventProcessingContext> processingContextFactory;
    private final ProcessingContextDAO processingContextDAO;

    public EventProcessingContext lockEventProcessing(SaleEvent event) {
        checkNotNull(event);

        String id = eventToProcessingContextIdMapper.apply(event);
        Optional<EventProcessingContext> optionalProcessingContext = processingContextDAO.fetchByEventId(id);

        EventProcessingContext processingContext;
        if (optionalProcessingContext.isPresent()) {
            processingContext = optionalProcessingContext.get();
        } else {
            processingContext = processingContextFactory.apply(event);
            processingContextDAO.saveNew(processingContext);
        }
        if (processingContext.isLocked()) {
            throw new IllegalStateException(String.format("Processing state for ID %s is locked", id));
        }
        processingContextDAO.acquireLock(processingContext);

        return processingContext;
    }

    public void releaseProcessingLock(EventProcessingContext state) {
        checkNotNull(state);
        checkState(state.isLocked(), "Processing state is not locked");

        processingContextDAO.releaseLock(state);
    }

    public void update(EventProcessingContext processingState) {
        checkNotNull(processingState);

        processingContextDAO.update(processingState);
    }
}
