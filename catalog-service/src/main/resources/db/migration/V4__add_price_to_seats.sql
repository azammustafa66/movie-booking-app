ALTER TABLE seats ADD COLUMN price NUMERIC(12,2);

-- Backfill existing seeded seats by tier (see V2__seed_dummy_catalog_data.sql
-- for the row-to-type layout: A-B recliner, C-D premium, E-F regular).
UPDATE seats SET price = 400.00 WHERE seat_type = 'RECLINER';
UPDATE seats SET price = 250.00 WHERE seat_type = 'PREMIUM';
UPDATE seats SET price = 150.00 WHERE seat_type = 'REGULAR';

ALTER TABLE seats ALTER COLUMN price SET NOT NULL;
