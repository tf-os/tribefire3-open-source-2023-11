# Tribefire serverless template.

Use this project to deploy UX (js) applications as serverless and implement SSR functionality.
Template is prepared to be used from CI/CD but it can also be used locally.

If UX APP have placeholders `__SERVICE_ENDPOINT__` and `__BASE_URL__` they will be replaced with `TRIBEFIRE_PUBLIC_SERVICES_URL` and `APP_BASE_URL`

App will be deployed to `https://xxxxxxxx.execute-api.$PROJECT_REGION.amazonaws.com/$STAGE`

eg. https://hnm4mxat7i.execute-api.eu-central-1.amazonaws.com/dev

To run or deploy app, put app in `serverless-template/dist` folder

## Test app

For test copy `_app_` to `app`

```bash
cd serverless-template
rm -rv app
mkdir app
cp -r _app/* app/
```

Configure `.env` file, and

## run locally

```bash
./start.sh dev
```

## deploy

```bash
./start.sh deploy
```

## undeploy

```bash
./start.sh undeploy
```
