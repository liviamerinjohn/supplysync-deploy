package com.example.supplysync_worker.extraction;

import com.example.supplysync_worker.dto.InvoiceExtractionResult;
import com.example.supplysync_worker.entity.Document;

public interface InvoiceExtractor {
    InvoiceExtractionResult extract(Document document);
}
