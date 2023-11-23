# tribefire.extension.elasticsearch

## Building
Run ./tb.sh . or sh tb.sh . in the tribefire.extension.elasticsearch group.

## Setup
Run the following jinni command to set up a messaging server:
`./jinni.sh setup-local-tomcat-platform setupDependency=tribefire.extension.elasticsearch:elasticsearch-setup#1.0 installationPath=<Your Path> --deletePackageBaseDir true --debugProject tribefire.extension.elasticsearch:elasticsearch-debug : options --verbose`

## Start Elasticsearch
Go to `docker/docker-compose.yml` and run `docker-compose up -d`

## Connect via UI
Browse to http://localhost:9200/ to check Elasticsearch availability.
Browse to http://localhost:5601/ to access Kibana.
