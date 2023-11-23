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
package com.braintribe.model.style;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

public enum FontWeight implements EnumBase {
	normal("normal"), bold("bold"), weight100("100"), weight200("200"), weight300("300"), weight400("400"), weight500("500"),
	weight600("600"), weight700("700"), weight800("800"), weight900("900");

    public static final EnumType T = EnumTypes.T(FontWeight.class);
	
	@Override
	public EnumType type() {
		return T;
	}

	private final String text;

    private FontWeight(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }	
}
