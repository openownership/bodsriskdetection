#!/bin/sh

mkdir -p db
cd db

pkill -F elasticsearch.pid
pkill -F graphdb.pid

if [ ! -d "clean-dbs" ]
then
    curl "https://bods-rdf.s3-eu-west-1.amazonaws.com/bods-risk-detection/clean-dbs.zip" > clean-dbs.zip
    rm -rf clean-dbs
    unzip clean-dbs.zip
    rm -f clean-dbs.zip
fi

rm -rf elastic*
rm -rf graphdb*
cp -r clean-dbs/* .

DB_DIR="$(realpath .)"
$(cd elastic*; bin/elasticsearch -d -p ${DB_DIR}/elasticsearch.pid > /dev/null)
$(cd graphdb*/bin; ./graphdb -d -p ${DB_DIR}/graphdb.pid > /dev/null)
