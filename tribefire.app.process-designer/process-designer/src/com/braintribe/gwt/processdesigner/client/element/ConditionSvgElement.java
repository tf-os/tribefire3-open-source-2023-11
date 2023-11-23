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

import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGTextElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerRenderer;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.gwt.processdesigner.client.vector.Complex;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processdefrep.EdgeRepresentation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.StandardNode;

public class ConditionSvgElement extends AbstractProcessSvgElement<ConditionProcessor>{
	
	private static final String DEFAULT_EDGE_HOVERING_COLOR = "#d2e9ff";
	private static final String DEFAULT_EDGE_SELECTION_COLOR = "#d2e9ff";
	//private static final int MAX_CONDITION_NAME_LENGTH = 250;
	
	OMSVGGElement conditionGroup;
	OMSVGRectElement conditionNameBox;
	OMSVGTextElement conditionName;
	EdgeSvgElement parentEdgeSvgElement;
	ProcessDesignerRenderer renderer;
	
	private boolean conditionWasHovered;
	private ConditionElementHandler conditionElementHandler = new ConditionElementHandler();
	
	public void setParentEdgeSvgElement(EdgeSvgElement parentEdgeSvgElement) {
		this.parentEdgeSvgElement = parentEdgeSvgElement;
	}
	
	public EdgeSvgElement getParentEdgeSvgElement() {
		return parentEdgeSvgElement;
	}
	
	public void setRenderer(ProcessDesignerRenderer renderer) {
		this.renderer = renderer;
	}
	
	public Edge getEdge(){
		if(parentEdgeSvgElement != null){
			EdgeRepresentation edgeRepresentation = (EdgeRepresentation) parentEdgeSvgElement.getEntity();
			if(edgeRepresentation != null)
				return edgeRepresentation.getEdge();
		}
		return null;
	}
	
	public ConditionSvgElement(ConditionProcessor genericEntity) {
		super(genericEntity);
	}

