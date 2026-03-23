package com.manleytech.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manleytech.constant.dify.ErrorCode;
import com.manleytech.entity.db.ConfigHistoryEntity;
import com.manleytech.entity.db.KnowledgeMappingEntity;
import com.manleytech.entity.db.RagConfigEntity;
import com.manleytech.entity.dify.resp.StandardResponse;
import com.manleytech.service.DatabaseConfigService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller("/api/config")
@Tag(name = "Configuration", description = "Configuration management endpoints")
public class ConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigController.class);

    private final DatabaseConfigService configService;
    private final ObjectMapper objectMapper;

    @Inject
    public ConfigController(DatabaseConfigService configService, ObjectMapper objectMapper) {
        this.configService = configService;
        this.objectMapper = objectMapper;
    }

    @Get("/mappings")
    @Operation(summary = "Get all knowledge base mappings", description = "Retrieves all knowledge base mappings with database availability status")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mappings retrieved successfully")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Map<String, Object>>> getMappings() {
        LOG.debug("Fetching all knowledge base mappings");

        List<KnowledgeMappingEntity> mappings = configService.getAllMappings();
        List<Map<String, Object>> mappingList = mappings.stream().map(this::toMappingResponse).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("mappings", mappingList);
        data.put("total", mappingList.size());
        data.put("dbAvailable", configService.isDbAvailable());

        return HttpResponse.ok(StandardResponse.success(data));
    }

    @Get("/mappings/{id}")
    @Operation(summary = "Get mapping by ID", description = "Retrieves a specific knowledge base mapping by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapping found"),
        @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Map<String, Object>>> getMapping(
            @Parameter(description = "Mapping ID") @PathVariable Long id) {
        LOG.debug("Fetching mapping by id: {}", id);

        Optional<KnowledgeMappingEntity> mapping = configService.getMappingById(id);
        if (mapping.isEmpty()) {
            return HttpResponse.notFound();
        }

        return HttpResponse.ok(StandardResponse.success(toMappingResponse(mapping.get())));
    }

    @Post("/mappings")
    @Operation(summary = "Create knowledge base mapping", description = "Creates a new knowledge base to provider mapping")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Mapping created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Map<String, Object>>> createMapping(
            @Parameter(description = "Mapping data") @Body @Valid Map<String, Object> data) {
        LOG.info("Creating new knowledge base mapping: {}", data);

        try {
            KnowledgeMappingEntity entity = new KnowledgeMappingEntity();
            entity.setKnowledgeId((String) data.get("knowledgeId"));
            entity.setProviderType((String) data.get("providerType"));
            entity.setTargetId((String) data.get("targetId"));
            entity.setWorkspaceId((String) data.get("workspaceId"));
            entity.setConfigParams(toJson(data.get("configParams")));
            entity.setStatus(getStatusFromData(data));
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            KnowledgeMappingEntity saved = configService.saveMapping(entity, getOperator(data));

            return HttpResponse.status(HttpStatus.CREATED).body(StandardResponse.success(toMappingResponse(saved)));
        } catch (IllegalArgumentException e) {
            LOG.warn("Validation error creating mapping: {}", e.getMessage());
            return HttpResponse.badRequest(StandardResponse.error(ErrorCode.VALIDATION_ERROR, e.getMessage()));
        } catch (Exception e) {
            LOG.error("Failed to create mapping", e);
            return HttpResponse.serverError(StandardResponse.error(ErrorCode.MAPPING_CREATE_FAILED, e.getMessage()));
        }
    }

    @Put("/mappings/{id}")
    @Operation(summary = "Update knowledge base mapping", description = "Updates an existing knowledge base mapping")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapping updated successfully"),
        @ApiResponse(responseCode = "404", description = "Mapping not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Map<String, Object>>> updateMapping(
            @Parameter(description = "Mapping ID") @PathVariable Long id,
            @Parameter(description = "Updated mapping data") @Body @Valid Map<String, Object> data) {
        LOG.info("Updating mapping id: {}, data: {}", id, data);

        try {
            KnowledgeMappingEntity entity = new KnowledgeMappingEntity();
            entity.setKnowledgeId((String) data.get("knowledgeId"));
            entity.setProviderType((String) data.get("providerType"));
            entity.setTargetId((String) data.get("targetId"));
            entity.setWorkspaceId((String) data.get("workspaceId"));
            entity.setConfigParams(toJson(data.get("configParams")));
            entity.setStatus(getStatusFromData(data));
            entity.setUpdatedAt(LocalDateTime.now());

            KnowledgeMappingEntity updated = configService.updateMapping(id, entity, getOperator(data));

            return HttpResponse.ok(StandardResponse.success(toMappingResponse(updated)));
        } catch (IllegalArgumentException e) {
            LOG.warn("Resource not found updating mapping: {}", e.getMessage());
            return HttpResponse.notFound();
        } catch (Exception e) {
            LOG.error("Failed to update mapping", e);
            return HttpResponse.serverError(StandardResponse.error(ErrorCode.MAPPING_UPDATE_FAILED, e.getMessage()));
        }
    }

    @Delete("/mappings/{id}")
    @Operation(summary = "Delete knowledge base mapping", description = "Deletes a knowledge base mapping by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapping deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Mapping not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Void>> deleteMapping(
            @Parameter(description = "Mapping ID") @PathVariable Long id) {
        LOG.info("Deleting mapping id: {}", id);

        try {
            Optional<KnowledgeMappingEntity> existing = configService.getMappingById(id);
            if (existing.isEmpty()) {
                return HttpResponse.notFound();
            }

            configService.deleteMapping(id, existing.get().getKnowledgeId(), "system");

            return HttpResponse.ok(StandardResponse.success());
        } catch (IllegalArgumentException e) {
            LOG.warn("Resource not found deleting mapping: {}", e.getMessage());
            return HttpResponse.notFound();
        } catch (Exception e) {
            LOG.error("Failed to delete mapping", e);
            return HttpResponse.serverError(StandardResponse.error(ErrorCode.MAPPING_DELETE_FAILED, e.getMessage()));
        }
    }

    @Get("/rag/{providerType}")
    @Operation(summary = "Get RAG config", description = "Retrieves RAG configuration for a specific provider type")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "RAG config retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "RAG config not found")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Map<String, Object>>> getRagConfig(
            @Parameter(description = "Provider type (e.g., bailian, ragflow)") @PathVariable String providerType) {
        LOG.debug("Fetching RAG config for provider: {}", providerType);

        Optional<RagConfigEntity> config = configService.getRagConfig(providerType);
        if (config.isEmpty()) {
            return HttpResponse.notFound();
        }

        return HttpResponse.ok(StandardResponse.success(toRagConfigResponse(config.get())));
    }

    @Put("/rag/{providerType}")
    @Operation(summary = "Update RAG config", description = "Updates RAG configuration for a specific provider")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "RAG config updated successfully"),
        @ApiResponse(responseCode = "404", description = "RAG config not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Map<String, Object>>> updateRagConfig(
            @Parameter(description = "Provider type") @PathVariable String providerType,
            @Parameter(description = "Updated RAG config data") @Body @Valid Map<String, Object> data) {
        LOG.info("Updating RAG config for provider: {}, data: {}", providerType, data);

        try {
            RagConfigEntity entity = new RagConfigEntity();
            entity.setProviderType(providerType);
            entity.setEndpoint((String) data.get("endpoint"));
            entity.setApiKey((String) data.get("apiKey"));
            entity.setConfigParams(toJson(data.get("configParams")));
            entity.setStatus(getStatusFromData(data));
            entity.setUpdatedAt(LocalDateTime.now());

            RagConfigEntity updated = configService.updateRagConfig(providerType, entity, getOperator(data));

            return HttpResponse.ok(StandardResponse.success(toRagConfigResponse(updated)));
        } catch (IllegalArgumentException e) {
            LOG.warn("Resource not found updating RAG config: {}", e.getMessage());
            return HttpResponse.notFound();
        } catch (Exception e) {
            LOG.error("Failed to update RAG config", e);
            return HttpResponse.serverError(StandardResponse.error(ErrorCode.RAG_CONFIG_UPDATE_FAILED, e.getMessage()));
        }
    }

    @Post("/rag/{providerType}/test")
    @Operation(summary = "Test RAG config connection", description = "Tests the connection to a RAG provider")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Connection test result"),
        @ApiResponse(responseCode = "404", description = "Provider configuration not found"),
        @ApiResponse(responseCode = "500", description = "Connection test failed")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Map<String, Object>>> testRagConfig(
            @Parameter(description = "Provider type") @PathVariable String providerType,
            @Parameter(description = "Test request data") @Body Map<String, Object> data) {
        LOG.info("Testing RAG config connection for provider: {}", providerType);

        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<RagConfigEntity> configOpt = configService.getRagConfig(providerType);
            if (configOpt.isEmpty()) {
                result.put("message", "Provider configuration not found");
                return HttpResponse.ok(StandardResponse.error(ErrorCode.RESOURCE_NOT_FOUND, "Provider configuration not found"));
            }

            RagConfigEntity config = configOpt.get();
            result.put("response_time", System.currentTimeMillis() - startTime);
            result.put("providerType", providerType);
            result.put("endpoint", config.getEndpoint());
            result.put("message", "Connection test placeholder - actual test not implemented");

            return HttpResponse.ok(StandardResponse.success(result));
        } catch (Exception e) {
            LOG.error("Connection test failed for provider: {}", providerType, e);
            result.put("response_time", System.currentTimeMillis() - startTime);
            result.put("message", e.getMessage());
            return HttpResponse.serverError(StandardResponse.error(ErrorCode.PROVIDER_CONNECTION_FAILED, e.getMessage()));
        }
    }

    @Get("/history/{configType}/{configId}")
    @Operation(summary = "Get config history", description = "Retrieves the change history for a specific configuration")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "History retrieved successfully")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Map<String, Object>>> getConfigHistory(
            @Parameter(description = "Config type (e.g., mapping, rag_config)") @PathVariable String configType,
            @Parameter(description = "Config ID") @PathVariable Long configId) {
        LOG.debug("Fetching config history for {} with id: {}", configType, configId);

        List<ConfigHistoryEntity> history = configService.getConfigHistory(configType, configId);

        Map<String, Object> data = new HashMap<>();
        data.put("history", history);
        data.put("total", history.size());

        return HttpResponse.ok(StandardResponse.success(data));
    }

    @Post("/rollback")
    @Operation(summary = "Rollback configuration", description = "Rolls back a configuration to a previous state from history")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rollback completed successfully"),
        @ApiResponse(responseCode = "404", description = "History record not found"),
        @ApiResponse(responseCode = "500", description = "Rollback failed")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Void>> rollback(
            @Parameter(description = "Rollback request containing historyId and operator") @Body Map<String, Object> data) {
        LOG.info("Rolling back config, data: {}", data);

        try {
            Long historyId = Long.valueOf(data.get("historyId").toString());
            String operator = (String) data.getOrDefault("operator", "system");

            configService.rollback(historyId, operator);

            return HttpResponse.ok(StandardResponse.success());
        } catch (IllegalArgumentException e) {
            LOG.warn("Resource not found during rollback: {}", e.getMessage());
            return HttpResponse.notFound();
        } catch (Exception e) {
            LOG.error("Rollback failed", e);
            return HttpResponse.serverError(StandardResponse.error(ErrorCode.ROLLBACK_FAILED, e.getMessage()));
        }
    }

    @Get("/export")
    @Operation(summary = "Export configuration", description = "Exports all mappings and RAG configurations as JSON")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Configuration exported successfully")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<StandardResponse<Map<String, Object>>> exportConfig() {
        LOG.info("Exporting all configuration");

        Map<String, Object> exportData = new HashMap<>();
        exportData.put("mappings", configService.getAllMappings().stream().map(this::toMappingResponse).toList());
        exportData.put("ragConfigs", configService.getAllRagConfigs().stream().map(this::toRagConfigResponse).toList());
        exportData.put("exportedAt", LocalDateTime.now().toString());

        return HttpResponse.ok(StandardResponse.success(exportData));
    }

    private Map<String, Object> toMappingResponse(KnowledgeMappingEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("knowledgeId", entity.getKnowledgeId());
        map.put("providerType", entity.getProviderType());
        map.put("targetId", entity.getTargetId());
        map.put("workspaceId", entity.getWorkspaceId());
        map.put("configParams", parseJson(entity.getConfigParams()));
        map.put("status", entity.getStatus());
        map.put("createdAt", entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        map.put("updatedAt", entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
        return map;
    }

    private Map<String, Object> toRagConfigResponse(RagConfigEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("providerType", entity.getProviderType());
        map.put("endpoint", entity.getEndpoint());
        map.put("apiKey", maskApiKey(entity.getApiKey()));
        map.put("configParams", parseJson(entity.getConfigParams()));
        map.put("status", entity.getStatus());
        map.put("createdAt", entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        map.put("updatedAt", entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
        return map;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }

    private Integer getStatusFromData(Map<String, Object> data) {
        Object status = data.get("status");
        if (status == null) {
            return 1;
        }
        if (status instanceof Integer) {
            return (Integer) status;
        }
        return Integer.parseInt(status.toString());
    }

    private String getOperator(Map<String, Object> data) {
        return (String) data.getOrDefault("operator", "system");
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            LOG.error("Failed to serialize to JSON", e);
            return null;
        }
    }

    private Object parseJson(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            LOG.error("Failed to parse JSON", e);
            return new HashMap<>();
        }
    }
}
