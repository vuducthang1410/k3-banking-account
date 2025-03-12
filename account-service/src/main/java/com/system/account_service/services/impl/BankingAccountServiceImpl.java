package com.system.account_service.services.impl;

import com.system.account_service.dtos.banking.BankingRp;
import com.system.account_service.dtos.banking.CreateBankingDTO;
import com.system.account_service.dtos.banking.ReportPaymentByRangeDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.AccountCommons;
import com.system.account_service.entities.BankingAccount;
import com.system.account_service.entities.type.AccountStatus;
import com.system.account_service.exception.payload.ExistedDataException;
import com.system.account_service.exception.payload.InvalidParamException;
import com.system.account_service.exception.payload.ResourceNotFoundException;
import com.system.account_service.repositories.BankingRepository;
import com.system.account_service.services.AccountCommonService;
import com.system.account_service.services.BankingAccountService;
import com.system.account_service.services.BranchBankingService;
import com.system.account_service.utils.DateTimeUtils;
import com.system.account_service.utils.MessageKeys;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankingAccountServiceImpl implements BankingAccountService {
    private final AccountCommonService accountCommonService;
    private final BranchBankingService branchBankingService;

    private final BankingRepository repository;


    @Override
    @Transactional
    public AccountInfoDTO create(CreateBankingDTO data) {
        try {
            // create account_detail => create Banking
            AccountCommons common = accountCommonService.create(data.getAccountCommon());

            BankingAccount bankingAccount = BankingAccount.builder()
                    .nickName(data.getNickName())
                    .accountCommon(common)
                    .branch(branchBankingService.findById(data.getBranchId()))
                    .build();
            bankingAccount.setCreatedAt(common.getCreatedAt());
            bankingAccount.setUpdatedAt(common.getUpdatedAt());

            BankingAccount createdData = repository.save(bankingAccount);

            return convertAccountInfoDTO(createdData);
        }
        catch (Exception e) {
            if(e instanceof ExistedDataException) {
                throw new ExistedDataException(((ExistedDataException) e).getMsgKey());
            }
            throw new InvalidParamException(MessageKeys.DATA_CREATE_FAILURE);
        }
    }

    @Override
//    @CachePut(value = "payments", key = "#id")
    public BankingRp updateBalance(String id, BigDecimal balance) {
        BankingAccount bankingAccount = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        bankingAccount.setBalance(balance);

        BankingAccount updatedData = repository.save(bankingAccount);
        return convertRp(updatedData);
    }

    public BankingAccount updateBalanceByCommonId(String commonId, BigDecimal amount) {
        BankingAccount bankingAccount = findByCommonId(commonId);

        BigDecimal newBalance = bankingAccount.getBalance().add(amount);

        if(newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidParamException(MessageKeys.BALANCE_INSUFFICIENT);
        }

        bankingAccount.setBalance(newBalance);

        return repository.save(bankingAccount);
    }

    @Override
//    @CacheEvict(value = "payments", key = "#id")
    public void delete(String id) {
        BankingAccount data = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        data.setDeleted(true);
        repository.save(data);
    }

    @Override
//    @Cacheable(value = "payments", key = "#id", unless = "#result == null")
    public BankingRp findById(String id) {
        BankingAccount data = repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);

        return convertRp(data);
    }

    @Override
    public BankingAccount findByCifCode(String cifCode) {
        AccountCommons commons = accountCommonService.findCommonBankingByCifCode(cifCode);
        return findByCommonId(commons.getAccountCommonId());
    }

    @Override
    public BankingAccount findByCommonId(String commonId) {
        return repository.findByAccountCommon_AccountCommonIdAndDeleted(commonId, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public PageDataDTO<BankingRp> findAll(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by("createdAt"));
        Page<BankingAccount> pageData = repository.findAllByDeleted(false, pageable);
        List<BankingRp> listData = pageData.stream()
                .map(this::convertRp)
                .toList();

        return PageDataDTO.<BankingRp> builder()
                .total(pageData.getTotalElements())
                .listData(listData)
                .build();
    }

    @Override
    public List<BankingAccount> findAllByBranchId(String branchId) {
        return repository.findAccountsByBranch_BranchId(branchId);
    }

    @Override
    public BankingAccount getDataId(String id) {
        return repository.findByAccountIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<BankingAccount> getReportsByRange(AccountReportRequest request) {
        ReportPaymentByRangeDTO reportByRangeDTO = ReportPaymentByRangeDTO.builder()
                .branchId(request.getBankBranch())
                .startBalance(BigDecimal.valueOf(request.getStartBalance()))
                .endBalance(BigDecimal.valueOf(request.getEndBalance()))
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(AccountStatus.valueOf(request.getStatus().name()))
                .build();

        return repository.getReportsByRange(reportByRangeDTO);
    }


//    Convert sang model response
    private BankingRp convertRp(BankingAccount data) {
        return BankingRp.builder()
                .id(data.getAccountId())
                .nickName(data.getNickName())
                .accountCommon(data.getAccountCommon().getAccountCommonId())
                .branch(data.getBranch().getBranchId())
                .balance(data.getBalance().stripTrailingZeros().toPlainString())
                .createAt(DateTimeUtils.format(DateTimeUtils.DD_MM_YYYY_HH_MM, data.getCreatedAt()))
                .build();
    }

    private AccountInfoDTO convertAccountInfoDTO(BankingAccount data) {
        AccountCommons common = data.getAccountCommon();

        return AccountInfoDTO.builder()
                .accountId(data.getAccountId())
                .customerId(common.getCustomerId())
                .cifCode(common.getCifCode())
                .accountNumber(common.getAccountNumber())
                .currentAccountBalance(data.getBalance())
                .accountType(AccountType.valueOf(common.getAccountType().name()))
                .statusAccount(ObjectStatus.valueOf(common.getStatus().name()))
                .branchName(data.getBranch().getBranchName())
                .createdAt(common.getCreatedAt())
                .build();
    }
}
