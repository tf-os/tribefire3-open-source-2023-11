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
package com.braintribe.model.crypto.key.keystore;

import com.braintribe.model.crypto.common.HasProvider;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface KeyStore extends HasProvider {

	EntityType<KeyStore> T = EntityTypes.T(KeyStore.class);

	String filePath = "filePath";
	String systemProperty = "systemProperty";
	String type = "type";
	String password = "password";

	@Name("File Path")
	@Description("The path to the keystore.")
	String getFilePath();
	void setFilePath(String filePath);

	@Name("System Property")
	@Description("The system property that may contain the path to the keystore.")
	String getSystemProperty();
	void setSystemProperty(String systemProperty);

	@Name("Type")
	@Description("The type of the keystore. See https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyStore for a list of supported types.")
	String getType();
	void setType(String type);

	@Name("Password")
	@Description("The password (if required) to access the keystore.")
	String getPassword();
	void setPassword(String password);

}
