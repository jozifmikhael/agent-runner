ALTER TABLE runs
ALTER COLUMN stage DROP DEFAULT;

UPDATE runs
SET stage = NULL
WHERE stage = 'not started';
