const express = require('express')
const { makeMiddleware } = require('./chrome-lambda.js')
const history = require('connect-history-api-fallback')
const serveStatic = require('serve-static')
const serverless = require('serverless-http')


const app = express()

app.use(
	makeMiddleware({
		debug: true,
		timeout: 50000,
		useCache: true,
	})
);

app.use(history())

const staticFileMiddleware = serveStatic('app')
app.use(staticFileMiddleware)

const handler = serverless(app, {
  request: function(request, event, context) {
		if (process.env.SERVERLESS_DEBUG_LOGGING_ENABLED === 'true') {
			console.log("Request: ", request)
		}
	},
	response: function(response, event, context) {
		if (process.env.SERVERLESS_DEBUG_LOGGING_ENABLED === 'true') {
			console.log("Response: ", response)
		}
	},
	// Matching content will be base64 encoded.
	// This also sets isBase64Encoded to true for the Lambda response to API Gateway by this library.
	binary: [
		'application/octet-stream',
		'application/gzip',
		'application/pdf',
		'application/zip',
		'audio/*',
		'font/*',
		'image/*'
	]
})


if (typeof process.env.SERVERLESS_TESTING_MODE === 'undefined') {
	// normal mode (i.e. running in AWS Lambda)
	module.exports.handler = handler
} else {
	let serverApp

	if (process.env.SERVERLESS_TESTING_MODE === 'without-serverless-http') {
		// use express app directly as server
		// (no serverless-http involved, handler defined above is not used)
		serverApp = app

	} else if (process.env.SERVERLESS_TESTING_MODE === 'with-serverless-http') {
		// create another express app to wrap serverless handler
		// (status 2023-08-24 this starts, but throws an error during request processing)
		serverApp = express();
		serverApp.all('*', (req, res) => {
			handler(req, res)
		})
	} else {
		throw new Error(`Unknown mode: ${process.env.SERVERLESS_TESTING_MODE}`)
	}

	const port = 8080
	serverApp.listen(port, () => {
		console.log(`Running in mode ${process.env.SERVERLESS_TESTING_MODE}. Listening on port http://localhost:${port}.`)
	})
}
