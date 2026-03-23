package com.manleytech.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manleytech.entity.db.ConfigHistoryEntity;
import com.manleytech.entity.db.KnowledgeMappingEntity;
import com.manleytech.entity.db.RagConfigEntity;
import com.manleytech.provider.config.BailianApiProperties;
import com.manleytech.provider.config.KnowledgeProviderProperties;
import com.manleytech.provider.config.ProviderMapping;
import com.manleytech.provider.config.RAGflowApiProperties;
import com.manleytech.repository.ConfigHistoryRepository;
import com.manleytech.repository.KnowledgeMappingRepository;
import com.manleytech.repository.RagConfigRepository;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
@Primary
@CacheConfig("config-cache")
public class DatabaseConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfigService.class);

    private static final String CONFIG_TYPE_MAPPING = "mapping";
    private static final String CONFIG_TYPE_RAG = "rag_config";
    private static final String OP_CREATE = "CREATE";
    private static final String OP_UPDATE = "UPDATE";
    private static final String OP_DELETE = "DELETE";

    private final KnowledgeMappingRepository mappingRepository;
    private final RagConfigRepository ragConfigRepository;
    private final ConfigHistoryRepository configHistoryRepository;
    private final KnowledgeProviderProperties yamlProperties;
    private final BailianApiProperties bailianApiProperties;
    private final RAGflowApiProperties ragflowApiProperties;
    private final ObjectMapper objectMapper;

    private boolean dbAvailable = true;

    @Inject
    public DatabaseConfigService(
            KnowledgeMappingRepository mappingRepository,
            RagConfigRepository ragConfigRepository,
            ConfigHistoryRepository configHistoryRepository,
            KnowledgeProviderProperties yamlProperties,
            BailianApiProperties bailianApiProperties,
            RAGflowApiProperties ragflowApiProperties,
            ObjectMapper objectMapper) {
        this.mappingRepository = mappingRepository;
        this.ragConfigRepository = ragConfigRepository;
        this.configHistoryRepository = configHistoryRepository;
        this.yamlProperties = yamlProperties;
        this.bailianApiProperties = bailianApiProperties;
        this.ragflowApiProperties = ragflowApiProperties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            LOG.info("Initializing DatabaseConfigService, checking DB connection...");
            ragConfigRepository.findAll().forEach(cfg -> LOG.info("Loaded RAG config from DB: {}", cfg.getProviderType()));
            mappingRepository.findAll().forEach(cfg -> LOG.info("Loaded mapping from DB: {}", cfg.getKnowledgeId()));
            LOG.info("DatabaseConfigService initialized successfully");
        } catch (Exception e) {
            LOG.warn("Database not available, falling back to YAML configuration: {}", e.getMessage());
            dbAvailable = false;
        }
    }

    public boolean isDbAvailable() {
        return dbAvailable;
    }

    @Cacheable
    public Optional<KnowledgeMappingEntity> getMapping(String knowledgeId) {
        if (!dbAvailable) {
            return getMappingFromYaml(knowledgeId);
        }
        try {
            return mappingRepository.findByKnowledgeId(knowledgeId);
        } catch (Exception e) {
            LOG.error("Error fetching mapping from DB, falling back to YAML", e);
            dbAvailable = false;
            return getMappingFromYaml(knowledgeId);
        }
    }

    public Optional<KnowledgeMappingEntity> getMappingById(Long id) {
        if (!dbAvailable) {
            return Optional.empty();
        }
        try {
            return mappingRepository.findById(id);
        } catch (Exception e) {
            LOG.error("Error fetching mapping by id from DB", e);
            return Optional.empty();
        }
    }

    private Optional<KnowledgeMappingEntity> getMappingFromYaml(String knowledgeId) {
        Map<String, ProviderMapping> providers = yamlProperties.getProviders();
        if (providers == null || !providers.containsKey(knowledgeId)) {
            return Optional.empty();
        }

        ProviderMapping pm = providers.get(knowledgeId);
        KnowledgeMappingEntity entity = new KnowledgeMappingEntity();
        entity.setKnowledgeId(knowledgeId);
        entity.setProviderType(pm.getProvider());
        entity.setTargetId(pm.getTargetId());
        entity.setWorkspaceId(pm.getWorkspaceId());
        entity.setStatus(1);
        return Optional.of(entity);
    }

    public List<KnowledgeMappingEntity> getAllMappings() {
        if (!dbAvailable) {
            return getAllMappingsFromYaml();
        }
        try {
            return (List<KnowledgeMappingEntity>) mappingRepository.findAll();
        } catch (Exception e) {
            LOG.error("Error fetching mappings from DB, falling back to YAML", e);
            dbAvailable = false;
            return getAllMappingsFromYaml();
        }
    }

    private List<KnowledgeMappingEntity> getAllMappingsFromYaml() {
        Map<String, ProviderMapping> providers = yamlProperties.getProviders();
        if (providers == null) {
            return List.of();
        }
        return providers.entrySet().stream().map(entry -> {
            KnowledgeMappingEntity entity = new KnowledgeMappingEntity();
            entity.setKnowledgeId(entry.getKey());
            ProviderMapping pm = entry.getValue();
            entity.setProviderType(pm.getProvider());
            entity.setTargetId(pm.getTargetId());
            entity.setWorkspaceId(pm.getWorkspaceId());
            entity.setStatus(1);
            return entity;
        }).collect(Collectors.toList());
    }

    @Cacheable
    public Optional<RagConfigEntity> getRagConfig(String providerType) {
        if (!dbAvailable) {
            return getRagConfigFromYaml(providerType);
        }
        try {
            return ragConfigRepository.findByProviderType(providerType);
        } catch (Exception e) {
            LOG.error("Error fetching RAG config from DB, falling back to YAML", e);
            dbAvailable = false;
            return getRagConfigFromYaml(providerType);
        }
    }

    private Optional<RagConfigEntity> getRagConfigFromYaml(String providerType) {
        RagConfigEntity entity = new RagConfigEntity();
        entity.setProviderType(providerType);

        if ("bailian".equalsIgnoreCase(providerType)) {
            entity.setEndpoint(bailianApiProperties.getEndpoint());
            entity.setApiKey(bailianApiProperties.getKey());
            entity.setConfigParams(toJson(buildBailianConfigParams()));
        } else if ("ragflow".equalsIgnoreCase(providerType)) {
            entity.setEndpoint(ragflowApiProperties.getEndpoint());
            entity.setApiKey(ragflowApiProperties.getKey());
            entity.setConfigParams(toJson(buildRAGflowConfigParams()));
        } else {
            return Optional.empty();
        }
        entity.setStatus(1);
        return Optional.of(entity);
    }

    public List<RagConfigEntity> getAllRagConfigs() {
        if (!dbAvailable) {
            return List.of(
                    getRagConfigFromYaml("bailian").orElse(null),
                    getRagConfigFromYaml("ragflow").orElse(null)
            ).stream().filter(c -> c != null).collect(Collectors.toList());
        }
        try {
            return (List<RagConfigEntity>) ragConfigRepository.findAll();
        } catch (Exception e) {
            LOG.error("Error fetching RAG configs from DB, falling back to YAML", e);
            dbAvailable = false;
            return getAllRagConfigs();
        }
    }

    @CacheInvalidate(parameters = "knowledgeId")
    public KnowledgeMappingEntity saveMapping(KnowledgeMappingEntity entity, String operator) {
        if (!dbAvailable) {
            throw new IllegalStateException("Database not available");
        }
        KnowledgeMappingEntity saved = mappingRepository.save(entity);
        recordHistory(CONFIG_TYPE_MAPPING, saved.getId(), OP_CREATE, null, toJson(entity), operator);
        return saved;
    }

    @CacheInvalidate(parameters = "knowledgeId")
    public KnowledgeMappingEntity updateMapping(Long id, KnowledgeMappingEntity newEntity, String operator) {
        if (!dbAvailable) {
            throw new IllegalStateException("Database not available");
        }
        Optional<KnowledgeMappingEntity> existing = mappingRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Mapping not found: " + id);
        }
        KnowledgeMappingEntity old = existing.get();
        String oldJson = toJson(old);

        newEntity.setId(id);
        newEntity.setCreatedAt(old.getCreatedAt());
        KnowledgeMappingEntity updated = mappingRepository.update(newEntity);

        recordHistory(CONFIG_TYPE_MAPPING, id, OP_UPDATE, oldJson, toJson(updated), operator);
        return updated;
    }

    @CacheInvalidate(parameters = "knowledgeId")
    public void deleteMapping(Long id, String knowledgeId, String operator) {
        if (!dbAvailable) {
            throw new IllegalStateException("Database not available");
        }
        Optional<KnowledgeMappingEntity> existing = mappingRepository.findById(id);
        if (existing.isPresent()) {
            recordHistory(CONFIG_TYPE_MAPPING, id, OP_DELETE, toJson(existing.get()), null, operator);
            mappingRepository.deleteById(id);
        }
    }

    @CacheInvalidate(parameters = "providerType")
    public RagConfigEntity saveRagConfig(RagConfigEntity entity, String operator) {
        if (!dbAvailable) {
            throw new IllegalStateException("Database not available");
        }
        RagConfigEntity saved = ragConfigRepository.save(entity);
        recordHistory(CONFIG_TYPE_RAG, saved.getId(), OP_CREATE, null, toJson(entity), operator);
        return saved;
    }

    @CacheInvalidate(parameters = "providerType")
    public RagConfigEntity updateRagConfig(String providerType, RagConfigEntity newEntity, String operator) {
        if (!dbAvailable) {
            throw new IllegalStateException("Database not available");
        }
        Optional<RagConfigEntity> existing = ragConfigRepository.findByProviderType(providerType);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("RAG config not found for provider: " + providerType);
        }
        RagConfigEntity old = existing.get();
        String oldJson = toJson(old);

        newEntity.setId(old.getId());
        newEntity.setCreatedAt(old.getCreatedAt());
        RagConfigEntity updated = ragConfigRepository.update(newEntity);

        recordHistory(CONFIG_TYPE_RAG, old.getId(), OP_UPDATE, oldJson, toJson(updated), operator);
        return updated;
    }

    public List<ConfigHistoryEntity> getConfigHistory(String configType, Long configId) {
        return configHistoryRepository.findByConfigTypeAndConfigIdOrderByCreatedAtDesc(configType, configId);
    }

    public void rollback(Long historyId, String operator) {
        if (!dbAvailable) {
            throw new IllegalStateException("Database not available");
        }
        Optional<ConfigHistoryEntity> historyOpt = configHistoryRepository.findById(historyId);
        if (historyOpt.isEmpty()) {
            throw new IllegalArgumentException("History record not found: " + historyId);
        }

        ConfigHistoryEntity history = historyOpt.get();
        String configType = history.getConfigType();
        Long configId = history.getConfigId();
        String oldValue = history.getOldValue();

        if (configType.equals(CONFIG_TYPE_MAPPING)) {
            Optional<KnowledgeMappingEntity> existing = mappingRepository.findById(configId);
            if (existing.isPresent()) {
                recordHistory(CONFIG_TYPE_MAPPING, configId, OP_UPDATE, toJson(existing.get()), oldValue, operator);
                KnowledgeMappingEntity restored = jsonToMapping(oldValue);
                restored.setId(configId);
                restored.setCreatedAt(existing.get().getCreatedAt());
                mappingRepository.update(restored);
            }
        } else if (configType.equals(CONFIG_TYPE_RAG)) {
            Optional<RagConfigEntity> existing = ragConfigRepository.findById(configId);
            if (existing.isPresent()) {
                recordHistory(CONFIG_TYPE_RAG, configId, OP_UPDATE, toJson(existing.get()), oldValue, operator);
                RagConfigEntity restored = jsonToRagConfig(oldValue);
                restored.setId(configId);
                restored.setCreatedAt(existing.get().getCreatedAt());
                ragConfigRepository.update(restored);
            }
        }
    }

    private void recordHistory(String configType, Long configId, String operationType, String oldValue, String newValue, String operator) {
        try {
            ConfigHistoryEntity history = new ConfigHistoryEntity();
            history.setConfigType(configType);
            history.setConfigId(configId);
            history.setOperationType(operationType);
            history.setOldValue(oldValue);
            history.setNewValue(newValue);
            history.setOperator(operator);
            history.setCreatedAt(LocalDateTime.now());
            configHistoryRepository.save(history);
        } catch (Exception e) {
            LOG.error("Failed to record config history", e);
        }
    }

    private Map<String, Object> buildBailianConfigParams() {
        return Map.of(
                "sparseSimilarityTopK", bailianApiProperties.getSparseSimilarityTopK(),
                "denseSimilarityTopK", bailianApiProperties.getDenseSimilarityTopK(),
                "enableReranking", bailianApiProperties.getEnableReranking(),
                "rerankMinScore", bailianApiProperties.getRerankMinScore(),
                "rerankTopN", bailianApiProperties.getRerankTopN(),
                "enableRewrite", bailianApiProperties.getEnableRewrite(),
                "saveRetrieverHistory", bailianApiProperties.getSaveRetrieverHistory()
        );
    }

    private Map<String, Object> buildRAGflowConfigParams() {
        return Map.of(
                "topK", ragflowApiProperties.getDefaultTopK(),
                "scoreThreshold", ragflowApiProperties.getSimilarityThreshold()
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize object to JSON", e);
            return "{}";
        }
    }

    private KnowledgeMappingEntity jsonToMapping(String json) {
        try {
            return objectMapper.readValue(json, KnowledgeMappingEntity.class);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to deserialize JSON to KnowledgeMappingEntity", e);
            throw new IllegalArgumentException("Invalid JSON for mapping");
        }
    }

    private RagConfigEntity jsonToRagConfig(String json) {
        try {
            return objectMapper.readValue(json, RagConfigEntity.class);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to deserialize JSON to RagConfigEntity", e);
            throw new IllegalArgumentException("Invalid JSON for RAG config");
        }
    }
}
