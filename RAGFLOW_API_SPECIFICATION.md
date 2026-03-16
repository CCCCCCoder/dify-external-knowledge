# RAGflow Retrieve接口规范

## 概述

本文档描述了dify-external-knowledge-api项目如何调用RAGflow的retrieve接口，以及如何处理RAGflow的响应数据。

## 接口端点

```
POST {ragflow_endpoint}/api/v1/retrieval
```

## 请求头

| 参数名            | 是否必需 | 类型   | 描述                           | 示例值                          |
| ----------------- | -------- | ------ | ------------------------------ | ------------------------------- |
| Content-Type      | 是       | 字符串 | 请求内容类型                   | application/json                |
| Authorization     | 是       | 字符串 | RAGflow API密钥，格式为Bearer {API_KEY} | Bearer ragflow-api-key-12345   |

## 请求体结构

### 请求参数

| 属性                      | 是否必需 | 类型     | 描述                                                                 | 默认值   | 示例值                                    |
| ------------------------- | -------- | -------- | -------------------------------------------------------------------- | -------- | ----------------------------------------- |
| question                  | 是       | 字符串   | 用户查询或查询关键词                                                 | -        | "What is advantage of ragflow?"          |
| dataset_ids               | 否       | 数组     | 要搜索的数据集ID列表，如果不设置则必须设置document_ids               | -        | ["b2a62730759d11ef987d0242ac120004"]       |
| document_ids              | 否       | 数组     | 要搜索的文档ID列表，确保所有选定的文档使用相同的嵌入模型             | -        | ["77df9ef4759a11ef8bdd0242ac120004"]       |
| page                      | 否       | 整数     | 指定显示块的页码                                                     | 1        | 1                                         |
| page_size                 | 否       | 整数     | 每页显示的最大块数                                                   | 30       | 10                                        |
| similarity_threshold      | 否       | 浮点数   | 最小相似度分数                                                       | 0.2      | 0.5                                       |
| vector_similarity_weight  | 否       | 浮点数   | 向量余弦相似度的权重，如果x表示向量余弦相似度的权重，则(1-x)是术语相似度权重 | 0.3      | 0.7                                       |
| top_k                     | 否       | 整数     | 参与向量余弦计算的块数量                                             | 1024     | 5                                         |
| rerank_id                 | 否       | 字符串   | 重排序模型的ID                                                       | -        | "rerank-model-001"                        |
| keyword                   | 否       | 布尔值   | 是否启用基于关键词的匹配                                             | false    | true                                      |
| highlight                 | 否       | 布尔值   | 是否在结果中启用匹配术语的高亮显示                                   | false    | true                                      |
| cross_languages           | 否       | 数组     | 应该翻译成的语言，以实现不同语言的关键词检索                         | -        | ["en", "zh", "ja"]                        |
| metadata_condition        | 否       | 对象     | 用于过滤块的元数据条件                                               | -        | {"category": "AI", "source": "official"}  |

## 请求示例

### 基本请求示例

```json
POST http://localhost:9380/api/v1/retrieval HTTP/1.1
Content-Type: application/json
Authorization: Bearer ragflow-api-key-12345

{
    "question": "What is advantage of ragflow?",
    "dataset_ids": ["b2a62730759d11ef987d0242ac120004"],
    "top_k": 5,
    "similarity_threshold": 0.5,
    "vector_similarity_weight": 0.7
}
```

### 完整参数请求示例

```json
POST http://localhost:9380/api/v1/retrieval HTTP/1.1
Content-Type: application/json
Authorization: Bearer ragflow-api-key-12345

{
    "question": "RAGflow的优势是什么？",
    "dataset_ids": ["b2a62730759d11ef987d0242ac120004"],
    "document_ids": ["77df9ef4759a11ef8bdd0242ac120004"],
    "page": 1,
    "page_size": 10,
    "similarity_threshold": 0.5,
    "vector_similarity_weight": 0.7,
    "top_k": 5,
    "rerank_id": "rerank-model-001",
    "keyword": true,
    "highlight": true,
    "cross_languages": ["en", "zh"],
    "metadata_condition": {
        "category": "AI",
        "source": "official"
    }
}
```

## 响应结构

### 成功响应

| 属性 | 是否必需 | 类型   | 描述         | 示例值 |
| ---- | -------- | ------ | ------------ | ------ |
| code | 是       | 整数   | 响应状态码   | 0      |
| data | 是       | 对象   | 响应数据     | 见下文 |

