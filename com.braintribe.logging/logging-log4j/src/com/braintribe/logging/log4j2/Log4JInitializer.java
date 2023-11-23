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
package com.braintribe.logging.log4j2;

import java.io.File;
import java.net.URI;

import org.apache.logging.log4j.core.config.Configurator;

import com.braintribe.logging.Logger;

public class Log4JInitializer {

	protected String name = null;
	protected File loggerConfigFile = null;

	public void initialize() {
		if (this.loggerConfigFile != null) {
			URI uri = loggerConfigFile.toURI();
			Configurator.initialize(this.name, this.getClass().getClassLoader(), uri);
		}
		Logger.setLoggerImpl(Log4JLogger.class);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getLoggerConfigFile() {
		return loggerConfigFile;
	}

	public void setLoggerConfigFile(File loggerConfigFile) {
		this.loggerConfigFile = loggerConfigFile;
	}

}
