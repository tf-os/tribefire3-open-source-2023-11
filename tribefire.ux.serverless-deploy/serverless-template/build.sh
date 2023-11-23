#!/bin/bash
echo "recreate src folder"

rm -rf ./src
mkdir src

echo "copy to src"
cp *.yml src
cp *.js src
cp -a *.sh src
cp *.json src
cp .env src
cp README.md src

echo "src folder ready"