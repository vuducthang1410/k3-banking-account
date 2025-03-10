package com.system.transaction_service.repository;

import com.system.common_library.dto.report.TransactionReportRequest;
import com.system.common_library.enums.*;
import com.system.transaction_service.entity.TransactionDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, String> {

    Optional<TransactionDetail> findByIdAndStatus(String id, Boolean status);

    @Query("SELECT d FROM TransactionDetail d " +
            "LEFT JOIN InternalTransaction i ON i.id = d.transaction.id " +
            "LEFT JOIN ExternalTransaction e ON e.id = d.transaction.id " +
            "LEFT JOIN PaymentTransaction p ON p.id = d.transaction.id " +
            "WHERE d.status = ?1 " +
            "AND (:#{#directionList.size()} = 0 OR d.direction IN ?2) " +
            "AND (:#{#feePayerList.size()} = 0 OR d.transaction.feePayer IN ?3) " +
            "AND (:#{#initiatorList.size()} = 0 OR d.transaction.initiator IN ?4) " +
            "AND (:#{#methodList.size()} = 0 OR d.transaction.method IN ?5) " +
            "AND (:#{#transactionTypeList.size()} = 0 OR d.transaction.transactionType IN ?6) " +
            "AND (:#{#stateList.size()} = 0 OR " +
            "(SELECT ts.state FROM d.transaction.transactionStateList ts WHERE ts.id = " +
            "(SELECT MAX(ts2.id) FROM d.transaction.transactionStateList ts2)) IN ?7) " +
            "AND (:#{#typeList.size()} = 0 OR (i.id IS NOT NULL AND d.transaction.type IN ?8) OR " +
            "((e.id IS NOT NULL OR p.id IS NOT NULL) AND ?9 = true)) " +
            "AND (d.transaction.senderAccount LIKE %?10% " +
            "OR d.transaction.senderAccountName LIKE %?10% " +
            "OR d.transaction.receiverAccount LIKE %?10% " +
            "OR d.transaction.receiverAccountName LIKE %?10% " +
            "OR d.transaction.note LIKE %?10% " +
            "OR d.transaction.description LIKE %?10%) " +
            "AND ABS(d.netAmount) BETWEEN ?11 AND ?12")
    Page<TransactionDetail> findAllByCondition(
            Boolean status, List<Direction> directionList, List<FeePayer> feePayerList, List<Initiator> initiatorList,
            List<Method> methodList, List<TransactionType> transactionTypeList, List<State> stateList, List<Type> typeList,
            Boolean checkType, String search, BigDecimal amountStart, BigDecimal amountEnd, Pageable pageable);

    @Query("SELECT d FROM TransactionDetail d " +
            "WHERE d.account = ?1")
    Page<TransactionDetail> findAllByAccount(String account, Pageable pageable);

    @Query("SELECT d FROM TransactionDetail d " +
            "WHERE d.transaction.cifCode = ?1")
    Page<TransactionDetail> findAllByCif(String cif, Pageable pageable);

    @Query("SELECT d FROM TransactionDetail d " +
            "WHERE (:#{#request.getTransactionType()} IS NULL OR d.transaction.transactionType = :#{#request.getTransactionType()}) " +
            "AND (:#{#request.getTransactionStatus()} IS NULL OR " +
            "(SELECT ts.state FROM d.transaction.transactionStateList ts ORDER BY ts.id DESC LIMIT 1) = :#{#request.getTransactionStatus()}) " +
            "AND (:#{#request.getSenderAccountNumber()} IS NULL OR :#{#request.getSenderAccountNumber()} = '' OR " +
            "d.transaction.senderAccount = :#{#request.getSenderAccountNumber()}) " +
            "AND (:#{#request.getRecipientAccountNumber()} IS NULL OR :#{#request.getRecipientAccountNumber()} = '' " +
            "OR d.transaction.receiverAccount = :#{#request.getRecipientAccountNumber()}) " +
            "AND (:#{#request.getAccountType()} IS NULL OR d.transaction.senderAccountType = :#{#request.getAccountType()} " +
            "OR d.transaction.receiverAccountType = :#{#request.getAccountType()}) " +
            "AND d.dateCreated BETWEEN :#{#request.getStartDate().atStartOfDay()} AND :#{#request.getEndDate().atStartOfDay()} " +
            "AND d.amount BETWEEN :#{#request.getMinAmount()} AND :#{#request.getMaxAmount()}")
    List<TransactionDetail> findAllByFilter(TransactionReportRequest request);
}
