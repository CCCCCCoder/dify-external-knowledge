package com.manleytech.entity.bailian.query;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(0)
    @Max(100)
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
     * 重排序配置。
     */
    private Rerank rerank;

    /**
     * 多轮对话改写配置。
     */
    private Rewrite rewrite;

    /**
     * 关键词检索 TopK，即在知识库中查找与输入文本的关键词精确匹配的切片。
     * 取值范围[0-100]。DenseSimilarityTopK和SparseSimilarityTopK二者之和小于等于 200。
     * 默认值为：100。
     */
    @Min(0)
    @Max(100)
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

    /**
     * 支持通过 SearchFilter 设置个性化的检索条件（如标签），对语义检索结果进行过滤，排除无关信息。
     */
    private List<SearchFilter> searchFilters;

    /**
     * 支持在提问时传入图片 URL 地址。仅当查询图片问答类知识库且存在图片索引时生效。
     */
    private List<String> images;

    /**
     * 多轮对话改写支持传入自定义的对话历史。仅在 enableRewrite=true 时生效。
     */
    private List<QueryHistory> queryHistory;
}
