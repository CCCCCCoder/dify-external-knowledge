package com.manleytech.entity.bailian.query;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.util.Map;

@Data
@Serdeable
public class SearchFilter {

    //todo dify转换成百炼的filter规则
    Map<String, String> filter;
}
