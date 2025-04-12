package org.denissudak.eventprocessing.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.denissudak.eventprocessing.model.SaleEvent;
import org.denissudak.eventprocessing.services.SaleEventProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode.ON_SUCCESS;
import static java.util.Objects.requireNonNull;


@Service
public class SaleEventsListener {

    private final SaleEventProcessor saleEventProcessor;

    @Autowired
    public SaleEventsListener(SaleEventProcessor saleEventProcessor) {
        this.saleEventProcessor = requireNonNull(saleEventProcessor);
    }

    @SqsListener(value = "sales", acknowledgementMode = ON_SUCCESS)
    public void process(SaleEvent event) {
        saleEventProcessor.process(event);
    }
}
