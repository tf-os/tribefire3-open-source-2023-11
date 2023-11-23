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
package com.braintribe.model.queryplan.value.range;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */

public interface SimpleRange extends Range {

	EntityType<SimpleRange> T = EntityTypes.T(SimpleRange.class);

	Value getLowerBound();
	void setLowerBound(Value lowerBound);

	Value getUpperBound();
	void setUpperBound(Value upperBound);

	boolean getLowerInclusive();
	void setLowerInclusive(boolean lowerInclusive);

	boolean getUpperInclusive();
	void setUpperInclusive(boolean upperInclusive);

	@Override
	default RangeType rangeType() {
		return RangeType.simple;
	}

}
