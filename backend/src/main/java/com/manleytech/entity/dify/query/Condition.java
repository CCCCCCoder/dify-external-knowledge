package com.manleytech.entity.dify.query;

import com.manleytech.constant.dify.ComparisonOperator;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.Data;

@Data
@Serdeable
public class Condition {
    /**
     * 需要筛选的 metadata 名称
     * 示例：["category", "tag"]
     */
    private List<String> name;

    /**
     * 比较操作符，支持的值请参考 {@link ComparisonOperator} 常量类
     * 包括：contains, not contains, start with, end with, is, is not,
     * empty, not empty, =, ≠, >, <, ≥, ≤, before, after
     */
    private String comparison_operator;

    /**
     * 对比值
     * 当操作符为 empty、not empty、null、not null 时可省略
     */
    private String value;
}
