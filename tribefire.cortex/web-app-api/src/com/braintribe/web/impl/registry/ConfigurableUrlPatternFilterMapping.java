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
import com.braintribe.web.api.registry.UrlPatternFilterMapping;

public class ConfigurableUrlPatternFilterMapping extends ConfigurableFilterMapping implements UrlPatternFilterMapping {

	@Required
	public void setUrlPatterns(List<String> urlPatterns) {
		super.keys = urlPatterns;
	}

	@Override
	public String[] getUrlPatternsArray() {
		return super.keys.toArray(new String[super.keys.size()]);
	}

	@Override
	public String toString() {
		return "URLPattern: " + super.toString();
	}

	/* builder methods */

	public ConfigurableUrlPatternFilterMapping patterns(String ... patterns) {
		setUrlPatterns(Arrays.asList(patterns));
		return this;
	}

	/* // builder methods */

}
