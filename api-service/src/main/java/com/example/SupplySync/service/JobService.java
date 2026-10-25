package com.example.SupplySync.service;

import com.example.SupplySync.config.RabbitMQConfig;
import com.example.SupplySync.entity.Document;
import com.example.SupplySync.entity.Job;
import com.example.SupplySync.repository.DocumentRepository;
import com.example.SupplySync.repository.JobRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final DocumentRepository documentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Path uploadDir;

    public JobService(JobRepository jobRepository,
                      DocumentRepository documentRepository,
                      RabbitTemplate rabbitTemplate,
                      @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.jobRepository = jobRepository;
        this.documentRepository = documentRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath();
    }

    public Job createJob(List<MultipartFile> files) throws IOException {
        validate(files);

        Job job = jobRepository.save(new Job());
        Files.createDirectories(uploadDir);

        for (MultipartFile file : files) {
            String savedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destination = uploadDir.resolve(savedName);
            file.transferTo(destination);

            Document document = new Document();
            document.setJob(job);
            document.setFilePath(destination.toString());
            documentRepository.save(document);
        }

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, job.getJobId().toString());
        return job;
    }

    private void validate(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required");
        }
        if (files.size() > 10) {
            throw new IllegalArgumentException("A maximum of 10 files is allowed");
        }
        for (MultipartFile file : files) {
            String name = file.getOriginalFilename();
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Empty file: " + name);
            }
            String lowerName = name == null ? "" : name.toLowerCase(Locale.ROOT);
            if (!lowerName.endsWith(".png") && !lowerName.endsWith(".jpg") && !lowerName.endsWith(".jpeg")) {
                throw new IllegalArgumentException(
                        "Unsupported file type: " + name + " (only PNG and JPG are allowed)");
            }
        }
    }
}