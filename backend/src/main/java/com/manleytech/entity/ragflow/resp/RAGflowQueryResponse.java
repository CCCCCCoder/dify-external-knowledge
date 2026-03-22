package com.manleytech.entity.ragflow.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class RAGflowQueryResponse {
    /**
     * 返回码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * 返回码 (legacy string field)
     */
    private String ret;

    /**
     * 数据部分
     */
    private Data data;

    @lombok.Data
    @Introspected
    public static class Data {
        /**
         * 文档列表 (chunks)
         */
        @JsonProperty("chunks")
        private List<Doc> chunks;
    }

    @lombok.Data
    @Introspected
    public static class Doc {
        /**
         * 文档ID
         */
        @JsonProperty("document_id")
        private String documentId;

        /**
         * 文档名称
         */
        @JsonProperty("doc_name")
        private String docName;

        /**
         * 文档关键词
         */
        @JsonProperty("document_keyword")
        private String documentKeyword;

        /**
         * 文档内容
         */
        private String content;

        /**
         * 相似度得分
         */
        private Double similarity;

        /**
         * 向量相似度
         */
        @JsonProperty("vector_similarity")
        private Double vectorSimilarity;

        /**
         * 词项相似度
         */
        @JsonProperty("term_similarity")
        private Double termSimilarity;

        /**
         * 元数据
         */
        private Map<String, Object> metadata;
    }

}