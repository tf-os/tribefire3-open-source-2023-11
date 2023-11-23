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
package com.braintribe.model.queryplan.filter;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 * represents a filter that is comparing two values
 * 
 * depending on the kind of the filter, the property path may be used or not.
 * 
 * if property path's set however, the addressedAlias must denote a valid entity.
 * 
 * @author pit & dirk
 *
 */
@Abstract
public interface ValueComparison extends Condition {

	EntityType<ValueComparison> T = EntityTypes.T(ValueComparison.class);

	Value getLeftOperand();
	void setLeftOperand(Value leftOperand);

	Value getRightOperand();
	void setRightOperand(Value rightOperand);

}
