CREATE TABLE IF NOT EXISTS audit_event (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(100) NOT NULL,
   user_uuid VARCHAR(100) NOT NULL,
   event text NOT NULL,
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE TABLE IF NOT EXISTS app_permissions (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(100) NOT NULL,
   permission_name VARCHAR(100) NOT NULL UNIQUE ,
   permission_description text NULL,
   created_at TIMESTAMP WITHOUT TIME ZONE,
   updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE role_permissions (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(100) NOT NULL,
   user_id VARCHAR(100) NOT NULL ,
   merchant_id VARCHAR(100) NOT NULL ,
   permission_id VARCHAR(100) NOT NULL
);

CREATE TABLE vestrapay_transactions  (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL ,
    payment_type VARCHAR(100) NOT NULL,
    amount NUMERIC(50,2) NOT NULL,
    pan VARCHAR(100) NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL,
    scheme VARCHAR(100) NOT NULL,
    description VARCHAR(100) NOT NULL,
    activity_status VARCHAR(100) NOT NULL,
    transaction_status VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE

);

CREATE TABLE notifications  (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(100) NOT NULL,
    message VARCHAR(100) NOT NULL ,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE

);