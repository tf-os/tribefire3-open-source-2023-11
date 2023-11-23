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

import org.vectomatic.dom.svg.OMSVGCircleElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGImageElement;

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.gwt.processdesigner.client.resources.ProcessDesignerResources;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

import tribefire.extension.process.model.deployment.DecoupledInteraction;

public class DecoupledInteractionElement extends AbstractProcessSvgElement<DecoupledInteraction>{
	
	//private static final String DEFAULT_EDGE_HOVERING_COLOR = "#d2e9ff";
	private static final String DEFAULT_EDGE_SELECTION_COLOR = "#d2e9ff";
	
	OMSVGGElement group;
	
	OMSVGCircleElement cogWheelCircle;
	OMSVGImageElement cogWheelIcon;
	
	OMSVGCircleElement userCircle;
	OMSVGImageElement userIcon;
	
	OMSVGCircleElement smallUndefinedCircle;
	OMSVGImageElement undefinedIcon;
	
	private int width = 20;
	private int iconWidth = 16;
	private int iconWidthBig = 32;
	
	private double groupOpacity = 1;
	private double elementOpacity = 1;
	private double iconFactor = 0.5;

	public DecoupledInteractionElement(DecoupledInteraction genericEntity) {
		super(genericEntity);
	}

	@Override
	public OMSVGGElement getRepresentation() {
		return group;
	}

	@Override
	public void setX(double x) {
		cogWheelCircle.setAttribute("cx", x+"");
		cogWheelIcon.setAttribute("x", x-(iconWidthBig/2)+"");
		
		userCircle.setAttribute("cx", x+"");
		userIcon.setAttribute("x", x-(iconWidth/2)+"");
		
		smallUndefinedCircle.setAttribute("cx", x+"");
		undefinedIcon.setAttribute("x", x-(iconWidth/2)+"");
		
		group.setAttribute("x", (x)+"");
	}

	@Override
	public double getX() {
		return 0;
	}

	@Override
	public void setY(double y) {
		cogWheelCircle.setAttribute("cy", y+"");
		cogWheelIcon.setAttribute("y", y-(iconWidthBig/2)+"");
		userCircle.setAttribute("cy", y+"");
		userIcon.setAttribute("y", y-(iconWidth/2)+"");
		smallUndefinedCircle.setAttribute("cy", y+"");
		undefinedIcon.setAttribute("y", y-(iconWidth/2)+"");
		group.setAttribute("y", (y)+"");
	}

	@Override
	public double getY() {
		return 0;
	}

	@Override
	public void setX2(double x2) {
		//NOP
	}

	@Override
	public double getX2() {
		return 0;
	}

	@Override
	public void setY2(double y2) {
		//NOP
	}

	@Override
	public double getY2() {
		return 0;
	}

	@Override
	public void setWidth(double width) {
		//NOP
	}

	@Override
	public double getWidth() {
		return 0;
	}

	@Override
	public void setHeight(double height) {
		//NOP
	}

	@Override
	public double getHeight() {
		return 0;
	}

	@Override
	public void handleSelectionChange() {
		if(selected){
			group.setAttribute("fill", DEFAULT_EDGE_SELECTION_COLOR);
		}else{
			group.setAttribute("fill", "white");
		}
	}

	@Override
	public void handleHoveringChange() {
		if(!selected){
			if(hovered){				
				group.setAttribute("fill", DEFAULT_EDGE_SELECTION_COLOR);
			}else{
				group.setAttribute("fill", "white");
			}
		}
	}

	@Override
	public void handleActiveChange() {
		//NOP
	}

