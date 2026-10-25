package com.example.supplysync_worker.service;

import com.example.supplysync_worker.cache.JobCacheEvictor;
import com.example.supplysync_worker.dto.InvoiceExtractionResult;
import com.example.supplysync_worker.entity.Document;
import com.example.supplysync_worker.entity.Job;
import com.example.supplysync_worker.extraction.InvoiceExtractor;
import com.example.supplysync_worker.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentProcessingServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock InvoiceExtractor extractor;
    @Mock JobCacheEvictor cacheEvictor;

    @InjectMocks DocumentProcessingService service;

    @Test
    void successfulExtraction_storesFields_andMarksSuccess() {
        Document document = newDocument();
        when(extractor.extract(document)).thenReturn(
                new InvoiceExtractionResult("INV-1", "ACME", new BigDecimal("6938.40"), LocalDate.of(2024, 3, 15)));

        service.processDocument(document);

        assertThat(document.getDocStatus()).isEqualTo("SUCCESS");
        assertThat(document.getInvoiceNumber()).isEqualTo("INV-1");
        assertThat(document.getVendor()).isEqualTo("ACME");
        assertThat(document.getTotalAmount()).isEqualByComparingTo("6938.40");
        verify(documentRepository, times(2)).save(document);                         // PROCESSING, then SUCCESS
        verify(cacheEvictor, times(2)).evict(document.getJob().getJobId());          // cache cleared each time
    }

    @Test
    void failingExtraction_marksFailed_andDoesNotThrow() {
        Document document = newDocument();
        when(extractor.extract(document)).thenThrow(new IllegalStateException("boom"));

        service.processDocument(document);                                            // must not throw

        assertThat(document.getDocStatus()).isEqualTo("FAILED");
        verify(documentRepository, times(2)).save(document);                         // PROCESSING, then FAILED
    }

    private static Document newDocument() {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        Document document = new Document();
        document.setJob(job);
        document.setFilePath("C:/uploads/abc_D1.png");
        return document;
    }
}