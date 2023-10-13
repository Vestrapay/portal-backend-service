CREATE TABLE IF NOT EXISTS app_user (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(100) NOT NULL,
   merchant_id VARCHAR(100) NOT NULL,
   country VARCHAR(100) NOT NULL,
   first_name VARCHAR(100) NOT NULL,
   last_name VARCHAR(100) NOT NULL,
   email VARCHAR(100) NOT NULL UNIQUE,
   phone_number VARCHAR(100) NULL,
   business_name VARCHAR(100) NOT NULL,
   referral_code VARCHAR(10) NULL,
   password VARCHAR(100) NOT NULL,
   user_type VARCHAR(20) NOT NULL,
   enabled boolean NOT NULL,
   kyc_completed boolean NOT NULL,
   otp varchar(6) NULL,
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS settlement (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(100) NOT NULL,
   country VARCHAR(100) NOT NULL,
   merchant_id VARCHAR(100) NOT NULL ,
   currency VARCHAR(100) NOT NULL,
   bank_name VARCHAR(100) NOT NULL,
   account_number VARCHAR(100) NULL UNIQUE ,
   primary_account boolean NOT NULL,
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS user_keys (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(100) NOT NULL,
   user_id VARCHAR(100) NOT NULL,
   key_usage VARCHAR(100) NOT NULL,
   public_key VARCHAR(100) NOT NULL,
   secret_key VARCHAR(100) NOT NULL,
   encryption_key VARCHAR(100) NOT NULL,
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE
);