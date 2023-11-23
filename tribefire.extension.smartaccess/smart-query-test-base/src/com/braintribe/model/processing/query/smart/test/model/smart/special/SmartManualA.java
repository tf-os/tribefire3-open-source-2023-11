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
package com.braintribe.model.processing.query.smart.test.model.smart.special;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ManualA;

/**
 * 
 * Mapped to ManualA (which is not a sub-type of BookA)
 */
public interface SmartManualA extends SmartBookA {

	EntityType<SmartManualA> T = EntityTypes.T(SmartManualA.class);

	/**
	 * This is mapped to {@link ManualA#getManualString()}. The reason for the weird name is, that it is also used in other use-cases so we
	 * do not have to add new properties when testing a use-case. For example, the mapping for
	 * {@link SmartReaderA#getWeakInverseFavoriteManuals()}.
	 */
	String getSmartManualString();
	void setSmartManualString(String smartManualString);

}
