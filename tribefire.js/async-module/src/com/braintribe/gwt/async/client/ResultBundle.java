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
package com.braintribe.gwt.async.client;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResultBundle {
	private Map<String, Object> results = new LinkedHashMap<String, Object>();
	private Object inputResult;
	
	public ResultBundle() {
	}
	
	public ResultBundle(Object inputResult) {
		this.inputResult = inputResult;
	}
	
	public void set(String name, Object value) {
		results.put(name, value);
	}
	
	public <T> T get(String key) {
		return (T)results.get(key);
	}
	
	public <T> T getInputResult() {
		return (T)inputResult; 
	}
	
	public Map<String, Object> getResults() {
		return results;
	}
}
