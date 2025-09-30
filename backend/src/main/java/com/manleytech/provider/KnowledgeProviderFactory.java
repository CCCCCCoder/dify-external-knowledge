package com.manleytech.provider;

import com.manleytech.provider.config.KnowledgeProviderProperties;
import com.manleytech.provider.config.ProviderMapping;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 知识库提供者工厂，用于根据知识库ID选择合适的提供者
 */
@Singleton
public class KnowledgeProviderFactory {

    private final Map<String, KnowledgeProvider> providerMap;
    private final KnowledgeProviderProperties providerProperties;

    @Inject
    public KnowledgeProviderFactory(List<KnowledgeProvider> providers, KnowledgeProviderProperties providerProperties) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(KnowledgeProvider::getProviderName, Function.identity()));
        this.providerProperties = providerProperties;
    }

    /**
     * 根据知识库ID获取对应的提供者
     *
     * @param knowledgeId 知识库ID
     * @return 知识库提供者
     * @throws IllegalArgumentException 如果未找到对应的提供者
     */
    public KnowledgeProvider getProvider(String knowledgeId) {
        ProviderMapping mapping = providerProperties.getProviders().get(knowledgeId);
        if (mapping == null) {
            throw new IllegalArgumentException("No provider mapping found for knowledge_id: " + knowledgeId);
        }

        KnowledgeProvider provider = providerMap.get(mapping.getProvider());
        if (provider == null) {
            throw new IllegalArgumentException("Unknown provider type: " + mapping.getProvider());
        }

        return provider;
    }
}