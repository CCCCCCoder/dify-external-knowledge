package com.manleytech.entity.bailian.query;

import com.manleytech.constant.bailian.RerankModel;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Serdeable
public class Rerank {
    /**
     * Rank 模型名称。
     * gte-rerank-hybrid：官方推荐。
     * gte-rerank：GTE 排序模型。
     * {@link RerankModel}
     */
    private String modelName = RerankModel.GTE_RERANK_HYBRID;

    /**
     * 相似度阈值。该阈值表示允许召回的文本切片的最低相似度分数，用于筛选 Rank 模型返回的文本切片。
     * 即只有分数超过此数值的文本切片才会被召回。取值范围 [0.01-1.00]。此参数优先级高于知识库相似度阈值配置。
     * 未指定时，默认采用知识库配置的相似度阈值。
     */
    @Min(0)
    @Max(1)
    private Float rerankMinScore;

    /**
     * 重排序后的 Top N 返回数据。取值范围 [1-20]，默认值为 5。
     */
    @Min(1)
    @Max(20)
    private Integer rerankTopN = 5;
}
