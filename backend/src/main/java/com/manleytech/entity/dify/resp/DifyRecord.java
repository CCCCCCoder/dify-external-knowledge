package com.manleytech.entity.dify.resp;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import java.util.Map;

@Data
@Serdeable
public class DifyRecord {
    /**
     * 包含知识库中数据源的文本块
     */
    private String content;
    /**
     * 结果与查询的相关性分数，范围：0~1
     */
    private double score;
    /**
     * 文档标题
     */
    private String title;
    /**
     * 包含数据源中文档的元数据属性及其值
     */
    private Map<String, Object> metadata;
}
