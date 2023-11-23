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
package tribefire.extension.demo.model.deployment;

import java.math.BigDecimal;

import com.braintribe.model.extensiondeployment.StateChangeProcessor;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

// ADDED: Challenge 1
public interface RevenueNotificationProcessor extends StateChangeProcessor {
	
	EntityType<RevenueNotificationProcessor> T = EntityTypes.T(RevenueNotificationProcessor.class);
	
	/*
	 * Constants for each property name.
	 */
	String minRevenue = "minRevenue";
	String maxRevenue = "maxRevenue";
	
	
	@Initializer("1000000.00b")
	BigDecimal getMinRevenue();
	void setMinRevenue(BigDecimal averageRevenue);

	
	@Initializer("10000000.00b")
	BigDecimal getMaxRevenue();
	void setMaxRevenue(BigDecimal averageRevenue);

}