### data 对象结构

| 属性      | 是否必需 | 类型   | 描述           | 示例值 |
| --------- | -------- | ------ | -------------- | ------ |
| chunks   | 是       | 数组   | 检索到的块列表 | 见下文 |
| doc_aggs | 是       | 数组   | 文档聚合信息   | 见下文 |
| total    | 是       | 整数   | 总结果数       | 1      |

### chunks 数组元素

| 属性                   | 是否必需 | 类型     | 描述                           | 示例值                                  |
| ---------------------- | -------- | -------- | ------------------------------ | --------------------------------------- |
| content                | 是       | 字符串   | 块的内容                       | "ragflow content"                       |
| content_ltks           | 是       | 字符串   | 内容的分词结果                 | "ragflow content"                       |
| document_id            | 是       | 字符串   | 文档ID                         | "5c5999ec7be811ef9cab0242ac120005"      |
| document_keyword       | 是       | 字符串   | 文档关键词                     | "1.txt"                                 |
| highlight              | 否       | 字符串   | 高亮显示的内容                 | "<em>ragflow</em> content"              |
| id                     | 是       | 字符串   | 块的唯一ID                     | "d78435d142bd5cf6704da62c778795c5"      |
| image_id               | 否       | 字符串   | 图像ID                         | ""                                      |
| important_keywords     | 是       | 数组     | 重要关键词列表                 | [""]                                    |
| kb_id                  | 是       | 字符串   | 知识库ID                       | "c7ee74067a2c11efb21c0242ac120006"      |
| positions              | 是       | 数组     | 位置信息                       | [""]                                    |
| similarity             | 是       | 浮点数   | 综合相似度分数                 | 0.9669436601210759                      |
| term_similarity        | 是       | 浮点数   | 术语相似度分数                 | 1.0                                     |
| vector_similarity      | 是       | 浮点数   | 向量相似度分数                 | 0.8898122004035864                      |

### doc_aggs 数组元素

| 属性    | 是否必需 | 类型   | 描述     | 示例值                              |
| ------- | -------- | ------ | -------- | ----------------------------------- |
| count   | 是       | 整数   | 文档数量 | 1                                   |
| doc_id  | 是       | 字符串 | 文档ID   | "5c5999ec7be811ef9cab0242ac120005"    |
| doc_name| 是       | 字符串 | 文档名称 | "1.txt"                             |

