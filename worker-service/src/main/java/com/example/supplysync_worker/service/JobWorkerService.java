package com.example.supplysync_worker.service;

import com.example.supplysync_worker.config.RabbitMQConfig;
import com.example.supplysync_worker.entity.Document;
import com.example.supplysync_worker.entity.Job;
import com.example.supplysync_worker.repository.DocumentRepository;
import com.example.supplysync_worker.repository.JobRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class JobWorkerService {
    /*@RabbitListener(queues = "job-processing-queue") — this is the consumer equivalent of @GetMapping.
    Instead of "run this method when an HTTP request hits this path," it means "run this method whenever a message arrives in this queue.
    " Spring handles all the underlying polling/connection-management for you — you just write what should happen when a message shows up.*/
    /*
    public void handleJobMessage(String jobId) — the parameter type matters:
     since your API published the message as a plain string (job.getJobId().toString()),
     Spring automatically converts the incoming message bytes into a String and hands it to you as jobId.
     If you'd published something more complex (like JSON), you'd use a different parameter
     type — we'll get to that in a later milestone when we send richer data.
     */
    private final JobRepository jobRepository;
    private final DocumentRepository documentRepository;
    private final ExecutorService executor;
    private final DocumentProcessingService processingService;

    /*public JobWorkerService(JobRepository jobRepository, DocumentRepository documentRepository) {
        this.jobRepository = jobRepository;
        this.documentRepository = documentRepository;
    }*/

    public JobWorkerService(JobRepository jobRepository, DocumentRepository documentRepository, ExecutorService executor, DocumentProcessingService processingService) {
        this.jobRepository = jobRepository;
        this.documentRepository = documentRepository;
        this.executor = executor;
        this.processingService = processingService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleJobMessage(String jobId) {
        UUID id = UUID.fromString(jobId);
        Optional<Job> jobOptional = jobRepository.findById(id);

        if (jobOptional.isPresent()) {
            Job job = jobOptional.get();
            System.out.println("Found job: " + job.getJobId() + ", status: " + job.getJobStatus());
            //store the result of findByJob(job) in a variable.
            List<Document>documents=documentRepository.findByJob(job);
            /*for (Document document : documents) {
                System.out.println("  Document: " + document.getDocId() + ", file: " + document.getFilePath());
            }*/

            long start = System.currentTimeMillis();

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Document document : documents) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> processingService.processDocument(document), executor);
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Job finished in " + elapsed + " ms");
        } else {
            System.out.println("No job found for ID: " + jobId);
        }
    }

}