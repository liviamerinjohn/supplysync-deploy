package com.example.supplysync_worker.repository;

import com.example.supplysync_worker.entity.Document;
import com.example.supplysync_worker.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByJob(Job job);
}
