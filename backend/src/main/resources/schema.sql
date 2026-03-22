-- =====================================================
-- dify-external-knowledge 数据库表结构
-- =====================================================
-- 注意: 当前系统使用 application.yml 静态配置
-- 以下表结构仅供参考，用于未来支持数据库动态配置
-- =====================================================

-- 知识库映射表
-- 将Dify的knowledge_id映射到具体的RAG框架和目标ID
CREATE TABLE IF NOT EXISTS knowledge_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    knowledge_id VARCHAR(255) NOT NULL UNIQUE COMMENT 'Dify知识库ID',
    provider_type VARCHAR(50) NOT NULL COMMENT 'RAG框架类型: bailian/ragflow',
    target_id VARCHAR(255) NOT NULL COMMENT 'RAG框架中的知识库ID或数据集ID',
    workspace_id VARCHAR(255) COMMENT '工作空间ID(百炼专用)',
    config_params JSON COMMENT '额外配置参数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_provider_type (provider_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库映射表';

-- RAG框架配置表
-- 存储各RAG框架的API凭证和默认参数
CREATE TABLE IF NOT EXISTS rag_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    provider_type VARCHAR(50) NOT NULL UNIQUE COMMENT 'RAG框架类型: bailian/ragflow',
    endpoint VARCHAR(500) NOT NULL COMMENT 'API端点URL',
    api_key VARCHAR(500) NOT NULL COMMENT 'API密钥',
    config_params JSON COMMENT '框架特定参数(sparseSimilarityTopK,denseSimilarityTopK等)',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_provider_type (provider_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG框架配置表';

-- API日志表
-- 记录所有API请求和响应
CREATE TABLE IF NOT EXISTS api_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    request_id VARCHAR(100) NOT NULL COMMENT '请求唯一ID',
    knowledge_id VARCHAR(255) COMMENT '知识库ID',
    provider_type VARCHAR(50) COMMENT 'RAG框架类型',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(20) COMMENT '请求方法',
    request_headers JSON COMMENT '请求头',
    request_body JSON COMMENT '请求体',
    response_status INT COMMENT '响应状态码',
    response_body JSON COMMENT '响应体',
    response_time BIGINT COMMENT '响应时间(毫秒)',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_request_id (request_id),
    INDEX idx_knowledge_id (knowledge_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API日志表';

-- 配置变更历史表
-- 记录配置变更的审计轨迹
CREATE TABLE IF NOT EXISTS config_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型: mapping/rag_config',
    config_id BIGINT NOT NULL COMMENT '配置的ID',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型: CREATE/UPDATE/DELETE',
    old_value JSON COMMENT '变更前的值',
    new_value JSON COMMENT '变更后的值',
    operator VARCHAR(100) COMMENT '操作人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_config_type (config_type),
    INDEX idx_config_id (config_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置变更历史表';

-- =====================================================
-- 初始化数据示例 (可选)
-- =====================================================

-- INSERT INTO rag_config (provider_type, endpoint, api_key, config_params) VALUES
-- ('bailian', 'https://bailian.aliyuncs.com', 'your-api-key', '{"sparseSimilarityTopK": 100, "denseSimilarityTopK": 100}'),
-- ('ragflow', 'http://localhost:9380', 'your-api-key', '{"defaultTopK": 5, "similarityThreshold": 0.7}');

-- INSERT INTO knowledge_mapping (knowledge_id, provider_type, target_id, workspace_id) VALUES
-- ('kb-bailian-001', 'bailian', 'kb-xxx', 'llm-xxx'),
-- ('kb-ragflow-001', 'ragflow', 'dataset-xxx', NULL);
