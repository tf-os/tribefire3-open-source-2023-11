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
package com.braintribe.devrock.mc.core.wirings.transitive.contract;

import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.wire.api.space.WireSpace;

/**
 * top tier 
 * @author pit
 *
 */
public interface TransitiveResolverContract extends WireSpace {
	/**
	 * @return - the {@link TransitiveDependencyResolver}
	 */
	TransitiveDependencyResolver transitiveDependencyResolver();
	/**
	 * @return - the {@link ArtifactDataResolverContract} as configured by the {@link TransitiveResolverContract}
	 */
	ArtifactDataResolverContract dataResolverContract();
}
