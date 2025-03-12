package com.system.account_service.services;

import com.system.account_service.dtos.branch.BranchBankingDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.BranchBanking;

import java.util.List;

public interface BranchBankingService {
    BranchBanking create(BranchBankingDTO data);

    BranchBanking update(String id, BranchBankingDTO data);

    void delete(String id);

    void deleteIds(List<String> ids);

    BranchBanking findById(String id);

    PageDataDTO<BranchBanking> findPagination(Integer page, Integer pageSize);

    List<BranchBanking> findAll();
}
