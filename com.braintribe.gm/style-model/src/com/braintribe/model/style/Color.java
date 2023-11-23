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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A <code>Color</code> entity represents a color defined by three values (i.e. red, green, blue) where each value is an
 * integer from <code>0</code> to <code>255</code>.
 * 
 * @author michael.lafite
 */

public interface Color extends GenericEntity {

	EntityType<Color> T = EntityTypes.T(Color.class);

	Integer getRed();
	void setRed(Integer red);

	Integer getGreen();
	void setGreen(Integer green);

	Integer getBlue();
	void setBlue(Integer blue);
	
	default Color color(String cssColorCode) {
		int index = 0;
		if (cssColorCode.startsWith("#"))
			index = 1;
		
		setRed(Integer.parseInt(cssColorCode.substring(index, index + 2), 16));
		setGreen(Integer.parseInt(cssColorCode.substring(index + 2, index + 4), 16));
		setBlue(Integer.parseInt(cssColorCode.substring(index + 4, index + 6), 16));
		
		return this;
	}
	
	default Color initColor(int r, int g, int b) {
		setRed(r);
		setGreen(g);
		setBlue(b);
		
		return this;
	}
	
	static Color create(String cssColorCode) {
		return T.create().color(cssColorCode);
	}
	
	static Color create(int r, int g, int b) {
		return T.create().initColor(r, g, b);
	}
	
}
