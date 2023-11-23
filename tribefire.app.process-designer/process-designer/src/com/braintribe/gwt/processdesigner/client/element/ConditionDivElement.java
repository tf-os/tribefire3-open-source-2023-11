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
package com.braintribe.gwt.processdesigner.client.element;

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerRenderer;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.gwt.processdesigner.client.vector.Complex;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processdefrep.EdgeRepresentation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.FlowPanel;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.StandardNode;

public class ConditionDivElement extends AbstractProcessSvgElement<ConditionProcessor> {

	EdgeSvgElement parentEdgeSvgElement;
	ProcessDesignerRenderer renderer;
	FlowPanel wrapper;

	private boolean conditionWasHovered;
	private ConditionElementHandler conditionElementHandler = new ConditionElementHandler();

	public ConditionDivElement(ConditionProcessor genericEntity) {
		super(genericEntity);
	}

	public FlowPanel getWrapper() {
		if (wrapper == null) {
			wrapper = new FlowPanel();
			wrapper.addStyleName("conditionElement");

			wrapper.addDomHandler(this, MouseOverEvent.getType());
			wrapper.addDomHandler(this, MouseOutEvent.getType());
			wrapper.addDomHandler(this, MouseDownEvent.getType());
			wrapper.addDomHandler(this, MouseMoveEvent.getType());
			wrapper.addDomHandler(this, MouseUpEvent.getType());
			wrapper.addDomHandler(this, ClickEvent.getType());

			wrapper.addDomHandler(conditionElementHandler, MouseOverEvent.getType());
			wrapper.addDomHandler(conditionElementHandler, MouseOutEvent.getType());
			wrapper.addDomHandler(conditionElementHandler, MouseOverEvent.getType());
			wrapper.addDomHandler(conditionElementHandler, MouseOutEvent.getType());
		}
		return wrapper;
	}

	@Override
	public FlowPanel getRepresentation() {
		return getWrapper();
	}

	@Override
	public void setX(double x) {
		adaptGroup();
	}

	@Override
	public double getX() {
		return 0;
	}

	@Override
	public void setY(double y) {
		adaptGroup();
	}

	@Override
	public double getY() {
		return 0;
	}

	@Override
	public void setX2(double x2) {
		adaptGroup();
	}

	@Override
	public double getX2() {
		return 0;
	}

	@Override
	public void setY2(double y2) {
		adaptGroup();
	}

	@Override
	public double getY2() {
		return 0;
	}

	@Override
	public void setCenterX(double centerX) {
		adaptGroup();
	}

	@Override
	public double getCenterX() {
		return 0;
	}

	@Override
	public void setCenterY(double centerY) {
		adaptGroup();
	}

	@Override
	public double getCenterY() {
		return 0;
	}

	@Override
	public void setWidth(double width) {
		adaptGroup();
	}

	@Override
	public double getWidth() {
		return getWrapper().getOffsetWidth();
	}

	@Override
	public void setHeight(double height) {
		adaptGroup();
	}

	@Override
	public double getHeight() {
		return getWrapper().getOffsetHeight();
	}

	@Override
	public void handleSelectionChange() {
		if (getSelected()) {
			getWrapper().removeStyleName("hovered");
			getWrapper().addStyleName("selected");
		} else {
			getWrapper().removeStyleName("selected");
		}
	}

	@Override
	public void handleHoveringChange() {
		if (!getSelected()) {
			if (getHovered()) {
				getWrapper().addStyleName("hovered");
			} else {
				getWrapper().removeStyleName("hovered");
			}
		} else
			getWrapper().removeStyleName("hovered");
	}

	@Override
	public void handleActiveChange() {
		//NOP
	}

	@Override
	public void initialize() {
		adaptGroup();
	}

	@Override
	public boolean doesIntersect(Rect mouseRect) {
		return new Rect(getX(), getY(), getWidth(), getHeight()).intersect(mouseRect) != null;
	}

