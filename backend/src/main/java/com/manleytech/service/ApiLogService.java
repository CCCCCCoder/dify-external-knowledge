package com.manleytech.service;

import com.manleytech.entity.db.ApiLogEntity;
import com.manleytech.repository.ApiLogRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class ApiLogService {

    private static final Logger LOG = LoggerFactory.getLogger(ApiLogService.class);

    private final ApiLogRepository apiLogRepository;
    private final BlockingQueue<ApiLogEntity> logQueue = new LinkedBlockingQueue<>(10000);

    @Inject
    public ApiLogService(ApiLogRepository apiLogRepository) {
        this.apiLogRepository = apiLogRepository;
    }

    public void logRequest(ApiLogEntity logEntity) {
        if (!logQueue.offer(logEntity)) {
            LOG.warn("API log queue is full, dropping log entry");
        }
    }

    public Mono<Void> logRequestAsync(ApiLogEntity logEntity) {
        return Mono.defer(() -> {
            try {
                apiLogRepository.save(logEntity);
                return Mono.empty();
            } catch (Exception e) {
                LOG.error("Failed to save API log", e);
                return Mono.empty();
            }
        });
    }

    @Scheduled(fixedDelay = "5s")
    public void flushLogs() {
        List<ApiLogEntity> batch = new java.util.ArrayList<>(100);
        logQueue.drainTo(batch, 100);
        if (!batch.isEmpty()) {
            try {
                batch.forEach(apiLogRepository::save);
                LOG.debug("Flushed {} API log entries to database", batch.size());
            } catch (Exception e) {
                LOG.error("Failed to flush API logs to database", e);
                batch.forEach(logQueue::offer);
            }
        }
    }

    public List<ApiLogEntity> getRecentLogs(int limit) {
        Iterable<ApiLogEntity> allLogs = apiLogRepository.findAll();
        List<ApiLogEntity> result = new java.util.ArrayList<>();
        for (ApiLogEntity entity : allLogs) {
            result.add(entity);
        }
        return result.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<ApiLogEntity> getLogsByKnowledgeId(String knowledgeId) {
        return apiLogRepository.findByKnowledgeIdOrderByCreatedAtDesc(knowledgeId);
    }

    public List<ApiLogEntity> getLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return apiLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    public String sanitizeHeader(String header) {
        if (header == null) return null;
        return header.replaceAll("([Aa]uthorization|[Kk]ey)[:\\s]*[^,]*", "$1: ***");
    }

    public String sanitizeBody(String body) {
        if (body == null) return null;
        return body.replaceAll("\"(api[keyK]ey|key|secret|password)\"\\s*:\\s*\"[^\"]*\"", "\"$1\": \"***\"");
    }
}
