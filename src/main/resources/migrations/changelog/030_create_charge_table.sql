CREATE TABLE IF NOT EXISTS charges (
       id SERIAL PRIMARY KEY,
       uuid VARCHAR(100) NOT NULL UNIQUE ,
       merchant_id VARCHAR(100) NOT NULL ,
       payment_method VARCHAR(100) NOT NULL ,
       percentage NUMERIC(50,2) NOT NULL ,
       floor NUMERIC(50,2) NOT NULL ,
       cap NUMERIC(50,2) NOT NULL ,
       flat_fee NUMERIC(50,2) NOT NULL ,
       use_flat_fee boolean NOT NULL default false,
       category varchar(50) NOT NULL,
       created_at TIMESTAMP WITHOUT TIME ZONE,
       updated_at TIMESTAMP WITHOUT TIME ZONE
);