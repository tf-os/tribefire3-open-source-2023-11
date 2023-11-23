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
package com.braintribe.model.platformreflection.host.tomcat;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface Ssl extends GenericEntity {

	EntityType<Ssl> T = EntityTypes.T(Ssl.class);
	
	Integer getPort();
	void setPort(Integer port);
	
	String getKeyStoreFile();
	void setKeyStoreFile(String keyStoreFile);
	
	String getKeyStoreType();
	void setKeyStoreType(String keyStoreType);
	
	String getKeyStoreProvider();
	void setKeyStoreProvider(String keyStoreProvider);
	
	String getTrustStoreFile();
	void setTrustStoreFile(String trustStoreFile);
	
	String getTrustStoreType();
	void setTrustStoreType(String trustStoreType);
	
	String getTrustStoreProvider();
	void setTrustStoreProvider(String trustStoreProvider);
	
	String getKeyAlias();
	void setKeyAlias(String keyAlias);
	
	List<String> getCiphers();
	void setCiphers(List<String> ciphers);
	
	String getProtocol();
	void setProtocol(String protocol);
	
}
