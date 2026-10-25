package com.example.SupplySync.repository;

import com.example.SupplySync.entity.Document;
import com.example.SupplySync.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByJob(Job job);

}
