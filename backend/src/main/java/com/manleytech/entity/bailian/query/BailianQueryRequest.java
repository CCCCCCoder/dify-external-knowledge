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
    private String query;

    /**
     * 向量检索 Top K，通过生成输入文本的向量并在知识库中检索与其向量表示最相似的 K 个文本切片。
     * K 的取值范围[0-100]。DenseSimilarityTopK和SparseSimilarityTopK二者之和小于等于 200。
     */
    private Integer denseSimilarityTopK = 100;

    /**
     * 是否开启 Rerank 重排序。
     * true：开启。
     * false：不开启。
     * 默认值为 true。
     */
    private Boolean enableReranking = true;

    /**
     * 是否开启多轮会话改写
     * <a href="https://help.aliyun.com/zh/model-studio/rag-optimization?spm=a2c4g.11186623.0.0.1383236aQtQyG1#b7031e2ad6cji">详见</a>
     * true：开启。
     * false：不开启。
     * 默认值为 false。
     */
    private Boolean enableRewrite = false;

    /**
     * 相似度阈值。该阈值表示允许召回的文本切片的最低相似度分数，用于筛选 Rank 模型返回的文本切片，
     * 即只有分数超过此数值的文本切片才会被召回。更多信息，请参见知识库。取值范围[0.01-1.00]。此参数的优先级大于知识库相似度阈值配置。
     * 当未指定具体值时，默认采用该知识库配置的相似度阈值。
     */
    private float rerankMinScore = 0.01f;

    /**
     * Rerank 后的 Top N 返回数据。取值范围[1-20]，默认值为 5。
     */
    private Integer rerankTopN = 5;

    /**
     * 关键词检索 TopK，即在知识库中查找与输入文本的关键词精确匹配的切片。
     * 取值范围[0-100]。DenseSimilarityTopK和SparseSimilarityTopK二者之和小于等于 200。
     * 默认值为：100。
     */
    private Integer sparseSimilarityTopK = 100;

    /**
     * 知识库所属的业务空间 ID。
     */
    @NotBlank
    private String workspaceId;

    /**
     * 知识库 ID。
     */
    @NotBlank
    private String indexId;

    /**
     * 是否保存历史文本切片召回测试数据。
     * true：保存。
     * false：不保存。
     * 默认值为：false。
     */
    private Boolean saveRetrieverHistory = false;
}
