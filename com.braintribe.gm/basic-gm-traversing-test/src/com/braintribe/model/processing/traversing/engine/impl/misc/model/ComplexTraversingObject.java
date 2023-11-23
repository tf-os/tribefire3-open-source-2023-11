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
package com.braintribe.model.processing.traversing.engine.impl.misc.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface ComplexTraversingObject extends GenericEntity {

	EntityType<ComplexTraversingObject> T = EntityTypes.T(ComplexTraversingObject.class);

	String getName();
	void setName(String name);

	EnumA getEnumComplex();
	void setEnumComplex(EnumA enumComplex);

	ComplexTraversingObject getFirstComplexObject();
	void setFirstComplexObject(ComplexTraversingObject firstComplexObject);

	ComplexTraversingObject getSecondComplexObject();
	void setSecondComplexObject(ComplexTraversingObject secondComplexObject);

	SimpleTraversingObject getSimpleObject();
	void setSimpleObject(SimpleTraversingObject simpleObject);

	Set<ComplexTraversingObject> getSetComplex();
	void setSetComplex(Set<ComplexTraversingObject> setComplex);

	List<ComplexTraversingObject> getListComplex();
	void setListComplex(List<ComplexTraversingObject> listComplex);

	Map<Integer, ComplexTraversingObject> getMapIntComplex();
	void setMapIntComplex(Map<Integer, ComplexTraversingObject> mapIntComplex);

	Map<ComplexTraversingObject, ComplexTraversingObject> getMapComplexComplex();
	void setMapComplexComplex(Map<ComplexTraversingObject, ComplexTraversingObject> mapComplexComplex);

}
