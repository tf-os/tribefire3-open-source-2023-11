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
package com.braintribe.model.generic.pr.criteria;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This criterion represents another criterion with can be resolved by {@link #getName() name}.
 * <p>
 * Example: Every access can have default {@link TraversingCriterion} which describe which entity properties should be loaded (fetched) eagerly. If
 * the client is fine with the defaults, he does not have to specify the TC at all, thus saving time building it and data transfered. Along the way,
 * some aspect might need to adjust the criteria to load more information, e.g. to apply some authorization checks. In order to be able to extend the
 * default criteria, instead of replacing them, an explicit representation of the default TC is needed, to describe how exactly they are combined with
 * the injected TC.
 */
public interface PlaceholderCriterion extends BasicCriterion {

	EntityType<PlaceholderCriterion> T = EntityTypes.T(PlaceholderCriterion.class);

	@Mandatory
	String getName();
	void setName(String name);

	@Override
	default CriterionType criterionType() {
		return CriterionType.PLACEHOLDER;
	}

}
