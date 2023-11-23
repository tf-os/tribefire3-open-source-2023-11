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
import org.vectomatic.dom.svg.OMSVGRect;
import org.vectomatic.dom.svg.OMSVGTextElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.gwt.processdesigner.client.resources.ProcessDesignerResources;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processdefrep.ProcessDefinitionRepresentation;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

import tribefire.extension.process.model.deployment.ProcessDefinition;

public class ProcessDefinitionSvgElement extends AbstractProcessSvgElement<ProcessDefinitionRepresentation>{

	private OMSVGGElement nodeGroup;	
	private OMSVGImageElement icon;
	private OMSVGCircleElement node;
	private OMSVGCircleElement hoverNode;
	private OMSVGTextElement gracePeriodTimeText;
	
	private DecoupledInteractionElement decoupledInteractionElement;
	
	public ProcessDefinitionSvgElement(ProcessDefinitionRepresentation genericEntity) {
		super(genericEntity);		
	}		
	
	@Override
	public void initialize() {		
	
		if(nodeGroup == null){
			nodeGroup = new OMSVGGElement();
			nodeGroup.setAttribute("opacity", "1");
		}		
		
		if(node == null){
			node = new OMSVGCircleElement(0, 0, ProcessElementStylingUtil.getFloat(pdc.getProcessNodeRadius()));
			node.setAttribute("style", ProcessElementStylingUtil.getInstance().getProcessNodeElementStyling().defaultStyle);
		}
		else
			node.setAttribute("r", getString(pdc.getProcessNodeRadius()));	
		
		if(hoverNode == null){
			hoverNode = new OMSVGCircleElement(0, 0, ProcessElementStylingUtil.getFloat(pdc.getProcessNodeRadius()));
			getHandlers().add(hoverNode.addMouseMoveHandler(this));
			getHandlers().add(hoverNode.addMouseDownHandler(this));
			getHandlers().add(hoverNode.addMouseUpHandler(this));
			getHandlers().add(hoverNode.addMouseOverHandler(this));
			getHandlers().add(hoverNode.addMouseOutHandler(this));
			getHandlers().add(hoverNode.addClickHandler(this));
			hoverNode.setAttribute("style", "fill:white;stroke:none;opacity:0");
		}
		else
			hoverNode.setAttribute("r", getString(pdc.getProcessNodeRadius()));		
		
		if(icon == null){
			icon = new OMSVGImageElement(0, 0, 32, 32, ProcessDesignerResources.INSTANCE.homeBig().getSafeUri().asString());
			icon.setAttribute("opacity", "0.5");
		}
		
		ProcessDefinitionRepresentation pdr = (ProcessDefinitionRepresentation)getEntity();
		ProcessDefinition pd = pdr.getProcessDefinition();
		
		TimeSpan gracePeriod = pd.getGracePeriod();
		if(gracePeriod != null /* && showGracePeriod */){
			if(gracePeriodTimeText == null){
				gracePeriodTimeText = OMSVGParser.currentDocument().createSVGTextElement();
				gracePeriodTimeText.setAttribute("text-anchor","middle");
				gracePeriodTimeText.setAttribute("dominant-baseline","middle");
				gracePeriodTimeText.setAttribute("font-family", "Open Sans");
				gracePeriodTimeText.setAttribute("font-weight", "bold");
				gracePeriodTimeText.setAttribute("opacity", "1.0");
				gracePeriodTimeText.setAttribute("fill", "silver");
			}
			
			gracePeriodTimeText.setAttribute("font-size", getString(pdc.getFontSize()*0.9)+"px");		
			gracePeriodTimeText.getElement().setInnerText(gracePeriod.getValue() + getTimeUnit(gracePeriod.getUnit()));
		}
		
		if(node.getParentNode() != nodeGroup)
			nodeGroup.appendChild(node);
		if(icon != null && icon.getParentNode() != nodeGroup)
			nodeGroup.appendChild(icon);
		if(gracePeriod != null){
			if(gracePeriodTimeText != null && gracePeriodTimeText.getParentNode() != nodeGroup)
				nodeGroup.appendChild(gracePeriodTimeText);
		}else{
			if(gracePeriodTimeText != null && gracePeriodTimeText.getParentNode() == nodeGroup)
				nodeGroup.removeChild(gracePeriodTimeText);
		}
		if(hoverNode.getParentNode() != nodeGroup)
			nodeGroup.appendChild(hoverNode);
		
		setWidth(getContainerWith());
		setHeight(getContainerHeight());		
	}

	@Override
	public OMSVGGElement getRepresentation() {
		return nodeGroup;
	}
	
	public void setDecoupledInteractionElement(DecoupledInteractionElement decoupledInteractionElement) {
		this.decoupledInteractionElement = decoupledInteractionElement;
	}

	@Override
	public void setX(double x) {		
		nodeGroup.setAttribute("x", x+"");

		x += (ProcessElementStylingUtil.getFloat(pdc.getDockingPointRadius()));
		x += (ProcessElementStylingUtil.getFloat(pdc.getDockingPointRadius()) + ProcessElementStylingUtil.getFloat(pdc.getProcessNodeRadius()));
		
		node.setAttribute("cx", x+"");
		hoverNode.setAttribute("cx", x+"");

		if(icon != null)
			icon.setAttribute("x",x-16+"");		
		
		if(gracePeriodTimeText != null)
			gracePeriodTimeText.setAttribute("x", x+"");

		x += (ProcessElementStylingUtil.getFloat(pdc.getProcessNodeRadius()) + ProcessElementStylingUtil.getFloat(pdc.getDockingPointRadius()));
		
		if(decoupledInteractionElement != null){
			decoupledInteractionElement.setX(getX() + getWidth());
		}
		
		
	}

