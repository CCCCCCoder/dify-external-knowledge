# Dify外部知识库API接口规范

## 概述

本文档描述了dify-external-knowledge-api项目如何接收Dify系统的知识库查询请求，并将查询结果按照Dify要求的格式返回给Dify系统。

## 接口端点

```
POST /dify/retrieval
```

## 请求头

| 参数名            | 是否必需 | 类型   | 描述                           | 示例值                          |
| ----------------- | -------- | ------ | ------------------------------ | ------------------------------- |
| Content-Type      | 是       | 字符串 | 请求内容类型                   | application/json                |
| Authorization     | 是       | 字符串 | API密钥，格式为Bearer {API_KEY} | Bearer your-dify-api-key        |

## 请求体结构

### 请求参数

| 属性                | 是否必需 | 类型   | 描述                     | 示例值         |
| ------------------- | -------- | ------ | ------------------------ | -------------- |
| knowledge_id        | 是       | 字符串 | 知识库唯一ID，用于映射到具体的RAG框架 | kb-bailian-001 |
| query               | 是       | 字符串 | 用户的查询内容           | Dify是什么？    |
| retrieval_setting   | 是       | 对象   | 知识检索参数             | 见下文         |
| metadata_condition  | 否       | 对象   | 元数据筛选条件           | 见下文         |

### retrieval_setting 参数

| 属性            | 是否必需 | 类型   | 描述                         | 示例值 |
| --------------- | -------- | ------ | ---------------------------- | ------ |
| top_k           | 是       | 整数   | 检索结果的最大数量           | 5      |
| score_threshold | 是       | 浮点数 | 结果与查询相关性的分数限制，范围：0~1 | 0.5    |

### metadata_condition 参数

| 属性                | 是否必需 | 类型   | 描述                              | 示例值 |
| ------------------- | -------- | ------ | --------------------------------- | ------ |
| logical_operator    | 否       | 字符串 | 逻辑操作符，取值为 `and` 或 `or`，默认 `and` | and    |
| conditions          | 是       | 数组   | 条件列表                          | 见下文 |

### conditions 数组元素

| 属性                | 是否必需 | 类型     | 描述                                                   | 示例值                   |
| ------------------- | -------- | -------- | ------------------------------------------------------ | ------------------------ |
| name                | 是       | 数组     | 需要筛选的 metadata 名称                                  | ["category", "tag"]      |
| comparison_operator | 是       | 字符串   | 比较操作符                                               | contains                 |
| value               | 否       | 字符串   | 对比值，当操作符为 `empty`、`not empty`、`null`、`not null` 时可省略 | "AI"                     |

## 请求示例

```json
POST /dify/retrieval HTTP/1.1
Content-Type: application/json
Authorization: Bearer your-dify-api-key

{
    "knowledge_id": "kb-bailian-001",
    "query": "Dify是什么？",
    "retrieval_setting": {
        "top_k": 3,
        "score_threshold": 0.5
    },
    "metadata_condition": {
        "logical_operator": "and",
        "conditions": [
            {
                "name": ["category"],
                "comparison_operator": "contains",
                "value": "AI"
            }
        ]
    }
}
```

## 响应结构

### 成功响应

| 属性     | 是否必需 | 类型   | 描述                  | 示例值 |
| -------- | -------- | ------ | --------------------- | ------ |
| records  | 是       | 数组   | 从知识库查询的记录列表 | 见下文 |

### records 数组元素

| 属性     | 是否必需 | 类型   | 描述                  | 示例值                  |
| -------- | -------- | ------ | --------------------- | ------------------------ |
| content  | 是       | 字符串 | 包含知识库中数据源的文本块 | Dify：GenAI 应用程序的创新引擎 |
| score    | 是       | 浮点数 | 结果与查询的相关性分数，范围：0~1 | 0.98                     |
| title    | 是       | 字符串 | 文档标题              | Dify 简介                |
| metadata | 否       | 对象   | 包含数据源中文档的元数据属性及其值 | 见示例                  |

