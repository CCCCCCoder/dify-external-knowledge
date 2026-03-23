package com.manleytech.web;

import com.manleytech.entity.db.RagConfigEntity;
import com.manleytech.service.DatabaseConfigService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller("/api/config/rag")
public class HealthController {

    private static final Logger LOG = LoggerFactory.getLogger(HealthController.class);

    private final DatabaseConfigService configService;

    @Inject
    public HealthController(DatabaseConfigService configService) {
        this.configService = configService;
    }

    @Get("/{providerType}/health")
    public Map<String, Object> checkHealth(@PathVariable String providerType) {
        LOG.debug("Health check for provider: {}", providerType);
        long startTime = System.currentTimeMillis();

        Map<String, Object> result = new HashMap<>();
        result.put("providerType", providerType);
        result.put("lastChecked", LocalDateTime.now().toString());

        try {
            Optional<RagConfigEntity> configOpt = configService.getRagConfig(providerType);
            if (configOpt.isEmpty()) {
                result.put("status", "DOWN");
                result.put("error", "Configuration not found");
                return result;
            }

            RagConfigEntity config = configOpt.get();
            result.put("status", "UP");
            result.put("endpoint", config.getEndpoint());
            result.put("latencyMs", System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            LOG.error("Health check failed for provider: {}", providerType, e);
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("latencyMs", System.currentTimeMillis() - startTime);
        }

        return result;
    }
}
