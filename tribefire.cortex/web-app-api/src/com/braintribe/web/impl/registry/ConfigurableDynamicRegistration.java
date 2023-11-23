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

import com.braintribe.web.api.registry.DynamicRegistration;

public abstract class ConfigurableDynamicRegistration extends ConfigurableRegistration implements DynamicRegistration {

	protected boolean asyncSupported;

	@Override
	public boolean isAsyncSupported() {
		return asyncSupported;
	}
	
	public void setAsyncSupported(boolean asyncSupported) {
		this.asyncSupported = asyncSupported;
	}

	@Override
	public String toString() {
		return super.toString()+";asyncSupported="+this.asyncSupported;
	}
}
