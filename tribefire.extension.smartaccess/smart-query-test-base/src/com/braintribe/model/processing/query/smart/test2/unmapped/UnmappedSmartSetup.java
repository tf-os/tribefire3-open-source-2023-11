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
package com.braintribe.model.processing.query.smart.test2.unmapped;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static java.util.Collections.emptyList;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetup;
import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetupBuilder;
import com.braintribe.model.processing.query.smart.test2._common.SmartTestSetupConstants;
import com.braintribe.model.processing.query.smart.test2.unmapped.model.accessA.SimpleUnmappedA;
import com.braintribe.model.processing.query.smart.test2.unmapped.model.smart.UnmappedPropertySmart;
import com.braintribe.model.processing.query.smart.test2.unmapped.model.smart.UnmappedSubTypeSmart;
import com.braintribe.model.processing.query.smart.test2.unmapped.model.smart.UnmappedTopLevelTypeSmart;
import com.braintribe.model.processing.smart.mapping.api.SmartMappingEditor;

/**
 * @author peter.gazdik
 */
public class UnmappedSmartSetup implements SmartTestSetupConstants {

	// @formatter:off
	private static final List<EntityType<?>> aEntities = asList(
			SimpleUnmappedA.T
	);

	private static final List<EntityType<?>> bEntities = emptyList();
	
	private static final List<EntityType<?>> smartEntities = asList(
			SimpleUnmappedA.T,

			UnmappedPropertySmart.T,
			UnmappedSubTypeSmart.T,
			UnmappedTopLevelTypeSmart.T
	);
	// @formatter:on

	public static final SmartModelTestSetup UNMAPPED_SETUP = get();

	private static SmartModelTestSetup get() {
		return SmartModelTestSetupBuilder.build( //
				aEntities, //
				bEntities, //
				smartEntities, //

				EntityType::create, UnmappedSmartSetup::configureMappings, "smart.unmapped");
	}

	private static void configureMappings(SmartMappingEditor mapper) {
		mapper.onEntityType(GenericEntity.T, "root") //
				.allAsIs();

		mapper.onEntityType(UnmappedPropertySmart.T) //
				.entityTo(SimpleUnmappedA.T) //
				.propertyTo(UnmappedPropertySmart.mappedSubName, SimpleUnmappedA.mappedName) //
				.propertiesUnmapped( //
						UnmappedPropertySmart.unmappedSubName, //
						UnmappedPropertySmart.unmappedEntity);

		mapper.onEntityType(UnmappedSubTypeSmart.T) //
				.entityUnmapped();

		mapper.onEntityType(UnmappedTopLevelTypeSmart.T) //
				.entityUnmapped();
	}
}
