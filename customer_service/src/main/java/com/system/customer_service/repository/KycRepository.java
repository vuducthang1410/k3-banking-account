package com.system.customer_service.repository;

import com.system.customer_service.entity.Kyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycRepository extends JpaRepository<Kyc,String> {
    boolean existsByIdentityNumber(String identityNumber);

    Optional<Kyc> findByCustomerId(String customerId);
    void deleteByCustomerId(String customerId);
    Optional<Kyc> findByIdentityNumber(String identityNumber);
}
