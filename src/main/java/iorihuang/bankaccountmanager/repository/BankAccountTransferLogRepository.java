package iorihuang.bankaccountmanager.repository;

import iorihuang.bankaccountmanager.model.BankAccountTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountTransferLogRepository extends JpaRepository<BankAccountTransferLog, Long> {
    /**
     * 批量插入转账流水
     */
    @Modifying
    @Query(value = "INSERT INTO bank_account_transfer_log (from_account_id, from_account_number, to_account_id, to_account_number, amount, before_balance_from, after_balance_from, before_balance_to, after_balance_to, created_at) VALUES ?1", nativeQuery = true)
    int batchInsert(List<Object[]> batchArgs);
}
