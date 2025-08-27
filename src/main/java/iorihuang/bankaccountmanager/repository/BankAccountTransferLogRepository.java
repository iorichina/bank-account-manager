package iorihuang.bankaccountmanager.repository;

import iorihuang.bankaccountmanager.model.BankAccountTransferLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BankAccountTransferLogRepository {
    /**
     * 批量插入转账流水
     */
    @Insert({
        "<script>",
        "INSERT INTO bank_account_transfer_log (from_account_id, from_account_number, to_account_id, to_account_number, amount, before_balance_from, after_balance_from, before_balance_to, after_balance_to, created_at) VALUES ",
        "<foreach collection='logs' item='log' separator=','>",
        "(#{log.fromAccountId}, #{log.fromAccountNumber}, #{log.toAccountId}, #{log.toAccountNumber}, #{log.amount}, #{log.beforeBalanceFrom}, #{log.afterBalanceFrom}, #{log.beforeBalanceTo}, #{log.afterBalanceTo}, #{log.createdAt})",
        "</foreach>",
        "</script>"
    })
    int batchInsert(@Param("logs") List<BankAccountTransferLog> logs);

    /**
     * 插入单条转账流水
     */
    @Insert("INSERT INTO bank_account_transfer_log (from_account_id, from_account_number, to_account_id, to_account_number, amount, before_balance_from, after_balance_from, before_balance_to, after_balance_to, created_at) VALUES (#{fromAccountId}, #{fromAccountNumber}, #{toAccountId}, #{toAccountNumber}, #{amount}, #{beforeBalanceFrom}, #{afterBalanceFrom}, #{beforeBalanceTo}, #{afterBalanceTo}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BankAccountTransferLog log);
}
