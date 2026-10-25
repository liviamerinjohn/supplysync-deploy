package com.example.SupplySync.service;

import com.example.SupplySync.cache.JobDetailCache;
import com.example.SupplySync.dto.JobDetailDto;
import com.example.SupplySync.dto.JobSummaryDto;
import com.example.SupplySync.entity.Job;
import com.example.SupplySync.exception.JobNotFoundException;
import com.example.SupplySync.repository.DocumentRepository;
import com.example.SupplySync.repository.JobRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobQueryService {

    private final JobRepository jobRepository;
    private final DocumentRepository documentRepository;
    private final JobDetailCache cache;

    public JobQueryService(JobRepository jobRepository,
                           DocumentRepository documentRepository,
                           JobDetailCache cache) {
        this.jobRepository = jobRepository;
        this.documentRepository = documentRepository;
        this.cache = cache;
    }

    public List<JobSummaryDto> listJobs() {
        Sort newestFirst = Sort.by(Sort.Order.desc("createdAt").nullsLast());
        return jobRepository.findAll(newestFirst).stream()
                .map(job -> JobMapper.toSummary(job, documentRepository.findByJob(job)))
                .toList();
    }

    public JobDetailDto getJob(UUID jobId) {
        Optional<JobDetailDto> cached = cache.get(jobId);          // 1. ask the cache
        if (cached.isPresent()) {
            return cached.get();
        }

        Job job = jobRepository.findById(jobId)                    // 2. miss: ask the database
                .orElseThrow(() -> new JobNotFoundException("Job not found: " + jobId));
        JobDetailDto dto = JobMapper.toDetail(job, documentRepository.findByJob(job));

        cache.put(dto);                                            // 3. remember it
        return dto;
    }

    public JobSummaryDto getSummary(Job job) {
        return JobMapper.toSummary(job, documentRepository.findByJob(job));
    }
}