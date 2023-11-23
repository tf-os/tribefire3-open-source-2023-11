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
package com.braintribe.model.processing.query.tools;

import com.braintribe.model.generic.GenericEntity;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;

/**
 * This is used as an identifiable replacement for PropertyOperand related to a {@link GenericEntity#partition} property, when replacing with String
 * is not possible (e.g. due to type constraints - cloning a {@link PropertyOperand} representing a partition property must result in an entity).
 */
public interface PlaceholderPartition extends Operand {

	EntityType<PlaceholderPartition> T = EntityTypes.T(PlaceholderPartition.class);

	PlaceholderPartition INSTANCE = PlaceholderPartition.T.create();

}
