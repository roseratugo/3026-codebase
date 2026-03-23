#!/bin/bash
set -e

DUMP_FILE="/docker-entrypoint-initdb.d/backup.dump"

if [ -f "$DUMP_FILE" ]; then
    echo "Restoring database from dump..."
    pg_restore --no-owner --no-privileges -d "$POSTGRES_DB" "$DUMP_FILE" || true
    echo "Database restore complete."
else
    echo "No dump file found at $DUMP_FILE, skipping restore."
fi
