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
package tribefire.extension.hydrux.model.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface HxJoat extends GenericEntity {

	EntityType<HxJoat> T = EntityTypes.T(HxJoat.class);

	Object getObject();
	void setObject(Object object);

	// ################################################
	// ## . . . . . . . . Simple . . . . . . . . . ##
	// ################################################

	Integer getInteger();
	void setInteger(Integer integer);

	Long getLong();
	void setLong(Long _long);

	Float getFloat();
	void setFloat(Float _float);

	Double getDouble();
	void setDouble(Double _double);

	Boolean getBoolean();
	void setBoolean(Boolean _boolean);

	String getString();
	void setString(String string);

	Date getDate();
	void setDate(Date date);

	BigDecimal getDecimal();
	void setDecimal(BigDecimal decimal);

	// ################################################
	// ## . . . . . . . . . Custom . . . . . . . . . ##
	// ################################################

	HxColorEnum getColor();
	void setColor(HxColorEnum color);

	HxJoat getEntity();
	void setEntity(HxJoat entity);

	// ################################################
	// ## . . . . . . Simple Collections . . . . . . ##
	// ################################################

	List<String> getListOfStrings();
	void setListOfStrings(List<String> listOfStrings);

	Set<String> getSetOfStrings();
	void setSetOfStrings(Set<String> setOfStrings);

	Map<String, String> getMapOfStrings();
	void setMapOfStrings(Map<String, String> mapOfStrings);

	// ################################################
	// ## . . . . . . Entity Collections . . . . . . ##
	// ################################################

	List<HxJoat> getListOfEntities();
	void setListOfEntities(List<HxJoat> listOfEntities);

	Set<HxJoat> getSetOfEntities();
	void setSetOfEntities(Set<HxJoat> setOfEntities);

	Map<String, HxJoat> getMapOfEntities();
	void setMapOfEntities(Map<String, HxJoat> mapOfEntities);

}
