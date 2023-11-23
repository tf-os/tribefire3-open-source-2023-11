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
import java.util.Map;

import com.braintribe.model.generic.StandardIdentifiable;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface SimpleMapsEntity extends StandardIdentifiable {

	final EntityType<SimpleMapsEntity> T = EntityTypes.T(SimpleMapsEntity.class);

	Map<Long, Long> getLongToLongMap();
	void setLongToLongMap(Map<Long, Long> value);

	Map<Long, String> getLongToStringMap();
	void setLongToStringMap(Map<Long, String> value);

	Map<Long, Date> getLongToDateMap();
	void setLongToDateMap(Map<Long, Date> value);

	Map<Long, SimpleEnum> getLongToEnumMap();
	void setLongToEnumMap(Map<Long, SimpleEnum> value);

	Map<String, String> getStringToStringMap();
	void setStringToStringMap(Map<String, String> value);

	Map<String, Long> getStringToLongMap();
	void setStringToLongMap(Map<String, Long> value);

	Map<String, Date> getStringToDateMap();
	void setStringToDateMap(Map<String, Date> value);

	Map<String, SimpleEnum> getStringToEnumMap();
	void setStringToEnumMap(Map<String, SimpleEnum> value);

	Map<SimpleEnum, String> getEnumToStringMap();
	void setEnumToStringMap(Map<SimpleEnum, String> value);

	Map<SimpleEnum, Long> getEnumToLongMap();
	void setEnumToLongMap(Map<SimpleEnum, Long> value);

	Map<SimpleEnum, Date> getEnumToDateMap();
	void setEnumToDateMap(Map<SimpleEnum, Date> value);

	Map<SimpleEnum, SimpleEnum> getEnumToEnumMap();
	void setEnumToEnumMap(Map<SimpleEnum, SimpleEnum> value);

	Map<Date, Long> getDateToLongMap();
	void setDateToLongMap(Map<Date, Long> value);

	Map<Date, String> getDateToStringMap();
	void setDateToStringMap(Map<Date, String> value);

	Map<Date, Date> getDateToDateMap();
	void setDateToDateMap(Map<Date, Date> value);

	Map<Date, SimpleEnum> getDateToEnumMap();
	void setDateToEnumMap(Map<Date, SimpleEnum> value);

}
