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
package tribefire.extension.webapi.web_api_server.wire.space;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.orTc;

import com.braintribe.ddra.endpoints.api.DdraTraversingCriteriaMap;
import com.braintribe.model.DdraEndpointDepthKind;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TribefireModuleContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class TcSpace implements TribefireModuleContract {

	@Managed
	public DdraTraversingCriteriaMap criteriaMap() {
		DdraTraversingCriteriaMap map = new DdraTraversingCriteriaMap();
		map.addDefaultCriterion(DdraEndpointDepthKind.shallow, tcShallow());
		map.addDefaultCriterion(DdraEndpointDepthKind.reachable, tcReachable());
		
		return map;
	}
	
	@Managed
	private TraversingCriterion tcReachable() {
		TraversingCriterion bean = TC.create().negation().joker().done();
		return bean;
	}
	
	@Managed
	private TraversingCriterion tcShallow() {
		return TC.create()
		.conjunction()
		.property()
		.typeCondition(orTc(
			isKind(TypeKind.collectionType),
			isKind(TypeKind.entityType)
		))
		.close()
		.done();
	}
}
