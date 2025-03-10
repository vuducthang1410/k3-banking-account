package com.system.customer_service.exception;

public class AppException extends RuntimeException {

    private final ErrorCode errorCode; // Thông tin mã lỗi
    private final Object[] params;     // Tham số động cho thông báo

    // Constructor chỉ với ErrorCode
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.params = null; // Không có tham số
    }

    // Constructor với ErrorCode và tham số động
    public AppException(ErrorCode errorCode, Object... params) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.params = params; // Lưu tham số động
    }

    // Getter cho ErrorCode
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    // Getter cho params
    public Object[] getParams() {
        return params;
    }
}