	@Override
	public double getX() {
		try{
			return Double.parseDouble(nodeGroup.getAttribute("x"));
		}catch(Exception ex){
			return 0;
		}		
	}

	@Override
	public void setY(double y) {
		
		nodeGroup.setAttribute("y", y+"");
		node.setAttribute("cy", y+(getHeight()/2)+"");
		hoverNode.setAttribute("cy", y+(getHeight()/2)+"");

		if(icon != null)
			icon.setAttribute("y", (y+(getHeight()/2))-16+"");
		
		if(gracePeriodTimeText != null)
			gracePeriodTimeText.setAttribute("y", y+(getHeight()*0.8)+"");
		
		if(decoupledInteractionElement != null){
			decoupledInteractionElement.setY(getY());
		}
	}

	@Override
	public double getY() {
		try{
			return Double.parseDouble(nodeGroup.getAttribute("y"));
		}catch(Exception ex){
			return 0;
		}	
	}

	@Override
	public void setWidth(double width) {
		getRepresentation().setAttribute("width", width+"");
	}

	@Override
	public double getWidth() {
		try{
			return Double.parseDouble(getRepresentation().getAttribute("width"));
		}catch(Exception ex){
			return 0;
		}	
	}

	@Override
	public void setHeight(double height) {
		getRepresentation().setAttribute("height", height+"");
	}

	@Override
	public double getHeight() {
		try{
			return Double.parseDouble(getRepresentation().getAttribute("height"));
		}catch(Exception ex){
			return 0;
		}	
	}

	@Override
	public void handleSelectionChange() {
		if(selected){
			node.setAttribute("style", ProcessElementStylingUtil.getInstance().getProcessNodeElementStyling().selectedStyle);
		}else{
			node.setAttribute("style", ProcessElementStylingUtil.getInstance().getProcessNodeElementStyling().defaultStyle);
		}
	}

	@Override
	public void handleHoveringChange() {
		if(!selected){
			if(hovered){
				node.setAttribute("style", ProcessElementStylingUtil.getInstance().getProcessNodeElementStyling().hoveredStyle);
			}else{
				node.setAttribute("style", ProcessElementStylingUtil.getInstance().getProcessNodeElementStyling().defaultStyle);
			}
		}
	}
	
	@Override
	public void handleActiveChange() {
		//NOP
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//NOP
	}
	
	private double getContainerWith(){
		return ProcessElementStylingUtil.getFloat(pdc.getProcessNodeRadius()*2) + ProcessElementStylingUtil.getFloat(pdc.getDockingPointRadius()*4);
	}
	
	private double getContainerHeight(){
		return ProcessElementStylingUtil.getFloat(pdc.getProcessNodeRadius() * 2);
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		parentEventHandler.delegateOnMouseDown(event);
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		//NOP
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		//NOP
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		parentEventHandler.hoverElement(this, true, event);
	}

	@Override
	public void onClick(ClickEvent event) {
		//NOP
	}
	
	@Override
	public boolean doesIntersect(Rect mouseRect) {
		return getRect(node.getBBox()).intersect(mouseRect) != null;
	}
	
	private Rect getRect(OMSVGRect omsvgRect){
		return new Rect(omsvgRect.getX(), omsvgRect.getY(), omsvgRect.getWidth(), omsvgRect.getHeight());
	}

	@Override
	public void setX2(double x2) {
		setX(x2 - pdc.getProcessNodeRadius());
	}

	@Override
	public double getX2() {
		try{
			return Double.parseDouble(node.getAttribute("cx"));
		}catch(Exception ex){
			return 0;
		}
	}

	@Override
	public void setY2(double y2) {
		setY(y2 - pdc.getProcessNodeRadius());
	}

	@Override
	public double getY2() {
		try{
			return Double.parseDouble(node.getAttribute("cy"));
		}catch(Exception ex){
			return 0;
		}
	}	

	private String getString(double d){
		return d + "";
	}
	
	@Override
	public void commit(){
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		((ProcessDefinitionRepresentation)getEntity()).setX(getX());
		((ProcessDefinitionRepresentation)getEntity()).setY(getY());
		nestedTransaction.commit();
	}
	
	@Override
	public String getDescription() {
		ProcessDefinitionRepresentation processDefinitionRepresentation = (ProcessDefinitionRepresentation)getEntity();
		ProcessDefinition pd = processDefinitionRepresentation.getProcessDefinition();
		return pd.getDescription();
	}

	@Override
	public void setCenterX(double centerX) {
		//NOP
	}

	@Override
	public double getCenterX() {
		try{
			return Double.parseDouble(node.getAttribute("cx"));
		}catch(Exception ex){
			return 0;
		}
	}

	@Override
	public void setCenterY(double centerY) {
		//NOP
	}

	@Override
	public double getCenterY() {
		try{
			return Double.parseDouble(node.getAttribute("cy"));
		}catch(Exception ex){
			return 0;
		}
	}
	
	@Override
	public ElementKind getElementKind() {
		return ElementKind.processDef;
	}
	
	@Override
	public boolean canBeConnected() {
		return true;
	}
	
	private String getTimeUnit(TimeUnit timeUnit){
		if(timeUnit != null){
			switch (timeUnit) {
			case day:
				return "day";
			case hour:
				return "hr";
			case microSecond:
				return "μs";
			case milliSecond:
				return "ms";
			case minute:
				return "min";
			case nanoSecond:
				return "ns";
			case planckTime:
				return "pt";
			case second:
				return "sec";
			default:
				return "sec";
			}
		}else
			return "";
	}

}
