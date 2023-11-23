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
package com.braintribe.web.impl.registry;

import java.util.Arrays;
import java.util.List;

import com.braintribe.cfg.Required;
import com.braintribe.web.api.registry.ServletNamesFilterMapping;

public class ConfigurableServletNamesFilterMapping extends ConfigurableFilterMapping implements ServletNamesFilterMapping {

	@Required
	public void setNames(List<String> names) {
		super.keys = names;
	}

	@Override
	public String[] getNamesArray() {
		return super.keys.toArray(new String[super.keys.size()]);
	}

	@Override
	public String toString() {
		return "ServletName: " + super.toString();
	}

	/* builder methods */

	public ConfigurableServletNamesFilterMapping names(String... names) {
		setNames(Arrays.asList(names));
		return this;
	}

	/* // builder methods */

}
