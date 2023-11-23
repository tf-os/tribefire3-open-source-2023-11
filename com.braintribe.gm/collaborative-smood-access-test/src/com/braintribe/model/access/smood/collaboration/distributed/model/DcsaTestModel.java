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
package com.braintribe.model.access.smood.collaboration.distributed.model;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * @author peter.gazdik
 */
public class DcsaTestModel {

	public static final String name = "test:DcsaTestModel";

	// @formatter:off
	public static final List<EntityType<?>> types = asList(
			DcsaEntity.T,
			DcsaEntityPointer.T
	);

	public static final List<EntityType<?>> depTypes = asList(
			Resource.T,
			FileSystemSource.T
	);

	// @formatter:off

	public static GmMetaModel raw() {
		return new NewMetaModelGeneration().buildMetaModel(name, types, deps());
	}

	private static List<GmMetaModel> deps() {
		return depTypes.stream() //
				.map(EntityType::getModel) //
				.map(m -> m.<GmMetaModel>getMetaModel()) //
				.collect(Collectors.toList());
	}

	
}
