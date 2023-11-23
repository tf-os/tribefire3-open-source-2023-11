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
package tribefire.extension.simple.deployables.service;

import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.ConfigurationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.genericmodel.GMCoreTools;

import tribefire.extension.simple.model.service.SimpleEchoRequest;
import tribefire.extension.simple.model.service.SimpleEchoResponse;

/**
 * This is a simple, example service implementation which processes {@link SimpleEchoRequest}s.
 *
 * @author michael.lafite
 */
public class SimpleEchoServiceProcessor implements AccessRequestProcessor<SimpleEchoRequest, SimpleEchoResponse> {

	/** The <code>Logger</code> used by this class. */
	private static Logger logger = Logger.getLogger(SimpleEchoServiceProcessor.class);

	/** See {@link #setDelay(Long)}. */
	private long delay = 0L;

	/** See {@link #setEchoCount(Integer)}. */
	private int echoCount;

	/**
	 * Sets the delay (in milliseconds) before returning the response. This is an optional configuration setting.
	 */
	public void setDelay(Long delay) {
		if (delay != null && delay > 0) {
			this.delay = delay;
		} else {
			this.delay = 0;
		}
	}

	/**
	 * Sets the echo count, i.e. how many times to return the message. This configuration setting is {@link Required}.
	 */
	@Required
	public void setEchoCount(Integer echoCount) {
		if (echoCount == null) {
			throw new ConfigurationException("The echo count is mandatory and thus must not be null!");
		}
		if (echoCount < 0) {
			throw new ConfigurationException("Invalid echo count " + echoCount + "! (Negative numbers are not allowed.)");
		}
		this.echoCount = echoCount;
	}

	/**
	 * Processes the specified <code>request</code>. If a {@link #setDelay(Long) delay} has been configured, the processor firsts sleeps for the
	 * specified amount of milliseconds. Afterwards the {@link SimpleEchoResponse response} is created based on the passed
	 * {@link SimpleEchoRequest#getMessage() message} and the configured {@link #setEchoCount(Integer) echo count}.
	 *
	 * @throws IllegalArgumentException
	 *             if the {@link SimpleEchoRequest#getMessage() message} is <code>null</code>.
	 */
	@Override
	public SimpleEchoResponse process(AccessRequestContext<SimpleEchoRequest> requestContext) {
		SimpleEchoRequest request = requestContext.getOriginalRequest();

		// log detailed info on trace level
		// (instead of checking, if logger.isTraceEnabled, we "guard" using lambda expression)
		logger.trace(() -> "Processing request " + GMCoreTools.getDescription(request));

		if (request.getMessage() == null) {
			throw new IllegalArgumentException("No message specified in request " + request + "!");
		}

		// if there is a delay configured, sleep now
		CommonTools.sleep(delay);

		// create response instance
		SimpleEchoResponse response = SimpleEchoResponse.T.create();

		// build echo message
		if (echoCount > 0) {
			StringBuilder echoBuilder = new StringBuilder();
			for (int i = 1; i <= echoCount; i++) {
				echoBuilder.append("(ECHO) " + request.getMessage());
			}
			response.setEcho(echoBuilder.toString());
		} else {
			// no echo --> leave echo property null
		}

		// we're done -> just return the response
		return response;
	}
}
