package com.example.notification_service.repository;

import com.example.notification_service.domain.entity.BalanceFluctuation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceFluctuationRepository extends CrudRepository<BalanceFluctuation, Long> {
//    public List<BalanceFluctuation> findByCustomerCIF(String customerCIF);
    Page<BalanceFluctuation> findByCustomerCIF(String customerCIF, Pageable pageable);

    Page<BalanceFluctuation> findAll(Pageable pageable);
}
