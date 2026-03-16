package com.manleytech.entity.bailian.query;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.util.Map;

@Data
@Serdeable
public class SearchFilter {

    /**
     * 检索条件对象（具体结构根据需求构建）
     * 支持通过 SearchFilter 设置个性化的检索条件（如标签），对语义检索结果进行过滤，排除无关信息。
     */
    private Map<String, Object> filter;
}
