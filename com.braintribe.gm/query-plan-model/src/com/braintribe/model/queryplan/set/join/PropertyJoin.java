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
package com.braintribe.model.queryplan.set.join;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.value.ValueProperty;

/**
 * Joins two tuple-sets to each other by associating each element of the {@link #getOperand() operand} with it's property value described by
 * {@link #getValueProperty() valueProperty}.
 * <p>
 * There are special subclasses for property join based on the type of the property.
 * 
 * @see EntityJoin
 * @see ListJoin
 * @see SetJoin
 * @see MapJoin
 * @see Join
 */
@Abstract
public interface PropertyJoin extends Join {

	EntityType<PropertyJoin> T = EntityTypes.T(PropertyJoin.class);

	ValueProperty getValueProperty();
	void setValueProperty(ValueProperty valueProperty);

	JoinKind getJoinKind();
	void setJoinKind(JoinKind joinKind);

}
