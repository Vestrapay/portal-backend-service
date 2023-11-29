CREATE TABLE IF NOT EXISTS payment_provider (
       id SERIAL PRIMARY KEY,
       uuid VARCHAR(100) NOT NULL UNIQUE ,
       name VARCHAR(100) NOT NULL UNIQUE ,
       supported_payment_methods text NOT NULL ,
       created_at TIMESTAMP WITHOUT TIME ZONE,
       updated_at TIMESTAMP WITHOUT TIME ZONE
);