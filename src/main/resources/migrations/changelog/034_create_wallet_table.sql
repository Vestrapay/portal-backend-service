CREATE TABLE IF NOT EXISTS wallet
(
    id                      SERIAL PRIMARY KEY,
    wallet_id               VARCHAR(100)   NOT NULL,
    balance                 NUMERIC(50, 2) NOT NULL,
    previous_transaction_id VARCHAR(100)   NOT NULL,
    currency                VARCHAR(100)   NOT NULL,
    created_at              TIMESTAMP WITHOUT TIME ZONE,
    updated_at              TIMESTAMP WITHOUT TIME ZONE
);