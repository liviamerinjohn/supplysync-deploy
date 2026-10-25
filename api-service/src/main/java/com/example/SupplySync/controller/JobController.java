package com.example.SupplySync.controller;

import com.example.SupplySync.dto.JobDetailDto;
import com.example.SupplySync.dto.JobSummaryDto;
import com.example.SupplySync.entity.Job;
import com.example.SupplySync.service.JobQueryService;
import com.example.SupplySync.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
public class JobController {

    private final JobService jobService;
    private final JobQueryService jobQueryService;

    public JobController(JobService jobService, JobQueryService jobQueryService) {
        this.jobService = jobService;
        this.jobQueryService = jobQueryService;
    }

    @PostMapping("/api/jobs")
    public ResponseEntity<JobSummaryDto> createJob(@RequestParam("files") List<MultipartFile> files)
            throws IOException {
        Job job = jobService.createJob(files);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobQueryService.getSummary(job));
    }

    @GetMapping("/api/jobs")
    public List<JobSummaryDto> listJobs() {
        return jobQueryService.listJobs();
    }

    @GetMapping("/api/jobs/{jobId}")
    public JobDetailDto getJob(@PathVariable UUID jobId) {
        return jobQueryService.getJob(jobId);
    }
}