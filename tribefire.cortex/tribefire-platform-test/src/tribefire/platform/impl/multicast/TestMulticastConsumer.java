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
package tribefire.platform.impl.multicast;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.processing.service.common.ServiceResults;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.service.api.result.StillProcessing;

public class TestMulticastConsumer extends MulticastConsumer {

	// constants
	private static final Logger log = Logger.getLogger(TestMulticastConsumer.class);

	private boolean replyAfterKeepAlive;
	private Long delay;
	private volatile boolean closing = false;

	public void setReplyAfterKeepAlive(boolean replyAfterKeepAlive) {
		this.replyAfterKeepAlive = replyAfterKeepAlive;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	@Override
	public void preDestroy() {
		closing = true;
		super.preDestroy();
	}

	@Override
	protected void processRequestMessage(Message requestMessage) {
		try {
			ServiceRequest serviceRequest = (ServiceRequest) requestMessage.getBody();

			transportSessionId(serviceRequest);

			boolean asynchronous = requestMessage.getReplyTo() == null;
			if (!asynchronous) {

				ServiceResult result = ServiceResults.envelope(serviceRequest);

				try {
					if (delay == null) {
						result = ServiceResults.envelope(serviceRequest);
					} else {
						if (replyAfterKeepAlive) {
							Thread.sleep(delay);
							reply(requestMessage, StillProcessing.T.create());
						}
						Thread.sleep(delay);
					}
				} catch (Throwable t) {
					result = ServiceResults.encodeFailure(t);
				}

				reply(requestMessage, result);

			}

		} catch (Exception e) {
			log.error("Failed to process incoming message: " + requestMessage + ": " + e, closing ? null : e);
		}
	}

	private void reply(Message requestMessage, ServiceResult result) {
		if (closing)
			return;
		Message responseMessage = createResponseMessage(requestMessage, result);
		responseProducer.sendMessage(responseMessage, requestMessage.getReplyTo());
	}

}
