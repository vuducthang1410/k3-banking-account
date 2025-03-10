package com.system.napas_service.service.interfaces;

import com.system.common_library.dto.request.transaction.CreateNapasTransactionDTO;
import com.system.common_library.dto.request.transaction.NapasTransactionRollbackDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;

public interface TransactionService {

    TransactionCoreNapasDTO create(CreateNapasTransactionDTO create);

    void rollback(NapasTransactionRollbackDTO rollback);
}
