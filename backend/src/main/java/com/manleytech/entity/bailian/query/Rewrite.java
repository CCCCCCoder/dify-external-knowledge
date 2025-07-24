package com.manleytech.entity.bailian.query;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

@Data
@Serdeable
public class Rewrite {
    /**
     * 会话改写模型名称。
     * conv-rewrite-qwen-1.8b：conv-rewrite-qwen-1.8b 模型（目前只支持该模型）
     * 默认值为空，采用 conv-rewrite-qwen-1.8b 模型。
     */
    private String modelName = "conv-rewrite-qwen-1.8b";
}
