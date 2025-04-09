package org.denissudak.eventprocessing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleEvent {
    String invoiceNumber;
    String clientBusinessName;
    BigDecimal totalDue;
}
