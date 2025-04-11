package org.demo.loanservice.repositories;

import feign.Param;
import org.demo.loanservice.entities.InterestRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InterestRateRepository extends JpaRepository<InterestRate, String> {
    Optional<InterestRate> findInterestRateByIdAndIsDeleted(String id, boolean isDeleted);


    Page<InterestRate> findAllByIsDeletedAndLoanProductId(Boolean isDeleted, String loanProductId, Pageable pageable);

    Optional<InterestRate> findFirstByMinimumAmountLessThanEqualAndMinimumLoanTermLessThanEqualAndIsDeletedAndIsActiveTrueOrderByMinimumAmountDesc(BigDecimal minimumAmount,int minimumLoanTerm, Boolean isDeleted);

    @Query("""
            SELECT ir FROM InterestRate ir 
            WHERE ir.loanProduct.id IN :loanProductIds 
            AND ir.isDeleted = false
            ORDER BY ir.createdDate DESC
            """)
    List<InterestRate> findValidInterestRates(@Param("loanProductIds") List<String> loanProductIds);

    @Query("""
            SELECT ir FROM InterestRate ir 
            WHERE ir.loanProduct.id IN :loanProductIds 
            AND ir.isActive = true
            AND ir.isDeleted = false
            ORDER BY ir.interestRate,ir.minimumLoanTerm
            """)
    List<InterestRate> findValidInterestRatesByIsActiveTrue(@Param("loanProductIds") List<String> loanProductIds);

}
