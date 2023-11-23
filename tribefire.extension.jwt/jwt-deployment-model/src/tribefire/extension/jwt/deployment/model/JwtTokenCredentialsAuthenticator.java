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
package tribefire.extension.jwt.deployment.model;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface JwtTokenCredentialsAuthenticator extends ServiceProcessor {

	EntityType<JwtTokenCredentialsAuthenticator> T = EntityTypes.T(JwtTokenCredentialsAuthenticator.class);

	String defaultRoles = "defaultRoles";
	String jwksUrl = "jwksUrl";
	String usernameClaim = "usernameClaim";
	String rolesClaim = "rolesClaim";
	String claimRolesAndPrefixes = "claimRolesAndPrefixes";
	String propertiesClaims = "propertiesClaims";
	String invalidateTokenCredentialsOnLogout = "invalidateTokenCredentialsOnLogout";

	@Name("Default Roles")
	@Description("A set of roles users should get.")
	Set<String> getDefaultRoles();
	void setDefaultRoles(Set<String> defaultRoles);

	@Name("JWKS URL")
	@Description("The URL where to download JWKS information.")
	String getJwksUrl();
	void setJwksUrl(String jwksUrl);

	@Name("Username Claim")
	@Description("The claim that contains the user name.")
	@Initializer("'sub'")
	String getUsernameClaim();
	void setUsernameClaim(String usernameClaim);

	@Name("Roles Claim")
	@Description("The claim that contains the user roles.")
	@Initializer("'roles'")
	String getRolesClaim();
	void setRolesClaim(String rolesClaim);

	@Description("A map that maps from a Claim property name to a prefix that should be applied to all values to deduce user roles")
	@Name("Claim Roles and Prefixes")
	Map<String, String> getClaimRolesAndPrefixes();
	void setClaimRolesAndPrefixes(Map<String, String> claimRolesAndPrefixes);

	@Name("Properties Claims")
	@Description("Claims that should be added to the properties (which eventually will be added to the user session).")
	Set<String> getPropertiesClaims();
	void setPropertiesClaims(Set<String> propertiesClaims);

	@Name("Invalidate JwtTokenCredentials on Logout")
	@Initializer("true")
	boolean getInvalidateTokenCredentialsOnLogout();
	void setInvalidateTokenCredentialsOnLogout(boolean invalidateTokenCredentialsOnLogout);
}
