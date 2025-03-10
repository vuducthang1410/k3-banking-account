package com.system.account_service.repositories;

import com.system.account_service.entities.InterestRates;
import com.system.account_service.entities.type.InterestGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRateRepository extends JpaRepository<InterestRates, String> {
    Optional<InterestRates> findByInterestRateIdAndDeleted(String interestRateId, Boolean deleted);

    Page<InterestRates> findAllByDeleted(Boolean deleted, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE InterestRates b " +
            "SET b.deleted = true " +
            "WHERE b.interestRateId in :ids")
    void softDeleteByIds(@Param("ids") List<String> ids);

    @Query("SELECT ir FROM InterestRates ir " +
            "WHERE ir.interestGroup = 'SAVING' " +
            "AND :term BETWEEN ir.minimumTerm AND ir.maximumTerm " +
            "AND ir.deleted = false " +
            "ORDER BY ir.rate ASC")
    List<InterestRates> findBySavingTermInRange(@Param("term") Integer term);

    List<InterestRates> findDescByInterestGroup(InterestGroup interestGroup);
}
