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
import org.vectomatic.dom.svg.OMSVGTSpanElement;
import org.vectomatic.dom.svg.OMSVGTextElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.ProcessDesigner;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.gwt.processdesigner.client.resources.ProcessDesignerResources;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processdefrep.NodeRepresentation;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;

public class NodeSvgElement extends AbstractProcessSvgElement<NodeRepresentation>{

	private boolean adaptFontSize = false;
	private double initialFontSize = 12;
	private OMSVGGElement nodeGroup;	
	private OMSVGTextElement statusTextWrapper;
	OMSVGTSpanElement status;
	OMSVGTSpanElement name;
	private OMSVGTextElement gracePeriodTimeText;
//	private OMSVGPolygonElement icon;
	private OMSVGImageElement icon;
	private OMSVGCircleElement node;
	private OMSVGCircleElement hoverNode;
	
	private OMSVGImageElement restartIcon;
	private OMSVGImageElement endIcon;
	
	private DecoupledInteractionElement decoupledInteractionElement;
	
	//private ProcessDesignerRenderer renderer;
	private ProcessDesigner designer;
	
	public NodeSvgElement(NodeRepresentation genericEntity) {
		super(genericEntity);	
	}
	
	@Override
	public void initialize() {		
	
		if(nodeGroup == null){
			nodeGroup = new OMSVGGElement();
			nodeGroup.setAttribute("opacity", "0");
		}		
		
		if(node == null){
			node = new OMSVGCircleElement(0, 0, ProcessElementStylingUtil.getFloat(pdc.getProcessNodeRadius()));
		}
		else
			node.setAttribute("r", getString(pdc.getProcessNodeRadius()));
		
		node.setAttribute("style", ProcessElementStylingUtil.getInstance().getProcessNodeElementStyling().defaultStyle);
		
		if(hoverNode == null){
			hoverNode = new OMSVGCircleElement(0, 0, ProcessElementStylingUtil.getFloat(pdc.getProcessNodeRadius()));
			getHandlers().add(hoverNode.addMouseMoveHandler(this));
			getHandlers().add(hoverNode.addMouseDownHandler(this));
			getHandlers().add(hoverNode.addMouseUpHandler(this));
			getHandlers().add(hoverNode.addMouseOverHandler(this));
			getHandlers().add(hoverNode.addMouseOutHandler(this));
			getHandlers().add(hoverNode.addClickHandler(this));
		}
		else
			hoverNode.setAttribute("r", getString(pdc.getProcessNodeRadius()));
		
		hoverNode.setAttribute("style", "fill:white;stroke:none;opacity:0");
		
		NodeRepresentation nodeRepresentation = genericEntity;
		
		Node node2 = nodeRepresentation.getNode();
		
		boolean isDrainNode = false;
		boolean hasGracePeriod = false;
		if(node2 != null){
			if(node2.getState() != null){
				if(statusTextWrapper == null){
					statusTextWrapper = OMSVGParser.currentDocument().createSVGTextElement();
					statusTextWrapper.setAttribute("text-anchor","middle");
					statusTextWrapper.setAttribute("dominant-baseline","middle");
					statusTextWrapper.setAttribute("font-family", "Open Sans");
					statusTextWrapper.setAttribute("font-weight", "bold");
					statusTextWrapper.setAttribute("opacity", "1.0");
					statusTextWrapper.setAttribute("fill", "black");
					
					name = OMSVGParser.currentDocument().createSVGTSpanElement();
					status = OMSVGParser.currentDocument().createSVGTSpanElement();
					status.setAttribute("dy", "15");
					status.setAttribute("fill", "grey");
					
					statusTextWrapper.appendChild(name);
					statusTextWrapper.appendChild(status);
				}				
								
				
			}else{
				adaptIcon();
			}
			
			if(node2 instanceof StandardNode){
				TimeSpan gracePeriod = ((StandardNode) node2).getGracePeriod();
				hasGracePeriod = gracePeriod != null;
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
				
				isDrainNode = designer.isDrainNode(node2);
				if(isDrainNode){
					if(endIcon == null){
						endIcon = new OMSVGImageElement(0, 0, 16, 16, ProcessDesignerResources.INSTANCE.end().getSafeUri().asString());
						endIcon.setAttribute("opacity", "0.2");
					}
				}
			}
			
			if(node2 instanceof RestartNode){
				if(restartIcon == null){
					restartIcon = new OMSVGImageElement(0, 0, 16, 16, ProcessDesignerResources.INSTANCE.restart().getSafeUri().asString());
					restartIcon.setAttribute("opacity", "0.2");
				}
			}			
		}else{
			adaptIcon();
		}	

		if(node.getParentNode() != nodeGroup)
			nodeGroup.appendChild(node);
		if(statusTextWrapper != null && statusTextWrapper.getParentNode() != nodeGroup)
			nodeGroup.appendChild(statusTextWrapper);
		
		if(hasGracePeriod){
			if(gracePeriodTimeText != null && gracePeriodTimeText.getParentNode() != nodeGroup)
				nodeGroup.appendChild(gracePeriodTimeText);
		}else{
			if(gracePeriodTimeText != null && gracePeriodTimeText.getParentNode() == nodeGroup)
				nodeGroup.removeChild(gracePeriodTimeText);
		}
		
		if(restartIcon != null && restartIcon.getParentNode() != nodeGroup)
			nodeGroup.appendChild(restartIcon);
		
		if(isDrainNode){
			if(endIcon != null && endIcon.getParentNode() != nodeGroup)
				nodeGroup.appendChild(endIcon);	
		}else{
			if(endIcon != null && endIcon.getParentNode() == nodeGroup)
				nodeGroup.removeChild(endIcon);
		}		
		
		if(node2 != null){
			if(node2.getState() == null){
				if(icon != null && icon.getParentNode() != nodeGroup)
					nodeGroup.appendChild(icon);
			}else{
				if(icon != null && icon.getParentNode() == nodeGroup)
					nodeGroup.removeChild(icon);
			}
		}
	
		if(hoverNode.getParentNode() != nodeGroup)
			nodeGroup.appendChild(hoverNode);
		
		setWidth(getContainerWith());
		setHeight(getContainerHeight());
		
		if(statusTextWrapper != null){
			if(adaptFontSize){
				double fontSize = initialFontSize;
				try{
					statusTextWrapper.setAttribute("font-size", getString(fontSize)+"px");		
					name.getElement().setInnerText(node2.getName() != null ? node2.getName().value() : "");
					status.getElement().setInnerText(node2.getState() != null ? "(" + node2.getState().toString()+ ")" : "");	
					
					float textWidth = statusTextWrapper.getBBox().getWidth();
					float containerWidth = node.getBBox().getWidth();
					
					while(textWidth > containerWidth){
						fontSize = fontSize * 0.9;
						statusTextWrapper.setAttribute("font-size", getString(fontSize)+"px");
						textWidth = statusTextWrapper.getBBox().getWidth();
					}
					
				}catch(Exception ex){
					statusTextWrapper.setAttribute("font-size", getString(pdc.getFontSize())+"px");	
					name.getElement().setInnerText(node2.getName() != null ? node2.getName().value() : "");
					status.getElement().setInnerText(node2.getState() != null ? "(" + node2.getState().toString()+ ")" : "");	
				}
				
			}else{
				statusTextWrapper.setAttribute("font-size", getString(pdc.getFontSize())+"px");	
				name.getElement().setInnerText(node2.getName() != null ? node2.getName().value() : "");
				status.getElement().setInnerText(node2.getState() != null ? "(" + node2.getState().toString()+ ")" : "");	
			}
		}
	}

