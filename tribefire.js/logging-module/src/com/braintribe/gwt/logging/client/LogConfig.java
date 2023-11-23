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

import java.util.List;

import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;

public class LogConfig implements InitializableBean {
	private List<LogListener> logListeners;
	private LogEventBuffer errorDialogLogEventBuffer;
	private boolean profilingEnabled = false;
	protected LogLevel logLevel = LogLevel.INFO;
	
	public LogLevel getLogLevel() {
		return logLevel;
	}
	@Configurable
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
	public void setLogLevel(String logLevelString) {
		LogLevel logLevel = LogLevel.INFO;
		try {
			if (logLevelString != null) {
				logLevel = LogLevel.valueOf(logLevelString);
			}
		} catch(Exception e) {
			logLevel = LogLevel.INFO;
		}
		this.setLogLevel(logLevel);
	}
	

	@Configurable
	public void setProfilingEnabled(boolean profilingEnabled) {
		this.profilingEnabled = profilingEnabled;
	}
	
	@Configurable
	public void setLogListeners(List<LogListener> logListeners) {
		this.logListeners = logListeners;
	}
	
	@Configurable
	public void setErrorDialogLogEventBuffer(
			LogEventBuffer errorDialogLogEventBuffer) {
		this.errorDialogLogEventBuffer = errorDialogLogEventBuffer;
	}
	
	public LogEventBuffer getErrorDialogLogEventBuffer() {
		return errorDialogLogEventBuffer;
	}
	
	public List<LogListener> getLogListeners() {
		return logListeners;
	}
	
	@Override
	public void intializeBean() throws Exception {
		for (LogListener logListener: getLogListeners()) {
			LogManager.addListener(logListener);
		}
		
		LogManager.setLogLevel(this.logLevel);
		
		Profiling.setProfilingEnabled(profilingEnabled);
		
		ErrorDialog.setLogEventBuffer(getErrorDialogLogEventBuffer());
	}
}
