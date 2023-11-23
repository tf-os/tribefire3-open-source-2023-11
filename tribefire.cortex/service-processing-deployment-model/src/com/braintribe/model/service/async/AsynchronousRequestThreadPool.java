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
package com.braintribe.model.service.async;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface AsynchronousRequestThreadPool extends GenericEntity {

	EntityType<AsynchronousRequestThreadPool> T = EntityTypes.T(AsynchronousRequestThreadPool.class);

	String getServiceId();
	void setServiceId(String serviceId);
	
	@Initializer("5")
	int getCorePoolSize();
	void setCorePoolSize(int corePoolSize);
	
	@Initializer("5")
	int getMaxPoolSize();
	void setMaxPoolSize(int maxPoolSize);

	@Initializer("180000l")
	long getKeepAliveTime();
	void setKeepAliveTime(long keepAliveTime);

	
}
