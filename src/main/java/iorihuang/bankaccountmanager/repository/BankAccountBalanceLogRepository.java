package iorihuang.bankaccountmanager.repository;

import iorihuang.bankaccountmanager.model.BankAccountBalanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountBalanceLogRepository extends JpaRepository<BankAccountBalanceLog, Long> {
    /**
     * 批量插入余额变更流水
     */
    @Modifying
    @Query(value = "INSERT INTO bank_account_balance_log (account_id, account_number, before_balance, after_balance, change_amount, change_type, change_desc, created_at) VALUES ?1", nativeQuery = true)
    int batchInsert(List<Object[]> batchArgs);
}
