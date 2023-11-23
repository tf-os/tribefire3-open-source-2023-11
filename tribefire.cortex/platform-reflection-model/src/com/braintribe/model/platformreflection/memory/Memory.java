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
package com.braintribe.model.platformreflection.memory;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface Memory extends GenericEntity {

	EntityType<Memory> T = EntityTypes.T(Memory.class);
	
	long getTotal();
	void setTotal(long total);

	Double getTotalInGb();
	void setTotalInGb(Double totalInGb);

	long getAvailable();
	void setAvailable(long available);

	Double getAvailableInGb();
	void setAvailableInGb(Double availableInGb);

	long getSwapTotal();
	void setSwapTotal(long swapTotal);

	Double getSwapTotalInGb();
	void setSwapTotalInGb(Double swapTotalInGb);

	long getSwapUsed();
	void setSwapUsed(long swapUsed);
	
	Double getSwapUsedInGb();
	void setSwapUsedInGb(Double swapUsedInGb);

	List<String> getMemoryBanksInformation();
	void setMemoryBanksInformation(List<String> memoryBanksInformation);
}
