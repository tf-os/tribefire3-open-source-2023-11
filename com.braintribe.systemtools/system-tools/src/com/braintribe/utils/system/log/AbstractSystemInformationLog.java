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
package com.braintribe.utils.system.log;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.utils.system.info.SystemInformationCollector;

public abstract class AbstractSystemInformationLog implements Runnable, DestructionAware {

	private static Logger logger = Logger.getLogger(AbstractSystemInformationLog.class);
	
	protected LogLevel logLevel = LogLevel.DEBUG;
	protected boolean stopProcessing = false;
	protected SystemInformationCollector systemInformationCollector = null;
	
	@Override
	public void run() {
		
		try {
			String info = this.systemInformationCollector.collectSystemInformation();
			this.logInformation(info);
		} catch(Exception e) {
			logger.error("Could not write system information to log", e);
		}
		
	}

	protected abstract void logInformation(String info);

	@Override
	public void preDestroy() {
		this.stopProcessing = true;
	}
	
	public LogLevel getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Configurable
	@Required
	public void setSystemInformationCollector(SystemInformationCollector systemInformationCollector) {
		this.systemInformationCollector = systemInformationCollector;
	}

}