	@Override
	public void initialize() {
		if(group == null){
			group = new OMSVGGElement();
			group.setAttribute("opacity", groupOpacity+"");
			group.setAttribute("cursor", "pointer");
			group.setAttribute("stroke", "white");
			group.setAttribute("stroke-width", "0");
			group.setAttribute("fill", "white");
			getHandlers().add(group.addMouseMoveHandler(this));
			getHandlers().add(group.addMouseDownHandler(this));
			getHandlers().add(group.addMouseUpHandler(this));
			getHandlers().add(group.addMouseOverHandler(this));
			getHandlers().add(group.addMouseOutHandler(this));
			getHandlers().add(group.addClickHandler(this));
		}
		
		if(userIcon == null){
			userIcon = new OMSVGImageElement(0, 0, iconWidth, iconWidth, ProcessDesignerResources.INSTANCE.user().getSafeUri().asString());
			userIcon.setAttribute("opacity", elementOpacity*iconFactor+"");
		}
		
		if(userCircle == null){
			userCircle = new OMSVGCircleElement(0, 0, iconWidth / 2);
		}
		
		if(cogWheelIcon == null){
			cogWheelIcon = new OMSVGImageElement(0, 0, iconWidthBig, iconWidthBig, ProcessDesignerResources.INSTANCE.worker().getSafeUri().asString());
			cogWheelIcon.setAttribute("opacity", elementOpacity*iconFactor+"");
		}
		
		if(cogWheelCircle == null){
			cogWheelCircle = new OMSVGCircleElement(0, 0, iconWidthBig / 2);
		}
		
		if(undefinedIcon == null){
			undefinedIcon = new OMSVGImageElement(0, 0, iconWidth, iconWidth, ProcessDesignerResources.INSTANCE.undefined().getSafeUri().asString());
			undefinedIcon.setAttribute("opacity", elementOpacity*iconFactor+"");
		}
		
		if(smallUndefinedCircle == null){
			smallUndefinedCircle = new OMSVGCircleElement(0, 0, width / 2);
			smallUndefinedCircle.setAttribute("stroke", "silver");
			smallUndefinedCircle.setAttribute("stroke-dasharray", "5,5");
		}

		if(cogWheelCircle.getParentNode() != group)
			group.appendChild(cogWheelCircle);
		
		if(cogWheelIcon.getParentNode() != group)
			group.appendChild(cogWheelIcon);
		
		if(userCircle.getParentNode() != group)
			group.appendChild(userCircle);
		
		if(userIcon.getParentNode() != group)
			group.appendChild(userIcon);
		
		if(smallUndefinedCircle.getParentNode() != group)
			group.appendChild(smallUndefinedCircle);
		if(undefinedIcon.getParentNode() != group)
			group.appendChild(undefinedIcon);
		
		group.setAttribute("height", width+"");
		group.setAttribute("width", width+"");
		
		DecoupledInteraction di = (DecoupledInteraction) getEntity();
		if(di != null){
			boolean workersSet = di.getWorkers() != null && di.getWorkers().size() > 0;
			if(workersSet){
				cogWheelCircle.setAttribute("opacity", elementOpacity+"");
				cogWheelIcon.setAttribute("opacity", elementOpacity*iconFactor+"");
				if(di.getWorkers().size() == 1)
					cogWheelIcon.getHref().setBaseVal(ProcessDesignerResources.INSTANCE.worker().getSafeUri().asString());
				else if(di.getWorkers().size() > 1)
					cogWheelIcon.getHref().setBaseVal(ProcessDesignerResources.INSTANCE.workers().getSafeUri().asString());
			}else{
				cogWheelCircle.setAttribute("opacity", "0");
				cogWheelIcon.setAttribute("opacity", "0");
			}
			
			userCircle.setAttribute("opacity", (di.getUserInteraction() ? elementOpacity : 0)+"");
			userIcon.setAttribute("opacity", (di.getUserInteraction() ? elementOpacity*iconFactor : 0)+"");	
			
			undefinedIcon.setAttribute("opacity", (workersSet || di.getUserInteraction() ? 0 : elementOpacity*iconFactor) + "");
			smallUndefinedCircle.setAttribute("opacity", (workersSet || di.getUserInteraction() ? 0 : elementOpacity) + "");
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
	public String getDescription() {
		DecoupledInteraction di = (DecoupledInteraction)getEntity();
		if(di != null)
			return di.getDescription();
		else
			return "define a decoupled interaction";
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//NOP
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
		parentEventHandler.delegateOnMouseMove(event);
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		parentEventHandler.hoverElement(this, true, event);
	}

	@Override
	public void onClick(ClickEvent event) {
		if(!getSelected())
			parentEventHandler.selectElement(this, false, true);
		else
			parentEventHandler.deselectElement(this, true);
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
