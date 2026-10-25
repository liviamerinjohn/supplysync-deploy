package com.example.SupplySync.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="jobs")
public class Job {
    //JobStatus status = JobStatusCalculator.calculate(documents);
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID jobId;
    @Column(name="job_status",nullable = false)
    private String jobStatus="QUEUED";

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

