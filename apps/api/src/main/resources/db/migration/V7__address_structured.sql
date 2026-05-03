ALTER TABLE therapist_profile RENAME COLUMN location TO city;
ALTER TABLE therapist_profile
    ADD COLUMN street_address VARCHAR(255),
    ADD COLUMN postal_code    VARCHAR(10),
    ADD COLUMN latitude       DOUBLE PRECISION,
    ADD COLUMN longitude      DOUBLE PRECISION;
