CREATE TABLE IF NOT EXISTS payment_link (
     id SERIAL PRIMARY KEY,
     uuid VARCHAR(100) NOT NULL UNIQUE ,
     merchant_id VARCHAR(100) NOT NULL,
     transaction_id VARCHAR(100) NOT NULL UNIQUE ,
     link VARCHAR(255) NOT NULL ,
     status VARCHAR(20) NOT NULL ,
     amount NUMERIC(50,2) NOT NULL,
     invoice_id VARCHAR(100) NOT NULL,
     expiry_date TIMESTAMP WITHOUT TIME ZONE,
     customer_name VARCHAR(255) NULL,
     customer_email VARCHAR(255) NULL,
     params text NULL,
     date_created TIMESTAMP WITHOUT TIME ZONE,
     date_updated TIMESTAMP WITHOUT TIME ZONE
);