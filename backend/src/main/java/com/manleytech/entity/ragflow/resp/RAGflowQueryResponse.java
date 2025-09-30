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
    private String ret;
    
    /**
     * 返回消息
     */
    private String msg;
    
    /**
     * 数据部分
     */
    private Data data;

    @lombok.Data
    @Introspected
    public static class Data {
        /**
         * 文档列表
         */
        private List<Doc> docs;
    }

    @lombok.Data
    @Introspected
    public static class Doc {
        /**
         * 文档ID
         */
        private String id;
        
        /**
         * 文档名称
         */
        @JsonProperty("doc_name")
        private String docName;
        
        /**
         * 文档内容
         */
        private String content;
        
        /**
         * 相似度得分
         */
        private Double similarity;
        
        /**
         * 元数据
         */
        private Map<String, Object> metadata;
    }

}