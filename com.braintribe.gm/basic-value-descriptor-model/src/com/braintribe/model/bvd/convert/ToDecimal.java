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
package com.braintribe.model.bvd.convert;

import java.math.BigDecimal;


import com.braintribe.model.generic.value.type.DecimalDescriptor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link FormattedConvert} that converts to decimal ({@link BigDecimal})
 *
 */

public interface ToDecimal extends FormattedConvert, DecimalDescriptor {

	final EntityType<ToDecimal> T = EntityTypes.T(ToDecimal.class);
	// can only convert from String, Boolean
}
