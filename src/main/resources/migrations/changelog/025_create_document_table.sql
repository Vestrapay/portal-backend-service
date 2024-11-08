CREATE TABLE IF NOT EXISTS document (
                                                  id SERIAL PRIMARY KEY,
                                                  document_id VARCHAR(100) NOT NULL,
                                                  merchant_id VARCHAR(100) NOT NULL,
                                                  document_name VARCHAR(100) NOT NULL,
                                                  file_url VARCHAR(255) NULL ,
                                                  date_created TIMESTAMP WITHOUT TIME ZONE,
                                                  date_updated TIMESTAMP WITHOUT TIME ZONE
);