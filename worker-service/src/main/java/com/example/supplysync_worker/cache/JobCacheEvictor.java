package com.example.supplysync_worker.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JobCacheEvictor {

    private static final Logger log = LoggerFactory.getLogger(JobCacheEvictor.class);

    // Must match the key format used by the API's JobDetailCache.
    private static final String KEY_PREFIX = "job:detail:";

    private final StringRedisTemplate redis;

    public JobCacheEvictor(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void evict(UUID jobId) {
        try {
            redis.delete(KEY_PREFIX + jobId);
        } catch (Exception e) {
            log.warn("Could not evict cache for job {}: {}", jobId, e.toString());
        }
    }
}