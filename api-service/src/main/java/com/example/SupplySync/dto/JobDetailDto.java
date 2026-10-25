package com.example.SupplySync.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JobDetailDto(
        UUID jobId,
        JobStatus status,
        Instant createdAt,
        long total,
        long succeeded,
        long failed,
        long inProgress,
        List<DocumentDto> documents) {
}