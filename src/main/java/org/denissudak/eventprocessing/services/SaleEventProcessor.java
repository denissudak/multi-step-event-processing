package org.denissudak.eventprocessing.services;

import lombok.RequiredArgsConstructor;
import org.denissudak.eventprocessing.model.SaleEvent;
import org.denissudak.eventprocessing.model.context.EventProcessingContext;
import org.denissudak.eventprocessing.model.context.StepProcessingStatus;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;


@RequiredArgsConstructor
public class SaleEventProcessor {
    private final List<ProcessingStep> processingSteps;
    private final ProcessingContextService processingContextService;
    private final Function<String, StepProcessingStatus> notStartedStepProcessingStatusFactory;

    public void process(SaleEvent event) {
        checkNotNull(event);

        EventProcessingContext processingContext = processingContextService.lockEventProcessing(event);
        Iterator<StepProcessingStatus> attemptedStepsIterator = processingContext.getStepProcessingStatuses().iterator();
        Iterator<ProcessingStep> processingStepIterator = processingSteps.iterator();
        boolean stepFailed = false;
        while (processingStepIterator.hasNext() && !stepFailed) {
            ProcessingStep currentStep = processingStepIterator.next();
            StepProcessingStatus status;
            if (attemptedStepsIterator.hasNext()) {
                status = attemptedStepsIterator.next();
            } else {
                status = notStartedStepProcessingStatusFactory.apply(currentStep.getCode());
                processingContext.addStepProcessingStatus(status);
            }
            if (!currentStep.getCode().equals(status.getStepCode())) {
                throw new IllegalStateException("The step is " + currentStep.getCode() + ", but the previous attempt record is for step " + status.getStepCode());
            }
            if (status.getStatus() == StepProcessingStatus.Status.PROCESSING) {
                throw new IllegalStateException("command is already in the " + StepProcessingStatus.Status.PROCESSING + " state");
            } else if (status.getStatus() != StepProcessingStatus.Status.SUCCESS) {
                status.setStatus(StepProcessingStatus.Status.PROCESSING);
                processingContextService.update(processingContext);
                stepFailed = !currentStep.process(event, processingContext);
                status.setStatus(stepFailed ? StepProcessingStatus.Status.TRY_AGAIN : StepProcessingStatus.Status.SUCCESS);
                processingContextService.update(processingContext);
            }
        }

        processingContextService.releaseProcessingLock(processingContext);

        if (stepFailed) {
            throw new RuntimeException("Failed to process the event");
        }
    }
}
