-- Replace single contact_method + contact_value with three nullable contact columns.
-- Existing rows are migrated: the old single value is moved into the matching column.

ALTER TABLE therapist_profile
    ADD COLUMN phone        VARCHAR(50),
    ADD COLUMN email        VARCHAR(255),
    ADD COLUMN booking_link VARCHAR(1000);

UPDATE therapist_profile SET email       = contact_value WHERE contact_method = 'EMAIL';
UPDATE therapist_profile SET phone       = contact_value WHERE contact_method = 'PHONE';
UPDATE therapist_profile SET booking_link = contact_value WHERE contact_method = 'BOOKING_LINK';

ALTER TABLE therapist_profile
    DROP COLUMN contact_method,
    DROP COLUMN contact_value;
