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
package com.braintribe.model.processing.sp.invocation.threaded;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.sp.invocation.AbstractSpInvocation;
import com.braintribe.model.spapi.StateChangeProcessorInvocationPacket;

/**
 * a simple asynchronous state change processor invocation
 * uses a single worker to access the queue AND then process the invocation
 * 
 *  derives from {@link AbstractSpInvocation} and implements {@link Consumer}
 * 
 * @author pit
 *
 */
public class ThreadedSpInvocation extends AbstractSpInvocation implements Consumer<StateChangeProcessorInvocationPacket>, LifecycleAware {

	private static Logger logger = Logger.getLogger(ThreadedSpInvocation.class);
	
	private Worker worker;
	private LinkedBlockingQueue<StateChangeProcessorInvocationPacket> queue = new LinkedBlockingQueue<StateChangeProcessorInvocationPacket>();
	private String name = "ThreadedSpInvocationWorker";
	protected int infoThreshold = 10;
	protected int warnThreshold = 50;
	protected int errorThreshold = 100;

	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void accept(StateChangeProcessorInvocationPacket invocation) throws RuntimeException {
		int queueSize = queue.size();
		if (queueSize > this.errorThreshold) {
			logger.error("The ThreadedSpInvocation queue has "+queueSize+" elements ("+invocation+")");
		} else if (queueSize > this.warnThreshold) {
			logger.warn("The ThreadedSpInvocation queue has "+queueSize+" elements ("+invocation+")");
		} else if (queueSize > this.infoThreshold) {
			logger.info("The ThreadedSpInvocation queue has "+queueSize+" elements ("+invocation+")");
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Putting invocation context "+invocation+" into queue with "+queueSize+" elements");
			}
		}
		queue.offer(invocation);		
	}
		
	/**
	 * the single worker thread, accesses the queue and processes the invocation 
	 * @author pit
	 *
	 */
	private class Worker extends Thread {
		
		public Worker() {
			super(name);
		}
		
		
		@Override
		public void run() {
			
			for (;;) {
				try {				
					// grab invocation packet from queue
					StateChangeProcessorInvocationPacket invocationPacket = queue.take();
					
					processInvocationPacket(invocationPacket);
					
				} catch (InterruptedException e) {
					// shutdown requested, expected situation
					return;
				}
			}
		}		
	}

	@Override 
	public void preDestroy() {
		worker.interrupt();
		try {
			worker.join();
		} catch (InterruptedException e) {
			logger.info("Got interrupted while waiting for the worker to finish.", e);
		}		
	}
	@Override
	public void postConstruct() {
		worker = new Worker();
		worker.start();
	}
	
	@Configurable
	public void setInfoThreshold(int infoThreshold) {
		this.infoThreshold = infoThreshold;
	}
	@Configurable
	public void setWarnThreshold(int warnThreshold) {
		this.warnThreshold = warnThreshold;
	}
	@Configurable
	public void setErrorThreshold(int errorThreshold) {
		this.errorThreshold = errorThreshold;
	}
}
