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
package com.braintribe.model.queryplan;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.join.PropertyJoin;

/**
 * 
 * Defines a slot / dimension / position in a tuple. The {@link #getIndex() index} property is a unique identifier and
 * is used by an evaluator to make access for given component value faster.
 * <p>
 * In other words, this index has to be set by the query planner and has to be unique through all the components of the
 * resulting tuple set. It determines the position of given component in the resulting tuple.
 * 
 * @see SourceSet
 * @see PropertyJoin
 * 
 * @author pit & dirk
 */
@Abstract
public interface TupleComponentPosition extends GenericEntity {

	EntityType<TupleComponentPosition> T = EntityTypes.T(TupleComponentPosition.class);

	int getIndex();
	void setIndex(int index);

}
