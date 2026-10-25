package com.example.supplysync_worker.extraction;

import com.example.supplysync_worker.dto.InvoiceExtractionResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class InvoiceAnswerParser {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private InvoiceAnswerParser() {
        // Prevent instantiation
    }
    public static InvoiceExtractionResult parse(String raw) {
        try {
            String json = extractJsonObject(raw);
            JsonNode root = MAPPER.readTree(json);

            String invoiceNumber = textOrNull(root, "invoiceNumber");
            String vendor = textOrNull(root, "vendor");

            BigDecimal totalAmount = parseAmount(root.get("totalAmount"));
            String dateText = textOrNull(root, "invoiceDate");
            LocalDate invoiceDate = dateText == null
                    ? null
                    : parseDate(dateText);

            validate(invoiceNumber, vendor, totalAmount, invoiceDate);

            return new InvoiceExtractionResult(
                    invoiceNumber,
                    vendor,
                    totalAmount,
                    invoiceDate
            );

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Invalid extraction: could not parse model response", e);
        }
    }

    private static String extractJsonObject(String raw) {
        if (raw == null) {
            throw new IllegalStateException("Invalid extraction: empty model answer");
        }

        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');

        if (start < 0 || end <= start) {
            throw new IllegalStateException(
                    "Invalid extraction: no JSON object in model answer");
        }

        return raw.substring(start, end + 1);
    }

    private static String textOrNull(JsonNode root, String field) {
        JsonNode node = root.get(field);

        if (node == null || node.isNull()) {
            return null;
        }

        String text = node.asString().trim();

        return text.isBlank() ? null : text;
    }

    private static BigDecimal parseAmount(JsonNode node) {
        if (node == null || node.isNull()) {
            throw new IllegalStateException(
                    "Invalid extraction: totalAmount is missing");
        }

        String text = node.asString();
        String cleaned = text.replaceAll("[^0-9.]", "");

        if (cleaned.isBlank() || cleaned.equals(".")) {
            throw new IllegalStateException(
                    "Invalid extraction: totalAmount is not a valid number");
        }

        try {
            return new BigDecimal(cleaned)
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Invalid extraction: totalAmount is not a valid number", e);
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException firstException) {
            try {
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException secondException) {
                throw new IllegalStateException(
                        "Invalid extraction: invoiceDate must use yyyy-MM-dd "
                                + "or dd-MMM-yyyy format",
                        secondException);
            }
        }
    }

    private static void validate(
            String invoiceNumber,
            String vendor,
            BigDecimal totalAmount,
            LocalDate invoiceDate) {

        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalStateException(
                    "Invalid extraction: invoiceNumber is missing");
        }

        if (vendor == null || vendor.isBlank()) {
            throw new IllegalStateException(
                    "Invalid extraction: vendor is missing");
        }

        if (totalAmount == null
                || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Invalid extraction: totalAmount must be greater than zero");
        }

        BigDecimal maximum = new BigDecimal("99999999.99");

        if (totalAmount.compareTo(maximum) > 0) {
            throw new IllegalStateException(
                    "Invalid extraction: totalAmount exceeds 99999999.99");
        }

        if (invoiceDate == null) {
            throw new IllegalStateException(
                    "Invalid extraction: invoiceDate is missing");
        }

        if (invoiceDate.isAfter(LocalDate.now())) {
            throw new IllegalStateException(
                    "Invalid extraction: invoiceDate cannot be in the future");
        }

        if (invoiceDate.isBefore(LocalDate.of(2000, 1, 1))) {
            throw new IllegalStateException(
                    "Invalid extraction: invoiceDate cannot be before 2000");
        }
    }
}