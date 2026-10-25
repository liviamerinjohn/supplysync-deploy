package com.example.supplysync_worker.extraction;

import com.example.supplysync_worker.dto.InvoiceExtractionResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceAnswerParserTest {

    @Test
    void cleanJson_isParsed() {
        String raw = """
                {"invoiceNumber":"INV-1001","vendor":"Acme Supplies",
                 "totalAmount":6938.40,"invoiceDate":"2024-03-15"}
                """;

        InvoiceExtractionResult result = InvoiceAnswerParser.parse(raw);

        assertThat(result.invoiceNumber()).isEqualTo("INV-1001");
        assertThat(result.vendor()).isEqualTo("Acme Supplies");
        assertThat(result.totalAmount()).isEqualByComparingTo("6938.40");
        assertThat(result.invoiceDate()).isEqualTo(LocalDate.of(2024, 3, 15));
    }

    @Test
    void markdownFence_isIgnored() {
        String raw = """
```json
                {"invoiceNumber":"INV-1","vendor":"Acme","totalAmount":10.5,"invoiceDate":"2024-03-15"}
```
                """;

        assertThat(InvoiceAnswerParser.parse(raw).totalAmount()).isEqualByComparingTo("10.50");
    }

    @Test
    void currencyTextAndDayMonthYearDate_areNormalised() {
        String raw = """
                {"invoiceNumber":"INV-1","vendor":"Acme",
                 "totalAmount":"₹6,938.40","invoiceDate":"15-Mar-2024"}
                """;

        InvoiceExtractionResult result = InvoiceAnswerParser.parse(raw);

        assertThat(result.totalAmount()).isEqualByComparingTo("6938.40");
        assertThat(result.invoiceDate()).isEqualTo(LocalDate.of(2024, 3, 15));
    }

    @Test
    void noJson_isRejected() {
        assertThatThrownBy(() -> InvoiceAnswerParser.parse("Sorry, I can't read this image"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no JSON object");
    }

    @Test
    void nullAnswer_isRejected() {
        assertThatThrownBy(() -> InvoiceAnswerParser.parse(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nullVendor_isRejected() {
        String raw = """
                {"invoiceNumber":"INV-1","vendor":null,"totalAmount":10,"invoiceDate":"2024-03-15"}
                """;

        assertThatThrownBy(() -> InvoiceAnswerParser.parse(raw))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vendor is missing");
    }

    @Test
    void missingAmount_isRejected() {
        String raw = """
                {"invoiceNumber":"INV-1","vendor":"Acme","invoiceDate":"2024-03-15"}
                """;

        assertThatThrownBy(() -> InvoiceAnswerParser.parse(raw))
                .hasMessageContaining("totalAmount is missing");
    }

    @Test
    void zeroAmount_isRejected() {
        String raw = """
                {"invoiceNumber":"INV-1","vendor":"Acme","totalAmount":0,"invoiceDate":"2024-03-15"}
                """;

        assertThatThrownBy(() -> InvoiceAnswerParser.parse(raw))
                .hasMessageContaining("greater than zero");
    }

    @Test
    void futureDate_isRejected() {
        String raw = """
                {"invoiceNumber":"INV-1","vendor":"Acme","totalAmount":10,"invoiceDate":"2999-01-01"}
                """;

        assertThatThrownBy(() -> InvoiceAnswerParser.parse(raw))
                .hasMessageContaining("cannot be in the future");
    }
}