	@Override
	public OMSVGGElement getRepresentation() {
		return conditionGroup;
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
	public void setWidth(double width) {
		adaptGroup();	
	}

	@Override
	public double getWidth() {
		return 0;
	}

	@Override
	public void setHeight(double height) {
		adaptGroup();
	}

	@Override
	public double getHeight() {
		return 0;
	}

	@Override
	public void handleSelectionChange() {
		if(selected){
			conditionNameBox.setAttribute("stroke", DEFAULT_EDGE_SELECTION_COLOR);
			conditionNameBox.setAttribute("stroke-width", "4");
			conditionNameBox.setAttribute("fill", DEFAULT_EDGE_SELECTION_COLOR);
		}else{
			conditionNameBox.setAttribute("stroke", "grey");
			conditionNameBox.setAttribute("stroke-width", "2");
			conditionNameBox.setAttribute("fill", "white");
		}
	}

	@Override
	public void handleHoveringChange() {
		if(!selected){
			if(hovered){
				conditionNameBox.setAttribute("stroke", DEFAULT_EDGE_HOVERING_COLOR);
				conditionNameBox.setAttribute("stroke-width", "4");
				conditionNameBox.setAttribute("fill", DEFAULT_EDGE_HOVERING_COLOR);
			}else{
				conditionNameBox.setAttribute("stroke", "grey");
				conditionNameBox.setAttribute("stroke-width", "2");
				conditionNameBox.setAttribute("fill", "white");
			}
		}
	}

	@Override
	public void handleActiveChange() {
		//NOP
	}

	@Override
	public void initialize() {
		if(conditionGroup == null)
			conditionGroup = new OMSVGGElement();
		
		if(conditionName == null){
			conditionName = OMSVGParser.currentDocument().createSVGTextElement();
			conditionName.setAttribute("text-anchor","middle");
			conditionName.setAttribute("dominant-baseline","middle");
			conditionName.setAttribute("font-family", "Open Sans");
			conditionName.setAttribute("font-size", "12px");
			conditionName.setAttribute("font-weight", "bold");			
			conditionName.setAttribute("cursor", "pointer");
			
			conditionNameBox = new OMSVGRectElement();
			conditionNameBox.setAttribute("rx", "5");
			conditionNameBox.setAttribute("ry", "5");
			conditionNameBox.setAttribute("cursor", "pointer");
			
			getHandlers().add(conditionNameBox.addMouseMoveHandler(this));
			getHandlers().add(conditionNameBox.addMouseDownHandler(this));
			getHandlers().add(conditionNameBox.addMouseUpHandler(this));
			getHandlers().add(conditionNameBox.addMouseOverHandler(this));
			getHandlers().add(conditionNameBox.addMouseOutHandler(this));
			getHandlers().add(conditionNameBox.addClickHandler(this));
			
			getHandlers().add(conditionName.addMouseMoveHandler(this));
			getHandlers().add(conditionName.addMouseDownHandler(this));
			getHandlers().add(conditionName.addMouseUpHandler(this));
			getHandlers().add(conditionName.addMouseOverHandler(this));
			getHandlers().add(conditionName.addMouseOutHandler(this));
			getHandlers().add(conditionName.addClickHandler(this));
			
			getHandlers().add(conditionNameBox.addMouseOverHandler(conditionElementHandler));
			getHandlers().add(conditionNameBox.addMouseOutHandler(conditionElementHandler));
			getHandlers().add(conditionName.addMouseOverHandler(conditionElementHandler));
			getHandlers().add(conditionName.addMouseOutHandler(conditionElementHandler));
		}
		
		conditionName.setAttribute("opacity", "1");
		conditionName.setAttribute("stroke", "grey");
		conditionName.setAttribute("stroke-width", "0");
		
		conditionNameBox.setAttribute("fill", "white");
		conditionNameBox.setAttribute("stroke", "grey");
		conditionNameBox.setAttribute("stroke-width", "2");
		
		if(conditionNameBox.getParentNode() != conditionGroup)
			conditionGroup.appendChild(conditionNameBox);
		if(conditionName.getParentNode() != conditionGroup)
			conditionGroup.appendChild(conditionName);
		
		adaptGroup();
	}
	
	public void adaptGroup(){
		
		Complex start = new Complex(parentEdgeSvgElement.getX(), parentEdgeSvgElement.getY());
		Complex end = new Complex(parentEdgeSvgElement.getX2(), parentEdgeSvgElement.getY2());
		
		Complex centre = new Complex(parentEdgeSvgElement.getCenterX(), parentEdgeSvgElement.getCenterY());
		Complex toDirection = end.minus(start);
		//Complex perpToDir = new Complex(toDirection.y, -toDirection.x);
		Complex fromDirection = end.minus(start);
		Complex perpFromDir = new Complex(fromDirection.y, -fromDirection.x);
		
		EdgeRepresentation edgeRepresentation = (EdgeRepresentation) parentEdgeSvgElement.getEntity();
		Edge edge = edgeRepresentation.getEdge();
		if(edge instanceof ConditionalEdge){
			//offset centre if more edges	
			/*
			List<ConditionalEdge> conditionalEdges = renderer.getConditionalEdges((ConditionalEdge) edge);
			if(conditionalEdges.size() > 1){
				int count = conditionalEdges.size();
				ConditionalEdge conditionalEdge = (ConditionalEdge)edge;
				boolean useOffset = count % 2 == 0;
				if(useOffset){
					centre = centre.minus(perpFromDir.normalize().times(pdc.getProcessNodeRadius() / 2));
				}
				
				int index = conditionalEdges.indexOf(conditionalEdge);
				int half = count / 2;
				
				if(index < half){
					centre = centre.plus(perpFromDir.normalize().times((pdc.getProcessNodeRadius()) * (half - index)));
				}
//				else if(index == half){
//					centre = centre;
//				}
				else if(index > half){
					centre = centre.minus(perpFromDir.normalize().times((pdc.getProcessNodeRadius()) * (index - half)));
				}
			}
			*/
			ConditionalEdge conditionalEdge = (ConditionalEdge) edge;
			StandardNode fromNode = conditionalEdge.getFrom();
			Complex rotation = start.minus(end);
			int degree = (int) (Math.atan2(rotation.y, rotation.x) * (180 / Math.PI));
			
			if(degree >= 90 && degree <= 180){
				degree -= 180;
			}else if(degree <= -90 && degree >= -180){
				degree += 180;
			}
			
			ConditionProcessor condition = conditionalEdge.getCondition();
			String name = (fromNode.getConditionalEdges() != null && fromNode.getConditionalEdges().size() > 1) ? 
					"ELSE" : 
						"TRUE";
			
			if(fromNode.getConditionalEdges().size() == 1 && conditionalEdge.getDescription() != null)
				name = conditionalEdge.getDescription();
			
			
			if(conditionalEdge.getName() != null) {
				name = edge.getName().value();
			}else {
				if(condition != null){
					//name = condition.getDescription() != null ? I18nTools.getDefault(condition.getDescription(), "") : null;
					//if(name == null || name.isEmpty())
						name = condition.getName() != null ? condition.getName() : null;
					if(name == null || name.isEmpty())
						name = condition.getExternalId();
				}
			}
			
			if(conditionName.getParentNode() == conditionGroup)
				conditionGroup.removeChild(conditionName);
			
			if(fromNode.getConditionalEdges() != null && fromNode.getConditionalEdges().size() > 1){
				int index = -1;
				try{
					index = fromNode.getConditionalEdges().indexOf(edge);
				}catch(Exception ex){
					index = -1;
				}finally{
					if(index != -1)
						name = (condition != null ? (index + 1) + ": " : "") + name;
				}
			}
			
			name = truncateText(parentEdgeSvgElement.getRepresentation(), conditionName, name, (int) (toDirection.abs() - pdc.getProcessNodeRadius() * 2));
			
			conditionName.getElement().setInnerText(name+"");
			conditionName.setAttribute("transform", "rotate(" + degree + "," + centre.x + "," + centre.y + ")");
			conditionName.setAttribute("x", centre.x + "");
			conditionName.setAttribute("y", centre.y + "");		
			
			conditionGroup.appendChild(conditionName);
			int x = 0,y = 0;
			try{
//				System.err.println("degree " + degree + " " + fromDirection.normalize().x);
				if(degree >= 0 && degree <= 90 && fromDirection.normalize().x > 0){ // 0-90
//					System.err.println("minus plus");
					x = (int) new Complex(conditionName.getBBox().getX(), conditionName.getBBox().getY()).minus(fromDirection.normalize().times(5)).x;
					y = (int) new Complex(conditionName.getBBox().getX(), conditionName.getBBox().getY()).plus(perpFromDir.normalize().times(2)).y;
				}else if(degree < 0 && degree >= -90 && fromDirection.normalize().x < 0){
//					System.err.println("plus minus");
					x = (int) new Complex(conditionName.getBBox().getX(), conditionName.getBBox().getY()).plus(fromDirection.normalize().times(5)).x;
					y = (int) new Complex(conditionName.getBBox().getX(), conditionName.getBBox().getY()).minus(perpFromDir.normalize().times(2)).y;
				}else if(degree < 0 && degree >= -90 && fromDirection.normalize().x > 0){
//					System.err.println("plus minus");
					x = (int) new Complex(conditionName.getBBox().getX(), conditionName.getBBox().getY()).minus(fromDirection.normalize().times(5)).x;
					y = (int) new Complex(conditionName.getBBox().getX(), conditionName.getBBox().getY()).plus(perpFromDir.normalize().times(2)).y;
				}else if(degree >= 0 && degree <= 90 && fromDirection.normalize().x < 0){
//					System.err.println("plus minus");
					x = (int) new Complex(conditionName.getBBox().getX(), conditionName.getBBox().getY()).plus(fromDirection.normalize().times(5)).x;
					y = (int) new Complex(conditionName.getBBox().getX(), conditionName.getBBox().getY()).minus(perpFromDir.normalize().times(2)).y;
				}
				
				float height = conditionName.getBBox().getHeight() + 5;
				float width = conditionName.getBBox().getWidth() + 10;
				
				conditionNameBox.setAttribute("x", x + "");
				conditionNameBox.setAttribute("y", y + "");
				conditionNameBox.setAttribute("width",  width + "");
				conditionNameBox.setAttribute("height", height + "");
				conditionNameBox.setAttribute("transform", "rotate(" + degree + "," + centre.x + "," + centre.y + ")");
			}catch(Exception ex){
				conditionNameBox.setAttribute("x", centre.x + "");
				conditionNameBox.setAttribute("y", centre.y + "");
				conditionNameBox.setAttribute("width", 16 + "");
				conditionNameBox.setAttribute("height", 16 + "");
				conditionNameBox.setAttribute("transform", "rotate(" + degree + "," + centre.x + "," + centre.y + ")");
			}
			
			if(conditionNameBox.getParentNode() == conditionGroup)
				conditionGroup.removeChild(conditionNameBox);
			
			conditionGroup.appendChild(conditionNameBox);
			conditionGroup.appendChild(conditionName);
			
		}else{
			if(conditionNameBox.getParentNode() == conditionGroup)
				conditionGroup.removeChild(conditionNameBox);
			if(conditionName.getParentNode() == conditionGroup)
				conditionGroup.removeChild(conditionName);
		}
	}

	@Override
	public boolean doesIntersect(Rect mouseRect) {
		return false;
	}

	@Override
	public void commit() {
		//NOP
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		adaptGroup();
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		parentEventHandler.delegateOnMouseDown(event);
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		parentEventHandler.delegateOnMouseUp(event);
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		parentEventHandler.delegateOnMouseMove(event);
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		parentEventHandler.hoverElement(this, true, event);
	}

	@Override
	public void onClick(ClickEvent event) {
		//NOP
	}
	
	private String truncateText(OMSVGElement parentElement, OMSVGTextElement textElement, String initText, int maxWidth){
//		System.err.println("maxWidth " + maxWidth);
		if(maxWidth < 0)
			maxWidth = 10;
		try{
			String truncatedText = initText;
			String suffix = "";
			if(textElement.getParentNode() != parentElement)
				parentElement.appendChild(textElement);
			
			textElement.getElement().setInnerText(truncatedText);
			
			float width = textElement.getBBox().getWidth();
			while(width > maxWidth && truncatedText.length() > 3){
				truncatedText = truncatedText.substring(0, truncatedText.length() - 2);
				textElement.getElement().setInnerText(truncatedText);				
				width = textElement.getBBox().getWidth();
				suffix = "...";
			}
			parentElement.removeChild(textElement);
			return truncatedText + suffix;
		}catch(Exception ex){
			return textElement.getElement().getInnerText();
		}
	}
	
	public void setConditionWasHovered(boolean conditionWasHovered) {
		this.conditionWasHovered = conditionWasHovered;
	}
	
	public boolean getConditionWasHovered() {
		return conditionWasHovered;
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
	
	@Override
	public String getDescription() {
		//Condition condition = (Condition)getEntity();
		
		return /*condition.getDescription() != null ? I18nTools.getDefault(condition.getDescription(), "") :*/ null;
	}

	@Override
	public void setCenterX(double centerX) {
		//NOP
	}

	@Override
	public double getCenterX() {
		return 0;
	}

	@Override
	public void setCenterY(double centerY) {
		//NOP
	}

	@Override
	public double getCenterY() {
		return 0;
	}
	
	@Override
	public ElementKind getElementKind() {
		return null;
	}
	
	@Override
	public boolean canBeConnected() {
		return false;
	}

}
