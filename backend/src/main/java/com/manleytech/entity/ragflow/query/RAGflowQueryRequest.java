package com.manleytech.entity.ragflow.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * RAGflow平台查询请求实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class RAGflowQueryRequest {
    /**
     * 查询内容
     */
    private String query;

    /**
     * 知识库ID
     */
    @JsonProperty("kb_id")
    private String kbId;

    /**
     * 检索数量限制
     */
    @JsonProperty("top_k")
    private Integer topK;

    /**
     * 相似度阈值
     */
    @JsonProperty("score_threshold")
    private Double scoreThreshold;

    /**
     * 元数据过滤条件
     */
    @JsonProperty("metadata_filter")
    private Map<String, Object> metadataFilter;
}