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
package com.braintribe.testing.model.test.technical.features;

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A complex entity (compared to {@link SimpleEntity} with more properties and relations to other entities.
 *
 * @author michael.lafite
 */

public interface ComplexEntity extends GenericEntity {

	EntityType<ComplexEntity> T = EntityTypes.T(ComplexEntity.class);

	String getStringProperty();
	void setStringProperty(String stringProperty);

	LocalizedString getLocalizedStringProperty();
	void setLocalizedStringProperty(LocalizedString localizedStringProperty);

	Boolean getBooleanProperty();
	void setBooleanProperty(Boolean booleanProperty);

	Integer getIntegerProperty();
	void setIntegerProperty(Integer integerProperty);

	Double getDoubleProperty();
	void setDoubleProperty(Double doubleProperty);

	Object getObjectProperty();
	void setObjectProperty(Object objectProperty);

	SimpleEntity getSimpleEntityProperty();
	void setSimpleEntityProperty(SimpleEntity simpleEntityProperty);

	ComplexEntity getComplexEntityProperty();
	void setComplexEntityProperty(ComplexEntity complexEntityProperty);

	AnotherComplexEntity getAnotherComplexEntityProperty();
	void setAnotherComplexEntityProperty(AnotherComplexEntity anotherComplexEntityProperty);

	StandardIdentifiable getStandardIdentifiableProperty();
	void setStandardIdentifiableProperty(StandardIdentifiable StandardIdentifiableProperty);

	List<ComplexEntity> getComplexEntityList();
	void setComplexEntityList(List<ComplexEntity> complexEntityList);

	Map<String, ComplexEntity> getComplexEntityMap();
	void setComplexEntityMap(Map<String, ComplexEntity> complexEntityMap);

	List<String> getStringList();
	void setStringList(List<String> stringList);

	SimpleEnum getSimpleEnum();
	void setSimpleEnum(SimpleEnum simpleEnum);

	Object getBaseTypeProperty();
	void setBaseTypeProperty(Object baseTypeProperty);

}
