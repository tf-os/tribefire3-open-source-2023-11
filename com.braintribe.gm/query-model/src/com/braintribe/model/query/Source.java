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
package com.braintribe.model.query;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Property;

/**
 * An {@link Operand} that encapsulates the sources for which the {@link Query} request will be applied.
 */
public interface Source extends Operand {

	EntityType<Source> T = EntityTypes.T(Source.class);

	String getName();
	void setName(String name);

	void setJoins(Set<Join> joins);
	Set<Join> getJoins();
	
	default Join join(String propertyName) {
		Join join = Join.T.create();
		this.getJoins().add(join);
		join.setProperty(propertyName);
		join.setSource(this);
		return join;
	}
	
	default Join join(String propertyName, JoinType joinType) {
		Join join = Join.T.create();
		this.getJoins().add(join);
		join.setProperty(propertyName);
		join.setSource(this);
		join.setJoinType(joinType);
		return join;
	}
	
	default Join leftJoin(String propertyName) {
		return join(propertyName, JoinType.left);
	}
	
	default Join rightJoin(String propertyName) {
		return join(propertyName, JoinType.right);
	}
	
	default Join innerJoin(String propertyName) {
		return join(propertyName, JoinType.inner);
	}
	
	default Join fullJoin(String propertyName) {
		return join(propertyName, JoinType.full);
	}
	
	default PropertyOperand property(String name) {
		return PropertyOperand.create(this, name);
	}
	
	default PropertyOperand property(Property property) {
		return PropertyOperand.create(this, property.getName());
	}
}
