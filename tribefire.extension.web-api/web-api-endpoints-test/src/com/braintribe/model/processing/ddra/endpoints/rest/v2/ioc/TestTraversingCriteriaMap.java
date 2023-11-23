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
package com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc;

import com.braintribe.model.generic.pr.criteria.PatternCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyTypeCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.typematch.CollectionTypeMatch;
import com.braintribe.model.generic.pr.criteria.typematch.EntityTypeMatch;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.ddra.endpoints.api.DdraTraversingCriteriaMap;
import com.braintribe.model.DdraEndpointDepthKind;

import static com.braintribe.wire.api.util.Lists.list;
import static com.braintribe.wire.api.util.Sets.set;

@SuppressWarnings("deprecation")
public class TestTraversingCriteriaMap {

	private static DdraTraversingCriteriaMap INSTANCE;

	public static DdraTraversingCriteriaMap traversingCriteriaMap() {
		if (INSTANCE == null) {
			INSTANCE = new DdraTraversingCriteriaMap();
			INSTANCE.addDefaultCriterion(DdraEndpointDepthKind.shallow, standard());
			INSTANCE.addDefaultCriterion(DdraEndpointDepthKind.reachable, TC.create().negation().joker().done());
		}

		return INSTANCE;
	}

	private static TraversingCriterion standard() {
		// this code is taken from com.braintribe.cartridge.master.wire.space.cortex.TraversingCriteriaSpace
		PropertyTypeCriterion ptc = PropertyTypeCriterion.T.create();
		ptc.setTypes(set(EntityTypeMatch.T.create(), CollectionTypeMatch.T.create()));

		PatternCriterion bean = PatternCriterion.T.create();
		bean.setCriteria(list(com.braintribe.model.generic.pr.criteria.EntityCriterion.T.create(), ptc));
		return bean;

	}
}
