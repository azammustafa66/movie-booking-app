ALTER TABLE theatres ADD COLUMN vendor_id BIGINT;

-- Backfill existing rows with the matching vendor seeded in user-service
-- (see user-service V3__seed_additional_vendors.sql).
UPDATE theatres SET vendor_id = 11 WHERE name = 'CineMax Downtown';
UPDATE theatres SET vendor_id = 12 WHERE name = 'Galaxy Cinemas';
UPDATE theatres SET vendor_id = 13 WHERE name = 'Metro Movie Hub';

ALTER TABLE theatres ALTER COLUMN vendor_id SET NOT NULL;

CREATE INDEX idx_theatres_vendor_id ON theatres (vendor_id);
