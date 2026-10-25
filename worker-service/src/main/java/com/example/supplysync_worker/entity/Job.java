package com.example.supplysync_worker.entity;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name="jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID jobId;
    @Column(name="job_status",nullable = false)
    private String jobStatus="QUEUED";

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

}

