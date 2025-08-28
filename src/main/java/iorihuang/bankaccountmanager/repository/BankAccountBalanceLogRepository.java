package iorihuang.bankaccountmanager.repository;

import iorihuang.bankaccountmanager.model.BankAccountBalanceLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BankAccountBalanceLogRepository {
    /**
     * 批量插入余额变更流水
     */
    @Insert({
        "<script>",
        "INSERT INTO bank_account_balance_log (account_id, account_number, before_balance, after_balance, change_amount, change_type, change_desc, created_at) VALUES ",
        "<foreach collection='logs' item='log' separator=','>",
        "(#{log.accountId}, #{log.accountNumber}, #{log.beforeBalance}, #{log.afterBalance}, #{log.changeAmount}, #{log.changeType}, #{log.changeDesc}, #{log.createdAt})",
        "</foreach>",
        "</script>"
    })
    int batchInsert(@Param("logs") List<BankAccountBalanceLog> logs);

    /**
     * 插入单条余额变更流水
     */
    @Insert("INSERT INTO bank_account_balance_log (account_id, account_number, before_balance, after_balance, change_amount, change_type, change_desc, created_at) VALUES (#{accountId}, #{accountNumber}, #{beforeBalance}, #{afterBalance}, #{changeAmount}, #{changeType}, #{changeDesc}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BankAccountBalanceLog log);
}
