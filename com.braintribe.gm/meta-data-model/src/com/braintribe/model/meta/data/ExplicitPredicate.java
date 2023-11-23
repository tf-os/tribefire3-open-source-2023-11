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
 * Represents a {@link Predicate} which is <tt>false</tt> by default. This means, when no {@link MetaData} is configured, the predicate
 * evaluates to false (because otherwise the default would be true).
 * 
 * Example: something like UniqueProperty would be an ExplicitPredicate, as we do not want properties to be unique, unless stated
 * explicitly.
 */
@Abstract
public interface ExplicitPredicate extends Predicate {

	EntityType<ExplicitPredicate> T = EntityTypes.T(ExplicitPredicate.class);

}
