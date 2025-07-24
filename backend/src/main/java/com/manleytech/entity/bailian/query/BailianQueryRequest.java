package com.manleytech.entity.bailian.query;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;


@Data
@Serdeable
public class BailianQueryRequest {
    /**
     * 输入文本（原始输入 prompt）。Query 的长度和字符没有限制。
     */
    private String Query;

    /**
     * 向量检索 Top K，通过生成输入文本的向量并在知识库中检索与其向量表示最相似的 K 个文本切片。
     * K 的取值范围[0-100]。DenseSimilarityTopK和SparseSimilarityTopK二者之和小于等于 200。
     */
    private Integer DenseSimilarityTopK = 100;

    /**
     * 是否开启 Rerank 重排序。
     * true：开启。
     * false：不开启。
     * 默认值为 true。
     */
    private Boolean EnableReranking = true;

    /**
     * 是否开启多轮会话改写
     * <a href="https://help.aliyun.com/zh/model-studio/rag-optimization?spm=a2c4g.11186623.0.0.1383236aQtQyG1#b7031e2ad6cji">详见</a>
     * true：开启。
     * false：不开启。
     * 默认值为 false。
     */
    private Boolean EnableRewrite = false;

    /**
     * Rank 配置。
     */
    private List<Rerank> Rerank;

    /**
     * 相似度阈值。该阈值表示允许召回的文本切片的最低相似度分数，用于筛选 Rank 模型返回的文本切片，
     * 即只有分数超过此数值的文本切片才会被召回。更多信息，请参见知识库。取值范围[0.01-1.00]。此参数的优先级大于知识库相似度阈值配置。
     * 当未指定具体值时，默认采用该知识库配置的相似度阈值。
     */
    private float RerankMinScore;

    /**
     * Rerank 后的 Top N 返回数据。取值范围[1-20]，默认值为 5。
     */
    private Integer RerankTopN;

    /**
     * 会话改写配置。
     */
    private List<Rewrite> Rewrite;


    /**
     * 关键词检索 TopK，即在知识库中查找与输入文本的关键词精确匹配的切片。
     * 取值范围[0-100]。DenseSimilarityTopK和SparseSimilarityTopK二者之和小于等于 200。
     * 默认值为：100。
     */
    private Integer SparseSimilarityTopK = 100;

    /**
     * 知识库所属的业务空间 ID。
     */
    @NotBlank
    private String WorkspaceId;

    /**
     * 知识库 ID。
     */
    @NotBlank
    private String IndexId;

    /**
     * 是否保存历史文本切片召回测试数据。
     * true：保存。
     * false：不保存。
     * 默认值为：false。
     */
    private Boolean SaveRetrieverHistory = false;

    /**
     * 支持通过 SearchFilter 设置个性化的检索条件（比如标签），
     * 对语义检索结果进行过滤，以排除与查询 Query 无关的信息。
     * 使用方法请参见 <a href="https://help.aliyun.com/zh/model-studio/how-to-use-search-filters?spm=a2c4g.11186623.0.0.1383236a6A2X8f#1e183a81a07ot"> 知识库 SearchFilters</a>
     */
    private List<SearchFilter> SearchFilters;

    /**
     * 支持在提问时传入图片 URL 地址。
     * 本字段不支持非结构化知识库（即使传入也不生效）
     * 请确保链接公开可访问且指向一个有效的图片文件。格式示例：https://example.com/downloads/pic.jpg
     */
    private List<String> Images;

    /**
     * 多轮会话改写支持您传入自己管理的对话历史。仅在 EnableRewrite=true 时生效。
     */
    private List<QueryHistory> QueryHistory;

}

