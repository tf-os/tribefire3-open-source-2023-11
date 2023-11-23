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
package com.braintribe.devrock.tbrunner;

import org.eclipse.core.runtime.IProgressMonitor;

import com.braintribe.devrock.api.console.ConsoleLogger;
import com.braintribe.devrock.api.process.listener.MessageType;
import com.braintribe.devrock.api.process.listener.ProcessNotificationListener;

public class MessageMonitorBridge implements ProcessNotificationListener {
	private IProgressMonitor monitor = null;
	private ConsoleLogger consoleLogger = null;
			
	public IProgressMonitor getMonitor() {
		return monitor;
	}
	
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	
	public MessageMonitorBridge( String consoleName, IProgressMonitor monitor) {
		this.monitor = monitor;
		consoleLogger = new ConsoleLogger( consoleName);		
	}	
	
	@Override
	public void acknowledgeProcessNotification(MessageType arg0, String msg) {
		monitor.beginTask( msg, IProgressMonitor.UNKNOWN);
		if (consoleLogger != null) {
			consoleLogger.log(msg);	
		}
	}
}
