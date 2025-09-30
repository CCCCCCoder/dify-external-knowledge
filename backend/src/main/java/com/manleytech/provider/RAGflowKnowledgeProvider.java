package com.manleytech.provider;

import com.manleytech.entity.dify.query.DifyQueryEntity;
import com.manleytech.entity.dify.resp.DifyQueryResponse;
import com.manleytech.entity.dify.resp.DifyRecord;
import com.manleytech.entity.ragflow.query.RAGflowQueryRequest;
import com.manleytech.entity.ragflow.resp.RAGflowQueryResponse;
import com.manleytech.provider.client.RAGflowApiClient;
import com.manleytech.provider.config.KnowledgeProviderProperties;
import com.manleytech.provider.config.ProviderMapping;
import com.manleytech.provider.config.RAGflowApiProperties;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.stream.Collectors;

@Singleton
public class RAGflowKnowledgeProvider implements KnowledgeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RAGflowKnowledgeProvider.class);
    
    private final RAGflowApiClient apiClient;
    private final RAGflowApiProperties apiProperties;
    private final KnowledgeProviderProperties providerProperties;

    public RAGflowKnowledgeProvider(RAGflowApiClient apiClient, RAGflowApiProperties apiProperties, KnowledgeProviderProperties providerProperties) {
        this.apiClient = apiClient;
        this.apiProperties = apiProperties;
        this.providerProperties = providerProperties;
    }

    @Override
    public String getProviderName() {
        return "ragflow";
    }

    @Override
    public Mono<DifyQueryResponse> query(DifyQueryEntity queryEntity) {
        LOG.debug("Processing RAGflow query for knowledge_id: {}", queryEntity.getKnowledge_id());
        
        ProviderMapping mapping = providerProperties.getProviders().get(queryEntity.getKnowledge_id());
        if (mapping == null) {
            return Mono.error(new IllegalArgumentException("No provider mapping found for knowledge_id: " + queryEntity.getKnowledge_id()));
        }

        RAGflowQueryRequest ragflowRequest = createRAGflowRequest(queryEntity, mapping.getTargetId());
        String authToken = "Bearer " + apiProperties.getKey();

        LOG.debug("Sending request to RAGflow API: {}", ragflowRequest);
        return apiClient.query(authToken, ragflowRequest)
                .doOnNext(response -> LOG.debug("Received response from RAGflow API: {}", response))
                .map(this::mapToDifyResponse)
                .doOnError(error -> LOG.error("Error querying RAGflow API", error));
    }

    private RAGflowQueryRequest createRAGflowRequest(DifyQueryEntity difyQuery, String kbId) {
        RAGflowQueryRequest request = new RAGflowQueryRequest();
        request.setQuery(difyQuery.getQuery());
        request.setKbId(kbId);
        
        // Set default values from configuration
        request.setTopK(apiProperties.getDefaultTopK());

        // Override with values from the Dify request if provided
        if (difyQuery.getRetrieval_setting() != null) {
            if (difyQuery.getRetrieval_setting().getTop_k() != null && difyQuery.getRetrieval_setting().getTop_k() > 0) {
                request.setTopK(difyQuery.getRetrieval_setting().getTop_k());
            }
            
            if (difyQuery.getRetrieval_setting().getScore_threshold() != null && 
                difyQuery.getRetrieval_setting().getScore_threshold() > 0) {
                request.setScoreThreshold(difyQuery.getRetrieval_setting().getScore_threshold());
            }
        }
        
        return request;
    }

    private DifyQueryResponse mapToDifyResponse(RAGflowQueryResponse ragflowResponse) {
        DifyQueryResponse difyResponse = new DifyQueryResponse();
        if ("SUCCESS".equals(ragflowResponse.getRet().toUpperCase()) && 
            ragflowResponse.getData() != null && 
            ragflowResponse.getData().getDocs() != null) {
            difyResponse.setRecords(ragflowResponse.getData().getDocs().stream()
                    .map(doc -> {
                        DifyRecord record = new DifyRecord();
                        record.setContent(doc.getContent());
                        record.setScore(doc.getSimilarity());
                        record.setTitle(doc.getDocName());
                        return record;
                    })
                    .collect(Collectors.toList()));
        } else {
            difyResponse.setRecords(Collections.emptyList());
        }
        return difyResponse;
    }
}