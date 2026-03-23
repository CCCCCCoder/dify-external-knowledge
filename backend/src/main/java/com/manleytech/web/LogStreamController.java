package com.manleytech.web;

import com.manleytech.entity.db.ApiLogEntity;
import com.manleytech.service.ApiLogService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.sse.Event;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller("/api/logs")
public class LogStreamController {

    private static final Logger LOG = LoggerFactory.getLogger(LogStreamController.class);

    private final ApiLogService apiLogService;
    private final Sinks.Many<Event<LogEvent>> sink = Sinks.many().multicast().directBestEffort();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Inject
    public LogStreamController(ApiLogService apiLogService) {
        this.apiLogService = apiLogService;
        startLogPolling();
    }

    @Get("/stream")
    public Publisher<Event<LogEvent>> streamLogs(@QueryValue(defaultValue = "100") int bufferSize) {
        LOG.info("Starting log stream with buffer size: {}", bufferSize);

        Flux<Event<LogEvent>> recentLogsFlux = Flux.fromIterable(apiLogService.getRecentLogs(bufferSize))
                .map(this::toLogEvent)
                .map(event -> Event.of(event));

        return Flux.merge(
                recentLogsFlux,
                sink.asFlux()
        );
    }

    @Get("/recent")
    public List<LogEvent> getRecentLogs(@QueryValue(defaultValue = "100") int limit) {
        return apiLogService.getRecentLogs(limit).stream().map(this::toLogEvent).toList();
    }

    private void startLogPolling() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<ApiLogEntity> recentLogs = apiLogService.getRecentLogs(10);
                if (!recentLogs.isEmpty()) {
                    for (ApiLogEntity log : recentLogs) {
                        sink.emitNext(Event.of(toLogEvent(log)), Sinks.EmitFailureHandler.FAIL_FAST);
                    }
                }
            } catch (Exception e) {
                LOG.error("Error polling logs", e);
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    private LogEvent toLogEvent(ApiLogEntity entity) {
        LogEvent event = new LogEvent();
        event.setId(entity.getId());
        event.setRequestId(entity.getRequestId());
        event.setKnowledgeId(entity.getKnowledgeId());
        event.setProviderType(entity.getProviderType());
        event.setRequestUrl(entity.getRequestUrl());
        event.setRequestMethod(entity.getRequestMethod());
        event.setResponseStatus(entity.getResponseStatus());
        event.setResponseTime(entity.getResponseTime());
        event.setErrorMessage(entity.getErrorMessage());
        event.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        return event;
    }

    public static class LogEvent {
        private Long id;
        private String requestId;
        private String knowledgeId;
        private String providerType;
        private String requestUrl;
        private String requestMethod;
        private Integer responseStatus;
        private Long responseTime;
        private String errorMessage;
        private String createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getKnowledgeId() {
            return knowledgeId;
        }

        public void setKnowledgeId(String knowledgeId) {
            this.knowledgeId = knowledgeId;
        }

        public String getProviderType() {
            return providerType;
        }

        public void setProviderType(String providerType) {
            this.providerType = providerType;
        }

        public String getRequestUrl() {
            return requestUrl;
        }

        public void setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public String getRequestMethod() {
            return requestMethod;
        }

        public void setRequestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
        }

        public Integer getResponseStatus() {
            return responseStatus;
        }

        public void setResponseStatus(Integer responseStatus) {
            this.responseStatus = responseStatus;
        }

        public Long getResponseTime() {
            return responseTime;
        }

        public void setResponseTime(Long responseTime) {
            this.responseTime = responseTime;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
