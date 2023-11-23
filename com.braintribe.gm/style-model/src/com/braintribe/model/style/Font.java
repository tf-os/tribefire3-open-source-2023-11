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
 * A <code>Font</code> entity represents a Font style, name, size, weight,..
 * 
 */
public interface Font extends GenericEntity {

	EntityType<Font> T = EntityTypes.T(Font.class);

	Color getColor();
	void setColor(Color color);

	String getFamily();
	void setFamily(String family);

	ValueWithUnit getSize();
	void setSize(ValueWithUnit size);

	FontStyle getStyle();
	void setStyle(FontStyle style);

	FontStretch getStrech();
	void setStrech(FontStretch strech);

	FontWeight getWeight();
	void setWeight(FontWeight weight);

	FontVariant getVariant();
	void setVariant(FontVariant variant);

	String getFontUrl();
	void setFontUrl(String fonUrl);

}
