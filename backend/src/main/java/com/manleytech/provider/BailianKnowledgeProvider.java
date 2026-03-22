package com.manleytech.provider;

import com.manleytech.constant.bailian.RerankModel;
import com.manleytech.constant.dify.ComparisonOperator;
import com.manleytech.entity.bailian.query.BailianQueryRequest;
import com.manleytech.entity.bailian.query.Rerank;
import com.manleytech.entity.bailian.query.Rewrite;
import com.manleytech.entity.bailian.query.SearchFilter;
import com.manleytech.entity.bailian.resp.BailianQueryResponse;
import com.manleytech.entity.dify.query.Condition;
import com.manleytech.entity.dify.query.DifyQueryEntity;
import com.manleytech.entity.dify.query.MetadataCondition;
import com.manleytech.entity.dify.resp.DifyQueryResponse;
import com.manleytech.entity.dify.resp.DifyRecord;
import com.manleytech.provider.client.BailianApiClient;
import com.manleytech.provider.config.BailianApiProperties;
import com.manleytech.provider.config.KnowledgeProviderProperties;
import com.manleytech.provider.config.ProviderMapping;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class BailianKnowledgeProvider implements KnowledgeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BailianKnowledgeProvider.class);
    
    private final BailianApiClient apiClient;
    private final BailianApiProperties apiProperties;
    private final KnowledgeProviderProperties providerProperties;

    public BailianKnowledgeProvider(BailianApiClient apiClient, BailianApiProperties apiProperties, KnowledgeProviderProperties providerProperties) {
        this.apiClient = apiClient;
        this.apiProperties = apiProperties;
        this.providerProperties = providerProperties;
    }

    @Override
    public String getProviderName() {
        return "bailian";
    }

    @Override
    public Mono<DifyQueryResponse> query(DifyQueryEntity queryEntity) {
        LOG.debug("Processing Bailian query for knowledge_id: {}", queryEntity.getKnowledge_id());

        ProviderMapping mapping = providerProperties.getProviders().get(queryEntity.getKnowledge_id());
        // Factory already validates mapping exists, no need to re-check

        BailianQueryRequest bailianRequest = createBailianRequest(queryEntity, mapping.getTargetId(), mapping.getWorkspaceId());
        String authToken = "Bearer " + apiProperties.getKey();

        LOG.debug("Sending request to Bailian API: {}", bailianRequest);
        return apiClient.query(authToken, bailianRequest)
                .doOnNext(response -> LOG.debug("Received response from Bailian API: {}", response))
                .map(this::mapToDifyResponse)
                .doOnError(error -> LOG.error("Error querying Bailian API", error));
    }

    private BailianQueryRequest createBailianRequest(DifyQueryEntity difyQuery, String indexId, String workspaceId) {
        BailianQueryRequest request = new BailianQueryRequest();
        request.setQuery(difyQuery.getQuery());
        request.setIndexId(indexId);
        request.setWorkspaceId(workspaceId);
        
        // Set default values from configuration
        request.setSparseSimilarityTopK(apiProperties.getSparseSimilarityTopK());
        request.setDenseSimilarityTopK(apiProperties.getDenseSimilarityTopK());
        request.setEnableReranking(apiProperties.getEnableReranking());
        request.setEnableRewrite(apiProperties.getEnableRewrite());
        request.setSaveRetrieverHistory(apiProperties.getSaveRetrieverHistory());

        // Set up Rerank configuration if reranking is enabled
        if (apiProperties.getEnableReranking()) {
            Rerank rerank = new Rerank();
            rerank.setModelName(RerankModel.GTE_RERANK_HYBRID);
            rerank.setRerankMinScore(apiProperties.getRerankMinScore());
            rerank.setRerankTopN(apiProperties.getRerankTopN());
            request.setRerank(Collections.singletonList(rerank));
        }

        // Set up Rewrite configuration if rewrite is enabled
        if (apiProperties.getEnableRewrite()) {
            Rewrite rewrite = new Rewrite();
            rewrite.setModelName("conv-rewrite-qwen-1.8b");
            request.setRewrite(rewrite);
        }

        // Override with values from the Dify request if provided
        if (difyQuery.getRetrieval_setting() != null) {
            if (difyQuery.getRetrieval_setting().getTop_k() != null && difyQuery.getRetrieval_setting().getTop_k() > 0) {
                // Split the topK between dense and sparse retrieval (dense gets the extra if odd)
                int topK = difyQuery.getRetrieval_setting().getTop_k();
                request.setDenseSimilarityTopK((topK + 1) / 2);
                request.setSparseSimilarityTopK(topK / 2);
            }

            if (difyQuery.getRetrieval_setting().getScore_threshold() != null &&
                difyQuery.getRetrieval_setting().getScore_threshold() > 0) {
                // Update the rerank configuration with the score threshold from Dify
                if (request.getRerank() != null && !request.getRerank().isEmpty()) {
                    request.getRerank().get(0).setRerankMinScore(difyQuery.getRetrieval_setting().getScore_threshold().floatValue());
                }
            }
        }

        // Handle metadata_condition for Bailian
        if (difyQuery.getMetadata_condition() != null) {
            List<SearchFilter> searchFilters = convertMetadataCondition(difyQuery.getMetadata_condition());
            if (searchFilters != null && !searchFilters.isEmpty()) {
                request.setSearchFilters(searchFilters);
            }
        }

        return request;
    }

    /**
     * 将Dify的metadata_condition转换为Bailian的searchFilters格式
     */
    private List<SearchFilter> convertMetadataCondition(MetadataCondition condition) {
        if (condition == null || condition.getConditions() == null || condition.getConditions().isEmpty()) {
            return null;
        }

        List<Condition> conditions = condition.getConditions();

        List<SearchFilter> filters = new ArrayList<>();

        for (Condition cond : conditions) {
            if (cond.getName() == null || cond.getName().isEmpty()) {
                continue;
            }

            String fieldName = cond.getName().get(0);
            String operator = cond.getComparison_operator();
            String value = cond.getValue();

            // 构建Bailian的filter条件
            Map<String, Object> filterMap = new HashMap<>();

            if (ComparisonOperator.CONTAINS.equals(operator)) {
                filterMap.put(fieldName, value);
            } else if (ComparisonOperator.EQUAL.equals(operator)) {
                filterMap.put(fieldName, value);
            } else if (ComparisonOperator.IS.equals(operator)) {
                filterMap.put(fieldName, value);
            } else if (ComparisonOperator.EMPTY.equals(operator)) {
                filterMap.put(fieldName, "");
            } else if (ComparisonOperator.NOT_EMPTY.equals(operator)) {
                // 需要特殊处理
                filterMap.put(fieldName, value);
            }
            // Note: Bailian的searchFilters格式可能需要根据实际API文档调整

            if (!filterMap.isEmpty()) {
                SearchFilter searchFilter = new SearchFilter();
                searchFilter.setFilter(filterMap);
                filters.add(searchFilter);
            }
        }

        return filters.isEmpty() ? null : filters;
    }

    private DifyQueryResponse mapToDifyResponse(BailianQueryResponse bailianResponse) {
        DifyQueryResponse difyResponse = new DifyQueryResponse();
        if (bailianResponse.isSuccess() && 
            bailianResponse.getData() != null && 
            bailianResponse.getData().getDocuments() != null) {
            difyResponse.setRecords(bailianResponse.getData().getDocuments().stream()
                    .map(doc -> {
                        DifyRecord record = new DifyRecord();
                        record.setContent(doc.getText());
                        record.setScore(doc.getScore());
                        record.setTitle(doc.getDocName());
                        // Map metadata from Bailian response
                        if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                            record.setMetadata(new HashMap<>(doc.getMetadata()));
                        }
                        return record;
                    })
                    .collect(Collectors.toList()));
        } else {
            difyResponse.setRecords(Collections.emptyList());
        }
        return difyResponse;
    }
}