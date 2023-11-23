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
package com.braintribe.model.processing.query.fluent;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.PersistentEntityReference;

/**
 * Interface for a builder which can be used for building operands.
 * 
 * @param <B>
 *            type of the builder that follows this builder in the chain (i.e. the one that is returned by any of the builder methods)
 * 
 * @see OperandBuilder
 * @see ConditionBuilder
 */
public interface IOperandBuilder<B> {

	/**
	 * Sets given value as operand without any conversion, e.g. if a {@link GenericEntity} is given, it is used as-is. This method enables
	 * setting a collection of references as operand, cause there is no other way of doing so. For other cases, those other methods are
	 * preferred.
	 */
	B operand(Object value);

	/**
	 * Builds a constant as an operand value. Note that the value is encoded, so for example if it is a {@link GenericEntity}, it is
	 * replaced with a reference. If you already have a reference, use {@link #entityReference(PersistentEntityReference)} or
	 * {@link #operand(Object)} instead. If you have a collection of references, you have to use the letter method only.
	 */
	B value(Object value);

	B property(String name);

	B property(String alias, String name);

	B entity(String alias);

	B entity(GenericEntity entity);

	B entityReference(PersistentEntityReference reference);

	B listIndex(String joinAlias);

	B mapKey(String joinAlias);

	IOperandBuilder<B> localize(String locale);

	B localize(Object operand, String locale);

	IOperandBuilder<B> entitySignature();

	IOperandBuilder<B> asString();

	// ###############################################
	// ## . . . . . . . Aggregations . . . . . . . .##
	// ###############################################

	B count();
	
	B count(String alias);

	B count(String alias, String propertyName);

	B count(String alias, String propertyName, boolean distinct);

	B max(String alias, String propertyName);
	
	B min(String alias, String propertyName);
	
	B sum(String alias, String propertyName);
	
	B avg(String alias, String propertyName);

}
