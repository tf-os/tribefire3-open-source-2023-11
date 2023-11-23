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

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import com.braintribe.model.crypto.key.Key;

@Abstract
public interface EncodedKey extends Key {

	EntityType<EncodedKey> T = EntityTypes.T(EncodedKey.class);

	static final String encodedKey = "encodedKey";
	static final String encodingFormat = "encodingFormat";
	static final String encodingStringFormat = "encodingStringFormat";

	@Name("Encoded Key")
	@Description("A String-representation of the key.")
	String getEncodedKey();
	void setEncodedKey(String encodedKey);

	@Name("Encoding Format")
	@Description("Specifies the format of the String-representation of the key (e.g., pkcs#8, x509).")
	KeyEncodingFormat getEncodingFormat();
	void setEncodingFormat(KeyEncodingFormat encodingFormat);

	@Name("Encoding String Format")
	@Description("Specifies the String-encoding of the key (e.g., base64, hex).")
	KeyEncodingStringFormat getEncodingStringFormat();
	void setEncodingStringFormat(KeyEncodingStringFormat keyEncodingStringFormat);

}
