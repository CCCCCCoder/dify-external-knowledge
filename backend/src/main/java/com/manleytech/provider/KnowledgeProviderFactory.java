package com.manleytech.provider;

import com.manleytech.entity.db.KnowledgeMappingEntity;
import com.manleytech.service.DatabaseConfigService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class KnowledgeProviderFactory {

    private final Map<String, KnowledgeProvider> providerMap;
    private final DatabaseConfigService configService;

    @Inject
    public KnowledgeProviderFactory(List<KnowledgeProvider> providers, DatabaseConfigService configService) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(KnowledgeProvider::getProviderName, Function.identity()));
        this.configService = configService;
    }

    public KnowledgeProvider getProvider(String knowledgeId) {
        KnowledgeMappingEntity mapping = configService.getMapping(knowledgeId).orElse(null);
        if (mapping == null) {
            throw new IllegalArgumentException("No provider mapping found for knowledge_id: " + knowledgeId);
        }

        KnowledgeProvider provider = providerMap.get(mapping.getProviderType());
        if (provider == null) {
            throw new IllegalArgumentException("Unknown provider type: " + mapping.getProviderType());
        }

        return provider;
    }
}
