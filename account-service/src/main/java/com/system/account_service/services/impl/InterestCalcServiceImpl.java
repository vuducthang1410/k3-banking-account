package com.system.account_service.services.impl;

import com.system.account_service.entities.CreditAccount;
import com.system.account_service.entities.SavingAccount;
import com.system.account_service.services.CreditAccountService;
import com.system.account_service.services.InterestCalcService;
import com.system.account_service.services.SavingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
public class InterestCalcServiceImpl implements InterestCalcService {
    private final CreditAccountService creditAccountService;
    private final SavingAccountService savingAccountService;

    @Override
    public void calcCreditInterest(String accountId) {
        CreditAccount account = creditAccountService.getDataId(accountId);
        BigDecimal rate = account.getInterestRate().getRate();

        BigDecimal interestAmount = account.getDebtBalance().multiply(rate.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));

        creditAccountService.updateDebtBalance(accountId, account.getDebtBalance().add(interestAmount));
    }

    @Override
    public void calcSavingInterest(String accountId) {
        SavingAccount account = savingAccountService.getDataId(accountId);
        BigDecimal rate = account.getInterestRate().getRate();

        BigDecimal interestAmount = account.getBalance().multiply(rate.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));

        savingAccountService.updateBalance(accountId, account.getBalance().add(interestAmount));
    }
}
