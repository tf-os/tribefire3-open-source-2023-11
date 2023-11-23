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

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.transport.jms.message.MessageContext;

/**
 * Abstract processor of incoming requests. It takes care that
 * the request queue knows how many workers currently are available
 * and calls an abstract processing method whenever a request is received.
 * 
 * Note that the use of this class is discouraged as it implies that it is
 * used with the deprecated {@link com.braintribe.transport.jms.queuecomm.StandardQueueCommunication} class.
 * Message processors should instead implement just the {@link MessageProcessor}
 * interface. 
 * 
 * @author roman.kurmanowytsch
 */
public abstract class AbstractProcessor implements Callable<Void>, MessageProcessor {

	private static Logger logger = Logger.getLogger(AbstractProcessor.class);
	
	protected String name = null;
	protected LinkedBlockingQueue<MessageContext> messageContexts = null;
	protected boolean stopProcessing = false;
	protected long timeout = 5000L;

	@Override
	public Void call() throws Exception {
		
		// set the thread's name so that we have better understanding when debugging
		Thread.currentThread().setName(this.getName());

		boolean exitLoop = false;
		
		// wait for requests infinitely until either the server
		// is shut down or this thread should be stopped due to
		// a TTL.
		while ((!this.stopProcessing) && (!exitLoop)) {
			
			MessageContext mc = this.messageContexts.poll(this.timeout, TimeUnit.MILLISECONDS);
			
			if (mc != null) {
				try {
					logger.trace("Processing incoming request "+mc);
					this.processMessageContext(mc);
				} catch(Throwable e) {
					logger.error("Error while processing connection context "+mc, e);
				}
			}
			
		}

		this.workerThreadCloses();
		
		// reset thread name
		Thread.currentThread().setName("Unused pool thread");
		
		return null;
	}
	
	protected void workerThreadCloses() {
		//Intentionally left blank
	}
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	@PreDestroy
	public void stopProcessing() {
		this.stopProcessing = true;
	}

	public long getTimeout() {
		return timeout;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public LinkedBlockingQueue<MessageContext> getMessageContexts() {
		return messageContexts;
	}
	@Required
	public void setMessageContexts(LinkedBlockingQueue<MessageContext> connectionContexts) {
		this.messageContexts = connectionContexts;
	}

}
