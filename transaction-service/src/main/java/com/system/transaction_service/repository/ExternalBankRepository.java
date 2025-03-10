package com.system.transaction_service.repository;

import com.system.transaction_service.entity.ExternalBank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalBankRepository extends JpaRepository<ExternalBank, String> {

    Boolean existsByCode(String code);

    Boolean existsByNapasCode(String napasCode);

    Boolean existsBySwiftCode(String swiftCode);

    Optional<ExternalBank> findByIdAndStatus(String id, Boolean status);

    Optional<ExternalBank> findByNapasCodeAndStatus(String napasCode, Boolean status);

    @Query("SELECT b FROM ExternalBank b " +
            "WHERE b.status = ?1 " +
            "AND (?2 IS NULL OR b.isAvailable = ?2) " +
            "AND (b.name LIKE %?3% " +
            "OR b.shortName LIKE %?3% " +
            "OR b.code LIKE %?3% " +
            "OR b.contactInfo LIKE %?3% " +
            "OR b.description LIKE %?3%)")
    Page<ExternalBank> findAllByCondition(Boolean status, Boolean isAvailable, String search, Pageable pageable);
}
