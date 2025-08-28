package iorihuang.bankaccountmanager.repository;

import iorihuang.bankaccountmanager.model.BankAccountChangeLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BankAccountChangeLogRepository {
    /**
     * 批量插入变更日志
     */
    @Insert({
        "<script>",
        "INSERT INTO bank_account_change_log (account_id, account_number, owner_id, change_type, change_desc, before_state, after_state, before_owner_name, after_owner_name, before_contact_info, after_contact_info, created_at) VALUES ",
        "<foreach collection='logs' item='log' separator=','>",
        "(#{log.accountId}, #{log.accountNumber}, #{log.ownerId}, #{log.changeType}, #{log.changeDesc}, #{log.beforeState}, #{log.afterState}, #{log.beforeOwnerName}, #{log.afterOwnerName}, #{log.beforeContactInfo}, #{log.afterContactInfo}, #{log.createdAt})",
        "</foreach>",
        "</script>"
    })
    int batchInsert(@Param("logs") List<BankAccountChangeLog> logs);

    /**
     * 插入单条变更日志
     */
    @Insert("INSERT INTO bank_account_change_log (account_id, account_number, owner_id, change_type, change_desc, before_state, after_state, before_owner_name, after_owner_name, before_contact_info, after_contact_info, created_at) VALUES (#{accountId}, #{accountNumber}, #{ownerId}, #{changeType}, #{changeDesc}, #{beforeState}, #{afterState}, #{beforeOwnerName}, #{afterOwnerName}, #{beforeContactInfo}, #{afterContactInfo}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BankAccountChangeLog log);
}
