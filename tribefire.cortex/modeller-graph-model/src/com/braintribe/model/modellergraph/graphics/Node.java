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
package com.braintribe.model.modellergraph.graphics;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.modellergraph.condensed.CondensedType;


public interface Node extends GraphElement {

	final EntityType<Node> T = EntityTypes.T(Node.class);

	// @formatter:off
	Object getCustomData();
	void setCustomData(Object customData);

	Point getCenter();
	void setCenter(Point center);

	Double getRadius();
	void setRadius(Double radius);

	String getText();
	void setText(String text);

	Color getColor();
	void setColor(Color color);

	double getOrder();
	void setOrder(double order);

	String getTypeSignature();
	void setTypeSignature(String typeSignature);

	CondensedType getType();
	void setType(CondensedType type);

	boolean getPinned();
	void setPinned(boolean pinned);

	boolean getSelected();
	void setSelected(boolean selected);
	// @formatter:on
}
