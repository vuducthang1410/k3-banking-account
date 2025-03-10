package com.system.napas_service.service.interfaces;

import com.system.napas_service.dto.bank.BankDTO;
import com.system.napas_service.dto.bank.BankExtraDTO;
import com.system.napas_service.dto.bank.CreateBankDTO;
import com.system.napas_service.dto.bank.UpdateBankDTO;
import com.system.napas_service.dto.response.PagedDTO;

public interface BankService {

    BankExtraDTO findById(String id);

    PagedDTO<BankDTO> findAllByCondition(Boolean isAvailable, String search, String sort, int page, int limit);

    void create(CreateBankDTO create);

    void update(UpdateBankDTO update, String id);

    void delete(String id);
}
