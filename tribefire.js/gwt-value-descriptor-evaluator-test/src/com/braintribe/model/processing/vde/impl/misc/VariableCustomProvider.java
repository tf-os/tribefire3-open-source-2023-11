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
package com.braintribe.model.processing.vde.impl.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.impl.root.VariableVdeTest;


/**
 * An  implementation of {@link Function} that is used for in {@link VariableVdeTest}
 *
 */
public class VariableCustomProvider implements Function<Variable, String> {

	private final Map<String,String> map;
	
	public VariableCustomProvider() {
		map = new HashMap<String, String>();
	}

	public void add(String key, String value){
		map.put(key, value);
	}

	@Override
	public String apply(Variable index) throws RuntimeException {
		return map.get(index.getName());
	}

}
