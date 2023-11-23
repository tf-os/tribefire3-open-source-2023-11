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
package com.braintribe.model.access.smart.test.base;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.util.Map;

import com.braintribe.model.access.smart.SmartAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.processing.query.smart.test.base.DelegateAccessSetup;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;

/**
 * 
 */
public class AccessSetup extends DelegateAccessSetup {

	private SmartAccess smartAccess;

	// ######################################
	// ## . . . . . Smart Access . . . . . ##
	// ######################################

	public AccessSetup(SmartMappingSetup setup) {
		super(setup);
	}

	public SmartAccess getSmartAccess() {
		if (smartAccess == null) {
			smartAccess = configureSmartAccess();
		}
		return smartAccess;
	}

	private SmartAccess configureSmartAccess() {
		SmartAccess result = new SmartAccess();

		result.setAccessMapping(accessMapping());
		result.setSmartDenotation(setup.accessS);
		result.setMetaModel(setup.modelS);
		result.setDefaultTraversingCriteria(defaultSmartTraversingCriteria());

		return result;
	}

	private Map<IncrementalAccess, com.braintribe.model.access.IncrementalAccess> accessMapping() {
		return asMap(setup.accessA, getAccessA(), setup.accessB, getAccessB());
	}

	/** We stop on any entity- or collection- property */
	private Map<Class<? extends GenericEntity>, TraversingCriterion> defaultSmartTraversingCriteria() {
		// @formatter:off
		TraversingCriterion defaultTc = TC.create()
					.typeCondition(
							or(
								isKind(TypeKind.entityType),
								isKind(TypeKind.collectionType)
							)
					)
				.done();
		// @formatter:on

		return asMap(GenericEntity.class, defaultTc);
	}

}
