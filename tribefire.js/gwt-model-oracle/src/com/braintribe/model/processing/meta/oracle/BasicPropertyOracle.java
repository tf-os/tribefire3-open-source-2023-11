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
package com.braintribe.model.processing.meta.oracle;

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.oracle.flat.FlatProperty;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class BasicPropertyOracle implements PropertyOracle {

	private final BasicEntityTypeOracle entityOracle;
	private final FlatProperty flatProperty;

	private Object initializer;

	public BasicPropertyOracle(BasicEntityTypeOracle entityOracle, FlatProperty flatProperty) {
		this.entityOracle = entityOracle;
		this.flatProperty = flatProperty;
	}

	@Override
	public EntityTypeOracle getEntityTypeOracle() {
		return entityOracle;
	}

	@Override
	public String getName() {
		return flatProperty.gmProperty.getName();
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return getGmPropertyInfos().stream().flatMap(gmPropertyInfo -> nullSafe(gmPropertyInfo.getMetaData()).stream());
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return getGmPropertyInfos().stream().flatMap(QualifiedMetaDataTools::ownMetaData);
	}

	@Override
	public List<GmPropertyInfo> getGmPropertyInfos() {
		return flatProperty.infos;
	}

	@Override
	public GmProperty asGmProperty() {
		return flatProperty.gmProperty;
	}

	@Override
	public Property asProperty() {
		return entityOracle.<EntityType<?>> asType().getProperty(flatProperty.gmProperty.getName());
	}

	@Override
	public Object getInitializer() {
		if (initializer == null)
			initializer = entityOracle.getSuperTypes().transitive().includeSelf().asEntityTypeOracles().stream() //
					.map(eto -> eto.findProperty(flatProperty.gmProperty)) //
					.filter(po -> po != null) //
					.flatMap(po -> po.getGmPropertyInfos().stream()) //
					.map(GmPropertyInfo::getInitializer) //
					.filter(i -> i != null) //
					.findFirst() //
					.orElse(void.class);

		return initializer == void.class ? null : initializer;
	}

}
