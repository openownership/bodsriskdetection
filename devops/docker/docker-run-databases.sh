#!/bin/sh

docker rmi $(docker images -q "cosmarginean/bods-risk-elasticsearch")
docker rmi $(docker images -q "cosmarginean/bods-risk-graphdb")

docker compose -f docker-compose-full.yml down
docker compose -f docker-compose-full.yml up -d
