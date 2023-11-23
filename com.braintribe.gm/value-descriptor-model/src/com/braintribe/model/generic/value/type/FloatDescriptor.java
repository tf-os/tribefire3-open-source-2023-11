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
package com.braintribe.model.generic.value.type;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.FloatType;
import com.braintribe.model.generic.reflection.SimpleTypes;

/**
 * A @link{StaticallyTypedDescriptor} that will always be a @Link{Float}
 *
 */
@Abstract
public interface FloatDescriptor extends StaticallyTypedDescriptor {

	EntityType<FloatDescriptor> T = EntityTypes.T(FloatDescriptor.class);

	@Override
	default FloatType valueType() {
		return SimpleTypes.TYPE_FLOAT;
	}

}
