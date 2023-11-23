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
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("${profile.lastName}, ${profile.firstName} (${status})")
public interface OktaUser extends GenericEntity {
	
	EntityType<OktaUser> T = EntityTypes.T(OktaUser.class);

	String status = "status";
	String created = "created";
	String activated = "activated";
	String statusChanged = "statusChanged";
	String lastLogin = "lastLogin";
	String lastUpdated = "lastUpdated";
	String passwordChanged = "passwordChanged";
	String profile = "profile";
	String credentials = "credentials";
	String type = "type";
	
	@Name("Status")
	OktaUserStatus getStatus();
	void setStatus(OktaUserStatus status);

	@Name("Created")
	Date getCreated();
	void setCreated(Date created);
	
	@Name("Activated")
	Date getActivated();
	void setActivated(Date activated);

	@Name("Status Changed")
	Date getStatusChanged();
	void setStatusChanged(Date statusChanged);

	@Name("Last Login")
	Date getLastLogin();
	void setLastLogin(Date lastLogin);

	@Name("Last Updated")
	Date getLastUpdated();
	void setLastUpdated(Date lastUpdated);

	@Name("Password Changed")
	Date getPasswordChanged();
	void setPasswordChanged(Date passwordChanged);

	@Name("Profile")
	OktaUserProfile getProfile();
	void setProfile(OktaUserProfile profile);
	
	@Name("Credentials")
	OktaCredentials getCredentials();
	void setCredentials(OktaCredentials credentials);
	
	@Name("Type")
	Map<String,Object> getType();
	void setType(Map<String,Object> type);
	
}
