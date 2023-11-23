#!/bin/bash
docker run --name pulsar -it -p 6650:6650  -p 8081:8080 --mount source=pulsardata,target=/pulsar/data --mount source=pulsarconf,target=/pulsar/conf apachepulsar/pulsar:2.9.1 bin/pulsar standalone
