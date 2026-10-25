package com.example.SupplySync.service;

import com.example.SupplySync.dto.DocumentDto;
import com.example.SupplySync.dto.JobDetailDto;
import com.example.SupplySync.dto.JobStatus;
import com.example.SupplySync.dto.JobSummaryDto;
import com.example.SupplySync.entity.Document;
import com.example.SupplySync.entity.Job;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobMapperTest {

    // ---------- the file name shown to clients ----------

    @Test
    void fileName_hidesFolderAndUuidPrefix() {
        Document document = document("C:/uploads/1234_D1.png", "SUCCESS");          // Arrange

        DocumentDto dto = JobMapper.toDocumentDto(document);                          // Act

        assertThat(dto.fileName()).isEqualTo("D1.png");                               // Assert
    }

    @Test
    void fileName_keepsUnderscoresThatBelongToTheOriginalName() {
        Document document = document("C:/uploads/1234_my_invoice_final.png", "SUCCESS");

        assertThat(JobMapper.toDocumentDto(document).fileName())
                .isEqualTo("my_invoice_final.png");        // only the FIRST underscore is a separator
    }

    @Test
    void fileName_withoutPrefix_isReturnedAsIs() {
        Document document = document("C:/uploads/plain.png", "SUCCESS");

        assertThat(JobMapper.toDocumentDto(document).fileName()).isEqualTo("plain.png");
    }

    // ---------- fields copied from the entity ----------

    @Test
    void documentDto_copiesIdStatusAndExtractedFields() {
        UUID id = UUID.randomUUID();
        Document document = document("C:/uploads/1_a.png", "SUCCESS");
        document.setDocId(id);
        document.setInvoiceNumber("INV-1");
        document.setVendor("ACME");
        document.setTotalAmount(new BigDecimal("6938.40"));
        document.setInvoiceDate(LocalDate.of(2024, 3, 15));

        DocumentDto dto = JobMapper.toDocumentDto(document);

        assertThat(dto.documentId()).isEqualTo(id);
        assertThat(dto.status()).isEqualTo("SUCCESS");
        assertThat(dto.invoiceNumber()).isEqualTo("INV-1");
        assertThat(dto.vendor()).isEqualTo("ACME");
        assertThat(dto.totalAmount()).isEqualByComparingTo("6938.40");
        assertThat(dto.invoiceDate()).isEqualTo(LocalDate.of(2024, 3, 15));
    }

    // ---------- the counts ----------

    @Test
    void summary_countsDocumentsByStatus() {
        Job job = job();
        List<Document> docs = List.of(
                document("C:/u/1_a.png", "PENDING"),
                document("C:/u/2_b.png", "PENDING"),
                document("C:/u/3_c.png", "PROCESSING"),
                document("C:/u/4_d.png", "SUCCESS"));

        JobSummaryDto summary = JobMapper.toSummary(job, docs);

        assertThat(summary.jobId()).isEqualTo(job.getJobId());
        assertThat(summary.total()).isEqualTo(4);
        assertThat(summary.succeeded()).isEqualTo(1);
        assertThat(summary.failed()).isZero();
        assertThat(summary.inProgress()).isEqualTo(3);          // 2 pending + 1 processing
        assertThat(summary.status()).isEqualTo(JobStatus.PROCESSING);
    }

    @Test
    void summary_ofFinishedJobWithOneFailure_isPartiallyCompleted() {
        List<Document> docs = List.of(
                document("C:/u/1_a.png", "SUCCESS"),
                document("C:/u/2_b.png", "SUCCESS"),
                document("C:/u/3_c.png", "FAILED"));

        JobSummaryDto summary = JobMapper.toSummary(job(), docs);

        assertThat(summary.succeeded()).isEqualTo(2);
        assertThat(summary.failed()).isEqualTo(1);
        assertThat(summary.inProgress()).isZero();
        assertThat(summary.status()).isEqualTo(JobStatus.PARTIALLY_COMPLETED);
    }

    // ---------- the detail view ----------

    @Test
    void detail_listsEveryDocumentWithItsFileName() {
        List<Document> docs = List.of(
                document("C:/u/1_a.png", "SUCCESS"),
                document("C:/u/2_b.png", "FAILED"));

        JobDetailDto detail = JobMapper.toDetail(job(), docs);

        assertThat(detail.documents())
                .extracting(DocumentDto::fileName)
                .containsExactly("a.png", "b.png");
    }

    // ---------- helpers: build test data without a database ----------

    private static Document document(String path, String status) {
        Document document = new Document();
        document.setFilePath(path);
        document.setDocStatus(status);
        return document;
    }

    private static Job job() {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        return job;
    }
}