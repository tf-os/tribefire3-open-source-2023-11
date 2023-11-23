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
import com.braintribe.gwt.ioc.client.Required;

public class PopupLog implements LogListener {
	private LogEventBuffer logEventBuffer;
	private LogPopup logPopup;
	private String title = "Braintribe GWT Log";
	private Formatter<String> formatter = new StandardStringFormatter();
	
	/**
	 * 
	 * @param logEventBuffer this buffer is used when getting {@link LogEvent} instances
	 * for the plain text display
	 */
	@Configurable @Required
	public void setLogEventBuffer(LogEventBuffer logEventBuffer) {
		this.logEventBuffer = logEventBuffer;
	}
	
	@Configurable
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void onLogEvent(LogEvent event) {
		if (isPopupOpen()) {
			appendLogEvent(event);
		}
	}

	protected void appendLogEvent(LogEvent event) {
		String color = "black";

		switch (event.getLevel()) {
		case DEBUG: color = "gray"; break;
		case ERROR: color = "red"; break;
		case FATAL: color = "magenta"; break;
		case INFO: color = "black"; break;
		case PROFILING: color = "blue"; break;
		case PROFILINGDEBUG: color = "lightblue"; break;
		case WARN: color = "#774400"; break;
		case TRACE: color = "orange"; break;
		}
		
		String line = formatter.format(event);
		
		logPopup.appendLine(line, color);
	}
	
	protected boolean isPopupOpen() {
		return logPopup != null && !logPopup.isClosed();
	}

	public void showPopup() {
		if (logPopup == null || logPopup.isClosed()) {
			logPopup = LogPopup.open(title);
			for (LogEvent event: logEventBuffer.getEvents()) {
				appendLogEvent(event);
			}
		}
		else {
			logPopup.focus();
		}
	}
}
