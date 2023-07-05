#!/bin/sh

docker rmi $(docker images -q "cosmarginean/bods-risk-elasticsearch")
docker rmi $(docker images -q "cosmarginean/bods-risk-graphdb")

ELASTICSEARCH_TAG="cosmarginean/bods-risk-elasticsearch:latest"
ELASTICSEARCH_CONTAINER_ID=$(docker ps -aqf "name=bods-risk-elasticsearch")
echo "Elasticsearch container ID is: ${ELASTICSEARCH_CONTAINER_ID}"
docker commit $ELASTICSEARCH_CONTAINER_ID "${ELASTICSEARCH_TAG}"

GRAPHDB_TAG="cosmarginean/bods-risk-graphdb:latest"
GRAPHDB_CONTAINER_ID=$(docker ps -aqf "name=bods-risk-graphdb")
echo "GraphDB container ID is: ${GRAPHDB_CONTAINER_ID}"
docker commit $GRAPHDB_CONTAINER_ID "${GRAPHDB_TAG}"

docker push $ELASTICSEARCH_TAG
docker push $GRAPHDB_TAG
