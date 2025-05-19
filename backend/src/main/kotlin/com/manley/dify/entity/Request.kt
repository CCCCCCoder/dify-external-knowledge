package com.manley.dify.entity

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class Request(
    @JsonProperty("knowledge_id")
    val knowledgeId: String,
    val query: String,
    @JsonProperty("retrieval_setting")
    val retrievalSetting: RetrievalSetting,
    @JsonProperty("metadata_condition")
    val metadataCondition: MetadataCondition? = null
)

data class RetrievalSetting(
    @JsonProperty("top_k")
    val topK: Int,
    @JsonProperty("score_threshold")
    val scoreThreshold: Float
)

data class MetadataCondition(
    @JsonProperty("logical_operator")
    val logicalOperator: String = "and",
    val conditions: List<Condition>
)

data class Condition(
    val name: List<String>,


    @JsonProperty("comparison_operator")
    val comparisonOperator: String,
    val value: String? = null
)