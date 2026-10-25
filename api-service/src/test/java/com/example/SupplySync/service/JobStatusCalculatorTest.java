package com.example.SupplySync.service;

import com.example.SupplySync.dto.JobStatus;
import com.example.SupplySync.entity.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobStatusCalculatorTest {

    @Test
    void emptyList_isQueued() {
        assertThat(JobStatusCalculator.calculate(List.of())).isEqualTo(JobStatus.QUEUED);
    }

    @Test
    void allPending_isQueued() {
        assertThat(JobStatusCalculator.calculate(docs("PENDING", "PENDING", "PENDING")))
                .isEqualTo(JobStatus.QUEUED);
    }

    @Test
    void successAndPending_isProcessing() {
        assertThat(JobStatusCalculator.calculate(docs("SUCCESS", "PENDING", "PENDING")))
                .isEqualTo(JobStatus.PROCESSING);
    }

    @Test
    void anyProcessing_isProcessing() {
        assertThat(JobStatusCalculator.calculate(docs("PROCESSING", "SUCCESS")))
                .isEqualTo(JobStatus.PROCESSING);
    }

    @Test
    void allSuccess_isCompleted() {
        assertThat(JobStatusCalculator.calculate(docs("SUCCESS", "SUCCESS", "SUCCESS")))
                .isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    void allFailed_isFailed() {
        assertThat(JobStatusCalculator.calculate(docs("FAILED", "FAILED", "FAILED")))
                .isEqualTo(JobStatus.FAILED);
    }

    @Test
    void successAndFailed_isPartiallyCompleted() {
        assertThat(JobStatusCalculator.calculate(docs("SUCCESS", "SUCCESS", "FAILED")))
                .isEqualTo(JobStatus.PARTIALLY_COMPLETED);
    }

    private static List<Document> docs(String... statuses) {
        List<Document> list = new ArrayList<>();
        for (String status : statuses) {
            Document d = new Document();
            d.setDocStatus(status);
            list.add(d);
        }
        return list;
    }
}