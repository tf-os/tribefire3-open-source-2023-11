#!/bin/bash
echo "PREPARE ENV"

#ENVFILE=/etc/resolv.conf
ENVFILE=./.env-test
if test -f "$ENVFILE"; then
  source .env-test
  export $(cut -d= -f1 .env)
fi

if [[ -z "${PROJECT_SERVICE}" ]]; then
  echo 'Please define PROJECT_SERVICE env!!!'
  exit 1
fi

if [[ -z "${PROJECT_STAGE}" ]]; then
  echo 'Please define PROJECT_STAGE env!!!'
  exit 1
fi

if [[ -z "${PROJECT_RUNTIME}" ]]; then
  echo 'Please define PROJECT_RUNTIME env!!!'
  exit 1
fi

if [[ -z "${PROJECT_REGION}" ]]; then
  echo 'Please define PROJECT_REGION env!!!'
  exit 1
fi

if [[ -z "${PROJECT_LAYER_ACCOUNT_ID}" ]]; then
  echo 'Please define PROJECT_LAYER_ACCOUNT_ID env!!!'
  exit 1
fi

if [[ -z "${PROJECT_LAYER_NAME}" ]]; then
  echo 'Please define PROJECT_LAYER_NAME env!!!'
  exit 1
fi

if [[ -z "${PROJECT_LAYER_VERSION}" ]]; then
  echo 'Please define PROJECT_LAYER_VERSION env!!!'
  exit 1
fi

if [[ -z "${PROJECT_FUNCTION_NAME}" ]]; then
  echo 'Please define PROJECT_FUNCTION_NAME env!!!'
  exit 1
fi

if [[ -z "${PROJECT_FUNCTION_NAME}" ]]; then
  echo 'Please define PROJECT_FUNCTION_NAME env!!!'
  exit 1
fi

if [[ -z "${PROJECT_FUNCTION_HANDLER}" ]]; then
  echo 'Please define PROJECT_FUNCTION_HANDLER env!!!'
  exit 1
fi

if [[ -z "${PROJECT_FUNCTION_MEMORY_SIZE}" ]]; then
  echo 'Please define PROJECT_FUNCTION_MEMORY_SIZE env!!!'
  exit 1
fi

if [[ -z "${PROJECT_FUNCTION_TIMEOUT}" ]]; then
  echo 'Please define PROJECT_FUNCTION_TIMEOUT env!!!'
  exit 1
fi

if [[ -z "${TRIBEFIRE_PUBLIC_SERVICES_URL}" ]]; then
  echo 'TRIBEFIRE_PUBLIC_SERVICES_URL env is not defined!!!'
else
  echo "${TRIBEFIRE_PUBLIC_SERVICES_URL}"
  # use , as separator because / is part of endpoint
  find ./app -type f -exec sed -i "s,__SERVICE_ENDPOINT__,${TRIBEFIRE_PUBLIC_SERVICES_URL},g" {} \;
fi

if [[ -z "${APP_BASE_URL}" ]]; then
  echo "APP_BASE_URL is not defined. '' will be used!"
  find ./app -type f -exec sed -i "s,/__BASE_URL__,,g" {} \;
else
  # strip trailing slash if there is any
  APP_BASE_URL=${APP_BASE_URL%/}
  echo "${APP_BASE_URL}"
  export APP_BASE_URL
  # use , as separator because / is part of endpoint
  find ./app -type f -exec sed -i "s,/__BASE_URL__,${APP_BASE_URL},g" {} \;
fi

### hotfix to test authentication
find ./app -type f -exec sed -i "s,__TECHNICAL_USER_USERNAME__,$TMP_USER,g" {} \;
find ./app -type f -exec sed -i "s,__TECHNICAL_USER_PASSWORD__,$TMP_PASS,g" {} \;
### remove once pipeline properly supports env vars

if [ $# -lt 2 ]; then
  echo "Wrong arguments number!"
  echo "Usage: 'start.sh cmd param' eg. 'start.sh sls deploy'"
  exit 1
else
  echo "run '$1 $2'"
  $1 $2
fi
