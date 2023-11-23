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
package com.braintribe.model.access.security.query;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.query.From;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;

/**
 * 
 */
class SourcesDescriptor {

	private EntityType<?> propertyOwnerType;
	private final Set<EntityType<?>> sourceTypes = newSet();
	private final Map<Source, EntityType<?>> explicitSources = newMap();
	private final Map<PropertyOperand, EntityType<?>> implicitSources = newMap();

	public From defaultSource;

	public void addExplicitSource(Source source, EntityType<?> sourceType) {
		explicitSources.put(source, sourceType);
		sourceTypes.add(sourceType);
	}

	public EntityType<?> getSourceType(Source source) {
		if (source == null) {
			source = defaultSource;
		}

		return explicitSources.get(source);
	}

	public void addImplicitSource(PropertyOperand propertyOperand, EntityType<?> propertyType) {
		implicitSources.put(propertyOperand, propertyType);
		sourceTypes.add(propertyType);
	}

	public Set<Entry<Source, EntityType<?>>> explicitSources() {
		return explicitSources.entrySet();
	}

	public Set<Entry<PropertyOperand, EntityType<?>>> implicitSources() {
		return implicitSources.entrySet();
	}

	public EntityType<?> getPropertyOwnerType() {
		return propertyOwnerType;
	}

	public void setPropertyOwnerType(EntityType<?> propertyOwnerType) {
		this.propertyOwnerType = propertyOwnerType;
		this.sourceTypes.add(propertyOwnerType);
	}

	public Set<EntityType<?>> getSourceTypes() {
		return sourceTypes;
	}

	public Source sourceToInject(Source source) {
		return source == defaultSource ? null : source;
	}

}
