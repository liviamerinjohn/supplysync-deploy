package com.example.supplysync_worker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceExtractionResult (
        String invoiceNumber,
        String vendor,
        BigDecimal totalAmount,
        LocalDate invoiceDate)
{


}
