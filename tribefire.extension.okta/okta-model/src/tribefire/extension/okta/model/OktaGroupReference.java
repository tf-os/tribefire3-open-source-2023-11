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
package tribefire.extension.okta.model;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("${groupId}")
public interface OktaGroupReference extends GenericEntity {

	EntityType<OktaGroupReference> T = EntityTypes.T(OktaGroupReference.class);

	String groupId = "groupId";
	String lastUpdated = "lastUpdated";
	String priority = "priority";

	@Name("Group Id")
	String getGroupId();
	void setGroupId(String groupId);

	@Name("Last Updated")
	Date getLastUpdated();
	void setLastUpdated(Date lastUpdated);

	@Name("Priority")
	Double getPriority();
	void setPriority(Double priority);
}
