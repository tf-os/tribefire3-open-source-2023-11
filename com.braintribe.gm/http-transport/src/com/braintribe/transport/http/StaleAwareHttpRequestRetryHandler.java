// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.transport.http;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.pool.ConnPoolControl;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import com.braintribe.logging.Logger;

/**
 * This class is there to sanely handle what could be stale connections which otherwise would not be recognized by the
 * http client libary without that trick.
 * 
 * The reason for the rarely happening NoHttpResponseException are most likely stale HTTP connections. Stale HTTP
 * connections are a result of kept-alive connections dropped by a server. A server may drop kept-alive connections to
 * safe resources and the HTTP-Client is not informed about that. A Connectionpool can't get a close signal on a resting
 * connection. The HttpClient library tries to detect stale connections which works in almost all cases but not 100%.
 * All other cases of stale connections will fail later and therefore consume retries. If there is more than one
 * stale-connection for a certain route, all retries are consumed by failing on undetectable stale connections
 * (resulting finally in an error given to the outside).
 * 
 * Wrapping the retry logic in order to reduce the execution count, when the reason for the retry is the
 * NoHttpResponseException. Such an exception is most likely due to stale connections. This is done so many times as
 * connections can be found in a pool based on the pool configuration.
 * 
 * @author dirk.scheffler
 * 
 */
public class StaleAwareHttpRequestRetryHandler extends StandardHttpRequestRetryHandler {
	private static final Logger logger = Logger.getLogger(StaleAwareHttpRequestRetryHandler.class);
	private static final String HTTP_CONNECTION_MAYBE_STALE_COUNT = "http.connection.maybe-stale-count";

	private HttpClientConnectionManager clientConnectionManager;

	public StaleAwareHttpRequestRetryHandler(ConnPoolControl<?> connPoolControl) {
		super(3, true);

		if (connPoolControl instanceof HttpClientConnectionManager) {
			this.clientConnectionManager = (HttpClientConnectionManager) connPoolControl;
		}
	}

	public StaleAwareHttpRequestRetryHandler(ConnPoolControl<?> connPoolControl, int retryCount, boolean requestSentRetryEnabled) {
		super(retryCount, requestSentRetryEnabled);
		if (connPoolControl instanceof HttpClientConnectionManager) {
			this.clientConnectionManager = (HttpClientConnectionManager) connPoolControl;
		}
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

		if (exception instanceof UnknownHostException || exception instanceof NoRouteToHostException) {
			// No point in retrying
			if (logger.isTraceEnabled())
				logger.trace("Got an UnknownHostException (" + exception.getMessage() + "). Not retrying this request: " + context);

			return false;
		}
		if (exception instanceof SocketException) {
			if (clientConnectionManager != null) {
				logger.info("Closing idling connections in pool because of potential stale connections [contextId=" + System.identityHashCode(context)
						+ "].");
				clientConnectionManager.closeIdleConnections(0, TimeUnit.MILLISECONDS);
			}
		}

		logger.debug(() -> "Got an exception that causes a retry (executionCount: " + executionCount + ")", exception);

		Integer count = (Integer) context.getAttribute(HTTP_CONNECTION_MAYBE_STALE_COUNT);

		if (count == null)
			count = 0;

		if (exception instanceof NoHttpResponseException) {
			boolean isMaybeStale = false;

			HttpClientConnection connection = (HttpClientConnection) context.getAttribute(HttpCoreContext.HTTP_CONNECTION);

			if (connection != null) {

				HttpConnectionMetrics metrics = connection.getMetrics();

				if (metrics.getRequestCount() > 1) {
					isMaybeStale = true;
				}
			}

			if (isMaybeStale) {
				count++;
				context.setAttribute(HTTP_CONNECTION_MAYBE_STALE_COUNT, count);

				logger.info("Detected potential use of stale connection [contextId=" + System.identityHashCode(context) + ", ignoreCount=" + count
						+ "].");

				if (clientConnectionManager != null) {
					logger.info("Closing idling connections in pool because of potential stale connections [contextId="
							+ System.identityHashCode(context) + "].");
					clientConnectionManager.closeIdleConnections(0, TimeUnit.MILLISECONDS);
				}
			}
		}

		return super.retryRequest(exception, executionCount - count, context);
	}
}
