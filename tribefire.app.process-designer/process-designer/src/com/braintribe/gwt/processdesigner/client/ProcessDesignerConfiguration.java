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
package com.braintribe.gwt.processdesigner.client;

import com.braintribe.gwt.processdesigner.client.vector.Point;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ProcessDesignerConfiguration extends GenericEntity {

	final EntityType<ProcessDesignerConfiguration> T = EntityTypes.T(ProcessDesignerConfiguration.class);

	// @formatter:off
	@Initializer("12d")
	double getFontSize();
	void setFontSize(double fontSize);

	double getOutterMargin();
	void setOutterMargin(double outterMargin);

	@Initializer("15d")
	double getGridResolution();
	void setGridResolution(double gridResolution);

	@Initializer("35d")
	double getProcessNodeRadius();
	void setProcessNodeRadius(double processNodeRadius);

	@Initializer("3d")
	double getProcessNodeStrokeWidth();
	void setProcessNodeStrokeWidth(double processNodeStrokeWidth);

	@Initializer("'#e3e3e3'")
	String getProcessNodeColor();
	void setProcessNodeColor(String processNodeColor);	

	@Initializer("'silver'")
	String getProcessNodeStrokeColor();
	void setProcessNodeStrokeColor(String processNodeStrokeColor);	

	@Initializer("10d")
	double getDockingPointRadius();
	void setDockingPointRadius(double dockingPointRadius);

	@Initializer("3d")
	double getDockingPointStrokeWidth();
	void setDockingPointStrokeWidth(double dockingPointStrokeWidth);

	@Initializer("'#e3e3e3'")
	String getDockingPointColor();
	void setDockingPointColor(String dockingPointColor);

	@Initializer("'silver'")
	String getDockingPointStrokeColor();
	void setDockingPointStrokeColor(String dockingPointStrokeColor);

	@Initializer("2d")
	double getEdgeWidth();
	void setEdgeWidth(double edgeWidth);

	@Initializer("'#949494'")
	String getEdgeColor();
	void setEdgeColor(String edgeColor);
	
	@Initializer("25d")
	double getDefaultOffset();
	void setDefaultOffset(double defaultOffset);

	// TODO DV public Point defaultStartingPoint = new Point();
	Point getDefaultStartingPoint();
	void setDefaultStartingPoint(Point defaultStartingPoint);
	
	@Initializer("true")
	boolean getRenderNodes();
	void setRenderNodes(boolean renderNodes);

	@Initializer("true")
	boolean getRenderEdges();
	void setRenderEdges(boolean renderEdges);

	boolean getSnapToGrid();
	void setSnapToGrid(boolean snapToGrid);

	boolean getShowGridLines();
	void setShowGridLines(boolean showGridLines);

	@Initializer("10d")
	double getArrowHeight();
	void setArrowHeight(double arrowHeight);

	@Initializer("5d")
	double getArrowWidth();
	void setArrowWidth(double arrowWidth);

	@Initializer("enum(com.braintribe.gwt.processdesigner.client.ProcessDesignerMode,selecting)")
	ProcessDesignerMode getProcessDesignerMode();
	void setProcessDesignerMode(ProcessDesignerMode processDesignerMode);

	@Initializer("1d")
	double getScaleLevel();
	void setScaleLevel(double scaleLevel);

	@Initializer("0.25d")
	double getScaleChangeFactor();
	void setScaleChangeFactor(double scaleChangeFactor);
	// @formatter:on
}
