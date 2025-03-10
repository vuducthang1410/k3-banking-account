package com.example.notification_service.service;

import com.example.notification_service.domain.entity.BalanceFluctuation;
import com.example.notification_service.repository.BalanceFluctuationRepository;
import com.example.notification_service.service.interfaces.SystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemServiceImpl implements SystemService {

    private final BalanceFluctuationRepository balanceFluctuationRepository;
    @Override
    public Page<BalanceFluctuation> getBalanceFluctuation(Pageable pageable) {
//        log.info(balanceFluctuationRepository.findAll(pageable).getContent().toString());
        return balanceFluctuationRepository.findAll(pageable);
    }
    @Override
    public BalanceFluctuation getBalanceFluctuationById(Long id) {
        return balanceFluctuationRepository.findById(id).orElse(null);
    }
    @Override
    public Page<BalanceFluctuation> getBalanceFluctuationByCustomerCIF(String customerCIF, Pageable pageable) {
        return balanceFluctuationRepository.findByCustomerCIF(customerCIF, pageable);
    }
    @Override
    public void saveBalanceFluctuation(BalanceFluctuation balanceFluctuation) {
        balanceFluctuationRepository.save(balanceFluctuation);
    }
}
