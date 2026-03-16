# 阿里云百炼知识库Retrieve接口规范

## 概述

本文档描述了dify-external-knowledge-api项目如何调用阿里云百炼的知识库查询接口，以及如何处理百炼的响应数据。

## 接口端点

```
POST {bailian_endpoint}/v1/api/knowledge/query
```

## 请求头

| 参数名            | 是否必需 | 类型   | 描述                           | 示例值                          |
| ----------------- | -------- | ------ | ------------------------------ | ------------------------------- |
| Content-Type      | 是       | 字符串 | 请求内容类型                   | application/json                |
| Authorization     | 是       | 字符串 | 百炼API密钥，格式为Bearer {API_KEY} | Bearer sk-96a7647c7e8c4decb439897c456a5237 |

## 请求体结构

### 请求参数

| 名称 | 类型 | 必填 | 描述 | 示例值 |
|------|------|------|------|--------|
| `Query` | string | 否 | 输入文本（原始输入 prompt）。Query 的长度和字符没有限制。 | 阿里云百炼平台介绍 |
| `DenseSimilarityTopK` | integer | 否 | 向量检索 Top K，通过生成输入文本的向量并在知识库中检索与其向量表示最相似的 K 个文本切片。K 的取值范围 `[0-100]`。`DenseSimilarityTopK` 和 `SparseSimilarityTopK` 二者之和 ≤ 200。<br><br>默认值为 `100`。 | 100 |
| `EnableReranking` | boolean | 否 | 是否开启重排序。更多信息，请参见知识库。<br><br>取值范围：<br>- `true`：开启<br>- `false`：不开启<br><br>默认值为 `true`。 | true |
| `EnableRewrite` | boolean | 否 | 是否开启多轮对话改写。<br><br>取值范围：<br>- `true`：开启<br>- `false`：不开启<br><br>默认值为 `false`。 | false |
| `Rerank` | array<object> | 否 | 重排序配置。 | - |
| &nbsp;&nbsp;• `ModelName` | string | 否 | 排序模型名称。更多信息，请参见知识库。<br><br>取值范围：<br>- `gte-rerank-hybrid`：官方推荐<br>- `gte-rerank`：GTE 排序模型<br><br>默认值为 `gte-rerank-hybrid`。 | gte-rerank-hybrid |
| &nbsp;&nbsp;• `RerankMinScore` | float | 否 | 相似度阈值。该阈值表示允许召回的文本切片的最低相似度分数，用于筛选 Rank 模型返回的文本切片。即只有分数超过此数值的文本切片才会被召回。取值范围 `[0.01-1.00]`。此参数优先级高于知识库相似度阈值配置。<br><br>未指定时，默认采用知识库配置的相似度阈值。 | 0.20 |
| &nbsp;&nbsp;• `RerankTopN` | integer | 否 | 重排序后的 Top N 返回数据。取值范围 `[1-20]`，默认值为 `5`。 | 5 |
| `Rewrite` | array<object> | 否 | 多轮对话改写配置。 | - |
| &nbsp;&nbsp;• `ModelName` | string | 否 | 多轮对话改写模型名称。它会基于会话上下文自动调整原始输入 prompt（用户问题）以提升检索效果。<br><br>取值范围：<br>- `conv-rewrite-qwen-1.8b`：conv-rewrite-qwen-1.8b 模型（目前只支持该模型）<br><br>默认值为空（即使用 `conv-rewrite-qwen-1.8b`）。 | conv-rewrite-qwen-1.8b |
| `SparseSimilarityTopK` | integer | 否 | 关键词检索 TopK，查找与输入文本关键词精确匹配的知识库切片，有助于过滤无关内容，提高准确性。取值范围 `[0-100]`。`DenseSimilarityTopK` 和 `SparseSimilarityTopK` 二者之和 ≤ 200。<br><br>默认值为 `100`。 | 100 |
| `WorkspaceId` | string | 是 | 知识库所属的业务空间 ID。获取方式请参见如何使用业务空间。 | llm-3shx2gu255oqxxxx |
| `IndexId` | string | 是 | 知识库 ID，即 `CreateIndex` 接口返回的 `Data.Id`。 | 5pwe0mxxxx |
| `SaveRetrieverHistory` | boolean | 否 | 是否保存历史文本切片召回测试数据。<br><br>取值范围：<br>- `true`：保存<br>- `false`：不保存<br><br>默认值为 `false`。 | false |
| `SearchFilters` | array<object> | 否 | 支持通过 `SearchFilter` 设置个性化的检索条件（如标签），对语义检索结果进行过滤，排除无关信息。使用方法请参见知识库 SearchFilters。 | - |
| &nbsp;&nbsp;• (object) | object | 否 | 检索条件对象（具体结构根据需求构建）。 | - |
| `Images` | array<string> | 否 | 支持在提问时传入图片 URL 地址。仅当查询图片问答类知识库且存在图片索引时生效。<br><br>**说明**：<br>- 不支持文档搜索/数据查询类知识库（即使传入也不生效）<br>- 图片链接需公开可访问，指向有效文件。<br><br>示例格式：`https://example.com/downloads/pic.jpg` | https://example.com/downloads/pic.jpg |
| `QueryHistory` | array<object> | 否 | 多轮对话改写支持传入自定义的对话历史。仅在 `EnableRewrite=true` 时生效。 | - |
| &nbsp;&nbsp;• `role` | string | 否 | 角色。<br><br>取值范围：<br>- `user`：代表用户输入的文本<br>- `assistant`：代表模型回复的内容 | user |
| &nbsp;&nbsp;• `content` | string | 否 | 对应角色的问题或回答内容，表示一段文本。 | （根据实际内容填写） |



