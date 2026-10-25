package com.example.SupplySync.dto;

import java.time.Instant;
import java.util.UUID;

public record JobSummaryDto(
        UUID jobId,
        JobStatus status,
        Instant createdAt,
        long total,
        long succeeded,
        long failed,
        long inProgress) {
}