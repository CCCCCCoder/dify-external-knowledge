package com.manleytech.entity.bailian.query;

import com.manleytech.constant.bailian.QueryHistoryRole;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

@Data
@Serdeable
public class QueryHistory {
    /**
     * 角色。
     * user：传入此角色，此时 content 表示您输入给阿里云百炼应用的文本。
     * assistant：传入此角色，此时 content 表示阿里云百炼应用的回复。
     * {@link QueryHistoryRole}
     */
    private String role;

    /**
     * 对应角色的问题或者回答内容。
     */
    private String content;
}
