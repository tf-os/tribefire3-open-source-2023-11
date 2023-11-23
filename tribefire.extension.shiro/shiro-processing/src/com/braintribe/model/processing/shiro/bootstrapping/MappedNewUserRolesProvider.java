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
package com.braintribe.model.processing.shiro.bootstrapping;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.shiro.deployment.UserToRolesMapEntry;
import com.braintribe.model.user.User;
import com.braintribe.utils.lcd.StringTools;

public class MappedNewUserRolesProvider implements NewUserRoleProvider {

	private static Logger logger = Logger.getLogger(MappedNewUserRolesProvider.class);
	
	private List<String> fields;

	private ConcurrentHashMap<String,Set<String>> rolesMap = new ConcurrentHashMap<>();

	@Override
	public Set<String> apply(User user) {

		String value = null;
		if (fields == null || fields.isEmpty()) {
			value = user.getEmail();
		} else {
			
			for (String field : fields) {
				switch(field) {
					case "email":
						value = user.getEmail();
						break;
					case "firstName":
						value = user.getFirstName();
						break;
					case "lastName":
						value = user.getLastName();
						break;
					case "name":
						value = user.getName();
						break;
					default:
						value = user.getEmail();
						break;
				}
				if (!StringTools.isBlank(value)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Found value "+value+" of field "+field+" to identify the roles.");
					}
					break;
				}
			}
			
		}
		if (value == null) {
			logger.debug(() -> "Could not find a single value for the user of these fields: "+fields);
			return null;
		}

		for (Map.Entry<String,Set<String>> entry : rolesMap.entrySet()) {
			String userSpec = entry.getKey();
			if (value.matches(userSpec) || value.equals(userSpec)) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Configurable
	@Required
	public void setConfiguredRoles(Set<UserToRolesMapEntry> configuredRoles) {
		if (configuredRoles != null) {
			configuredRoles.stream().forEach(entry -> {
				Set<String> usernameSpecifications = entry.getUsernameSpecifications();
				Set<String> roles = entry.getRoles();
				
				usernameSpecifications.stream().forEach(us -> rolesMap.put(us, roles));
			});
		}
	}
	@Configurable
	public void setFields(List<String> fields) {
		this.fields = fields;
	}


}
