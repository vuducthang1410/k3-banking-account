package com.system.common_library.exception;

import java.io.Serializable;

public class DubboException extends RuntimeException implements Serializable {

    public DubboException(String message) {
        super(message);
    }
}
