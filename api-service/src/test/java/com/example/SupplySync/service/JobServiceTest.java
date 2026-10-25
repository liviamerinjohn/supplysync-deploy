package com.example.SupplySync.service;

import com.example.SupplySync.config.RabbitMQConfig;
import com.example.SupplySync.entity.Document;
import com.example.SupplySync.entity.Job;
import com.example.SupplySync.repository.DocumentRepository;
import com.example.SupplySync.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock JobRepository jobRepository;
    @Mock DocumentRepository documentRepository;
    @Mock RabbitTemplate rabbitTemplate;

    @TempDir Path tempDir;          // JUnit gives each test its own temporary folder and deletes it afterwards

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository, documentRepository, rabbitTemplate, tempDir.toString());
    }

    @Test
    void moreThanTenFiles_isRejected_andNothingIsSavedOrPublished() {
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            files.add(image("f" + i + ".png"));
        }

        assertThatThrownBy(() -> jobService.createJob(files))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum of 10");

        verifyNoInteractions(jobRepository, documentRepository, rabbitTemplate);
    }

    @Test
    void pdf_isRejected_andNothingIsSavedOrPublished() {
        List<MultipartFile> files = List.of(image("invoice.pdf"));

        assertThatThrownBy(() -> jobService.createJob(files))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file type");

        verifyNoInteractions(jobRepository, documentRepository, rabbitTemplate);
    }

    @Test
    void validUpload_savesJobAndDocuments_storesFiles_andPublishesMessage() throws Exception {
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setJobId(UUID.randomUUID());          // behave like the database: assign an id
            return job;
        });

        Job job = jobService.createJob(List.of(image("a.png"), image("b.jpg")));

        verify(documentRepository, times(2)).save(any(Document.class));
        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, job.getJobId().toString());
        assertThat(tempDir.toFile().listFiles()).hasSize(2);
    }

    private static MockMultipartFile image(String name) {
        return new MockMultipartFile("files", name, "image/png", new byte[]{1, 2, 3});
    }
}