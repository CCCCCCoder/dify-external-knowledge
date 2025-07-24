package com.manleytech.entity.dify.query;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.Data;

@Data
@Serdeable
public class MetadataCondition {

    /**
     * 	逻辑操作符，取值为 and 或 or，默认 and
     */
    private String logical_operator;
    private List<Condition> conditions;
}
