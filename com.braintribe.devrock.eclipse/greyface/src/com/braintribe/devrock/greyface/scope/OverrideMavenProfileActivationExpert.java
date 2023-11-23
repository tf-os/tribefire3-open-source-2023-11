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
package com.braintribe.devrock.greyface.scope;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenProfileActivationExpertImpl;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

public class OverrideMavenProfileActivationExpert extends MavenProfileActivationExpertImpl {	
	private VirtualPropertyResolver virtualPropertyResolver;
	
	@Required @Configurable
	public void setVirtualPropertyResolver(VirtualPropertyResolver virtualPropertyResolver) {
		this.virtualPropertyResolver = virtualPropertyResolver;
	}

	@Override
	protected String resolveProperty(String key) {
		String keyToUse = "${" + key + "}";
		String value = virtualPropertyResolver.resolve(keyToUse);
		if (value != null)
			return value;
		return super.resolveProperty(keyToUse);
	} 

	
}
