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
package com.braintribe.model.processing.query.smart.test2.basic;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static java.util.Collections.emptyList;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetup;
import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetupBuilder;
import com.braintribe.model.processing.query.smart.test2._common.SmartTestSetupConstants;
import com.braintribe.model.processing.query.smart.test2.basic.model.accessA.SimplePropertiesEntityA;
import com.braintribe.model.processing.query.smart.test2.basic.model.smart.SmartSimplePropertiesEntityA;
import com.braintribe.model.processing.smart.mapping.api.SmartMappingEditor;

/**
 * @author peter.gazdik
 */
public class BasicSmartSetup implements SmartTestSetupConstants {

	// @formatter:off
	private static final List<EntityType<?>> aEntities = asList(
			SimplePropertiesEntityA.T
	);

	private static final List<EntityType<?>> bEntities = emptyList();
	
	private static final List<EntityType<?>> smartEntities = asList(
			SmartSimplePropertiesEntityA.T
	);
	// @formatter:on

	public static final SmartModelTestSetup BASIC_SETUP = get();

	private static SmartModelTestSetup get() {
		return SmartModelTestSetupBuilder.build( //
				aEntities, //
				bEntities, //
				smartEntities, //

				EntityType::create, BasicSmartSetup::configureMappings, "smart.basic");
	}

	private static void configureMappings(SmartMappingEditor mapper) {
		mapper.onEntityType(GenericEntity.T, "root") //
				.allAsIs();

		mapper.onEntityType(SmartSimplePropertiesEntityA.T) //
				.forDelegate(SmartTestSetupConstants.accessIdA) //
				.entityTo(SimplePropertiesEntityA.T) //
				.propertyTo(SmartSimplePropertiesEntityA.smartString, SimplePropertiesEntityA.string) //
				.propertyTo(SmartSimplePropertiesEntityA.smartDate, SimplePropertiesEntityA.date);
	}
}
