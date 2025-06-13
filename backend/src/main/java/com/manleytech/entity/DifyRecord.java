package com.manleytech.entity;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import java.util.Map;

@Data
@Serdeable
public class DifyRecord {
    private String content;
    private double score;
    private String title;
    private Map<String, Object> metadata;
}
