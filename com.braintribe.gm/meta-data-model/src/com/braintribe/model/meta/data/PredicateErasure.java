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
package com.braintribe.model.meta.data;

import com.braintribe.model.generic.annotation.Abstract;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link PredicateErasure} is a predicate that removes the positive effect of it's supertype. For example, for visibility we have a
 * {@link Predicate} called Visible. In order to make something not visible, we use a predicate called Hidden, which is a subtype of both Visible and
 * {@link PredicateErasure}, thus saying this negates the positive effect of Visible.
 * <p>
 * Note that one should (almost) never resolve a PredicateErasure with CmdResolver. When resolving the positive predicate, the resolved MD could be an
 * instance of that positive type, or the erasure, but the resolution of an erasure always returns just the erasure (obviously, as that is the
 * subtype). The CMD resolver would actually throw an exception if the predicate-specific resolution method would be used ({@code .is(Hidden.T)}), but
 * there is no exception when resolving in a general way (via {@code list()/exclusive()} methods).
 * 
 * @see Predicate
 */
@Abstract
public interface PredicateErasure extends Predicate {

	EntityType<PredicateErasure> T = EntityTypes.T(PredicateErasure.class);

	@Override
	default boolean isTrue() {
		return false;
	}

}
