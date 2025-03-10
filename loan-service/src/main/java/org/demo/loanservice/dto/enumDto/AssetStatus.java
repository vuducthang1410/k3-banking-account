package org.demo.loanservice.dto.enumDto;

public enum AssetStatus {
    NEW,               // Mới
    PARTIALLY_LIQUIDATED, // Đã thanh lý một phần
    IN_USE,            // Đang sử dụng (đặc thù BĐS)
    ON_SALE,           // Đang giao dịch (đặc thù BĐS)
}