### 成功响应示例

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
    "records": [
        {
            "content": "Dify 是一个开源的 LLM 应用开发平台。它提供了直观的界面，让用户能够快速构建和部署基于大语言模型的应用程序。",
            "score": 0.98,
            "title": "Dify 产品介绍",
            "metadata": {
                "source": "official_docs",
                "category": "product",
                "language": "zh-CN",
                "last_updated": "2024-01-15"
            }
        },
        {
            "content": "作为 GenAI 应用程序的创新引擎，Dify 支持多种大语言模型，包括 GPT、Claude 等，并提供了丰富的工具集成能力。",
            "score": 0.85,
            "title": "Dify 技术特性",
            "metadata": {
                "source": "technical_docs",
                "category": "technology",
                "language": "zh-CN",
                "last_updated": "2024-01-10"
            }
        },
        {
            "content": "Dify 的核心功能包括知识库管理、对话流程设计、API 集成等，帮助企业快速构建智能客服、内容生成等应用。",
            "score": 0.72,
            "title": "Dify 功能概述",
            "metadata": {
                "source": "user_guide",
                "category": "features",
                "language": "zh-CN",
                "last_updated": "2024-01-05"
            }
        }
    ]
}
```

### 错误响应

| 属性        | 是否必需 | 类型   | 描述       | 示例值                                              |
| ----------- | -------- | ------ | ---------- | --------------------------------------------------- |
| error_code  | 是       | 整数   | 错误代码    | 1001                                                |
| error_msg   | 是       | 字符串 | API 异常描述 | 无效的 Authorization 头格式。预期格式为 `Bearer <api-key>`。 |

### 错误代码说明

| 代码   | 描述                    |
| ------ | --------------------- |
| 1001   | 无效的 Authorization 头格式 |
| 1002   | 授权失败                  |
| 2001   | 知识库不存在                |
| 3001   | RAG框架连接失败            |
| 3002   | 查询参数格式错误            |
| 5001   | 内部服务器错误              |

### 错误响应示例

```json
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
    "error_code": 1002,
    "error_msg": "授权失败，请检查API密钥是否正确"
}
```

```json
HTTP/1.1 404 Not Found
Content-Type: application/json

{
    "error_code": 2001,
    "error_msg": "知识库不存在，请检查knowledge_id是否正确"
}
```

```json
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
    "error_code": 3001,
    "error_msg": "RAG框架连接失败，请检查网络连接和配置"
}
```

## 项目内部处理流程

### 1. 请求接收与验证

1. **接收请求**：DifyController接收POST `/dify/retrieval`请求
2. **身份验证**：验证Authorization头中的API密钥
3. **参数验证**：验证请求参数的完整性和格式

### 2. 知识库映射查找

1. **查找映射**：根据knowledge_id从数据库中查找对应的RAG框架配置
2. **获取配置**：获取目标RAG框架的API端点、认证信息和参数配置
3. **验证状态**：检查映射配置和RAG框架配置是否处于启用状态

### 3. 请求转发与处理

1. **参数转换**：将Dify的查询参数转换为对应RAG框架的参数格式
2. **发送请求**：调用目标RAG框架的API进行查询
3. **结果处理**：接收RAG框架的响应并进行处理

### 4. 响应格式化

1. **结果转换**：将RAG框架的响应转换为Dify要求的格式
2. **参数映射**：确保所有必需字段都存在且格式正确
3. **返回响应**：将格式化后的响应返回给Dify系统

### 5. 日志记录

1. **请求日志**：记录接收到的请求详情
2. **处理日志**：记录请求处理过程中的关键信息
3. **响应日志**：记录返回给Dify的响应详情
4. **错误日志**：记录处理过程中的错误信息

## 支持的RAG框架参数映射

### 阿里云百炼参数映射

| Dify参数          | 百炼参数                  | 说明                     |
| ----------------- | ------------------------- | ------------------------ |
| query             | query                     | 查询内容                 |
| top_k             | denseSimilarityTopK + sparseSimilarityTopK | 检索数量               |
| score_threshold   | rerankMinScore            | 相似度阈值               |
| metadata_filter   | filter                    | 元数据过滤条件           |

### RAGflow参数映射

| Dify参数          | RAGflow参数    | 说明           |
| ----------------- | -------------- | -------------- |
| query             | query          | 查询内容       |
| top_k             | top_k          | 检索数量       |
| score_threshold   | score_threshold | 相似度阈值     |
| metadata_filter   | metadata_filter | 元数据过滤条件 |

## 配置要求

### 1. 知识库映射配置

在数据库的`knowledge_mapping`表中需要配置：

- `knowledge_id`: Dify系统中的知识库ID
- `provider_type`: RAG框架类型（bailian/ragflow）
- `target_id`: RAG框架中的知识库ID
- `workspace_id`: 工作空间ID（百炼专用）
- `config_params`: 额外的配置参数（JSON格式）

### 2. RAG框架配置

在数据库的`rag_config`表中需要配置：

- `provider_type`: RAG框架类型
- `endpoint`: API端点URL
- `api_key`: API密钥
- `config_params`: 框架特定参数（JSON格式）

## 安全考虑

1. **API密钥验证**：所有请求必须包含有效的API密钥
2. **参数验证**：对所有输入参数进行严格验证
3. **错误处理**：避免在错误信息中泄露敏感信息
4. **访问限制**：可根据需要实施IP白名单等访问限制
5. **日志脱敏**：在日志中隐藏敏感信息如API密钥

## 监控指标

1. **请求量**：统计接收到的查询请求数量
2. **响应时间**：记录每个请求的处理时间
3. **成功率**：统计成功和失败的请求比例
4. **错误分布**：分析各类错误的发生频率
5. **RAG框架性能**：监控各个RAG框架的响应时间和可用性