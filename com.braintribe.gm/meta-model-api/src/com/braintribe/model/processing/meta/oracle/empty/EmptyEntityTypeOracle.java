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
package com.braintribe.model.processing.meta.oracle.empty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.processing.meta.oracle.ElementInOracleNotFoundException;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeProperties;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;
import com.braintribe.model.processing.meta.oracle.TypeHierarchy;

/**
 * @author peter.gazdik
 */
public class EmptyEntityTypeOracle extends EmptyTypeOracle implements EntityTypeOracle {

	public EmptyEntityTypeOracle(ModelOracle modelOracle) {
		super(modelOracle);
	}

	protected final PropertyOracle emptyPropertyOracle = new EmptyPropertyOracle(this);

	@Override
	public GmEntityType asGmEntityType() {
		return null;
	}

	@Override
	public TypeHierarchy getSubTypes() {
		return EmptyTypeHierarchy.INSTANCE;
	}

	@Override
	public TypeHierarchy getSuperTypes() {
		return EmptyTypeHierarchy.INSTANCE;
	}

	@Override
	public List<GmEntityTypeInfo> getGmEntityTypeInfos() {
		return Collections.emptyList();
	}

	@Override
	public EntityTypeProperties getProperties() {
		return EmptyEntityTypeProperties.INSTANCE;
	}

	@Override
	public PropertyOracle getProperty(String propertyName) {
		PropertyOracle result = findProperty(propertyName);
		if (result == null) {
			throw new ElementInOracleNotFoundException("Property not found: " + propertyName);
		}

		return result;
	}

	@Override
	public PropertyOracle getProperty(GmProperty gmProperty) {
		return getProperty(gmProperty.getName());
	}

	@Override
	public PropertyOracle getProperty(Property property) {
		return getProperty(property.getName());
	}

	@Override
	public PropertyOracle findProperty(String propertyName) {
		return emptyPropertyOracle;
	}

	@Override
	public PropertyOracle findProperty(GmProperty gmProperty) {
		return findProperty(gmProperty.getName());
	}

	@Override
	public PropertyOracle findProperty(Property property) {
		return findProperty(property.getName());
	}

	@Override
	public Stream<MetaData> getPropertyMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedPropertyMetaData() {
		return Stream.empty();
	}

	@Override
	public boolean hasProperty(String propertyName) {
		return false;
	}

	@Override
	public boolean isEvaluable() {
		return false;
	}

	@Override
	public Optional<GmType> getEvaluatesTo() {
		return Optional.empty();
	}

}
