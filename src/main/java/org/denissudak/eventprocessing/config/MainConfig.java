package org.denissudak.eventprocessing.config;

import org.denissudak.eventprocessing.model.SaleEvent;
import org.denissudak.eventprocessing.model.context.EventProcessingContext;
import org.denissudak.eventprocessing.model.context.StepProcessingStatus;
import org.denissudak.eventprocessing.services.ProcessingContextDAO;
import org.denissudak.eventprocessing.services.ProcessingContextService;
import org.denissudak.eventprocessing.services.ProcessingStep;
import org.denissudak.eventprocessing.services.SaleEventProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;

@Configuration
public class MainConfig {
    @Bean
    public SaleEventProcessor saleEventProcessor(
            ProcessingContextService processingContextService,
            @Qualifier("createNewInvoice") ProcessingStep createNewInvoice,
            @Qualifier("emailInvoice") ProcessingStep emailInvoice,
            Function<String, StepProcessingStatus> notStartedStepProcessingStatusFactory
    ) {
        return new SaleEventProcessor(
                newArrayList(
                        createNewInvoice,
                        emailInvoice
                ),
                processingContextService,
                notStartedStepProcessingStatusFactory
        );
    }

    @Bean
    public ProcessingStep createNewInvoice() {
        return new ProcessingStep() {
            @Override
            public String getCode() {
                return "create-new-invoice";
            }

            @Override
            public boolean process(SaleEvent event, EventProcessingContext state) {
                UUID newInvoiceId = UUID.randomUUID();
                System.out.println(getCode()+" step has created a new invoice with ID " + newInvoiceId);
                state.setCreatedInvoiceId(newInvoiceId.toString());
                return true;
            }
        };
    }

    @Bean
    public ProcessingStep emailInvoice() {
        return new ProcessingStep() {
            @Override
            public String getCode() {
                return "email-invoice";
            }

            @Override
            public boolean process(SaleEvent event, EventProcessingContext state) {
                System.out.println(getCode()+" step has emailed invoice with ID " + state.getCreatedInvoiceId());
                return true;
            }
        };
    }

    @Bean
    public ProcessingContextService processingContextService(
            ProcessingContextDAO processingContextDAO,
            Function<SaleEvent, EventProcessingContext> processingContextFactory) {
        return new ProcessingContextService(
                SaleEvent::getInvoiceNumber,
                processingContextFactory,
                processingContextDAO
        );
    }

    @Bean
    public Function<SaleEvent, EventProcessingContext> processingContextFactory() {
        return event -> {
            EventProcessingContext newStatus = new EventProcessingContext();
            newStatus.setEventId(event.getInvoiceNumber());

            return newStatus;
        };
    }

    @Bean
    public Function<String, StepProcessingStatus> notStartedStepProcessingStatusFactory() {
        return commandCode -> {
            StepProcessingStatus status = new StepProcessingStatus();
            status.setStepCode(commandCode);
            status.setStatus(StepProcessingStatus.Status.NOT_STARTED);
            return status;
        };
    }
}
