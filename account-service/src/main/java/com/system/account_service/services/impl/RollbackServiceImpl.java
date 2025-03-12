package com.system.account_service.services.impl;

import com.system.account_service.client.CoreAccountClient;
import com.system.account_service.services.RollbackService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RollbackServiceImpl implements RollbackService {
    private final CoreAccountClient coreAccountClient;

    @Override
    public void rollbackCreateCoreBankingAccount(String accountId) {
        try {
            coreAccountClient.delete(accountId);
        }
        catch (FeignException fe) {
            log.error("Lỗi Feign khi gọi Core Banking: {}, Response: {}", fe.getMessage(), fe.contentUTF8(), fe);
        }
        catch (Exception e) {
            log.error("Lỗi khi gọi Core Banking: {}", e.getMessage(), e);
        }
    }
}
