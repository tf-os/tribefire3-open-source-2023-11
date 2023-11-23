# SLS TEMPLATE

## Get started

Install the serverless...

Please check this tutorial https://www.serverless.com/framework/docs/providers/aws/guide/quick-start/

```bash
npm install -g serverless
```

Install the dependencies...

```bash
yarn install
```

## Test it locally

first, add certs to sls-certs folder

Create cert.pem and key.pem file

```bash
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem
```

copy it to sls-certs

copy your compiled js project to `app` folder

NOTE: in git repo we have simple test app. in `_app` folder already. You can use it for test
If UX APP have placeholders `__SERVICE_ENDPOINT__` and `__BASE_URL__` they will be replaced with `TRIBEFIRE_PUBLIC_SERVICES_URL` and `APP_BASE_URL`

All parametars can be placed in `.env` file

than, start code offline

```bash
yarn dev
```

test it:

```bash
curl -k https://localhost:3000/
```

or open link https://localhost:3000/ in browser

## Deploy

install aws using this tutorial https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-linux.html#cliv2-linux-install

```bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

configure aws, and set your `Access key ID` and `Secret access key`.
Use `eu-central-1` for region

```bash
aws configure
```

deploy it

```bash
yarn deploy
```

read logs:

```bash
sls logs -f ssr -t
```
