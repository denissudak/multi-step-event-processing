package org.denissudak.eventprocessing.services;

import org.denissudak.eventprocessing.model.SaleEvent;
import org.denissudak.eventprocessing.model.context.EventProcessingContext;

public interface ProcessingStep {

  String getCode();

  boolean process(SaleEvent event, EventProcessingContext state);

}
