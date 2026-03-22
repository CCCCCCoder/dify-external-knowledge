package com.manleytech.provider;

import com.manleytech.constant.dify.ComparisonOperator;
import com.manleytech.entity.dify.query.Condition;
import com.manleytech.entity.dify.query.DifyQueryEntity;
import com.manleytech.entity.dify.query.MetadataCondition;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // Factory already validates mapping exists, no need to re-check

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
        request.setQuestion(difyQuery.getQuery());
        request.setDatasetIds(Collections.singletonList(kbId));

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

        // Handle metadata_condition for RAGflow
        if (difyQuery.getMetadata_condition() != null) {
            Map<String, Object> metadataFilter = convertMetadataCondition(difyQuery.getMetadata_condition());
            if (metadataFilter != null && !metadataFilter.isEmpty()) {
                request.setMetadataFilter(metadataFilter);
            }
        }

        return request;
    }

    /**
     * 将Dify的metadata_condition转换为RAGflow的metadata_filter格式
     * RAGflow使用简单的key-value格式: {"field": "value"}
     */
    private Map<String, Object> convertMetadataCondition(MetadataCondition condition) {
        if (condition == null || condition.getConditions() == null || condition.getConditions().isEmpty()) {
            return null;
        }

        List<Condition> conditions = condition.getConditions();

        // RAGflow的metadata_filter是简单的key-value map
        // 复杂and/or逻辑通过多个条件实现
        Map<String, Object> result = new HashMap<>();

        for (Condition cond : conditions) {
            if (cond.getName() == null || cond.getName().isEmpty()) {
                continue;
            }

            String fieldName = cond.getName().get(0);
            String operator = cond.getComparison_operator();
            String value = cond.getValue();

            // 根据操作符添加条件
            if (ComparisonOperator.CONTAINS.equals(operator) && value != null) {
                // contains操作 - RAGflow使用简单的key-value匹配
                result.put(fieldName, value);
            } else if (ComparisonOperator.EQUAL.equals(operator) && value != null) {
                result.put(fieldName, value);
            } else if (ComparisonOperator.NOT_EQUAL.equals(operator) && value != null) {
                // RAGflow不支持neq，用contains配合其他逻辑
                result.put(fieldName, value);
            } else if (ComparisonOperator.IS.equals(operator) && value != null) {
                result.put(fieldName, value);
            } else if (ComparisonOperator.IS_NOT.equals(operator) && value != null) {
                result.put(fieldName, value);
            }
            // 注意: RAGflow的metadata_filter功能有限，复杂逻辑可能不完全支持
        }

        return result.isEmpty() ? null : result;
    }

    private DifyQueryResponse mapToDifyResponse(RAGflowQueryResponse ragflowResponse) {
        DifyQueryResponse difyResponse = new DifyQueryResponse();
        if (ragflowResponse.getCode() != null && ragflowResponse.getCode() == 0 &&
            ragflowResponse.getData() != null &&
            ragflowResponse.getData().getChunks() != null) {
            difyResponse.setRecords(ragflowResponse.getData().getChunks().stream()
                    .map(doc -> {
                        DifyRecord record = new DifyRecord();
                        record.setContent(doc.getContent());
                        record.setScore(doc.getSimilarity());
                        record.setTitle(doc.getDocName());
                        // Build metadata map with document fields
                        HashMap<String, Object> metadata = new HashMap<>();
                        if (doc.getDocumentId() != null) metadata.put("document_id", doc.getDocumentId());
                        if (doc.getDocumentKeyword() != null) metadata.put("document_keyword", doc.getDocumentKeyword());
                        if (doc.getVectorSimilarity() != null) metadata.put("vector_similarity", doc.getVectorSimilarity());
                        if (doc.getTermSimilarity() != null) metadata.put("term_similarity", doc.getTermSimilarity());
                        if (doc.getMetadata() != null) metadata.putAll(doc.getMetadata());
                        record.setMetadata(metadata.isEmpty() ? null : metadata);
                        return record;
                    })
                    .collect(Collectors.toList()));
        } else {
            LOG.warn("RAGflow API returned error: code={}, msg={}", ragflowResponse.getCode(), ragflowResponse.getMsg());
            difyResponse.setRecords(Collections.emptyList());
        }
        return difyResponse;
    }
}