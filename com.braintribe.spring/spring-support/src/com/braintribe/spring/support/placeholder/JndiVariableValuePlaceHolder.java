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
package com.braintribe.spring.support.placeholder;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.braintribe.cfg.Configurable;

public class JndiVariableValuePlaceHolder extends PropertyPlaceholderConfigurer {

	protected String propertyPrefix = "myjndi:";
	protected Context envContext = null; 

	public JndiVariableValuePlaceHolder() throws NamingException {
		Context ctx = new InitialContext();
		this.envContext = (Context) ctx.lookup("java:comp/env");
	}

	@Override
	protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {

		if (placeholder.startsWith(this.propertyPrefix)) {
			placeholder = placeholder.substring(this.propertyPrefix.length());
			String value = null;
			try {
				value = (String) this.envContext.lookup(placeholder);
			} catch (Exception e) {
				throw new RuntimeException("Error while trying to look for "+placeholder+" in JNDI directory.", e);
			}
			return value;
		} 

		return super.resolvePlaceholder(placeholder, props, systemPropertiesMode);    

	}

	@Configurable
	public void setPropertyPrefix(String propertyPrefix) {
		this.propertyPrefix = propertyPrefix;
	}


}
