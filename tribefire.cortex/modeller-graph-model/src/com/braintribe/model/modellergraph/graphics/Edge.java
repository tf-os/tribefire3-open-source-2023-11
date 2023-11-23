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
import com.braintribe.model.meta.GmProperty;


public interface Edge extends GraphElement {

	final EntityType<Edge> T = EntityTypes.T(Edge.class);

	// @formatter:off
	String getName();
	void setName(String name);
	
	String getInverseName();
	void setInverseName(String inverseName);

	String getDescription();
	void setDescription(String description);

	Point getStart();
	void setStart(Point start);

	Point getEnd();
	void setEnd(Point end);

	Point getTurning();
	void setTurning(Point turning);

	Point getStartControl();
	void setStartControl(Point startControl);

	// Point getTurningControl();
	// void setTurningControl(Point turningControl);
	Point getEndControl();
	void setEndControl(Point endControl);

	// DockingKind getStartDockingKind();
	// void setStartDockingKind(DockingKind startDockingKind);

	// DockingKind getEndDockingKind();
	// void setEndDockingKind(DockingKind endDockingKind);

	Color getColor();
	void setColor(Color color);
	
	Color getStartColor();
	void setStartColor(Color color);
	
	Color getEndColor();
	void setEndColor(Color color);

	AggregationKind getStartAggregationKind();
	void setStartAggregationKind(AggregationKind startAggregationKind);

	AggregationKind getEndAggregationKind();
	void setEndAggregationKind(AggregationKind endAggregationKind);

	GeneralizationKind getGeneralizationKind();
	void setGeneralizationKind(GeneralizationKind generalizationKind);

	Node getFromNode();
	void setFromNode(Node fromNode);

	Node getToNode();
	void setToNode(Node toNode);

	double getOrder();
	void setOrder(double order);

	GmProperty getGmProperty();
	void setGmProperty(GmProperty gmProperty);

	boolean getCircular();
	void setCircular(boolean circular);
	
	boolean getAbove();
	void setAbove(boolean above);
	
	int getIndex();
	void setIndex(int index);
	// @formatter:on

}
