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
package com.braintribe.model.processing.meta.oracle.proxy;

import java.util.stream.Stream;

import com.braintribe.model.generic.proxy.DynamicEntityType;
import com.braintribe.model.generic.proxy.DynamicProperty;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.index.ConcurrentCachedIndex;
import com.braintribe.model.processing.meta.oracle.BasicQualifiedMetaData;
import com.braintribe.model.processing.meta.oracle.EntityTypeProperties;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;
import com.braintribe.model.processing.meta.oracle.empty.EmptyEntityTypeOracle;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class DynamicEntityTypeOracle extends EmptyEntityTypeOracle {

	private final DynamicEntityType entityType;

	protected final DynamicPropertyOraclesIndex propertyTypeOracles = new DynamicPropertyOraclesIndex();

	public DynamicEntityTypeOracle(ModelOracle modelOracle, DynamicEntityType entityType) {
		super(modelOracle);
		this.entityType = entityType;
	}

	class DynamicPropertyOraclesIndex extends ConcurrentCachedIndex<String, PropertyOracle> {
		@Override
		protected PropertyOracle provideValueFor(String propertyName) {
			DynamicProperty property = (DynamicProperty) entityType.findProperty(propertyName);
			return property != null ? new DynamicPropertyOracle(DynamicEntityTypeOracle.this, property) : emptyPropertyOracle;
		}
	}

	@Override
	public EntityTypeProperties getProperties() {
		throw new UnsupportedOperationException("Method 'DynamicEntityTypeOracle.getProperties' is not supported!");
	}

	@Override
	public PropertyOracle findProperty(String propertyName) {
		PropertyOracle result = propertyTypeOracles.acquireFor(propertyName);
		return result == emptyPropertyOracle ? null : result;
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return entityType.getMetaData().stream();
	}

	@Override
	public Stream<MetaData> getPropertyMetaData() {
		return entityType.getPropertyMetaData().stream();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return getMetaData().map(md -> new BasicQualifiedMetaData(md, null));
	}
	
	@Override
	public Stream<QualifiedMetaData> getQualifiedPropertyMetaData() {
		return getPropertyMetaData().map(md -> new BasicQualifiedMetaData(md, null));
	}

	@Override
	public boolean hasProperty(String propertyName) {
		return entityType.findProperty(propertyName) != null;
	}

	@Override
	public final <T extends CustomType> T asType() {
		return (T) entityType;
	}

}
