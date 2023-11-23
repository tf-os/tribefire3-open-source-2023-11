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
 * A Predicate is a {@link MetaData} which evaluates to either <code>true</code> or <code>false</code> (e.g. Visible).
 * 
 * <h2>Basics</h2>
 * 
 * In order to configure a Predicate and to be able to remove it's effect, we use a type-hierarchy where the positive side is represented by a direct
 * sub-type of this type (or {@link ExplicitPredicate}, see later), and the negative side is represented by it's sub-type, which must also be a
 * sub-type of {@link PredicateErasure}.
 * 
 * <h2>Example</h2>
 * 
 * For configuring visibility the <tt>essential-meta-data-model</tt> contains an MD type {@code Visible} which extends {@code Predicate}, and
 * {@code Hidden}, which extends both {@code Visible} and {@code PredicateErasure}. Now, if we want to say for some entity "E" that all properties
 * except "X" are hidden, we can attach {@code Hidden} as a MD for all properties of "E", and attach {@code Visible} to the property "X".
 * 
 * <h2>Default value and ExplicitPredicate</h2>
 * 
 * We can also specify the default value of our predicate in case no MD is configured. For example {@code Visible} is <tt>true</tt> by default, but
 * {@code Unique} is <tt>false</tt> by default. In general, if the predicate type is a direct sub-type of this type ({@link Predicate}), it is
 * <tt>true</tt> by default, while if it is a sub-type of {@link ExplicitPredicate}, it is <tt>false</tt> by default. The term "explicit" means it has
 * to be configured explicitly to be <tt>true</tt>.
 * 
 * @see ExplicitPredicate
 * @see PredicateErasure
 */
@Abstract
public interface Predicate extends MetaData {

	EntityType<Predicate> T = EntityTypes.T(Predicate.class);

	default boolean isTrue() {
		return true;
	}

}
