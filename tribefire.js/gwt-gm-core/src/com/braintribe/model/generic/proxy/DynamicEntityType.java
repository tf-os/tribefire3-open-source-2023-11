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
package com.braintribe.model.generic.proxy;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.data.MetaData;

public class DynamicEntityType extends AbstractProxyEntityType {

	private Set<MetaData> metaData = newSet();
	private Set<MetaData> propertyMetaData = newSet();

	public DynamicEntityType(String typeSignature) {
		super(typeSignature);

		properties = newList();

		addProperty(GenericEntity.id, BaseType.INSTANCE);
		addProperty(GenericEntity.globalId, SimpleTypes.TYPE_STRING);
		addProperty(GenericEntity.partition, SimpleTypes.TYPE_STRING);
	}

	@Override
	public Property getProperty(String name) throws GenericModelException {
		Property property = findProperty(name);
		if (property == null)
			throw new GenericModelException("Property '" + name + "' not found for dynamic entity type: " + typeSignature);

		return property;
	}

	public DynamicProperty addProperty(String name, GenericModelType type) {
		if (propertiesByName.containsKey(name))
			throw new GenericModelException("Dynamic type '" + typeSignature + "' already contains a property named: " + name);

		DynamicProperty result = new DynamicProperty(this, name, type);
		propertiesByName.put(result.getName(), result);
		properties.add(result);

		return result;
	}

	public Set<MetaData> getMetaData() {
		return metaData;
	}

	public void setMetaData(Set<MetaData> metaData) {
		this.metaData = metaData;
	}

	public Set<MetaData> getPropertyMetaData() {
		return propertyMetaData;
	}

	public void setPropertyMetaData(Set<MetaData> propertyMetaData) {
		this.propertyMetaData = propertyMetaData;
	}

}
