package com.manleytech.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manleytech.entity.db.KnowledgeMappingEntity;
import com.manleytech.entity.db.RagConfigEntity;
import com.manleytech.service.DatabaseConfigService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigControllerTest {

    @Mock
    private DatabaseConfigService configService;

    private ConfigController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new ConfigController(configService, objectMapper);
    }

    @Test
    void getMappings_returnsAllMappings() {
        List<KnowledgeMappingEntity> mappings = List.of(
                createMappingEntity(1L, "kb-1", "bailian"),
                createMappingEntity(2L, "kb-2", "ragflow")
        );
        when(configService.getAllMappings()).thenReturn(mappings);
        when(configService.isDbAvailable()).thenReturn(true);

        HttpResponse<?> response = controller.getMappings();

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
    }

    @Test
    void getMapping_returnsMapping_whenExists() {
        KnowledgeMappingEntity mapping = createMappingEntity(1L, "kb-1", "bailian");
        when(configService.getMappingById(1L)).thenReturn(Optional.of(mapping));

        HttpResponse<?> response = controller.getMapping(1L);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
    }

    @Test
    void getMapping_returnsNotFound_whenMappingDoesNotExist() {
        when(configService.getMappingById(999L)).thenReturn(Optional.empty());

        HttpResponse<?> response = controller.getMapping(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void createMapping_createsNewMapping() {
        Map<String, Object> data = new HashMap<>();
        data.put("knowledgeId", "new-kb");
        data.put("providerType", "bailian");
        data.put("targetId", "target-123");
        data.put("workspaceId", "ws-456");

        KnowledgeMappingEntity savedEntity = createMappingEntity(1L, "new-kb", "bailian");
        savedEntity.setTargetId("target-123");
        savedEntity.setWorkspaceId("ws-456");
        when(configService.saveMapping(any(KnowledgeMappingEntity.class), anyString()))
                .thenReturn(savedEntity);

        HttpResponse<?> response = controller.createMapping(data);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        verify(configService).saveMapping(any(KnowledgeMappingEntity.class), eq("system"));
    }

    @Test
    void createMapping_returnsBadRequest_whenValidationFails() {
        Map<String, Object> data = new HashMap<>();
        data.put("knowledgeId", "new-kb");
        data.put("providerType", "bailian");
        when(configService.saveMapping(any(KnowledgeMappingEntity.class), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        HttpResponse<?> response = controller.createMapping(data);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }

    @Test
    void updateMapping_updatesExistingMapping() {
        Map<String, Object> data = new HashMap<>();
        data.put("knowledgeId", "updated-kb");
        data.put("providerType", "bailian");
        data.put("targetId", "new-target");
        data.put("workspaceId", "new-ws");

        KnowledgeMappingEntity updatedEntity = createMappingEntity(1L, "updated-kb", "bailian");
        when(configService.updateMapping(eq(1L), any(KnowledgeMappingEntity.class), anyString()))
                .thenReturn(updatedEntity);

        HttpResponse<?> response = controller.updateMapping(1L, data);

        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    void updateMapping_returnsNotFound_whenMappingDoesNotExist() {
        Map<String, Object> data = new HashMap<>();
        data.put("knowledgeId", "updated-kb");
        data.put("providerType", "bailian");
        when(configService.updateMapping(eq(999L), any(KnowledgeMappingEntity.class), anyString()))
                .thenThrow(new IllegalArgumentException("Mapping not found: 999"));

        HttpResponse<?> response = controller.updateMapping(999L, data);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void deleteMapping_deletesExistingMapping() {
        KnowledgeMappingEntity existing = createMappingEntity(1L, "kb-1", "bailian");
        when(configService.getMappingById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(configService).deleteMapping(eq(1L), eq("kb-1"), eq("system"));

        HttpResponse<?> response = controller.deleteMapping(1L);

        assertEquals(HttpStatus.OK, response.getStatus());
        verify(configService).deleteMapping(1L, "kb-1", "system");
    }

    @Test
    void deleteMapping_returnsNotFound_whenMappingDoesNotExist() {
        when(configService.getMappingById(999L)).thenReturn(Optional.empty());

        HttpResponse<?> response = controller.deleteMapping(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void getRagConfig_returnsConfig_whenExists() {
        RagConfigEntity config = createRagConfigEntity(1L, "bailian");
        when(configService.getRagConfig("bailian")).thenReturn(Optional.of(config));

        HttpResponse<?> response = controller.getRagConfig("bailian");

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
    }

    @Test
    void getRagConfig_returnsNotFound_whenConfigDoesNotExist() {
        when(configService.getRagConfig("unknown")).thenReturn(Optional.empty());

        HttpResponse<?> response = controller.getRagConfig("unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void updateRagConfig_updatesExistingConfig() {
        Map<String, Object> data = new HashMap<>();
        data.put("endpoint", "https://new-endpoint.com");
        data.put("apiKey", "new-api-key");
        data.put("status", 1);

        RagConfigEntity updatedEntity = createRagConfigEntity(1L, "bailian");
        updatedEntity.setEndpoint("https://new-endpoint.com");
        when(configService.updateRagConfig(eq("bailian"), any(RagConfigEntity.class), anyString()))
                .thenReturn(updatedEntity);

        HttpResponse<?> response = controller.updateRagConfig("bailian", data);

        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    void updateRagConfig_returnsNotFound_whenConfigDoesNotExist() {
        Map<String, Object> data = new HashMap<>();
        data.put("endpoint", "https://new-endpoint.com");
        data.put("apiKey", "new-api-key");
        when(configService.updateRagConfig(eq("unknown"), any(RagConfigEntity.class), anyString()))
                .thenThrow(new IllegalArgumentException("RAG config not found for provider: unknown"));

        HttpResponse<?> response = controller.updateRagConfig("unknown", data);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void testRagConfig_returnsSuccess_whenConfigExists() {
        RagConfigEntity config = createRagConfigEntity(1L, "bailian");
        when(configService.getRagConfig("bailian")).thenReturn(Optional.of(config));

        HttpResponse<?> response = controller.testRagConfig("bailian", new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    void testRagConfig_returnsError_whenConfigNotFound() {
        when(configService.getRagConfig("unknown")).thenReturn(Optional.empty());

        HttpResponse<?> response = controller.testRagConfig("unknown", new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    void rollback_rollsBackSuccessfully() {
        Map<String, Object> data = new HashMap<>();
        data.put("historyId", 1L);
        data.put("operator", "admin");

        doNothing().when(configService).rollback(1L, "admin");

        HttpResponse<?> response = controller.rollback(data);

        assertEquals(HttpStatus.OK, response.getStatus());
        verify(configService).rollback(1L, "admin");
    }

    @Test
    void rollback_returnsNotFound_whenHistoryRecordNotFound() {
        Map<String, Object> data = new HashMap<>();
        data.put("historyId", 999L);
        data.put("operator", "admin");

        doThrow(new IllegalArgumentException("History record not found: 999"))
                .when(configService).rollback(999L, "admin");

        HttpResponse<?> response = controller.rollback(data);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void exportConfig_exportsAllConfiguration() {
        List<KnowledgeMappingEntity> mappings = List.of(createMappingEntity(1L, "kb-1", "bailian"));
        List<RagConfigEntity> configs = List.of(createRagConfigEntity(1L, "bailian"));
        when(configService.getAllMappings()).thenReturn(mappings);
        when(configService.getAllRagConfigs()).thenReturn(configs);

        HttpResponse<?> response = controller.exportConfig();

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
    }

    private KnowledgeMappingEntity createMappingEntity(Long id, String knowledgeId, String providerType) {
        KnowledgeMappingEntity entity = new KnowledgeMappingEntity();
        entity.setId(id);
        entity.setKnowledgeId(knowledgeId);
        entity.setProviderType(providerType);
        entity.setTargetId("target-" + id);
        entity.setWorkspaceId("ws-" + id);
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    private RagConfigEntity createRagConfigEntity(Long id, String providerType) {
        RagConfigEntity entity = new RagConfigEntity();
        entity.setId(id);
        entity.setProviderType(providerType);
        entity.setEndpoint("https://api." + providerType + ".com");
        entity.setApiKey("test-api-key-" + id);
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}