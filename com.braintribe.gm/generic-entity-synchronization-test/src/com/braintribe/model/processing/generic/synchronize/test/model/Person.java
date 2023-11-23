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
package com.braintribe.model.processing.generic.synchronize.test.model;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;



public interface Person extends StandardIdentifiable {

	
	EntityType<Person> T = EntityTypes.T(Person.class);
	
	void setSsnr(Integer ssnr);
	Integer getSsnr();
	
	void setLastName(String lastName);
	String getLastName();
	
	void setFirstName(String firstName);
	String getFirstName();
	
	void setDescription(String description);
	String getDescription();
	
	void setAddress(Address address);
	Address getAddress();
	
}
