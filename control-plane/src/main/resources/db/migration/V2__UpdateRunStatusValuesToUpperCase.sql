ALTER TYPE run_status
RENAME VALUE 'queued' TO 'QUEUED';
ALTER TYPE run_status
RENAME VALUE 'running' TO 'RUNNING';
ALTER TYPE run_status
RENAME VALUE 'succeeded' TO 'SUCCEEDED';
ALTER TYPE run_status
RENAME VALUE 'failed' TO 'FAILED';