## 请求示例

### 基本请求示例

```json
POST https://bailian.aliyuncs.com/v1/api/knowledge/query HTTP/1.1
Content-Type: application/json
Authorization: Bearer sk-96a7647c7e8c4decb439897c456a5237

{
    "query": "什么是人工智能？",
    "workspaceId": "llm-j62ei2m0ij16xwhk",
    "indexId": "kb-bailian-001",
    "denseSimilarityTopK": 5,
    "sparseSimilarityTopK": 5,
    "enableReranking": true,
    "rerankMinScore": 0.5,
    "rerankTopN": 5
}
```

### 完整参数请求示例

```json
POST https://bailian.aliyuncs.com/v1/api/knowledge/query HTTP/1.1
Content-Type: application/json
Authorization: Bearer sk-96a7647c7e8c4decb439897c456a5237

{
    "query": "人工智能的发展历史是什么？",
    "workspaceId": "llm-j62ei2m0ij16xwhk",
    "indexId": "kb-bailian-001",
    "denseSimilarityTopK": 10,
    "sparseSimilarityTopK": 10,
    "enableReranking": true,
    "rerankMinScore": 0.7,
    "rerankTopN": 5,
    "enableRewrite": true,
    "saveRetrieverHistory": false
}
```

## 响应结构

### 成功响应

| 属性      | 是否必需 | 类型   | 描述         | 示例值 |
| --------- | -------- | ------ | ------------ | ------ |
| success   | 是       | 布尔值 | 是否成功     | true   |
| code      | 否       | 字符串 | 错误码       | -      |
| message   | 否       | 字符串 | 错误信息     | -      |
| data      | 是       | 对象   | 响应数据     | 见下文 |
| request_id| 是       | 字符串 | 请求ID       | "req-12345" |

### data 对象结构

| 属性      | 是否必需 | 类型   | 描述           | 示例值 |
| --------- | -------- | ------ | -------------- | ------ |
| documents | 是       | 数组   | 文档列表       | 见下文 |

### documents 数组元素

| 属性      | 是否必需 | 类型   | 描述           | 示例值                                  |
| --------- | -------- | ------ | -------------- | --------------------------------------- |
| doc_id    | 是       | 字符串 | 文档ID         | "doc-abc123"                            |
| doc_name  | 是       | 字符串 | 文档名称       | "人工智能基础知识.txt"                   |
| text      | 是       | 字符串 | 文档内容       | "人工智能是指由人制造出来的机器所表现出来的智能..." |
| score     | 是       | 浮点数 | 相似度得分     | 0.85                                    |
| metadata  | 否       | 对象   | 元数据         | {"category": "技术", "source": "official"} |

