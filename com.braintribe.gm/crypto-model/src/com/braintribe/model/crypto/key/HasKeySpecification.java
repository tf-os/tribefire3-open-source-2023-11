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
package com.braintribe.model.crypto.key;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import com.braintribe.model.crypto.common.HasDescription;

@Abstract
public interface HasKeySpecification extends HasDescription {

	EntityType<HasKeySpecification> T = EntityTypes.T(HasKeySpecification.class);
	
	static final String keyAlgorithm = "keyAlgorithm";
	static final String keySize = "keySize";

	@Name("Key Algorithm")
	@Description("The algorithm used to generate the key.")
	String getKeyAlgorithm();
	void setKeyAlgorithm(String keyAlgorithm);

	@Name("Key Size")
	@Description("The size of the key, in bits.")
	Integer getKeySize();
	void setKeySize(Integer keySize);

}
