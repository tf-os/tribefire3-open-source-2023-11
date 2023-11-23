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
package com.braintribe.spring.support.converter;

import org.springframework.core.convert.converter.Converter;

import com.braintribe.model.style.Color;

public class StringHexToColorConverter implements Converter<String, Color>{
	
	@Override
	public Color convert(String source) {
		String cut = cutHex(source);
		Color c = Color.T.create();
		c.setRed(hexToR(cut));
		c.setGreen(hexToG(cut));
		c.setBlue(hexToB(cut));
		return c;
	}

	
	private int hexToR(String h) {
		return Integer.parseInt(h.substring(0,2),16);
	}
	private int hexToG(String h) {
		return Integer.parseInt(h.substring(2,4),16);
	}
	private int hexToB(String h) {
		return Integer.parseInt(h.substring(4,6),16);
	}
	
	private String cutHex(String h) {
		return (h.startsWith("#")) ? h.substring(1,7): h;
	}
	
}
