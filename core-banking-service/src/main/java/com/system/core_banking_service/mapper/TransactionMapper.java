package com.system.core_banking_service.mapper;

import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.core_banking_service.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "accountNumber", source = "account.accountNumber")
    TransactionCoreNapasDTO entityToDTO(Transaction entity);
}
