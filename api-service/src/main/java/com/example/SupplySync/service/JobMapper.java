package com.example.SupplySync.service;

import com.example.SupplySync.dto.DocumentDto;
import com.example.SupplySync.dto.JobDetailDto;
import com.example.SupplySync.dto.JobSummaryDto;
import com.example.SupplySync.entity.Document;
import com.example.SupplySync.entity.Job;

import java.nio.file.Path;
import java.util.List;

public final class JobMapper {

    private JobMapper() { }

    public static JobSummaryDto toSummary(Job job, List<Document> docs) {
        return new JobSummaryDto(
                job.getJobId(),
                JobStatusCalculator.calculate(docs),
                job.getCreatedAt(),
                docs.size(),
                JobStatusCalculator.countByStatus(docs, "SUCCESS"),
                JobStatusCalculator.countByStatus(docs, "FAILED"),
                JobStatusCalculator.countByStatus(docs, "PENDING")
                        + JobStatusCalculator.countByStatus(docs, "PROCESSING"));
    }

    public static JobDetailDto toDetail(Job job, List<Document> docs) {
        return new JobDetailDto(
                job.getJobId(),
                JobStatusCalculator.calculate(docs),
                job.getCreatedAt(),
                docs.size(),
                JobStatusCalculator.countByStatus(docs, "SUCCESS"),
                JobStatusCalculator.countByStatus(docs, "FAILED"),
                JobStatusCalculator.countByStatus(docs, "PENDING")
                        + JobStatusCalculator.countByStatus(docs, "PROCESSING"),
                docs.stream().map(JobMapper::toDocumentDto).toList());
    }

    public static DocumentDto toDocumentDto(Document d) {
        return new DocumentDto(
                d.getDocId(),
                fileNameOf(d.getFilePath()),
                d.getDocStatus(),
                d.getInvoiceNumber(),
                d.getVendor(),
                d.getTotalAmount(),
                d.getInvoiceDate());
    }

    // Saved names look like "<uuid>_<originalName>"; a UUID never contains "_".
    private static String fileNameOf(String path) {
        String name = Path.of(path).getFileName().toString();
        int i = name.indexOf('_');
        return i >= 0 ? name.substring(i + 1) : name;
    }
}