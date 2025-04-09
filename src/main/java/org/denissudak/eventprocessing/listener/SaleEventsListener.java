package org.denissudak.eventprocessing.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.denissudak.eventprocessing.model.SaleEvent;
import org.denissudak.eventprocessing.services.SaleEventProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode.ON_SUCCESS;


@Service
public class SaleEventsListener {

    private final SaleEventProcessor saleEventProcessor;

    @Autowired
    public SaleEventsListener(SaleEventProcessor saleEventProcessor) {
        this.saleEventProcessor = saleEventProcessor;
    }

    @SqsListener(value = "salesQueue", acknowledgementMode = ON_SUCCESS)
    public void process(SaleEvent event) {
        saleEventProcessor. process(event);
    }
}
