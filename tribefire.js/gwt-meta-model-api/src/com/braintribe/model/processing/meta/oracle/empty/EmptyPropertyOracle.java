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
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class EmptyPropertyOracle implements PropertyOracle {

	private final EntityTypeOracle entityOracle;

	public EmptyPropertyOracle(EntityTypeOracle entityOracle) {
		this.entityOracle = entityOracle;
	}

	@Override
	public EntityTypeOracle getEntityTypeOracle() {
		return entityOracle;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public GmProperty asGmProperty() {
		return null;
	}

	@Override
	public Property asProperty() {
		return null;
	}

	@Override
	public List<GmPropertyInfo> getGmPropertyInfos() {
		return Collections.emptyList();
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return Stream.empty();
	}

	@Override
	public Object getInitializer() {
		return null;
	}

}
