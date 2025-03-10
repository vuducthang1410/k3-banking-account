package com.example.notification_service.service.interfaces;

import com.example.notification_service.domain.entity.BalanceFluctuation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SystemService {

     Page<BalanceFluctuation> getBalanceFluctuation(Pageable pageable);
     BalanceFluctuation getBalanceFluctuationById(Long id) ;
     Page<BalanceFluctuation> getBalanceFluctuationByCustomerCIF(String customerCIF, Pageable pageable) ;
     void saveBalanceFluctuation(BalanceFluctuation balanceFluctuation) ;
}
