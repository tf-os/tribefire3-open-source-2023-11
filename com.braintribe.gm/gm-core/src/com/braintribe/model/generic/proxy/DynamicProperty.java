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

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.data.MetaData;

public class DynamicProperty extends AbstractProxyProperty {

	private final GenericModelType type;

	private Set<MetaData> metaData = newSet();

	public DynamicProperty(DynamicEntityType entityType, String name, GenericModelType type) {
		super(entityType, name);
		this.type = type;
	}

	@Override
	public GenericModelType getType() {
		return type;
	}

	public Set<MetaData> getMetaData() {
		return metaData;
	}

	public void setMetaData(Set<MetaData> metaData) {
		this.metaData = metaData;
	}

}
