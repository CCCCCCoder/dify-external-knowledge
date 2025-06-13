package com.manleytech.entity;

import com.manleytech.constant.ComparisonOperator;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.Data;

@Data
@Serdeable
public class Condition {
    private List<String> name;

    /**
     * 比较操作符，支持的值请参考 {@link ComparisonOperator} 常量类
     * 包括：contains, not contains, start with, end with, is, is not,
     * empty, not empty, =, ≠, >, <, ≥, ≤, before, after
     */
    private String comparison_operator;

    /**
     * 对比值
     * 当操作符为 empty、not empty 时可省略
     */
    private String value;
}
