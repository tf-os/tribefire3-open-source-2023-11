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

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import com.braintribe.model.generic.GenericEntity;

@Abstract
public interface HasKeyStoreEntry extends GenericEntity {

	EntityType<HasKeyStoreEntry> T = EntityTypes.T(HasKeyStoreEntry.class);

	static final String keyStore = "keyStore";
	static final String keyEntryAlias = "keyEntryAlias";
	static final String keyEntryPassword = "keyEntryPassword";
	
	@Name("Key Store")
	@Description("A key store specification.")
	KeyStore getKeyStore();
	void setKeyStore(KeyStore keyStore);

	@Name("Key Store Entry Alias")
	@Description("The alias of the key within the keystore.")
	String getKeyEntryAlias();
	void setKeyEntryAlias(String keyEntryAlias);

	@Name("Key Entry Alias Password")
	@Description("The password (if required) to access the key in the keystore.")
	String getKeyEntryPassword();
	void setKeyEntryPassword(String keyEntryPassword);

}
