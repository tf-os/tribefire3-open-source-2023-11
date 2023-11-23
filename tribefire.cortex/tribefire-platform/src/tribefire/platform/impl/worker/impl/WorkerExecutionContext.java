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
package tribefire.platform.impl.worker.impl;

import com.braintribe.logging.Logger;

/**
 * This is a helper class to be used by the BasicWorkerManager to provide
 * a meaningful thread name and push the same information to the logging NDC.
 */
public class WorkerExecutionContext {

	protected static Logger logger = Logger.getLogger(WorkerExecutionContext.class);
	
	private String context;
	private String originalThreadName;
	private int maxLength = 100;
	
	public WorkerExecutionContext(String prefix, Object executable) {
		context = prefix != null ? (prefix + ">") : "";
		context += executable != null ? executable.toString() : "unknown";
		int idx = context.indexOf("$$Lambda$");
		if (idx > 0) {
			context = context.substring(0, idx);
		}
		if (context.length() > maxLength) {
			context = context.substring(0, maxLength);
		}
	}
	
	public void push() {
		originalThreadName = Thread.currentThread().getName();
		context = originalThreadName + ">" + context;
		logger.pushContext(context);
		Thread.currentThread().setName(context);
	}
	
	public void pop() {
		if (originalThreadName != null) {
			Thread.currentThread().setName(originalThreadName);
			logger.popContext();
		}
	}
	
	@Override
	public String toString() {
		return context;
	}
	
}
