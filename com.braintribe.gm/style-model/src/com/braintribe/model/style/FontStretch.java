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

public enum FontStretch implements EnumBase {
	normal("normal"), condensed("condensed"), ultraCondensed("ultra-condensed"), extraCondensed("extra-condensed"), 
	semiCondensed("semi-condensed"), expanded("expanded"), semiExpanded("semi-expanded"), extraExpanded("extra-expanded"), ultraExpanded("ultra-expanded");
    
    public static final EnumType T = EnumTypes.T(FontStretch.class);
	
	@Override
	public EnumType type() {
		return T;
	}
    
	private final String text;

    private FontStretch(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }	
}
