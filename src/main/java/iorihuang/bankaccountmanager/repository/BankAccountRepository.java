package iorihuang.bankaccountmanager.repository;

import io.micrometer.observation.annotation.Observed;
import iorihuang.bankaccountmanager.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Update account precisely by primary key ID to avoid gap locks on unique indexes.
 * Actually implemented as find-then-update, to be implemented in the service layer.
 */
@Repository
@Observed(name = "bank.account.repository")
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    /**
     * Find all accounts by state, paginate by primary key ID
     *
     * @param state
     * @param lastId
     * @param limit
     * @return
     */
    @Query(value = "SELECT id, account_number, account_type, owner_id, owner_name, contact_info, balance, balance_at, state, ver, created_at, updated_at, delete_at FROM bank_account WHERE state = :state AND id < :lastId ORDER BY id DESC LIMIT :limit",
            nativeQuery = true)
    @Observed(name = "bank.account.repository.find-by-state")
    List<BankAccount> findByState(int state, long lastId, int limit);

    /**
     * Find account by account number, used only for unique index precise lookup
     */
    @Observed(name = "bank.account.repository.find-by-account-number")
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    /**
     * frozen an account using native SQL by updating its state and version.
     *
     * @param id            Account ID (condition)
     * @param accountNumber Account number (condition)
     * @param state         Current state (condition)
     * @param version       Version number (condition)
     * @param newState      New state (update value)
     * @param updateAt      update timestamp (update value)
     * @param newVersion    New version number (update value)
     * @return Number of updated records
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE bank_account SET
             state = :newState,
             update_at = :updateAt,
             ver = :newVersion
             WHERE id = :id AND account_number = :accountNumber AND state = :state AND ver = :version limit 1""",
            nativeQuery = true)
    @Observed(name = "bank.account.repository.frozen-account")
    int frozenAccountByIdAndVersion(
            @Param("id") long id,//same id may in other zone so we need accountNumber
            @Param("accountNumber") String accountNumber,
            @Param("state") int state,
            @Param("version") long version,
            @Param("newState") int newState,
            @Param("updateAt") LocalDateTime updateAt,
            @Param("newVersion") long newVersion);

    /**
     * Logically delete an account using native SQL by updating its state and version.
     *
     * @param id            Account ID (condition)
     * @param accountNumber Account number (condition)
     * @param state         Current state (condition)
     * @param version       Version number (condition)
     * @param newState      New state (update value)
     * @param deleteAt      Deletion timestamp (update value)
     * @param newVersion    New version number (update value)
     * @return Number of updated records
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE bank_account SET " +
            "state = :newState, " +
            "delete_at = :deleteAt, " +
            "ver = :newVersion " +
            "WHERE id = :id AND account_number = :accountNumber AND state = :state AND ver = :version and balance = 0 limit 1",
            nativeQuery = true)
    @Observed(name = "bank.account.repository.close-account")
    int closeAccountByIdAndVersion(
            @Param("id") long id,//same id may in other zone so we need accountNumber
            @Param("accountNumber") String accountNumber,
            @Param("state") int state,
            @Param("version") long version,
            @Param("newState") int newState,
            @Param("deleteAt") LocalDateTime deleteAt,
            @Param("newVersion") long newVersion);

    /**
     * Update account information using native SQL
     *
     * @param id            Account ID
     * @param accountNumber Account number (used to ensure uniqueness across zones)
     * @param state         Current state (condition)
     * @param version       Current version (condition)
     * @param ownerName     Owner name (update value)
     * @param contactInfo   Contact information (update value)
     * @param updatedAt     Update timestamp (update value)
     * @param newVersion    New version number (update value)
     * @return Number of updated records
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE bank_account SET " +
            "owner_name = :ownerName, " +
            "contact_info = :contactInfo, " +
            "updated_at = :updatedAt, " +
            "ver = :newVersion " +
            "WHERE id = :id AND account_number = :accountNumber AND state = :state AND ver = :version limit 1",
            nativeQuery = true)
    @Observed(name = "bank.account.repository.update-account")
    int updateAccountByIdAndVersion(
            @Param("id") long id,//same id may in other zone so we need accountNumber
            @Param("accountNumber") String accountNumber,
            @Param("state") int state,
            @Param("version") long version,
            @Param("ownerName") String ownerName,
            @Param("contactInfo") String contactInfo,
            @Param("updatedAt") LocalDateTime updatedAt,
            @Param("newVersion") long newVersion);

    /**
     * Reduce balance using native SQL
     *
     * @param id
     * @param accountNumber
     * @param state
     * @param version
     * @param balance
     * @param amount
     * @param updatedAt
     * @param newVersion
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE bank_account SET " +
            "balance = balance - :amount, " +
            "updated_at = :updatedAt, " +
            "ver = :newVersion " +
            "WHERE id = :id AND account_number = :accountNumber AND state = :state AND ver = :version AND balance = :balance AND balance >= :amount limit 1",
            nativeQuery = true)
    @Observed(name = "bank.account.repository.reduce-balance")
    int reduceBalanceByIdAndVersion(
            @Param("id") long id,//same id may in other zone so we need accountNumber
            @Param("accountNumber") String accountNumber,
            @Param("state") int state,
            @Param("version") long version,
            @Param("balance") BigDecimal balance,
            @Param("amount") BigDecimal amount,
            @Param("updatedAt") LocalDateTime updatedAt,
            @Param("newVersion") long newVersion);

    /**
     * Increase balance using native SQL
     *
     * @param id
     * @param accountNumber
     * @param state
     * @param version
     * @param balance
     * @param amount
     * @param updatedAt
     * @param newVersion
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE bank_account SET " +
            "balance = balance + :amount, " +
            "updated_at = :updatedAt, " +
            "ver = :newVersion " +
            "WHERE id = :id AND account_number = :accountNumber AND state = :state AND ver = :version AND balance = :balance limit 1",
            nativeQuery = true)
    @Observed(name = "bank.account.repository.increase-balance")
    int increaseBalanceByIdAndVersion(
            @Param("id") long id,//same id may in other zone so we need accountNumber
            @Param("accountNumber") String accountNumber,
            @Param("state") int state,
            @Param("version") long version,
            @Param("balance") BigDecimal balance,
            @Param("amount") BigDecimal amount,
            @Param("updatedAt") LocalDateTime updatedAt,
            @Param("newVersion") long newVersion);

}