### 成功响应示例

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
    "code": 0,
    "data": {
        "chunks": [
            {
                "content": "RAGflow是一个深度理解开源的RAG（检索增强生成）引擎。它的核心优势在于能够处理多种文档格式，提供可视化的知识库管理，并支持多种大语言模型。",
                "content_ltks": "ragflow 是 一个 深度 理解 开源 的 rag 引擎",
                "document_id": "5c5999ec7be811ef9cab0242ac120005",
                "document_keyword": "ragflow_intro.txt",
                "highlight": "<em>RAGflow</em>是一个深度理解开源的RAG（检索增强生成）引擎",
                "id": "d78435d142bd5cf6704da62c778795c5",
                "image_id": "",
                "important_keywords": [
                    "RAGflow",
                    "开源",
                    "RAG",
                    "引擎"
                ],
                "kb_id": "c7ee74067a2c11efb21c0242ac120006",
                "positions": [
                    "0-100"
                ],
                "similarity": 0.9669436601210759,
                "term_similarity": 1.0,
                "vector_similarity": 0.8898122004035864
            },
            {
                "content": "RAGflow支持多种文档格式的解析，包括PDF、Word、Excel、PPT等，能够自动提取文档内容并进行向量化处理。",
                "content_ltks": "ragflow 支持 多种 文档 格式 的 解析",
                "document_id": "5c5999ec7be811ef9cab0242ac120006",
                "document_keyword": "ragflow_features.txt",
                "highlight": "<em>RAGflow</em>支持多种文档格式的解析",
                "id": "e78435d142bd5cf6704da62c778795c6",
                "image_id": "",
                "important_keywords": [
                    "RAGflow",
                    "文档格式",
                    "解析"
                ],
                "kb_id": "c7ee74067a2c11efb21c0242ac120006",
                "positions": [
                    "0-80"
                ],
                "similarity": 0.854321601210759,
                "term_similarity": 0.9,
                "vector_similarity": 0.7898122004035864
            }
        ],
        "doc_aggs": [
            {
                "count": 1,
                "doc_id": "5c5999ec7be811ef9cab0242ac120005",
                "doc_name": "ragflow_intro.txt"
            },
            {
                "count": 1,
                "doc_id": "5c5999ec7be811ef9cab0242ac120006",
                "doc_name": "ragflow_features.txt"
            }
        ],
        "total": 2
    }
}
```

### 错误响应

| 属性    | 是否必需 | 类型   | 描述       | 示例值                    |
| ------- | -------- | ------ | ---------- | ------------------------- |
| code    | 是       | 整数   | 错误代码   | 102                       |
| message | 是       | 字符串 | 错误信息   | "`datasets` is required." |

### 常见错误代码

| 代码 | 描述                         |
| ---- | ---------------------------- |
| 102  | 数据集参数必需               |
| 103  | 文档ID参数必需               |
| 104  | 文档使用不同的嵌入模型       |
| 105  | 数据集不存在                 |
| 106  | 文档不存在                   |
| 107  | 重排序模型不存在             |
| 500  | 内部服务器错误               |

### 错误响应示例

```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
    "code": 102,
    "message": "`datasets` is required."
}
```

```json
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
    "code": 500,
    "message": "Internal server error occurred while processing the request."
}
```

## 项目内部实现

### 1. Dify参数到RAGflow参数的映射

| Dify参数          | RAGflow参数             | 转换逻辑                                                                 |
| ----------------- | ----------------------- | ------------------------------------------------------------------------ |
| query             | question                | 直接映射                                                                 |
| top_k             | top_k                   | 直接映射，默认使用数据库中的配置值                                       |
| score_threshold   | similarity_threshold     | 直接映射，默认使用数据库中的配置值                                       |
| metadata_filter   | metadata_condition       | 将Dify的metadata_filter转换为RAGflow的metadata_condition格式             |

### 2. 配置获取逻辑

1. **从数据库获取RAGflow配置**：
   - API端点：从`rag_config`表中获取`endpoint`和`api_key`
   - 默认参数：从`config_params`中获取默认的`top_k`、`similarity_threshold`等值

2. **从数据库获取知识库映射**：
   - 数据集ID：从`knowledge_mapping`表中获取`target_id`作为`dataset_ids`
   - 工作空间ID：根据需要设置其他相关参数

### 3. 请求构建逻辑

```java
public RAGflowQueryRequest buildRAGflowRequest(DifyQueryEntity difyQuery, ProviderMapping mapping) {
    RAGflowQueryRequest request = new RAGflowQueryRequest();
    
    // 基本参数映射
    request.setQuestion(difyQuery.getQuery());
    request.setDatasetIds(Arrays.asList(mapping.getTargetId()));
    
    // 检索设置映射
    if (difyQuery.getRetrieval_setting() != null) {
        // 使用Dify请求中的参数
        if (difyQuery.getRetrieval_setting().getTop_k() != null) {
            request.setTopK(difyQuery.getRetrieval_setting().getTop_k());
        }
        if (difyQuery.getRetrieval_setting().getScore_threshold() != null) {
            request.setSimilarityThreshold(difyQuery.getRetrieval_setting().getScore_threshold());
        }
    } else {
        // 使用默认配置
        request.setTopK(apiProperties.getDefaultTopK());
        request.setSimilarityThreshold(apiProperties.getSimilarityThreshold());
    }
    
    // 元数据条件映射
    if (difyQuery.getMetadata_condition() != null) {
        request.setMetadataCondition(convertMetadataCondition(difyQuery.getMetadata_condition()));
    }
    
    // 其他可选参数
    request.setVectorSimilarityWeight(0.7); // 可配置
    request.setKeyword(true); // 可配置
    request.setHighlight(true); // 可配置
    
    return request;
}
```

### 4. 响应转换逻辑

```java
public DifyQueryResponse convertToDifyResponse(RAGflowQueryResponse ragflowResponse) {
    DifyQueryResponse difyResponse = new DifyQueryResponse();
    
    if (ragflowResponse.getCode() == 0 && ragflowResponse.getData() != null) {
        List<DifyRecord> records = ragflowResponse.getData().getChunks().stream()
                .map(chunk -> {
                    DifyRecord record = new DifyRecord();
                    record.setContent(chunk.getContent());
                    record.setScore(chunk.getSimilarity());
                    record.setTitle(chunk.getDocumentKeyword());
                    
                    // 构建元数据
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("document_id", chunk.getDocumentId());
                    metadata.put("kb_id", chunk.getKb_id());
                    metadata.put("vector_similarity", chunk.getVectorSimilarity());
                    metadata.put("term_similarity", chunk.getTermSimilarity());
                    
                    // 添加重要关键词到元数据
                    if (chunk.getImportantKeywords() != null && !chunk.getImportantKeywords().isEmpty()) {
                        metadata.put("important_keywords", chunk.getImportantKeywords());
                    }
                    
                    record.setMetadata(metadata);
                    return record;
                })
                .collect(Collectors.toList());
        
        difyResponse.setRecords(records);
    } else {
        // 处理错误情况
        difyResponse.setRecords(Collections.emptyList());
    }
    
    return difyResponse;
}
```

### 5. 错误处理逻辑

```java
private Mono<DifyQueryResponse> handleRAGflowError(Throwable error) {
    if (error instanceof WebClientResponseException) {
        WebClientResponseException ex = (WebClientResponseException) error;
        
        try {
            // 尝试解析RAGflow错误响应
            RAGflowErrorResponse errorResponse = objectMapper.readValue(
                ex.getResponseBodyAsString(), 
                RAGflowErrorResponse.class
            );
            
            // 记录RAGflow错误日志
            LOG.error("RAGflow API error: code={}, message={}", 
                errorResponse.getCode(), errorResponse.getMessage());
            
            // 返回空结果而不是错误，避免影响Dify的使用体验
            return Mono.just(new DifyQueryResponse(Collections.emptyList()));
            
        } catch (JsonProcessingException e) {
            // 无法解析错误响应，记录原始错误
            LOG.error("Failed to parse RAGflow error response", e);
            return Mono.just(new DifyQueryResponse(Collections.emptyList()));
        }
    } else {
        // 其他类型的错误
        LOG.error("Unexpected error when calling RAGflow API", error);
        return Mono.just(new DifyQueryResponse(Collections.emptyList()));
    }
}
```

## 配置参数说明

### 1. RAGflow API配置（rag_config表）

| 参数名              | 类型   | 描述                           | 示例值                                   |
| ------------------- | ------ | ------------------------------ | ---------------------------------------- |
| endpoint            | 字符串 | RAGflow API端点                | http://localhost:9380                   |
| api_key             | 字符串 | RAGflow API密钥                | ragflow-api-key-12345                   |
| default_top_k       | 整数   | 默认检索数量                   | 5                                        |
| similarity_threshold | 浮点数 | 默认相似度阈值                 | 0.5                                      |
| vector_weight       | 浮点数 | 向量相似度权重                 | 0.7                                      |
| enable_keyword      | 布尔值 | 是否启用关键词匹配             | true                                     |
| enable_highlight    | 布尔值 | 是否启用高亮显示               | true                                     |

### 2. 知识库映射配置（knowledge_mapping表）

| 参数名         | 类型   | 描述                     | 示例值                                    |
| -------------- | ------ | ------------------------ | ----------------------------------------- |
| knowledge_id   | 字符串 | Dify知识库ID            | kb-ragflow-001                           |
| provider_type  | 字符串 | 提供者类型               | ragflow                                  |
| target_id      | 字符串 | RAGflow数据集ID         | b2a62730759d11ef987d0242ac120004         |
| config_params  | 对象   | 额外配置参数（JSON格式） | {"rerank_id": "rerank-001", "cross_languages": ["en", "zh"]} |

## 监控和日志

### 1. 请求监控指标

- **请求量**：统计对RAGflow API的调用次数
- **响应时间**：记录RAGflow API的响应时间
- **成功率**：统计成功和失败的请求比例
- **错误分布**：分析各类错误的发生频率

### 2. 关键日志点

1. **请求开始**：记录请求参数和配置信息
2. **响应接收**：记录RAGflow的响应数据和状态
3. **参数转换**：记录Dify参数到RAGflow参数的转换过程
4. **错误处理**：记录详细的错误信息和堆栈
5. **性能指标**：记录各个处理阶段的耗时

### 3. 告警规则

- RAGflow API响应时间超过阈值
- RAGflow API错误率超过阈值
- RAGflow服务不可用
- 配置缺失或无效

## 性能优化建议

1. **连接池配置**：合理配置HTTP客户端连接池大小
2. **超时设置**：设置合适的连接和读取超时时间
3. **重试机制**：对于临时性错误实现重试逻辑
4. **缓存策略**：对于相同查询考虑实现结果缓存
5. **批量请求**：如果可能，考虑将多个请求合并为批量请求