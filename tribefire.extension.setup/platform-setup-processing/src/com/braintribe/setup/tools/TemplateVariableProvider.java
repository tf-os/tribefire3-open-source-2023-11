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
package com.braintribe.setup.tools;

import java.util.function.Function;

import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.model.MergeContext;

/**
 * <p>
 * This class is used in context of {@link Template Templates}. If this provider is assigned to a via
 * {@link MergeContext#setVariableProvider(Function)} the given {@link #valueToCheck} is being evaluated and substituted
 * by {@link #returnValue} in case a match is given.
 * 
 * <p>
 * If {@link #valueToCheck} did not match, the provider states this with {@link #failed} set to true.
 * 
 */
public class TemplateVariableProvider implements Function<String, String> {
	private boolean failed;
	private String valueToCheck;
	private String returnValue;

	public TemplateVariableProvider(String valueToCheck, String returnValue) {
		this.valueToCheck = valueToCheck;
		this.returnValue = returnValue;
	}

	@Override
	public String apply(String value) {
		if (value.equals(valueToCheck))
			return returnValue;
		else {
			this.failed = true;
			return "";
		}
	}

	public boolean getFailed() {
		return failed;
	}
	
}
