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
package test.models.basic;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface SimpleCollectionsEntity extends StandardIdentifiable {

	final EntityType<SimpleCollectionsEntity> T = EntityTypes.T(SimpleCollectionsEntity.class);

	String longList = "longList";
	String longSet = "longSet";
	String integerList = "integerList";
	String integerSet = "integerSet";
	String stringList = "stringList";
	String stringSet = "stringSet";
	String dateList = "dateList";
	String dateSet = "dateSet";
	String enumList = "enumList";
	String enumSet = "enumSet";

	List<Long> getLongList();
	void setLongList(List<Long> value);

	Set<Long> getLongSet();
	void setLongSet(Set<Long> value);

	List<Integer> getIntegerList();
	void setIntegerList(List<Integer> value);

	Set<Integer> getIntegerSet();
	void setIntegerSet(Set<Integer> value);

	List<String> getStringList();
	void setStringList(List<String> value);

	Set<String> getStringSet();
	void setStringSet(Set<String> value);

	List<Date> getDateList();
	void setDateList(List<Date> value);

	Set<Date> getDateSet();
	void setDateSet(Set<Date> value);

	List<SimpleEnum> getEnumList();
	void setEnumList(List<SimpleEnum> value);

	Set<SimpleEnum> getEnumSet();
	void setEnumSet(Set<SimpleEnum> value);

}
