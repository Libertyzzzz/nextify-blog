package com.nextify.blog.enums;

import lombok.Getter;

@Getter
public enum AgentRoleEnum {
    USER("user", 1),
    ASSISTANT("assistant", 2),
    SYSTEM("system", 3),
    TOOL("tool", 4);

    private String role; //

    private final String value;
    private final Integer id;

    AgentRoleEnum(String value, Integer id){
        this.value = value;
        this.id = id;
    }

}
