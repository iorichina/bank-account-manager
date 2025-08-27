-- bank account info
CREATE TABLE IF NOT EXISTS bank_account (
    id BIGINT PRIMARY KEY,
    account_number VARCHAR(32) NOT NULL DEFAULT '',
    account_type INT NOT NULL DEFAULT 0,
    owner_id VARCHAR(32) NOT NULL DEFAULT '',
    owner_name VARCHAR(64) NOT NULL DEFAULT '',
    contact_info VARCHAR(64) NOT NULL DEFAULT '',
    balance DECIMAL(25,10) NOT NULL DEFAULT 0,
    balance_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    state INT NOT NULL DEFAULT 0, -- account state
    ver BIGINT NOT NULL DEFAULT 0, -- version for optimistic locking
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uniq_account ON bank_account(account_number);
CREATE INDEX idx_owner_id ON bank_account(owner_id);
CREATE INDEX idx_state ON bank_account(state);

-- account info change log
CREATE TABLE IF NOT EXISTS bank_account_change_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL DEFAULT 0,
    account_number VARCHAR(32) NOT NULL DEFAULT '',
    owner_id VARCHAR(32) NOT NULL DEFAULT '',
    change_type INT NOT NULL DEFAULT 0, -- 1:Open Account, 2:Close Account, 3:Info Change, 4:FRONZEN State Change, 5:ReActive State Change
    change_desc VARCHAR(128) NOT NULL DEFAULT '',
    before_state INT NOT NULL DEFAULT 0,
    after_state INT NOT NULL DEFAULT 0,
    before_owner_name VARCHAR(64) NOT NULL DEFAULT '',
    after_owner_name VARCHAR(64) NOT NULL DEFAULT '',
    before_contact_info VARCHAR(64) NOT NULL DEFAULT '',
    after_contact_info VARCHAR(64) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_account_change_account_number ON bank_account_change_log(account_number,created_at);

-- account transfer log
CREATE TABLE IF NOT EXISTS bank_account_transfer_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_account_id BIGINT NOT NULL DEFAULT 0,
    from_account_number VARCHAR(32) NOT NULL DEFAULT '',
    to_account_id BIGINT NOT NULL DEFAULT 0,
    to_account_number VARCHAR(32) NOT NULL DEFAULT '',
    amount DECIMAL(25,10) NOT NULL DEFAULT 0,
    before_balance_from DECIMAL(25,10) NOT NULL DEFAULT 0,
    after_balance_from DECIMAL(25,10) NOT NULL DEFAULT 0,
    before_balance_to DECIMAL(25,10) NOT NULL DEFAULT 0,
    after_balance_to DECIMAL(25,10) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_transfer_from_account_number ON bank_account_transfer_log(from_account_number,created_at);
CREATE INDEX idx_transfer_to_account_number ON bank_account_transfer_log(to_account_number,created_at);

-- account balance change log
CREATE TABLE IF NOT EXISTS bank_account_balance_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL DEFAULT 0,
    account_number VARCHAR(32) NOT NULL DEFAULT '',
    before_balance DECIMAL(25,10) NOT NULL DEFAULT 0,
    after_balance DECIMAL(25,10) NOT NULL DEFAULT 0,
    change_amount DECIMAL(25,10) NOT NULL DEFAULT 0,
    change_type INT NOT NULL, -- 1:Deposit, 2:Withdrawal, 3:Transfer In, 4:Transfer Out, 5:Other
    change_desc VARCHAR(128) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_balance_account_number ON bank_account_balance_log(account_number,created_at);
