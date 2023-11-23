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


public interface HashedMulti extends Hashed {

	EntityType<HashedMulti> T = EntityTypes.T(HashedMulti.class);

	// @formatter:off
	String getHashedProperty1();
	void setHashedProperty1(String hashedProperty1);

	String getHashedProperty2();
	void setHashedProperty2(String hashedProperty2);

	String getHashedProperty3();
	void setHashedProperty3(String hashedProperty3);

	String getHashedProperty4();
	void setHashedProperty4(String hashedProperty4);
	// @formatter:on

}