	@Override
	public OMSVGGElement getRepresentation() {
		return nodeGroup;
	}
	
	public void setDecoupledInteractionElement(DecoupledInteractionElement decoupledInteractionElement) {
		this.decoupledInteractionElement = decoupledInteractionElement;
	}
	
	/*public void setRenderer(ProcessDesignerRenderer renderer) {
		this.renderer = renderer;
	}*/
	
	public void setDesigner(ProcessDesigner designer) {
		this.designer = designer;
		
		getSession().listeners().entity(designer.getProcessDesignerConfiguration()).add(this);
	}

	@Override
	public void setX(double x) {		
		nodeGroup.setAttribute("x", x+"");
		
		x += (ProcessElementStylingUtil.getFloat(pdc.getDockingPointRadius()));
		x += (ProcessElementStylingUtil.getFloat(pdc.getDockingPointRadius()) + ProcessElementStylingUtil.getFloat(pdc.getProcessNodeRadius()));
		
		node.setAttribute("cx", x+"");
		
		if(restartIcon != null)
			restartIcon.setAttribute("x", (x-8)+"");
		
		if(endIcon != null)
			endIcon.setAttribute("x", (x-8)+"");
		
		hoverNode.setAttribute("cx", x+"");
		if(statusTextWrapper != null)
			statusTextWrapper.setAttribute("x", x+"");
		if(status != null)
			status.setAttribute("x", x+"");
		if(name != null)
			name.setAttribute("x", x+"");
		if(icon != null)
			adaptIcon();
		
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
		if(statusTextWrapper != null)
			statusTextWrapper.setAttribute("y", y+(getHeight()/2)+"");
		if(status != null)
			status.setAttribute("y", y+(getHeight()/2)+"");
		if(name != null)
			name.setAttribute("y", y+(getHeight()/2)+"");
		if(icon != null)
			adaptIcon();
		
		if(restartIcon != null)
			restartIcon.setAttribute("y", y+(getHeight()*0.3)-8+"");
		
		if(endIcon != null)
			endIcon.setAttribute("y", y+(getHeight()*0.3)-8+"");
		
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
//		GWT.debugger();
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
		((NodeRepresentation)getEntity()).setX(getX());
		((NodeRepresentation)getEntity()).setY(getY());
		nestedTransaction.commit();
	}
	