### 成功响应示例

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
    "success": true,
    "request_id": "req-abc123",
    "data": {
        "documents": [
            {
                "doc_id": "doc-001",
                "doc_name": "人工智能基础知识.txt",
                "text": "人工智能（Artificial Intelligence，AI）是指由人制造出来的机器所表现出来的智能。它研究如何让计算机模拟人类的思维过程和行为。",
                "score": 0.95,
                "metadata": {
                    "category": "技术",
                    "source": "official",
                    "last_updated": "2024-01-15"
                }
            },
            {
                "doc_id": "doc-002",
                "doc_name": "AI发展历程.txt",
                "text": "人工智能的发展可以分为几个阶段：1950年代的萌芽期，1970年代的第一次AI寒冬，1980年代的复苏期，1990年代的第二次AI寒冬，以及2000年以来的快速发展期。",
                "score": 0.87,
                "metadata": {
                    "category": "历史",
                    "source": "research",
                    "last_updated": "2024-01-10"
                }
            },
            {
                "doc_id": "doc-003",
                "doc_name": "机器学习应用.txt",
                "text": "机器学习是人工智能的一个重要分支，它使计算机能够在没有明确编程的情况下学习和改进。常见的机器学习算法包括监督学习、无监督学习和强化学习。",
                "score": 0.82,
                "metadata": {
                    "category": "技术",
                    "source": "tutorial",
                    "last_updated": "2024-01-05"
                }
            }
        ]
    }
}
```

### 错误响应

| 属性      | 是否必需 | 类型   | 描述       | 示例值                    |
| --------- | -------- | ------ | ---------- | ------------------------- |
| success   | 是       | 布尔值 | 是否成功   | false                     |
| code      | 是       | 字符串 | 错误代码   | "InvalidParameter"         |
| message   | 是       | 字符串 | 错误信息   | "参数workspaceId不能为空"   |
| request_id| 是       | 字符串 | 请求ID     | "req-error-123"           |

### 错误响应示例

```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
    "success": false,
    "code": "InvalidParameter",
    "message": "参数workspaceId不能为空",
    "request_id": "req-error-123"
}
```

```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
    "success": false,
    "code": "Unauthorized",
    "message": "API密钥无效或已过期",
    "request_id": "req-auth-456"
}
```

## 项目内部实现

### 1. Dify参数到百炼参数的映射

| Dify参数          | 百炼参数                   | 转换逻辑                                                                 |
| ----------------- | -------------------------- | ------------------------------------------------------------------------ |
| query             | query                      | 直接映射                                                                 |
| top_k             | denseSimilarityTopK + sparseSimilarityTopK | 将topK平均分配给denseSimilarityTopK和sparseSimilarityTopK           |
| score_threshold   | rerankMinScore             | 直接映射，转换为float类型                                                |

### 2. 配置获取逻辑

1. **从数据库获取百炼配置**：
   - API端点：从`rag_config`表中获取`endpoint`和`api_key`
   - 默认参数：从`config_params`中获取默认的检索参数

2. **从数据库获取知识库映射**：
   - 知识库ID：从`knowledge_mapping`表中获取`targetId`作为`indexId`
   - 工作空间ID：从`knowledge_mapping`表中获取`workspaceId`

### 3. 请求构建逻辑

```java
public BailianQueryRequest buildBailianRequest(DifyQueryEntity difyQuery, ProviderMapping mapping) {
    BailianQueryRequest request = new BailianQueryRequest();
    
    // 基本参数映射
    request.setQuery(difyQuery.getQuery());
    request.setIndexId(mapping.getTargetId());
    request.setWorkspaceId(mapping.getWorkspaceId());
    
    // 设置默认配置值
    request.setSparseSimilarityTopK(apiProperties.getSparseSimilarityTopK());
    request.setDenseSimilarityTopK(apiProperties.getDenseSimilarityTopK());
    request.setEnableReranking(apiProperties.getEnableReranking());
    request.setRerankMinScore(apiProperties.getRerankMinScore());
    request.setRerankTopN(apiProperties.getRerankTopN());
    request.setEnableRewrite(apiProperties.getEnableRewrite());
    request.setSaveRetrieverHistory(apiProperties.getSaveRetrieverHistory());
    
    // 使用Dify请求中的参数覆盖默认值
    if (difyQuery.getRetrieval_setting() != null) {
        if (difyQuery.getRetrieval_setting().getTop_k() != null && 
            difyQuery.getRetrieval_setting().getTop_k() > 0) {
            // 将topK平均分配给dense和sparse检索
            int topK = difyQuery.getRetrieval_setting().getTop_k();
            request.setDenseSimilarityTopK(topK / 2);
            request.setSparseSimilarityTopK(topK - (topK / 2));
        }
        
        if (difyQuery.getRetrieval_setting().getScore_threshold() != null && 
            difyQuery.getRetrieval_setting().getScore_threshold() > 0) {
            request.setRerankMinScore(difyQuery.getRetrieval_setting().getScore_threshold().floatValue());
        }
    }
    
    return request;
}
```

### 4. 响应转换逻辑

```java
public DifyQueryResponse convertToDifyResponse(BailianQueryResponse bailianResponse) {
    DifyQueryResponse difyResponse = new DifyQueryResponse();
    
    if (bailianResponse.isSuccess() && 
        bailianResponse.getData() != null && 
        bailianResponse.getData().getDocuments() != null) {
        
        List<DifyRecord> records = bailianResponse.getData().getDocuments().stream()
                .map(doc -> {
                    DifyRecord record = new DifyRecord();
                    record.setContent(doc.getText());
                    record.setScore(doc.getScore());
                    record.setTitle(doc.getDocName());
                    
                    // 如果有元数据，则设置元数据
                    if (doc.getMetadata() != null) {
                        record.setMetadata(doc.getMetadata());
                    }
                    
                    return record;
                })
                .collect(Collectors.toList());
        
        difyResponse.setRecords(records);
    } else {
        // 处理错误情况，返回空结果
        difyResponse.setRecords(Collections.emptyList());
    }
    
    return difyResponse;
}
```

### 5. 错误处理逻辑

```java
private Mono<DifyQueryResponse> handleBailianError(Throwable error) {
    if (error instanceof WebClientResponseException) {
        WebClientResponseException ex = (WebClientResponseException) error;
        
        try {
            // 尝试解析百炼错误响应
            BailianQueryResponse errorResponse = objectMapper.readValue(
                ex.getResponseBodyAsString(), 
                BailianQueryResponse.class
            );
            
            // 记录百炼错误日志
            if (!errorResponse.isSuccess()) {
                LOG.error("Bailian API error: code={}, message={}", 
                    errorResponse.getCode(), errorResponse.getMessage());
            }
            
            // 返回空结果而不是错误，避免影响Dify的使用体验
            return Mono.just(new DifyQueryResponse(Collections.emptyList()));
            
        } catch (JsonProcessingException e) {
            // 无法解析错误响应，记录原始错误
            LOG.error("Failed to parse Bailian error response: {}", ex.getResponseBodyAsString(), e);
            return Mono.just(new DifyQueryResponse(Collections.emptyList()));
        }
    } else {
        // 其他类型的错误
        LOG.error("Unexpected error when calling Bailian API", error);
        return Mono.just(new DifyQueryResponse(Collections.emptyList()));
    }
}
```

## 配置参数说明

### 1. 百炼API配置（rag_config表）

| 参数名                   | 类型    | 描述                           | 示例值                                   |
| ------------------------ | ------- | ------------------------------ | ---------------------------------------- |
| endpoint                 | 字符串  | 百炼API端点                    | https://bailian.aliyuncs.com             |
| api_key                  | 字符串  | 百炼API密钥                    | sk-96a7647c7e8c4decb439897c456a5237      |
| sparseSimilarityTopK     | 整数    | 默认关键词检索TopK              | 100                                      |
| denseSimilarityTopK      | 整数    | 默认向量检索TopK                | 100                                      |
| enableReranking          | 布尔值  | 是否开启重排序                 | true                                     |
| rerankMinScore           | 浮点数  | 默认相似度阈值                 | 0.7                                      |
| rerankTopN               | 整数    | 默认重排序后返回数量            | 5                                        |
| enableRewrite            | 布尔值  | 是否开启多轮会话改写           | false                                    |
| saveRetrieverHistory     | 布尔值  | 是否保存检索历史                | false                                    |

### 2. 知识库映射配置（knowledge_mapping表）

| 参数名        | 类型   | 描述                     | 示例值                                |
| ------------- | ------ | ------------------------ | ------------------------------------- |
| knowledge_id  | 字符串 | Dify知识库ID            | kb-bailian-001                       |
| provider_type | 字符串 | 提供者类型               | bailian                              |
| target_id     | 字符串 | 百炼知识库ID             | kb-bailian-internal-001               |
| workspace_id  | 字符串 | 百炼工作空间ID           | llm-j62ei2m0ij16xwhk                 |

## 扩展功能

### 1. 重排序模型配置

百炼支持以下重排序模型：

| 模型名称                | 描述           | 推荐度 |
| ----------------------- | -------------- | ------ |
| gte-rerank-hybrid       | 官方推荐模型   | ⭐⭐⭐⭐⭐ |
| gte-rerank              | GTE排序模型    | ⭐⭐⭐⭐   |

### 2. 会话改写功能

- **模型名称**：conv-rewrite-qwen-1.8b
- **功能**：支持多轮对话的上下文理解，自动改写查询以包含上下文信息
- **使用场景**：适用于多轮对话场景，提高检索准确性

### 3. 检索历史保存

- **功能**：保存文本切片召回测试数据
- **用途**：用于后续分析和优化检索效果
- **建议**：在测试阶段启用，生产环境可根据需要关闭

## 监控和日志

### 1. 请求监控指标

- **请求量**：统计对百炼API的调用次数
- **响应时间**：记录百炼API的响应时间
- **成功率**：统计成功和失败的请求比例
- **错误分布**：分析各类错误的发生频率
- **检索效果**：监控平均相似度分数和检索结果数量

### 2. 关键日志点

1. **请求开始**：记录请求参数和配置信息
2. **参数转换**：记录Dify参数到百炼参数的转换过程
3. **响应接收**：记录百炼的响应数据和状态
4. **错误处理**：记录详细的错误信息和堆栈
5. **性能指标**：记录各个处理阶段的耗时

### 3. 告警规则

- 百炼API响应时间超过阈值（如5秒）
- 百炼API错误率超过阈值（如10%）
- 百炼服务不可用
- 配置缺失或无效
- 检索结果质量下降（平均相似度低于阈值）

## 性能优化建议

1. **参数调优**：
   - 根据实际需求调整denseSimilarityTopK和sparseSimilarityTopK的比例
   - 优化rerankMinScore阈值，平衡召回率和精确率
   - 合理设置rerankTopN，避免过多结果影响性能

2. **缓存策略**：
   - 对于相同查询考虑实现结果缓存
   - 缓存时间可根据知识库更新频率设置

3. **并发控制**：
   - 合理配置HTTP客户端连接池大小
   - 实现请求限流，避免触发API限制

4. **错误重试**：
   - 对于临时性错误实现重试逻辑
   - 设置合理的重试次数和间隔

## 注意事项

1. **参数限制**：
   - denseSimilarityTopK和sparseSimilarityTopK之和不能超过200
   - rerankMinScore取值范围为[0.01-1.00]
   - rerankTopN取值范围为[1-20]

2. **API限制**：
   - 注意百炼API的调用频率限制
   - 监控API配额使用情况

3. **数据安全**：
   - 妥善保管API密钥
   - 在日志中避免记录敏感信息

4. **费用控制**：
   - 合理设置检索参数，控制API调用成本
   - 监控API使用量和费用