package com.system.core_banking_service.repository;

import com.system.core_banking_service.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    Boolean existsByCifCode(String cifCode);

    Boolean existsByPhone(String phone);

    Optional<Customer> findByIdAndStatus(String id, Boolean status);

    Optional<Customer> findByCifCodeAndStatus(String cifCode, Boolean status);

    @Query("SELECT c FROM Customer c " +
            "WHERE c.status = ?1 " +
            "AND (?2 IS NULL OR c.isActive = ?2) " +
            "AND (c.cifCode LIKE %?3% " +
            "OR c.fullName LIKE %?3% " +
            "OR c.email LIKE %?3% " +
            "OR c.phone LIKE %?3% " +
            "OR c.address LIKE %?3% " +
            "OR c.description LIKE %?3%)")
    Page<Customer> findAllByCondition(Boolean status, Boolean isActive, String search, Pageable pageable);
}
