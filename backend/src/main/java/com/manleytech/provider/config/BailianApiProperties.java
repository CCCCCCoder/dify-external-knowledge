package com.manleytech.provider.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties("bailian.api")
public class BailianApiProperties {

    private String key;
    private String endpoint;
    private Integer sparseSimilarityTopK = 100;
    private Integer denseSimilarityTopK = 100;
    private Boolean enableReranking = true;
    private Float rerankMinScore = 0.7f;
    private Integer rerankTopN = 5;
    private Boolean enableRewrite = false;
    private Boolean saveRetrieverHistory = false;
}