package com.system.transaction_service.service.interfaces;

import com.system.common_library.dto.transaction.*;
import com.system.common_library.enums.*;
import com.system.transaction_service.dto.response.PagedDTO;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionDetailService {

    TransactionExtraDTO findById(String id);

    PagedDTO<TransactionDTO> findAllByCondition(
            List<Direction> directionList, List<FeePayer> feePayerList, List<Initiator> initiatorList,
            List<Method> methodList, List<TransactionType> transactionTypeList, List<State> stateList, List<Type> typeList,
            String search, BigDecimal amountStart, BigDecimal amountEnd, String sort, int page, int limit);

    TransactionInitDTO createExternal(CreateExternalDTO create);

    TransactionExtraDTO confirmExternal(String transactionId, String otp);

    TransactionInitDTO createInternal(CreateInternalDTO create);

    TransactionExtraDTO confirmInternal(String transactionId, String otp);

    TransactionInitDTO createPayment(CreatePaymentDTO create);

    TransactionExtraDTO confirmPayment(String transactionId, String otp);

    TransactionExtraDTO createSystem(CreateSystemDTO create);

    void rollback(String transactionId);
}
