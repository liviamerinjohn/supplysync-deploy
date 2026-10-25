package com.example.SupplySync.service;

import com.example.SupplySync.dto.JobStatus;
import com.example.SupplySync.entity.Document;

import java.util.List;

public final class JobStatusCalculator {

    private JobStatusCalculator() { }

    public static JobStatus calculate(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return JobStatus.QUEUED;
        }

        long total = documents.size();
        long pending = countByStatus(documents, "PENDING");
        long processing = countByStatus(documents, "PROCESSING");
        long success = countByStatus(documents, "SUCCESS");
        long failed = countByStatus(documents, "FAILED");

        if (pending == total) {                       // nothing has started
            return JobStatus.QUEUED;
        }
        if (pending > 0 || processing > 0) {          // something is still unfinished
            return JobStatus.PROCESSING;
        }
        if (success == total) {                       // everything worked
            return JobStatus.COMPLETED;
        }
        if (failed == total) {                        // everything failed
            return JobStatus.FAILED;
        }
        return JobStatus.PARTIALLY_COMPLETED;         // a mix of success and failure
    }

    public static long countByStatus(List<Document> documents, String status) {
        return documents.stream()
                .filter(d -> status.equals(d.getDocStatus()))
                .count();
    }
}