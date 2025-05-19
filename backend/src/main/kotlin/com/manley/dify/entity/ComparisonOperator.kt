package com.manley.dify.entity

enum class ComparisonOperator(val operator: String) {
    CONTAINS("contains"),
    NOT_CONTAINS("not contains"),
    START_WITH("start with"),
    END_WITH("end with"),
    IS("is"),
    IS_NOT("is not"),
    EMPTY("empty"),
    NOT_EMPTY("not empty"),
    EQUAL("="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN_OR_EQUAL("<="),
    BEFORE("before"),
    AFTER("after")
}