	@Override
	public void commit() {
		//NOP
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public ElementKind getElementKind() {
		return null;
	}

	@Override
	public boolean canBeConnected() {
		return false;
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		adaptGroup();
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
//		parentEventHandler.delegateOnMouseDown(event);
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
//		parentEventHandler.delegateOnMouseUp(event);
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
//		parentEventHandler.delegateOnMouseMove(event);
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
//		Console.log("over");
		parentEventHandler.hoverElement(this, true, event);
	}

	@Override
	public void onClick(ClickEvent event) {
//		Console.log(getSelected());
		if (!getSelected())
			parentEventHandler.selectElement(this, false, true);
		else
			parentEventHandler.deselectElement(this, true);
	}

	public void setParentEdgeSvgElement(EdgeSvgElement parentEdgeSvgElement) {
		this.parentEdgeSvgElement = parentEdgeSvgElement;
	}

	public void setRenderer(ProcessDesignerRenderer processDesignerRenderer) {
		this.renderer = processDesignerRenderer;
	}

	public void adaptGroup() {
		Complex start = new Complex(parentEdgeSvgElement.getX(), parentEdgeSvgElement.getY());
		Complex end = new Complex(parentEdgeSvgElement.getX2(), parentEdgeSvgElement.getY2());

		Complex centre = new Complex(parentEdgeSvgElement.getCenterX(), parentEdgeSvgElement.getCenterY());

		EdgeRepresentation edgeRepresentation = (EdgeRepresentation) parentEdgeSvgElement.getEntity();
		Edge edge = edgeRepresentation.getEdge();

		ConditionalEdge conditionalEdge = (ConditionalEdge) edge;
		StandardNode fromNode = conditionalEdge.getFrom();
		Complex rotation = start.minus(end);
		int degree = (int) (Math.atan2(rotation.y, rotation.x) * (180 / Math.PI));

		if (degree >= 90 && degree <= 180) {
			degree -= 180;
		} else if (degree <= -90 && degree >= -180) {
			degree += 180;
		}

		ConditionProcessor condition = conditionalEdge.getCondition();
		String name = (fromNode.getConditionalEdges() != null && fromNode.getConditionalEdges().size() > 1) ? "ELSE"
				: "TRUE";

		if (fromNode.getConditionalEdges().size() == 1 && conditionalEdge.getDescription() != null)
			name = conditionalEdge.getDescription();

		if (conditionalEdge.getName() != null) {
			name = edge.getName().value();
		} else {
			if (condition != null) {
				// name = condition.getDescription() != null ?
				// I18nTools.getDefault(condition.getDescription(), "") : null;
				// if(name == null || name.isEmpty())
				name = condition.getName() != null ? condition.getName() : null;
				if (name == null || name.isEmpty())
					name = condition.getExternalId();
			}
		}

		if (fromNode.getConditionalEdges() != null && fromNode.getConditionalEdges().size() > 1) {
			int index = -1;
			try {
				index = fromNode.getConditionalEdges().indexOf(edge);
			} catch (Exception ex) {
				index = -1;
			} finally {
				if (index != -1)
					name = (condition != null ? (index + 1) + ": " : "") + name;
			}
		}

		FlowPanel fp = getWrapper();
		fp.getElement().setInnerText(name);

		Style style = fp.getElement().getStyle();
		style.setProperty("transform", "rotate(" + degree + "deg)");
		style.setLeft(centre.x - getWidth() / 2, Unit.PX);
		style.setTop(centre.y - getHeight() / 2, Unit.PX);
	}

	public EdgeSvgElement getParentEdgeSvgElement() {
		return parentEdgeSvgElement;
	}

	public Edge getEdge() {
		if (parentEdgeSvgElement != null) {
			EdgeRepresentation edgeRepresentation = (EdgeRepresentation) parentEdgeSvgElement.getEntity();
			if (edgeRepresentation != null)
				return edgeRepresentation.getEdge();
		}
		return null;
	}

	class ConditionElementHandler implements MouseOutHandler, MouseOverHandler {

		@Override
		public void onMouseOut(MouseOutEvent event) {
			conditionWasHovered = false;
		}

		@Override
		public void onMouseOver(MouseOverEvent event) {
			conditionWasHovered = true;
		}
	}

	public void setConditionWasHovered(boolean conditionWasHovered) {
		this.conditionWasHovered = conditionWasHovered;
	}

	public boolean getConditionWasHovered() {
		return conditionWasHovered;
	}
}