	private void adaptIcon(){
		if(icon == null){
			icon = new OMSVGImageElement(0, 0, 32, 32, ProcessDesignerResources.INSTANCE.init().getSafeUri().asString());
			icon.setAttribute("style", "fill:grey; stroke:silver; stroke-width:0; opacity:0.5");
		}
		
		/*String points = "";
		double angle = Math.PI * 2 / 8;
		
		for (int i = 1; i <= 8; i++) {
			if(i % 2 == 0){
				double a1 = angle * i;
				double a2 = a1 + angle;
	
				double ny = Math.sin(a2) * (pdc.processNodeRadius / 3) + getX2();
				double nx = Math.cos(a2) * (pdc.processNodeRadius / 3) + getY2();
				ny -= pdc.processNodeRadius / 8;
	
				points += ny + "," + nx + " ";
			}
			if(i == 1){
				double a1 = angle * i;
				double a2 = a1 + angle;
	
				double ny = Math.sin(a2) * (pdc.processNodeRadius / 1.5) + getX2();
				double nx = Math.cos(a2) * (pdc.processNodeRadius / 1.5) + getY2();
				ny -= pdc.processNodeRadius / 8;
				
				points += ny + "," + nx + " ";
			}
		}
		
		icon.setAttribute("points", points); */
		icon.setAttribute("x", getCenterX() - 16 + "");
		icon.setAttribute("y", getCenterY() - 16 + "");
	}
	
	@Override
	public String getDescription() {
		NodeRepresentation nodeRepresentation = (NodeRepresentation)getEntity();
		Node node = nodeRepresentation.getNode();
		return node.getDescription();
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
		NodeRepresentation nodeRepresentation = genericEntity;
		
		Node node2 = nodeRepresentation.getNode();
		if(node2 instanceof StandardNode)
			return ElementKind.standardNode;
		else if(node2 instanceof RestartNode)
			return ElementKind.restartNode;
		
		return null;
	}
	
	@Override
	public boolean canBeConnected() {
		return true;
	}	
}
