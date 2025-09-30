package com.manleytech.entity.dify.resp;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;

@Data
@Introspected
public class DifyError {
    private String message;
}