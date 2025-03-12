package com.system.account_service.services.impl;

import com.system.account_service.dtos.branch.BranchBankingDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.BranchBanking;
import com.system.account_service.exception.payload.ResourceNotFoundException;
import com.system.account_service.repositories.BranchBankingRepository;
import com.system.account_service.services.BranchBankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchBankingServiceImpl implements BranchBankingService {
    private final BranchBankingRepository repository;

    @Override
    public BranchBanking create(BranchBankingDTO data) {
        BranchBanking interestRate = BranchBanking.builder()
                .branchName(data.getBranchName())
                .address(data.getAddress())
                .description(data.getDescription())
                .build();

        return repository.save(interestRate);
    }

    @Override
//    @CachePut(value = "branch_banking", key = "#id")
    public BranchBanking update(String id, BranchBankingDTO data) {
        BranchBanking interestRate = repository.findByBranchIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        interestRate.setBranchName(data.getBranchName());
        interestRate.setAddress(data.getAddress());
        interestRate.setDescription(data.getDescription());

        return repository.save(interestRate);
    }

    @Override
//    @CacheEvict(value = "branch_banking", key = "#id")
    public void delete(String id) {
        BranchBanking data = repository.findByBranchIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        data.setDeleted(true);
        repository.save(data);
    }

    @Override
//    @CacheEvict(value = "branch_banking", key = "#id")
    public void deleteIds(List<String> ids) {
        repository.softDeleteByIds(ids);
    }

    @Override
//    @Cacheable(value = "branch_banking", key = "#id", unless = "#result == null")
    public BranchBanking findById(String id) {
        return repository.findByBranchIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public PageDataDTO<BranchBanking> findPagination(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by("createdAt"));
        Page<BranchBanking> pageData = repository.findAllByDeleted(false, pageable);
        List<BranchBanking> listData = pageData.stream().toList();

        return PageDataDTO.<BranchBanking> builder()
                .total(pageData.getTotalElements())
                .listData(listData)
                .build();
    }

    @Override
    public List<BranchBanking> findAll() {
        return repository.findAll();
    }
}
