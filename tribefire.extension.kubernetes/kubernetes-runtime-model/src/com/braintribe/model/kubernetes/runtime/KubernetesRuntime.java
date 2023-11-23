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
package com.braintribe.model.kubernetes.runtime;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.plugin.Plugable;

public interface KubernetesRuntime extends Plugable {

	final EntityType<KubernetesRuntime> T = EntityTypes.T(KubernetesRuntime.class);

	void setInitiativeName(String initiativeName);
	String getInitiativeName();

	void setCartridgeId(String cartridgeId);
	String getCartridgeId();

	void setCheckUrl(String checkUrl);
	String getCheckUrl();

	void setCheckToken(String checkToken);
	String getCheckToken();

	void setCheckIntervalMs(Long checkIntervalMs);
	@Initializer("2000L")
	Long getCheckIntervalMs();

	void setMaxWaitTimeMs(Long maxWaitTimeMs);
	@Initializer("600000L")
	Long getMaxWaitTimeMs();

}
