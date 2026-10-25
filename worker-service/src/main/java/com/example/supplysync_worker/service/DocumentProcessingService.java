package com.example.supplysync_worker.service;

import com.example.supplysync_worker.cache.JobCacheEvictor;
import com.example.supplysync_worker.dto.InvoiceExtractionResult;
import com.example.supplysync_worker.entity.Document;
import com.example.supplysync_worker.extraction.InvoiceExtractor;
import com.example.supplysync_worker.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final InvoiceExtractor extractor;
    private final JobCacheEvictor cacheEvictor;

    public DocumentProcessingService(DocumentRepository documentRepository,
                                     InvoiceExtractor extractor,
                                     JobCacheEvictor cacheEvictor) {
        this.documentRepository = documentRepository;
        this.extractor = extractor;
        this.cacheEvictor = cacheEvictor;
    }

    public void processDocument(Document document) {
        String fileName = Path.of(document.getFilePath()).getFileName().toString();
        System.out.println("START " + document.getDocId() + " (" + fileName + ") on "
                + Thread.currentThread().getName());

        document.setDocStatus("PROCESSING");
        saveAndEvict(document);

        try {
            InvoiceExtractionResult result = extractor.extract(document);

            document.setInvoiceNumber(result.invoiceNumber());
            document.setVendor(result.vendor());
            document.setTotalAmount(result.totalAmount());
            document.setInvoiceDate(result.invoiceDate());

            document.setDocStatus("SUCCESS");
            saveAndEvict(document);

            System.out.println("END   " + document.getDocId() + " (" + fileName + ") on "
                    + Thread.currentThread().getName());
        } catch (Exception e) {
            markFailed(document, fileName, e.getMessage());
        }
    }

    private void markFailed(Document document, String fileName, String reason) {
        document.setDocStatus("FAILED");
        saveAndEvict(document);
        System.out.println("FAILED " + document.getDocId() + " (" + fileName + "): " + reason);
    }

    // Database first, then the cache. Otherwise a reader could re-cache the old value.
    private void saveAndEvict(Document document) {
        documentRepository.save(document);
        cacheEvictor.evict(document.getJob().getJobId());
    }
}