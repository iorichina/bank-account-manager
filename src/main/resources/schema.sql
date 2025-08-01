-- bank account info
CREATE TABLE IF NOT EXISTS bank_account (
    id BIGINT PRIMARY KEY,
    account_number VARCHAR(32) NOT NULL DEFAULT '',
    owner_id VARCHAR(32) NOT NULL DEFAULT '',
    owner_name VARCHAR(64) NOT NULL DEFAULT '',
    account_type INT NOT NULL DEFAULT 0,
    contact_info VARCHAR(64) NOT NULL DEFAULT '',
    balance DECIMAL(25,10) NOT NULL DEFAULT 0,
    balance_at TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
    state INT NOT NULL DEFAULT 0, -- account state 0 means active, 4 means deleted
    ver BIGINT NOT NULL DEFAULT 0, -- version for optimistic locking
    created_at TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
    updated_at TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
    delete_at TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00'
);
CREATE UNIQUE INDEX uniq_account ON bank_account(account_number);
CREATE INDEX idx_owner_id ON bank_account(owner_id);
CREATE INDEX idx_state ON bank_account(state);

-- 账号信息变更流水表
CREATE TABLE IF NOT EXISTS bank_account_change_log (
    id BIGINT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    account_number VARCHAR(32) NOT NULL,
    owner_id VARCHAR(32) NOT NULL,
    change_type INT NOT NULL, -- 1:开户, 2:销户, 3:信息变更, 4:状态变更, 5:余额变更
    change_desc VARCHAR(128) NOT NULL DEFAULT '',
    before_state INT NOT NULL,
    after_state INT NOT NULL,
    before_balance DECIMAL(25,10) NOT NULL DEFAULT 0,
    after_balance DECIMAL(25,10) NOT NULL DEFAULT 0,
    operator VARCHAR(64) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_account_change_account_id ON bank_account_change_log(account_id);
CREATE INDEX idx_account_change_account_number ON bank_account_change_log(account_number);

-- 账号转账流水表
CREATE TABLE IF NOT EXISTS bank_account_transfer_log (
    id BIGINT PRIMARY KEY,
    from_account_id BIGINT NOT NULL,
    from_account_number VARCHAR(32) NOT NULL,
    to_account_id BIGINT NOT NULL,
    to_account_number VARCHAR(32) NOT NULL,
    amount DECIMAL(25,10) NOT NULL,
    before_balance_from DECIMAL(25,10) NOT NULL DEFAULT 0,
    after_balance_from DECIMAL(25,10) NOT NULL DEFAULT 0,
    before_balance_to DECIMAL(25,10) NOT NULL DEFAULT 0,
    after_balance_to DECIMAL(25,10) NOT NULL DEFAULT 0,
    operator VARCHAR(64) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_transfer_from_account_id ON bank_account_transfer_log(from_account_id);
CREATE INDEX idx_transfer_to_account_id ON bank_account_transfer_log(to_account_id);

-- 账号余额变更表，每行记录单个账号余额的变更信息
CREATE TABLE IF NOT EXISTS bank_account_balance_log (
    id BIGINT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    account_number VARCHAR(32) NOT NULL,
    before_balance DECIMAL(25,10) NOT NULL DEFAULT 0,
    after_balance DECIMAL(25,10) NOT NULL DEFAULT 0,
    change_amount DECIMAL(25,10) NOT NULL DEFAULT 0,
    change_type INT NOT NULL, -- 1:存款, 2:取款, 3:转账收入, 4:转账支出, 5:其他
    change_desc VARCHAR(128) NOT NULL DEFAULT '',
    operator VARCHAR(64) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_balance_account_id ON bank_account_balance_log(account_id);
CREATE INDEX idx_balance_account_number ON bank_account_balance_log(account_number);
