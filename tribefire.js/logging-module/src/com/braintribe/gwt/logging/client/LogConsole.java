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
package com.braintribe.gwt.logging.client;

import com.braintribe.gwt.ioc.client.Configurable;
import com.google.gwt.core.client.GWT;

public class LogConsole implements LogListener {
	
	private Formatter<String> formatter = new StandardStringFormatter();
	
	@Configurable
	public void setFormatter(Formatter<String> formatter) {
		this.formatter = formatter;
	}
	
	@Override
	public void onLogEvent(LogEvent event) {
		if (GWT.isScript()) {
			logConsole(formatter.format(event));
		} else {
			System.out.println(formatter.format(event));
		}
	}
	
	private static native void logConsole(String message)
	/*-{
		return $wnd.console.log(message);
	}-*/;

}
