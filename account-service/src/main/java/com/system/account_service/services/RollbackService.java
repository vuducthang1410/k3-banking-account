package com.system.account_service.services;

public interface RollbackService {
    void rollbackCreateCoreBankingAccount(String accountId);
}
