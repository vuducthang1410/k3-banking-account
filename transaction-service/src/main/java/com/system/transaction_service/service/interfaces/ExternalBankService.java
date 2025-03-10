package com.system.transaction_service.service.interfaces;

import com.system.transaction_service.dto.bank.CreateExternalBankDTO;
import com.system.transaction_service.dto.bank.ExternalBankDTO;
import com.system.transaction_service.dto.bank.ExternalBankExtraDTO;
import com.system.transaction_service.dto.bank.UpdateExternalBankDTO;
import com.system.transaction_service.dto.response.PagedDTO;

public interface ExternalBankService {

    ExternalBankExtraDTO findById(String id);

    PagedDTO<ExternalBankDTO> findAllByCondition(Boolean isAvailable, String search, String sort, int page, int limit);

    void create(CreateExternalBankDTO create);

    ExternalBankExtraDTO update(UpdateExternalBankDTO update, String id);

    void delete(String id);
}
