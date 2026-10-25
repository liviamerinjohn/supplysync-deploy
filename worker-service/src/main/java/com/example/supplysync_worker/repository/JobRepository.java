package com.example.supplysync_worker.repository;

import com.example.supplysync_worker.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
}
