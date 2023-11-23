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
package com.braintribe.plugin.commons.properties;

import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;


public class PropertyResolver implements VirtualPropertyResolver {	
	@Override
	public String getEnvironmentProperty(String key) {
		
		
		String value = null;
		if (isActive()) {
			value  = VirtualEnvironmentPlugin.getEnvironmentOverrides().get(key);			
		}
		if (value == null) {
			value = System.getenv(key);
		}
		return value;
	}

	@Override
	public String getSystemProperty(String key) {
		String value = null;
		if (isActive()) {
			value = VirtualEnvironmentPlugin.getPropertyOverrides().get(key);			
		}
		if (value == null) {
			value = System.getProperty(key);
		}
		return value;
	}

	@Override
	public boolean isActive() {
		return VirtualEnvironmentPlugin.getOverrideActivation();
	}
	
	@Override
	public String resolve(String expression) {
		if (!requiresEvaluation(expression))
			return expression;
		String value = expression;
		do {
			String rawKey = extract(value);
			// strip points.. here			
			String [] keys = rawKey.split( "\\.");
			String key = keys[0];
			if (keys.length > 1) {
				if (keys[0].equalsIgnoreCase("env"))
					key = keys[1];
				else
					key = rawKey;				
			}
			String v = getEnvironmentProperty(key);
			if (v == null) {
				v = getSystemProperty(key);
			}
			value = replace( rawKey, v, value);			
		} while ( requiresEvaluation( value));
		return value;
	}
	
	/**
	 * just checks if the expression contains ${..} somehow
	 * @param expression - the string to check 
	 * @return - true if a variable reference is in the string and false otherwise 
	 */
	protected boolean requiresEvaluation(String expression) {
		String extract = extract(expression);
		return !extract.equalsIgnoreCase(expression);
	}

	/**
	 * extracts the first variable in the expression 
	 * @param expression - the {@link String} to extract the variable from 
	 * @return - the first variable (minus the ${..} stuff)
	 */
	protected String extract(String expression) {
		int p = expression.indexOf( "${");
		if (p < 0)
			return expression;
		int q = expression.indexOf( "}", p+1);
		return expression.substring(p+2, q);
	}

	/**
	 * replaces any occurrence of the variable by its value 
	 * @param variable - without ${..}, it will be added 
	 * @param value - the value of the variable
	 * @param expression - expression to replace in
	 * @return - the result
	 */
	protected String replace(String variable, String value, String expression) {
		if (value != null) 
			return expression.replace( "${" + variable + "}", value);
		else
			return expression.replace( "${" + variable + "}", "<n/a>");
	}

}
