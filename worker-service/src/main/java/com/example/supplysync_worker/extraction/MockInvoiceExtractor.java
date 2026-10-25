package com.example.supplysync_worker.extraction;

import com.example.supplysync_worker.dto.InvoiceExtractionResult;
import com.example.supplysync_worker.entity.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
@Service
@ConditionalOnProperty(name="supplysync.extractor",havingValue="mock",matchIfMissing=true)
public class MockInvoiceExtractor implements InvoiceExtractor {
    @Override
    public InvoiceExtractionResult extract(Document document) {

        // Simulate a slow extraction call
        try {

            // Simulate a file-reading failure
            if (document.getFilePath() != null
                    && document.getFilePath().contains("FAIL")) {
                throw new IllegalStateException("Could not read invoice");
            }

            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted", e);
        }



        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Generate a random invoice number
        String invoiceNumber = "INV-" + random.nextInt(1000, 10000);

        // Select a random vendor
        List<String> vendors = List.of(
                "Acme Supplies",
                "Global Office Products",
                "Kerala Wholesale Traders",
                "Prime Industrial Goods"
        );

        String vendor = vendors.get(random.nextInt(vendors.size()));

        // Generate a random amount with two decimal places
        double amount = random.nextDouble(100.0, 100000.0);

        BigDecimal totalAmount = BigDecimal.valueOf(amount)
                .setScale(2, RoundingMode.HALF_UP);

        // Use today's date
        LocalDate invoiceDate = LocalDate.now();

        // Return the generated result
        return new InvoiceExtractionResult(
                invoiceNumber,
                vendor,
                totalAmount,
                invoiceDate
        );
    }
}
