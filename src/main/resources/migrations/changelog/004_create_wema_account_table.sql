CREATE TABLE IF NOT EXISTS wema_accounts (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(100) NOT NULL UNIQUE ,
   merchant_id VARCHAR(100) NOT NULL UNIQUE,
   account_name VARCHAR(150) NOT NULL,
   account_number VARCHAR(10) NOT NULL UNIQUE,
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE
);