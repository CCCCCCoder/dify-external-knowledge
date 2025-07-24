package com.manleytech.constant.dify;

/**
 * 比较操作符常量类
 * 包含所有支持的元数据比较操作符
 */
public class ComparisonOperator {
    // 包含操作符
    public static final String CONTAINS = "contains";
    public static final String NOT_CONTAINS = "not contains";

    // 开头结尾操作符
    public static final String START_WITH = "start with";
    public static final String END_WITH = "end with";

    // 等值操作符
    public static final String IS = "is";
    public static final String IS_NOT = "is not";

    // 空值操作符
    public static final String EMPTY = "empty";
    public static final String NOT_EMPTY = "not empty";

    // 数值比较操作符
    public static final String EQUAL = "=";
    public static final String NOT_EQUAL = "≠";
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN = "<";
    public static final String GREATER_THAN_OR_EQUAL = "≥";
    public static final String LESS_THAN_OR_EQUAL = "≤";

    // 日期操作符
    public static final String BEFORE = "before";
    public static final String AFTER = "after";

    /**
     * 检查给定的操作符是否有效
     * @param operator 要检查的操作符
     * @return 如果操作符有效则返回true，否则返回false
     */
    public static boolean isValid(String operator) {
        return operator != null && (
            CONTAINS.equals(operator) ||
            NOT_CONTAINS.equals(operator) ||
            START_WITH.equals(operator) ||
            END_WITH.equals(operator) ||
            IS.equals(operator) ||
            IS_NOT.equals(operator) ||
            EMPTY.equals(operator) ||
            NOT_EMPTY.equals(operator) ||
            EQUAL.equals(operator) ||
            NOT_EQUAL.equals(operator) ||
            GREATER_THAN.equals(operator) ||
            LESS_THAN.equals(operator) ||
            GREATER_THAN_OR_EQUAL.equals(operator) ||
            LESS_THAN_OR_EQUAL.equals(operator) ||
            BEFORE.equals(operator) ||
            AFTER.equals(operator)
        );
    }

    /**
     * 检查给定的操作符是否需要值
     * @param operator 要检查的操作符
     * @return 如果操作符需要值则返回true，否则返回false
     */
    public static boolean requiresValue(String operator) {
        return operator != null &&
               !EMPTY.equals(operator) &&
               !NOT_EMPTY.equals(operator);
    }
}
