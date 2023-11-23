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
package com.braintribe.transport.jms.queuecomm.error;

import com.braintribe.transport.jms.IServer;
import com.braintribe.transport.jms.queuecomm.IQueueContext;

public class ExponentialIntervalErrorHandler extends com.braintribe.execution.errorhandler.ExponentialIntervalErrorHandler<IQueueContext> {

	@Override
	protected String getLoggingInformation(IQueueContext queueContext) {
		if (queueContext == null) {
			return "Unknown Queue";
		}
		String queueName = queueContext.getQueueName();
		if ((queueName == null) || (queueName.trim().length() == 0)) {
			return "Unknown Queue Name";
		}
		return queueName;
	}


	@Override
	protected String getContextKey(IQueueContext context) {
		if (context == null) {
			logger.debug("No IQueueContext available");
			return null;
		}
		IServer server = context.getServer();
		if (server == null) {
			logger.debug("Server unknown: using queue name "+context.getQueueName());
			return context.getQueueName();
		}
		String key = server.toString();
		return key;
	}

	
}
