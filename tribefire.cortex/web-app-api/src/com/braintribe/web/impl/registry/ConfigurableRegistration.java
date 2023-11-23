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

import java.util.Collections;
import java.util.Map;

import com.braintribe.web.api.registry.Registration;

public abstract class ConfigurableRegistration implements Registration {

	protected Map<String, String> initParameters = Collections.emptyMap();
	private Integer order;

	@Override
	public Map<String, String> getInitParameters() {
		return initParameters;
	}

	public void setInitParameters(Map<String, String> initParameters) {
		this.initParameters = initParameters == null ? Collections.emptyMap() : initParameters;
	}

	@Override
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ConfigurableRegistration[");
		if (this.initParameters != null) {
			boolean first = true;
			for (Map.Entry<String, String> entry : this.initParameters.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append(entry.getKey() + "=" + entry.getValue());
			}
		}
		sb.append(']');
		return sb.toString();
	}

}
