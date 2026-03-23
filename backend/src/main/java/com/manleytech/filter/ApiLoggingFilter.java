package com.manleytech.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manleytech.entity.db.ApiLogEntity;
import com.manleytech.service.ApiLogService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Filter("/dify/**")
public class ApiLoggingFilter implements HttpServerFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiLoggingFilter.class);

    private final ApiLogService apiLogService;
    private final ObjectMapper objectMapper;

    @Inject
    public ApiLoggingFilter(ApiLogService apiLogService, ObjectMapper objectMapper) {
        this.apiLogService = apiLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        return Mono.from(chain.proceed(request))
                .doOnSuccess(response -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    try {
                        ApiLogEntity logEntity = new ApiLogEntity();
                        logEntity.setRequestId(requestId);
                        logEntity.setRequestUrl(request.getUri().toString());
                        logEntity.setRequestMethod(request.getMethodName());
                        logEntity.setRequestHeaders(apiLogService.sanitizeHeader(toJson(request.getHeaders())));
                        logEntity.setRequestBody(apiLogService.sanitizeBody(extractRequestBody(request)));
                        logEntity.setResponseStatus(response != null ? response.getStatus().getCode() : null);
                        logEntity.setResponseTime(responseTime);
                        logEntity.setCreatedAt(LocalDateTime.now());

                        apiLogService.logRequest(logEntity);
                        LOG.debug("Logged API request {} - {} {} -> {} in {}ms",
                                requestId, request.getMethod(), request.getUri(),
                                response != null ? response.getStatus().getCode() : "unknown", responseTime);
                    } catch (Exception e) {
                        LOG.error("Failed to log API request", e);
                    }
                })
                .doOnError(error -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    try {
                        ApiLogEntity logEntity = new ApiLogEntity();
                        logEntity.setRequestId(requestId);
                        logEntity.setRequestUrl(request.getUri().toString());
                        logEntity.setRequestMethod(request.getMethodName());
                        logEntity.setRequestHeaders(apiLogService.sanitizeHeader(toJson(request.getHeaders())));
                        logEntity.setRequestBody(apiLogService.sanitizeBody(extractRequestBody(request)));
                        logEntity.setErrorMessage(error.getMessage());
                        logEntity.setResponseTime(responseTime);
                        logEntity.setCreatedAt(LocalDateTime.now());

                        apiLogService.logRequest(logEntity);
                        LOG.error("Logged failed API request {} - {} {} -> {} in {}ms: {}",
                                requestId, request.getMethod(), request.getUri(),
                                "ERROR", responseTime, error.getMessage());
                    } catch (Exception e) {
                        LOG.error("Failed to log error for API request", e);
                    }
                });
    }

    private String extractRequestBody(HttpRequest<?> request) {
        try {
            if (request.getBody().isPresent()) {
                Object body = request.getBody().get();
                if (body instanceof String) {
                    return (String) body;
                }
                return objectMapper.writeValueAsString(body);
            }
        } catch (Exception e) {
            LOG.warn("Failed to extract request body", e);
        }
        return null;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
