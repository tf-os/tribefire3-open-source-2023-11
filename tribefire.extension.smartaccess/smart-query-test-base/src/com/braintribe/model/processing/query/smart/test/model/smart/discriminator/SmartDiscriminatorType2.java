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
package com.braintribe.model.processing.query.smart.test.model.smart.discriminator;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @see SmartDiscriminatorBase
 */

public interface SmartDiscriminatorType2 extends SmartDiscriminatorBase {

	EntityType<SmartDiscriminatorType2> T = EntityTypes.T(SmartDiscriminatorType2.class);

	final String DISC_TYPE2 = "type2";

	// @formatter:off
	String getType2Name();
	void setType2Name(String value);

	/** For special case testing, only Type2 has "discriminator" property mapped */
	String getDiscriminator();
	void setDiscriminator(String value);
	// @formatter:on

}
