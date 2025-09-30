package com.manleytech.entity.bailian.resp;

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
public class BailianQueryResponse {
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误码
     */
    private String code;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 数据部分
     */
    private Data data;
    
    /**
     * 请求ID
     */
    @JsonProperty("request_id")
    private String requestId;

    @lombok.Data
    @Introspected
    public static class Data {
        /**
         * 文档列表
         */
        private List<Document> documents;
    }

    @lombok.Data
    @Introspected
    public static class Document {
        /**
         * 文档ID
         */
        @JsonProperty("doc_id")
        private String docId;
        
        /**
         * 文档名称
         */
        @JsonProperty("doc_name")
        private String docName;
        
        /**
         * 文档内容
         */
        private String text;
        
        /**
         * 相似度得分
         */
        private Double score;
        
        /**
         * 元数据
         */
        private Map<String, Object> metadata;
    }
}