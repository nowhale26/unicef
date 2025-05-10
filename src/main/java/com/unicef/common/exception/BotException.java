package com.unicef.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BotException extends RuntimeException {
    private String code;
    private String name;

    public BotException(String message, String code, String name, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.name = name;
    }

    public BotException(String message, String code, String name) {
        this(message, code, name, null);
    }
}
