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
package com.braintribe.model.processing.meta.cmd.context.experts;

import com.braintribe.model.meta.selector.NullPropertyDiscriminator;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;

@SuppressWarnings("unusable-by-js")
public class NullPropertyDiscriminatorExpert extends SimplePropertyDiscriminatorExpert<NullPropertyDiscriminator, Object> {

	public NullPropertyDiscriminatorExpert() {
		super(Object.class);
	}

	@Override
	public boolean matches(NullPropertyDiscriminator selector, SelectorContext context) throws Exception {
		Object actualValue = getProperty(selector, context);

		/* In case you are wondering, the ^ operator is xor, which is (for booleans) equivalent to !=. For me, however,
		 * the xor is more readable than !=. */
		return (actualValue == null) ^ selector.getInverse();
	}

}
