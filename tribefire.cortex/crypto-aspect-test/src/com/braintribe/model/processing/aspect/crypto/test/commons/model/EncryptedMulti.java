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
package com.braintribe.model.processing.aspect.crypto.test.commons.model;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface EncryptedMulti extends Encrypted {

	EntityType<EncryptedMulti> T = EntityTypes.T(EncryptedMulti.class);

	// @formatter:off
	String getEncryptedProperty1();
	void setEncryptedProperty1(String encryptedProperty1);

	String getEncryptedProperty2();
	void setEncryptedProperty2(String encryptedProperty2);

	String getEncryptedProperty3();
	void setEncryptedProperty3(String encryptedProperty3);

	String getEncryptedProperty4();
	void setEncryptedProperty4(String encryptedProperty4);
	// @formatter:on

}
