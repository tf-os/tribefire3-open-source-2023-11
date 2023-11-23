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
package com.braintribe.gwt.action.client;

import java.util.HashMap;
import java.util.Map;

import jsinterop.annotations.JsType;

/**
 * Helper class for getting properties from the map and using checked type.
 * @author Dirk
 *
 */
@JsType (namespace = "$tf.view")
public abstract class ActionPropertyHolder {
	
	private Map<String, Object> properties = new HashMap<String, Object>();
	
	public Object put(String property, Object value) {
		return properties.put(property, value);
	}
	
	public <T> T get(String property) {
		return (T)properties.get(property);
	}

}
