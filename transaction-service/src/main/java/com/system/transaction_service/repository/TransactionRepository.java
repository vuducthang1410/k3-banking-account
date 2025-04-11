package com.system.transaction_service.repository;

import com.system.transaction_service.dto.projection.TransactionHistoryProjection;
import com.system.transaction_service.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    String queryGetHistoryTransactions = """
            SELECT tbt.id,
                   tbt.date_created                                  as dateCreated,
                   tbt.amount,
                   tbt.description,
                   (IF(tbt.receiver_account = :accountNumber, 1, 0)) AS isTransfer,
                   tbt.sender_account                                as senderAccountNumber,
                   tbt.receiver_account                              as receiverAccountNumber,
                   tbt.transaction_type                              as transactionType
            FROM tbl_transaction tbt
                     join (select ttd2.transaction_id
                           from tbl_transaction_detail ttd2
                           where ttd2.account = :accountNumber) ttd on tbt.id = ttd.transaction_id
                     join tbl_transaction_state ttt on tbt.id = ttt.transaction_id
            where tbt.transaction_type != 'ROLLBACK'
              and ttt.state = 'COMPLETED'
            ORDER by tbt.date_created DESC
            """;
    String queryCountHistoryTransactions = """
                        SELECT count(*)
            FROM tbl_transaction tbt
                     join (select ttd2.transaction_id
                           from tbl_transaction_detail ttd2
                           where ttd2.account = :accountNumber) ttd on tbt.id = ttd.transaction_id
                     join tbl_transaction_state ttt on tbt.id = ttt.transaction_id
            where tbt.transaction_type != 'ROLLBACK'
              and ttt.state = 'COMPLETED'
            ORDER by tbt.date_created DESC
            """;

    @Query(nativeQuery = true, value = queryGetHistoryTransactions, countQuery = queryCountHistoryTransactions)
    Page<TransactionHistoryProjection> findByBankingAccountNumber(String accountNumber, Pageable pageable);
}
