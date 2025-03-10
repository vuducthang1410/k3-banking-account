package com.system.napas_service.mapper;

import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.napas_service.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "accountId", source = "account.accountId")
    @Mapping(target = "accountNumber", source = "account.accountNumber")
    @Mapping(target = "customerName", source = "account.customerName")
    TransactionCoreNapasDTO entityToDTO(Transaction entity);
}
