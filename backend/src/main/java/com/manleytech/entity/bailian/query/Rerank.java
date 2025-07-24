package com.manleytech.entity.bailian.query;

import com.manleytech.constant.bailian.RerankModel;
import io.micronaut.serde.annotation.Serdeable;
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
    private String ModelName;
}
