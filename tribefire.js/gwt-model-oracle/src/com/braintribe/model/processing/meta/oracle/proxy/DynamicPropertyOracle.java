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

import com.braintribe.model.generic.proxy.DynamicProperty;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.oracle.BasicQualifiedMetaData;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;
import com.braintribe.model.processing.meta.oracle.empty.EmptyPropertyOracle;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class DynamicPropertyOracle extends EmptyPropertyOracle {

	private final DynamicProperty property;

	public DynamicPropertyOracle(EntityTypeOracle entityOracle, DynamicProperty property) {
		super(entityOracle);
		this.property = property;
	}

	@Override
	public String getName() {
		return property.getName();
	}

	@Override
	public Property asProperty() {
		return property;
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return property.getMetaData().stream();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return getMetaData().map(md -> new BasicQualifiedMetaData(md, null));
	}

}
