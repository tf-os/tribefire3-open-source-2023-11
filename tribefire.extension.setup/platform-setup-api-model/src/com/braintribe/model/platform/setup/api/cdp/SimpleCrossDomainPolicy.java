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
package com.braintribe.model.platform.setup.api.cdp;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A very simplified {@link CrossDomainPolicy} where one just configures a {@link #getTrustedDomain() trusted domain}
 * (optionally with wildcard). This domain is considered to be trusted and thus it is assumed that cross origin access
 * within that domain is safe.
 * 
 * @author michael.lafite
 */
public interface SimpleCrossDomainPolicy extends CrossDomainPolicy {
	EntityType<SimpleCrossDomainPolicy> T = EntityTypes.T(SimpleCrossDomainPolicy.class);

	@Mandatory
	@Initializer("''")
	@Description("The trusted domain within which access is considered to be safe, e.g. '*.example.com'.")
	String getTrustedDomain();
	void setTrustedDomain(String trustedDomain);
}
