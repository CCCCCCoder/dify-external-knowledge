package com.manleytech.entity.dify.query;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

@Data
@Serdeable
public class RetrievalSetting {
    private Integer top_k;
    private Double score_threshold;

}