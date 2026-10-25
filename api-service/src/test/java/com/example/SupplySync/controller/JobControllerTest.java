package com.example.SupplySync.controller;

import com.example.SupplySync.dto.JobStatus;
import com.example.SupplySync.dto.JobSummaryDto;
import com.example.SupplySync.entity.Job;
import com.example.SupplySync.exception.JobNotFoundException;
import com.example.SupplySync.service.JobQueryService;
import com.example.SupplySync.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JobService jobService;
    @MockitoBean JobQueryService jobQueryService;

    @Test
    void listJobs_returnsSummaries() throws Exception {
        JobSummaryDto summary = new JobSummaryDto(
                UUID.randomUUID(), JobStatus.PROCESSING, Instant.parse("2026-10-19T21:42:24Z"), 4, 1, 0, 3);
        when(jobQueryService.listJobs()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PROCESSING"))
                .andExpect(jsonPath("$[0].total").value(4))
                .andExpect(jsonPath("$[0].inProgress").value(3));
    }

    @Test
    void unknownJob_returns404WithErrorMessage() throws Exception {
        UUID id = UUID.randomUUID();
        when(jobQueryService.getJob(id)).thenThrow(new JobNotFoundException("Job not found: " + id));

        mockMvc.perform(get("/api/jobs/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Job not found: " + id));
    }

    @Test
    void invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/jobs/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidUpload_returns400WithErrorMessage() throws Exception {
        when(jobService.createJob(any()))
                .thenThrow(new IllegalArgumentException("Unsupported file type: x.pdf (only PNG and JPG are allowed)"));
        MockMultipartFile pdf = new MockMultipartFile("files", "x.pdf", "application/pdf", new byte[]{1});

        mockMvc.perform(multipart("/api/jobs").file(pdf))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Unsupported file type")));
    }

    @Test
    void validUpload_returns201WithSummary() throws Exception {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        JobSummaryDto summary = new JobSummaryDto(job.getJobId(), JobStatus.QUEUED, Instant.now(), 1, 0, 0, 1);
        when(jobService.createJob(any())).thenReturn(job);
        when(jobQueryService.getSummary(job)).thenReturn(summary);
        MockMultipartFile png = new MockMultipartFile("files", "a.png", "image/png", new byte[]{1});

        mockMvc.perform(multipart("/api/jobs").file(png))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andExpect(jsonPath("$.total").value(1));
    }
}