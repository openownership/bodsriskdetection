#!/bin/sh

docker compose -f docker-compose-blank.yml down
docker compose -f docker-compose-blank.yml up -d

until curl -s -k https://localhost:9200 | grep -q "missing authentication credentials"; do
  echo "Waiting for Elasticsearch to start ..."
  sleep 3
done

ELASTICSEARCH_PASSWORD=$(docker exec -it bods-risk-elasticsearch /usr/share/elasticsearch/bin/elasticsearch-reset-password -b -u elastic | grep "New value:" | sed -E 's/New value: //' | tr -d '\r')
echo "Elasticsearch password is: $ELASTICSEARCH_PASSWORD"
sed -i '' "s/ELASTICSEARCH_PASSWORD.*/ELASTICSEARCH_PASSWORD=$ELASTICSEARCH_PASSWORD/" ../../.env
