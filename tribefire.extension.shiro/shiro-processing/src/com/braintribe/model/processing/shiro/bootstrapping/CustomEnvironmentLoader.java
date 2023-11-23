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
package com.braintribe.model.processing.shiro.bootstrapping;

import javax.servlet.ServletContext;

import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.shiro.bootstrapping.ini.ShiroIniFactory;

/**
 * Extension of the {@link EnvironmentLoader} that allows for an external supplier of
 * an INI file. This file content will be provided by the {@link ShiroIniFactory}.
 */
public class CustomEnvironmentLoader extends EnvironmentLoader {

	private static Logger logger = Logger.getLogger(CustomEnvironmentLoader.class);

	private StringBasedIniEnvironment iniEnvironmentSupplier;
	
	@Override
	protected WebEnvironment determineWebEnvironment(ServletContext servletContext) {
		if (iniEnvironmentSupplier != null) {
			
			logger.debug(() -> "IniEnvironmentSupplier used: "+iniEnvironmentSupplier.toString());
			
			return iniEnvironmentSupplier;
		} else {
			
			logger.debug(() -> "No IniEnvironmentSupplier available.");
			
			return super.determineWebEnvironment(servletContext);
		}
	}

	@Required
	@Configurable
	public void setIniEnvironment(StringBasedIniEnvironment iniEnvironmentSupplier) {
		this.iniEnvironmentSupplier = iniEnvironmentSupplier;
	}

}
