package com.manleytech.provider;

import com.manleytech.entity.dify.query.DifyQueryEntity;
import com.manleytech.entity.dify.resp.DifyQueryResponse;
import reactor.core.publisher.Mono;

/**
 * Interface for external knowledge providers.
 */
public interface KnowledgeProvider {

    /**
     * Gets the name of the provider strategy (e.g., "bailian", "ragflow").
     *
     * @return The provider name.
     */
    String getProviderName();

    /**
     * Queries the external knowledge base.
     *
     * @param queryEntity The query entity from Dify.
     * @return A Mono containing the Dify-formatted response.
     */
    Mono<DifyQueryResponse> query(DifyQueryEntity queryEntity);
}