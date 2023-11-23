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
package com.braintribe.web.velocity.renderer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author pit
 *
 */
public class VelocityRenderContext {

	private String name;
	private Map<String, Object> contents;
	
	public VelocityRenderContext( String name){
		this.name = name;
	}
	
	public void setValue( String name, Object object) {
		if (contents == null)
			contents = new HashMap<String, Object>();
		contents.put( name, object);				
	}
	
	public Object getValue( String name) {
		if (contents == null)
			return null;
		return contents.get( name);
	}

	public String getName() {
		return name;
	}

	public Set<String> getVariables() {
		if (contents != null)
			return contents.keySet();
		return new HashSet<String>();
	}
	
	
	public void clear() {
		contents.clear();
	}
	
	
}
