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
package com.braintribe.model.processing.deployment.hibernate.testmodel.collections;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * 
 */

public interface Person extends StandardIdentifiable {

	EntityType<Person> T = EntityTypes.T(Person.class);

	// @formatter:off
	String getName();
	void setName(String name);

	List<String> getStrList();
	void setStrList(List<String> strList);

	Set<String> getStrSet();
	void setStrSet(Set<String> strSet);

	List<Car> getCarList();
	void setCarList(List<Car> carList);

	Set<Car> getCarSet();
	void setCarSet(Set<Car> carSet);

	List<Person> getPersonList();
	void setPersonList(List<Person> personList);

	Set<Person> getPersonSet();
	void setPersonSet(Set<Person> personSet);

	// ##########################################################################################
	// ## . . . . . . . . . . . . . . Other Simple Types . . . . . . . . . . . . . . . . . . . ##
	// ##########################################################################################

	List<Integer> getIntList();
	void setIntList(List<Integer> intList);

	List<Long> getLongList();
	void setLongList(List<Long> longList);

	List<Float> getFloatList();
	void setFloatList(List<Float> floatList);

	List<Double> getDoubleList();
	void setDoubleList(List<Double> doubleList);

	List<Date> getDateList();
	void setDateList(List<Date> dateList);

	Map<CarPlate, Car> getCarPlateCarMap();
	void setCarPlateCarMap(Map<CarPlate, Car> carPlateCarMap);

	Map<CarPlate, String> getCarPlateCarMarkMap();
	void setCarPlateCarMarkMap(Map<CarPlate, String> carPlateCarMarkMap);

	Map<String, Car> getCarPlateIdentifierCarMap();
	void setCarPlateIdentifierCarMap(Map<String, Car> carPlateIdentifierCarMap);

	Map<String, String> getCarPlateIdentifierCarMarkMap();
	void setCarPlateIdentifierCarMarkMap(Map<String, String> carPlateIdentifierCarMarkMap);
	// @formatter:on

}
