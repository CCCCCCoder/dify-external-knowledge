package com.manleytech.entity.dify.query;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Serdeable
public class DifyQueryEntity {
    @NotBlank
    private String knowledge_id;

    @NotBlank
    private String query;

    @NotNull
    private RetrievalSetting retrieval_setting;

    private MetadataCondition metadata_condition;
}
