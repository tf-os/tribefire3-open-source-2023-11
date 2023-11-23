#!/bin/bash
bin/elasticsearch-plugin install ingest-attachment --batch
exec /usr/local/bin/docker-entrypoint.sh elasticsearch