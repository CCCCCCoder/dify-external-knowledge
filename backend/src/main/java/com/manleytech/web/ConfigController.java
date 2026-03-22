package com.manleytech.web;

import com.manleytech.entity.dify.resp.DifyError;
import com.manleytech.provider.config.BailianApiProperties;
import com.manleytech.provider.config.KnowledgeProviderProperties;
import com.manleytech.provider.config.ProviderMapping;
import com.manleytech.provider.config.RAGflowApiProperties;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 配置管理控制器
 * 提供只读的配置API，用于前端页面展示配置信息
 */
@Controller("/api/config")
public class ConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigController.class);

    private final KnowledgeProviderProperties knowledgeProviderProperties;
    private final BailianApiProperties bailianApiProperties;
    private final RAGflowApiProperties ragflowApiProperties;

    @Inject
    public ConfigController(KnowledgeProviderProperties knowledgeProviderProperties,
                            BailianApiProperties bailianApiProperties,
                            RAGflowApiProperties ragflowApiProperties) {
        this.knowledgeProviderProperties = knowledgeProviderProperties;
        this.bailianApiProperties = bailianApiProperties;
        this.ragflowApiProperties = ragflowApiProperties;
    }

    /**
     * 获取知识库映射列表
     * 返回格式化的映射数据（静态配置）
     */
    @Get("/mappings")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<Map<String, Object>>> getMappings() {
        LOG.debug("Fetching knowledge base mappings");

        List<Map<String, Object>> mappings = new ArrayList<>();
        Map<String, ProviderMapping> providers = knowledgeProviderProperties.getProviders();

        if (providers != null) {
            int index = 1;
            for (Map.Entry<String, ProviderMapping> entry : providers.entrySet()) {
                Map<String, Object> mapping = new HashMap<>();
                mapping.put("id", index++);
                mapping.put("knowledgeId", entry.getKey());
                mapping.put("providerType", entry.getValue().getProvider());
                mapping.put("targetId", entry.getValue().getTargetId());
                mapping.put("workspaceId", entry.getValue().getWorkspaceId());
                mapping.put("configParams", new HashMap<>());
                mapping.put("status", 1); // Enabled
                mapping.put("createdAt", new Date().toString());
                mappings.add(mapping);
            }
        }

        return HttpResponse.ok(mappings);
    }

    /**
     * 创建知识库映射（静态配置不支持）
     */
    @Post("/mappings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<DifyError> createMapping(@Body Map<String, Object> data) {
        LOG.warn("Create mapping not supported - using static configuration");
        return HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new DifyError(3002, "静态配置不支持创建映射，请修改application.yml"));
    }

    /**
     * 更新知识库映射（静态配置不支持）
     */
    @Put("/mappings/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<DifyError> updateMapping(@PathVariable Integer id, @Body Map<String, Object> data) {
        LOG.warn("Update mapping not supported - using static configuration");
        return HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new DifyError(3002, "静态配置不支持更新映射，请修改application.yml"));
    }

    /**
     * 删除知识库映射（静态配置不支持）
     */
    @Delete("/mappings/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<DifyError> deleteMapping(@PathVariable Integer id) {
        LOG.warn("Delete mapping not supported - using static configuration");
        return HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new DifyError(3002, "静态配置不支持删除映射，请修改application.yml"));
    }

    /**
     * 获取RAG框架配置
     */
    @Get("/rag/{providerType}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> getRagConfig(@PathVariable String providerType) {
        LOG.debug("Fetching RAG config for provider: {}", providerType);

        Map<String, Object> config = new HashMap<>();

        if ("bailian".equalsIgnoreCase(providerType)) {
            config.put("endpoint", bailianApiProperties.getEndpoint());
            config.put("apiKey", bailianApiProperties.getKey());
            config.put("configParams", buildBailianConfigParams());
        } else if ("ragflow".equalsIgnoreCase(providerType)) {
            config.put("endpoint", ragflowApiProperties.getEndpoint());
            config.put("apiKey", ragflowApiProperties.getKey());
            config.put("configParams", buildRAGflowConfigParams());
        } else {
            config.put("error", "Unknown provider type: " + providerType);
        }

        return HttpResponse.ok(config);
    }

    /**
     * 更新RAG框架配置（静态配置不支持）
     */
    @Put("/rag/{providerType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<DifyError> updateRagConfig(@PathVariable String providerType, @Body Map<String, Object> data) {
        LOG.warn("Update RAG config not supported - using static configuration");
        return HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new DifyError(3002, "静态配置不支持更新，请修改application.yml"));
    }

    /**
     * 测试RAG框架连接
     */
    @Post("/rag/{providerType}/test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> testRagConfig(@PathVariable String providerType, @Body Map<String, Object> data) {
        LOG.info("Testing RAG config for provider: {}", providerType);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Test not implemented - this is a placeholder");
        result.put("response_time", 0);

        // TODO: Implement actual connection test
        // For now, return a placeholder response

        return HttpResponse.ok(result);
    }

    /**
     * 构建百炼配置参数
     */
    private Map<String, Object> buildBailianConfigParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("sparseSimilarityTopK", bailianApiProperties.getSparseSimilarityTopK());
        params.put("denseSimilarityTopK", bailianApiProperties.getDenseSimilarityTopK());
        params.put("enableReranking", bailianApiProperties.getEnableReranking());
        params.put("rerankMinScore", bailianApiProperties.getRerankMinScore());
        params.put("rerankTopN", bailianApiProperties.getRerankTopN());
        params.put("enableRewrite", bailianApiProperties.getEnableRewrite());
        params.put("saveRetrieverHistory", bailianApiProperties.getSaveRetrieverHistory());
        return params;
    }

    /**
     * 构建RAGflow配置参数
     */
    private Map<String, Object> buildRAGflowConfigParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("topK", ragflowApiProperties.getDefaultTopK());
        params.put("scoreThreshold", ragflowApiProperties.getSimilarityThreshold());
        return params;
    }
}
