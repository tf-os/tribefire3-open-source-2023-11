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
package com.braintribe.devrock.greyface.generics.commons;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.model.maven.settings.Server;

public class CommonConversions {

	
	public static Server serverFrom(RepositorySetting setting) {
		Server server = Server.T.create();
		server.setUsername( setting.getUser());
		server.setPassword( setting.getPassword());
		server.setId( setting.getName());
		return server;
	}

	/**
	 * upcasts a {@link RepositorySetting} from a {@link RemoteRepository}<br/>
	 * only works because {@link RepositorySetting} is a sub type of {@link RemoteRepository}
	 * @param remoteRepository - {@link RemoteRepository} as base 
	 * @return - the {@link RepositorySetting} returned, other values are default. 
	 */
	public static RepositorySetting repositorySettingFrom( RemoteRepository remoteRepository) {
		RepositorySetting setting = RepositorySetting.T.create();
		for (Property property : RemoteRepository.T.getDeclaredProperties()) {
			
			Property targetProperty = RepositorySetting.T.getProperty( property.getName());
			if (targetProperty != null) {
				targetProperty.set(setting, property.get(remoteRepository));
			}
		}
		return setting;
	}
	
	
}
