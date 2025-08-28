package iorihuang.bankaccountmanager.repository;

import iorihuang.bankaccountmanager.model.BankAccount;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import io.micrometer.observation.annotation.Observed;

@Mapper
@Observed(name = "bank.account.repository")
public interface BankAccountRepository {
    @Select("SELECT id, account_number, account_type, owner_id, owner_name, contact_info, balance, balance_at, state, ver, created_at, updated_at, deleted_at FROM bank_account WHERE state = #{state} AND id < #{lastId} ORDER BY id DESC LIMIT #{limit}")
    @Observed(name = "bank.account.repository.find-by-state")
    List<BankAccount> findByState(@Param("state") int state, @Param("lastId") long lastId, @Param("limit") int limit);

    @Select("SELECT id, account_number, account_type, owner_id, owner_name, contact_info, balance, balance_at, state, ver, created_at, updated_at, deleted_at FROM bank_account WHERE account_number = #{accountNumber} LIMIT 1")
    @Observed(name = "bank.account.repository.find-by-account-number")
    Optional<BankAccount> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Update("UPDATE bank_account SET state = #{newState}, updated_at = #{updateAt}, ver = #{newVersion} WHERE id = #{id} AND account_number = #{accountNumber} AND state = #{state} AND ver = #{version} LIMIT 1")
    @Observed(name = "bank.account.repository.frozen-account")
    int frozenAccountByIdAndVersion(@Param("id") long id, @Param("accountNumber") String accountNumber, @Param("state") int state, @Param("version") long version, @Param("newState") int newState, @Param("updateAt") LocalDateTime updateAt, @Param("newVersion") long newVersion);

    @Update("UPDATE bank_account SET state = #{newState}, deleted_at = #{deletedAt}, ver = #{newVersion} WHERE id = #{id} AND account_number = #{accountNumber} AND state = #{state} AND ver = #{version} AND balance = 0 LIMIT 1")
    @Observed(name = "bank.account.repository.close-account")
    int closeAccountByIdAndVersion(@Param("id") long id, @Param("accountNumber") String accountNumber, @Param("state") int state, @Param("version") long version, @Param("newState") int newState, @Param("deletedAt") LocalDateTime deletedAt, @Param("newVersion") long newVersion);

    @Update("UPDATE bank_account SET owner_name = #{ownerName}, contact_info = #{contactInfo}, updated_at = #{updatedAt}, ver = #{newVersion} WHERE id = #{id} AND account_number = #{accountNumber} AND state = #{state} AND ver = #{version} LIMIT 1")
    @Observed(name = "bank.account.repository.update-account")
    int updateAccountByIdAndVersion(@Param("id") long id, @Param("accountNumber") String accountNumber, @Param("state") int state, @Param("version") long version, @Param("ownerName") String ownerName, @Param("contactInfo") String contactInfo, @Param("updatedAt") LocalDateTime updatedAt, @Param("newVersion") long newVersion);

    @Update("UPDATE bank_account SET balance = balance - #{amount}, updated_at = #{updatedAt}, ver = #{newVersion} WHERE id = #{id} AND account_number = #{accountNumber} AND state = #{state} AND ver = #{version} AND balance = #{balance} AND balance >= #{amount} LIMIT 1")
    @Observed(name = "bank.account.repository.reduce-balance")
    int reduceBalanceByIdAndVersion(@Param("id") long id, @Param("accountNumber") String accountNumber, @Param("state") int state, @Param("version") long version, @Param("balance") BigDecimal balance, @Param("amount") BigDecimal amount, @Param("updatedAt") LocalDateTime updatedAt, @Param("newVersion") long newVersion);

    @Update("UPDATE bank_account SET balance = balance + #{amount}, updated_at = #{updatedAt}, ver = #{newVersion} WHERE id = #{id} AND account_number = #{accountNumber} AND state = #{state} AND ver = #{version} AND balance = #{balance} LIMIT 1")
    @Observed(name = "bank.account.repository.increase-balance")
    int increaseBalanceByIdAndVersion(@Param("id") long id, @Param("accountNumber") String accountNumber, @Param("state") int state, @Param("version") long version, @Param("balance") BigDecimal balance, @Param("amount") BigDecimal amount, @Param("updatedAt") LocalDateTime updatedAt, @Param("newVersion") long newVersion);

    @Insert("INSERT INTO bank_account (id, account_number, account_type, owner_id, owner_name, contact_info, balance, balance_at, state, ver, created_at, updated_at, deleted_at) VALUES (#{id}, #{accountNumber}, #{accountType}, #{ownerId}, #{ownerName}, #{contactInfo}, #{balance}, #{balanceAt}, #{state}, #{ver}, #{createdAt}, #{updatedAt}, #{deletedAt})")
    int insert(BankAccount account);
}