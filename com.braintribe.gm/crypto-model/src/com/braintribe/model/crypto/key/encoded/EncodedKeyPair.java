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
package com.braintribe.model.crypto.key.encoded;

import com.braintribe.model.crypto.key.KeyPair;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface EncodedKeyPair extends KeyPair {

	EntityType<EncodedKeyPair> T = EntityTypes.T(EncodedKeyPair.class);
	
	@Name("Public Key")
	@Description("The public key of the key pair.")
	EncodedPublicKey getPublicKey();
	void setPublicKey(EncodedPublicKey publicKey);
	
	@Name("Private Key")
	@Description("The private key of the key pair.")
	EncodedPrivateKey getPrivateKey();
	void setPrivateKey(EncodedPrivateKey privateKey);
	
}
