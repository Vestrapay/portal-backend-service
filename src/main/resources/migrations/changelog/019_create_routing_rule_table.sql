CREATE TABLE IF NOT EXISTS routing_rule (
       id SERIAL PRIMARY KEY,
       uuid VARCHAR(100) NOT NULL UNIQUE ,
       payment_method VARCHAR(100) NOT NULL UNIQUE ,
       provider VARCHAR(100) NOT NULL ,
       created_at TIMESTAMP WITHOUT TIME ZONE,
       updated_at TIMESTAMP WITHOUT TIME ZONE
);