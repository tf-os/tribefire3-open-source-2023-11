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
package tribefire.extension.js.core.wire.space;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.space.FilterConfigurationSpace;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.js.core.impl.JsDependencyFiltering;
import tribefire.extension.js.core.wire.contract.JsResolverConfigurationContract;

/**
 * specific implementation of the {@link FilterConfigurationContract} that takes the relevant settings (partExpectation) for the {@link JsResolverConfigurationContract}
 * @author pit
 *
 */
@Managed
public class JsFilterConfigurationSpace extends FilterConfigurationSpace{
	@Import
	JsResolverConfigurationContract jsResolverConfiguration;
	
	@Import
	JsResolverSpace jsResolver;
	
	
	@Override
	public BiPredicate<? super Solution, ? super Dependency> solutionDependencyFilter() {
		return JsDependencyFiltering::filter;
	}
	
	@Override
	public Predicate<? super Solution> solutionFilter() {
		return jsResolver.jsResolver()::filterSolution;
	}
	
	@Override
	public Collection<PartTuple> partExpectation() {		
		if (jsResolverConfiguration.relevantPartTuples() != null) {
			return jsResolverConfiguration.relevantPartTuples();
		}
		else {
			return Collections.emptyList();
		}
		
	}
	
}
