package com.system.transaction_service.mapper;

import com.system.transaction_service.dto.projection.TransactionHistoryProjection;
import com.system.transaction_service.dto.response.TransactionHistoryRp;
import lombok.Data;

@Data
public class MapToDto {
    public static TransactionHistoryRp mapTransactionHistoryRp(TransactionHistoryProjection transactionHistoryProjection){
        TransactionHistoryRp transactionHistoryRp=new TransactionHistoryRp();
        transactionHistoryRp.setId(transactionHistoryProjection.getId());
        transactionHistoryRp.setDateCreated(transactionHistoryProjection.getDateCreated());
        transactionHistoryRp.setAmount(transactionHistoryProjection.getAmount());
        transactionHistoryRp.setIsTransfer(transactionHistoryProjection.getIsTransfer());
        transactionHistoryRp.setReceiverAccountNumber(transactionHistoryProjection.getReceiverAccountNumber());
        transactionHistoryRp.setSenderAccountNumber(transactionHistoryProjection.getSenderAccountNumber());
        transactionHistoryRp.setTransactionType(transactionHistoryProjection.getTransactionType());
        transactionHistoryRp.setDescription(transactionHistoryProjection.getDescription());
        return transactionHistoryRp;
    }
}
