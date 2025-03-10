package com.system.core_banking_service.service.interfaces;

import com.system.common_library.dto.request.transaction.CoreTransactionRollbackDTO;
import com.system.common_library.dto.request.transaction.CreateExternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateInternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateSystemTransactionDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.common_library.exception.GroupValidationException;

public interface TransactionService {

    TransactionCoreNapasDTO createExternal(CreateExternalTransactionDTO create);

    TransactionCoreNapasDTO createInternal(CreateInternalTransactionDTO create);

    TransactionCoreNapasDTO createSystem(CreateSystemTransactionDTO create);

    void rollback(CoreTransactionRollbackDTO rollback) throws GroupValidationException;
}
