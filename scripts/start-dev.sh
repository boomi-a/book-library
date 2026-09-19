#!/usr/bin/env bash

# Starts the local PostgreSQL database, API, and React app without Docker.
# Press Ctrl+C to stop the API and frontend.
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
runtime_dir="$project_dir/.runtime"
mkdir -p "$runtime_dir"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1"
    exit 1
  fi
}

require_command java
require_command node
require_command psql
require_command pg_isready
require_command createdb

if ! java -version >/dev/null 2>&1; then
  if command -v brew >/dev/null 2>&1 && brew --prefix openjdk@21 >/dev/null 2>&1; then
    export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

if ! java -version >/dev/null 2>&1; then
  echo "A working Java 21+ runtime is required to start the backend."
  exit 1
fi

cleanup() {
  echo
  echo "Stopping backend and frontend..."
  kill "$backend_pid" "$frontend_pid" 2>/dev/null || true
}

if ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
  if ! command -v brew >/dev/null 2>&1; then
    echo "PostgreSQL is not running. Start your local PostgreSQL server, then run this script again."
    exit 1
  fi

  echo "Starting local PostgreSQL service..."
  brew services start postgresql@15
fi

echo "Waiting for local PostgreSQL..."
for _ in {1..30}; do
  if pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
  echo "PostgreSQL did not become available on localhost:5432."
  exit 1
fi

if ! psql -d postgres -tAc "SELECT 1" >/dev/null 2>&1; then
  echo "Cannot connect to PostgreSQL as the current local user."
  echo "Configure a local PostgreSQL superuser, then run this script again."
  exit 1
fi

if ! psql -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname = 'bookuser'" | grep -q 1; then
  psql -d postgres -c "CREATE ROLE bookuser LOGIN PASSWORD 'bookpass';"
fi

if ! psql -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'bookdb'" | grep -q 1; then
  createdb -O bookuser bookdb
fi

echo "Preparing the BookLibrary database..."
psql -d bookdb -v ON_ERROR_STOP=1 -f "$project_dir/docker/postgres/init.sql"
psql -d bookdb -v ON_ERROR_STOP=1 -c "GRANT USAGE ON SCHEMA public TO bookuser;"
psql -d bookdb -v ON_ERROR_STOP=1 -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO bookuser;"
psql -d bookdb -v ON_ERROR_STOP=1 -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO bookuser;"

echo "Starting backend at http://localhost:8080..."
(cd "$project_dir/backend" && ./mvnw spring-boot:run) >"$runtime_dir/backend.log" 2>&1 &
backend_pid=$!

echo "Starting frontend at http://localhost:5173..."
(cd "$project_dir/frontend" && npm run dev -- --host 127.0.0.1) >"$runtime_dir/frontend.log" 2>&1 &
frontend_pid=$!

trap cleanup INT TERM EXIT
echo "Home Library is starting. Open http://localhost:5173"
echo "Logs: $runtime_dir"
wait "$backend_pid" "$frontend_pid"
