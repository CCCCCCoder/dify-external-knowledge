package com.manleytech.entity;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

@Data
@Serdeable
public class RetrievalSetting {
    private int top_k;
    private double score_threshold;
}
