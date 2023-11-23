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
package com.braintribe.model.resourceapi.stream.range;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface StreamRange extends GenericEntity {

	EntityType<StreamRange> T = EntityTypes.T(StreamRange.class);

	@Description("The starting point of the requested stream, with first byte starting on position 0.")
	Long getStart();
	void setStart(Long start);

	@Description("The inclusive end point of the requested stream.")
	Long getEnd();
	void setEnd(Long end);

	static StreamRange create(Long start, Long end) {
		StreamRange result = StreamRange.T.create();
		result.setStart(start);
		result.setEnd(end);

		return result;
	}
}