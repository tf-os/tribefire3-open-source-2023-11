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
package com.braintribe.model.accessdeployment.smart.meta.conversion;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Represents a conversion of given (property) value.
 * <p>
 * The direction of the conversion is determined by the {@link #setInverse(boolean) inverse} flag. The standard
 * direction is the direction from delegate to the smart level, so for example a {@link DateToString} conversion (not
 * inverted) could be used if the property has type {@link Date} in the delegate, but is a {@link String} on the smart
 * level.
 */
@Abstract
public interface SmartConversion extends GenericEntity {

	EntityType<SmartConversion> T = EntityTypes.T(SmartConversion.class);

	void setInverse(boolean inverse);
	boolean getInverse();

}
