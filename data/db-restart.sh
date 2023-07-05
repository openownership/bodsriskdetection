#!/bin/sh

cd db

pkill -F elasticsearch.pid
pkill -F graphdb.pid

DB_DIR="$(realpath .)"
$(cd elastic*; bin/elasticsearch -d -p ${DB_DIR}/elasticsearch.pid > /dev/null)
$(cd graphdb*/bin; ./graphdb -d -p ${DB_DIR}/graphdb.pid > /dev/null)
