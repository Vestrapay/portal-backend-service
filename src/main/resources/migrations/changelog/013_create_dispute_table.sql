CREATE TABLE IF NOT EXISTS dispute_management (
     id SERIAL PRIMARY KEY,
     uuid VARCHAR(100) NOT NULL UNIQUE ,
     merchant_id VARCHAR(100) NOT NULL,
     file_url VARCHAR(255) NULL ,
     status VARCHAR(10) NOT NULL ,
     transaction_reference VARCHAR(100) NOT NULL ,
     comment text NULL ,
     created_at TIMESTAMP WITHOUT TIME ZONE,
     updated_at TIMESTAMP WITHOUT TIME ZONE
);