package com.manleytech.entity;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.Data;

@Data
@Serdeable
public class MetadataCondition {
    private String logical_operator = "and";
    private List<Condition> conditions;
}
