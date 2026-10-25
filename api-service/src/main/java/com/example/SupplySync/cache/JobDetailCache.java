package com.example.SupplySync.cache;

import com.example.SupplySync.dto.JobDetailDto;
import com.example.SupplySync.dto.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class JobDetailCache {

    private static final Logger log = LoggerFactory.getLogger(JobDetailCache.class);
    private static final String KEY_PREFIX = "job:detail:";

    private final StringRedisTemplate redis;
    private final JsonMapper mapper;
    private final Duration finishedTtl;
    private final Duration activeTtl;

    public JobDetailCache(StringRedisTemplate redis,
                          JsonMapper mapper,
                          @Value("${cache.job.finished-ttl-seconds:600}") long finishedTtlSeconds,
                          @Value("${cache.job.active-ttl-seconds:30}") long activeTtlSeconds) {
        this.redis = redis;
        this.mapper = mapper;
        this.finishedTtl = Duration.ofSeconds(finishedTtlSeconds);
        this.activeTtl = Duration.ofSeconds(activeTtlSeconds);
    }

    public Optional<JobDetailDto> get(UUID jobId) {
        try {
            String json = redis.opsForValue().get(key(jobId));
            if (json == null) {
                log.info("Cache MISS for job {}", jobId);
                return Optional.empty();
            }
            log.info("Cache HIT for job {}", jobId);
            return Optional.of(mapper.readValue(json, JobDetailDto.class));
        } catch (Exception e) {
            log.warn("Cache read failed for job {}: {}", jobId, e.toString());
            return Optional.empty();          // fail open: behave like a miss
        }
    }

    public void put(JobDetailDto dto) {
        try {
            Duration ttl = isFinished(dto.status()) ? finishedTtl : activeTtl;
            redis.opsForValue().set(key(dto.jobId()), mapper.writeValueAsString(dto), ttl);
        } catch (Exception e) {
            log.warn("Cache write failed for job {}: {}", dto.jobId(), e.toString());
        }
    }

    public void evict(UUID jobId) {
        try {
            redis.delete(key(jobId));
        } catch (Exception e) {
            log.warn("Cache evict failed for job {}: {}", jobId, e.toString());
        }
    }

    private boolean isFinished(JobStatus status) {
        return status == JobStatus.COMPLETED
                || status == JobStatus.FAILED
                || status == JobStatus.PARTIALLY_COMPLETED;
    }

    private String key(UUID jobId) {
        return KEY_PREFIX + jobId;
    }
}