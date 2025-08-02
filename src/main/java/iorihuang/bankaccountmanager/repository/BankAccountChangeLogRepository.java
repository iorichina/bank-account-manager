package iorihuang.bankaccountmanager.repository;

import iorihuang.bankaccountmanager.model.BankAccountChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountChangeLogRepository extends JpaRepository<BankAccountChangeLog, Long> {
    /**
     * 批量插入变更日志
     */
    @Modifying
    @Query(value = "INSERT INTO bank_account_change_log (account_id, account_number, owner_id, change_type, change_desc, before_state, after_state, before_owner_name, after_owner_name, before_contact_info, after_contact_info, created_at) VALUES ?1", nativeQuery = true)
    int batchInsert(List<Object[]> batchArgs);
}
