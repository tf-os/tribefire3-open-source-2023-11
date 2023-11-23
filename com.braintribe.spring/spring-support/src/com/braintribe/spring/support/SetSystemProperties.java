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
package com.braintribe.spring.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;

public class SetSystemProperties implements InitializingBean {

	private static final Logger logger = Logger.getLogger(SetSystemProperties.class);

	protected Map<String,String> systemProperties = new HashMap<String,String>();

	@Override
	public void afterPropertiesSet() throws Exception {

		for (Map.Entry<String,String> entry : this.systemProperties.entrySet()) {

			String key = entry.getKey();
			String value = entry.getValue();

			if (logger.isDebugEnabled()) {
				logger.debug("Setting system property: '"+key+"'='"+value+"'");
			}
			try {
				System.setProperty(key, value);
			} catch(Exception e) {
				throw new BeanInitializationException("Error while trying to set system property: "+key+"="+value, e);
			}
		}

	}

	@Configurable
	public void setSystemProperties(Map<String, String> systemProperties) {
		this.systemProperties = systemProperties;
	}

}
