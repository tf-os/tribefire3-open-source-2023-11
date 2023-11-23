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
package com.braintribe.ddra.endpoints.api;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.DdraEndpointDepth;
import com.braintribe.model.DdraEndpointDepthKind;
import com.braintribe.model.processing.web.rest.HttpExceptions;

public class DdraTraversingCriteriaMap {

	private Map<Object, TraversingCriterion> criterias = new HashMap<>();
	
	@Configurable
	public void addDefaultCriterion(DdraEndpointDepthKind kind, TraversingCriterion criterion) {
		criterias.put(kind, criterion);
	}
	
	public TraversingCriterion getCriterion(DdraEndpointDepthKind kind) {
		TraversingCriterion result = criterias.get(kind);
		if(result == null) {
			throw new IllegalStateException("Unexpected state: DdraTraversingCriteriaMap has not been initialized with a criterion for " + kind);
		}
		return result;
	}
	
	public TraversingCriterion getCriterion(int depth) {
		TraversingCriterion result = criterias.get(depth);
		if(result == null) {
			result = createFor(depth);
			criterias.put(depth, result);
		}
		return result;
	}
	
	public TraversingCriterion getCriterion(DdraEndpointDepth depth) {
		switch(depth.getKind()) {
			case custom:
				return getCriterion(depth.getCustomDepth());
			case shallow:
				return getCriterion(DdraEndpointDepthKind.shallow);
			case reachable:
				return getCriterion(DdraEndpointDepthKind.reachable);
			default:
				HttpExceptions.internalServerError("Unexpected enpoint depth kind %s", depth.getKind());
				return null;
		}
	}
	
	private TraversingCriterion createFor(int depth) {
		TraversingCriterion shallow = getCriterion(DdraEndpointDepthKind.shallow);
		
		// @formatter:off
		return TC.create()
				.pattern() // pattern 1
					.recursion(depth, depth)
						.pattern() // pattern 2
							.entity()
							.disjunction() // disjunction 1
								.property()
								.pattern() // pattern 3
									.property()
									.disjunction() // disjunction 2
										.listElement()
										.setElement()
										.pattern().map().mapKey().close()
										.pattern().map().mapValue().close()
									.close() // disjunction 2
								.close() // pattern 3
							.close() // disjunction 1
						.close() // pattern 2
					.criterion(shallow)
				.close() // pattern 1
			.done();
		// @formatter:on
	}
}
