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
package com.braintribe.devrock.eclipse.model.storage;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Color extends GenericEntity{
	
	EntityType<Color> T = EntityTypes.T(Color.class);
	
	String red = "red";
	String green = "green";
	String blue = "blue";
	
	int getRed();
	void setRed(int value);

	int getGreen();
	void setGreen(int value);

	int getBlue();
	void setBlue(int value);

	
	static Color create( int r, int g, int b) {
		Color color = Color.T.create();
		color.setRed(r);
		color.setGreen(g);
		color.setBlue(b);
		return color;
	}
	
	default int [] asArray() {
		int [] retval = new int[3];
		retval[0] = getRed();
		retval[1] = getGreen();
		retval[2] = getBlue();
		return retval;
	}
}
