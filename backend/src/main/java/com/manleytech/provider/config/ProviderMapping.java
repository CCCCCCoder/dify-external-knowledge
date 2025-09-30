package com.manleytech.provider.config;

import lombok.Data;

/**
 * 提供者映射类，用于将Dify的knowledge_id映射到对应的提供者和目标ID
 */
@Data
public class ProviderMapping {
    /**
     * 提供者名称，如 "bailian" 或 "ragflow"
     */
    private String provider;

    /**
     * 目标提供者中的知识库ID
     */
    private String targetId;
    
    /**
     * 工作空间ID（特定于某些提供者，如百炼）
     */
    private String workspaceId;
}