package com.example.SupplySync.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DocumentDto(
        UUID documentId,
        String fileName,
        String status,
        String invoiceNumber,
        String vendor,
        BigDecimal totalAmount,
        LocalDate invoiceDate) {
}