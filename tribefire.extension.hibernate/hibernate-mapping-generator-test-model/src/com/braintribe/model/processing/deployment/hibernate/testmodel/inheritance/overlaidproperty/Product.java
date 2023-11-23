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
package com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.overlaidproperty;

import com.braintribe.model.generic.StandardIdentifiable;

public interface Product extends StandardIdentifiable {
	
	@Override
	<T> T getId();
	@Override
	void setId(Object id);
	
	String getModel();
	void setModel(String model);
	
	//results in ERROR:
	//Caused by: javassist.bytecode.DuplicateMemberException: duplicate method: getManufacturer in org.hibernate.proxy.HibernateProxy_$$_javassist_42
	//Manufacturer getManufacturer();
	//void setManufacturer(Manufacturer manufacturer);
	
}
