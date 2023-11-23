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

import com.braintribe.model.meta.selector.StringPropertyDiscriminator;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;

public class StringPropertyDiscriminatorExpert extends SimplePropertyDiscriminatorExpert<StringPropertyDiscriminator, String> {

	public StringPropertyDiscriminatorExpert() {
		super(String.class);
	}

	@Override
	public boolean matches(StringPropertyDiscriminator selector, SelectorContext context) throws Exception {
		Object actualValue = getProperty(selector, context);

		/* null will not enter the if block since null instanceof X returns false */
		if (actualValue instanceof String || actualValue instanceof Enum<?>) {
			String discriminatorValue = selector.getDiscriminatorValue();
			return actualValue.toString().equals(discriminatorValue);
		}

		return false;
	}

}
