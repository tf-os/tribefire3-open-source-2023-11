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
import com.braintribe.transport.jms.util.JmsUtil;


public class EchoMessageProcessor extends AbstractProcessor {

	protected static Logger logger = Logger.getLogger(EchoMessageProcessor.class);
	
	protected String messagePrefix = "You sent: ";

	@Override
	public void processMessageContext(IMessageContext cc) throws Exception {
		try {
			String messageText = JmsUtil.getMessageText(cc);
			cc.reply(this.messagePrefix + messageText);
		} catch (Exception e) {
			logger.error("Could not send reply.", e);
		}
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public String getMessagePrefix() {
		return messagePrefix;
	}

	public void setMessagePrefix(String messagePrefix) {
		this.messagePrefix = messagePrefix;
	}

}
