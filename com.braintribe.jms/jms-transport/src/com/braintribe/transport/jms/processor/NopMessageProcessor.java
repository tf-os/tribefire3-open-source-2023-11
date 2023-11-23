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
package com.braintribe.transport.jms.processor;

import com.braintribe.logging.Logger;
import com.braintribe.transport.jms.message.IMessageContext;


public class NopMessageProcessor extends AbstractProcessor {

	protected static Logger logger = Logger.getLogger(NopMessageProcessor.class);
	
	@Override
	public void processMessageContext(IMessageContext cc) throws Exception {
		logger.debug("No operation performed (NopMessageProcessor).");
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
}
