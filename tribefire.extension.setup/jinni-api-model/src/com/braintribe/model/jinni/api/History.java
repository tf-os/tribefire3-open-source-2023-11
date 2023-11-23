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
package com.braintribe.model.jinni.api;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.PlatformRequest;

@Description("Jinni history extension. When run without a parameter, last service requests are displayed. " +
		     "When run with the integer 'repeat' parameter, the corresponding historized service will be executed again." + 
			 "When run with 'repeat' and 'alias' parameters, the corresponding historized service will be stored under the provided alias.")
public interface History extends PlatformRequest {
	EntityType<History> T = EntityTypes.T(History.class);

	@Description("If set, the history service will attempt to repeat the service call corresponding to the number of this parameter. See 'jinni history' for more details.")
	@com.braintribe.model.generic.annotation.meta.Alias("r")
	String getRepeat();
	void setRepeat(String repeat);

	@Description("If set, the history service will attempt to create an alias corresponding to the history entry. See 'jinni history' for more details.")
	@com.braintribe.model.generic.annotation.meta.Alias("a")
	String getAlias();
	void setAlias(String repeat);

}
