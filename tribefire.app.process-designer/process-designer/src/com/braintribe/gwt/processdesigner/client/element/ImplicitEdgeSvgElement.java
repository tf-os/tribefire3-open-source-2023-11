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

import java.util.ArrayList;
import java.util.List;

import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGPolygonElement;

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerRenderer;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.gwt.processdesigner.client.vector.Complex;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processdefrep.EdgeRepresentation;
import com.braintribe.model.processdefrep.NodeRepresentation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

import tribefire.extension.process.model.deployment.ConditionalEdge;

public class ImplicitEdgeSvgElement extends AbstractProcessSvgElement<GenericEntity>{

	private ImplicitEdgeMode implicitEdgeMode;
	private GenericEntity childEntity;
	private boolean showArrow = true;
	
	OMSVGGElement edgeGroup;
	OMSVGPathElement hover;
	OMSVGPathElement edge;
	OMSVGPolygonElement arrow;
	
	ProcessDesignerRenderer renderer;
	
	ImplicitEdgeMode edgeMode;
	
	AbstractProcessSvgElement<?> toElement;
	AbstractProcessSvgElement<?> fromElement;
	
	double x1,x2,y1,y2;
	
	public void setToElement(AbstractProcessSvgElement<?> toElement) {
		this.toElement = toElement;
	}
	
	public void setFromElement(AbstractProcessSvgElement<?> fromElement) {
		this.fromElement = fromElement;
	}
	
	public void setEdgeMode(ImplicitEdgeMode edgeMode) {
		this.edgeMode = edgeMode;
	}
		
	public void setRenderer(ProcessDesignerRenderer renderer) {
		this.renderer = renderer;
	}
	
	public void setImplicitEdgeMode(ImplicitEdgeMode implicitEdgeMode) {
		this.implicitEdgeMode = implicitEdgeMode;
	}
	
	public ImplicitEdgeMode getImplicitEdgeMode() {
		return implicitEdgeMode;
	}
	
	public void setChildEntity(GenericEntity childEntity) {
		this.childEntity = childEntity;
	}
	
	public GenericEntity getChildEntity() {
		return childEntity;
	}

	public ImplicitEdgeSvgElement(GenericEntity genericEntity) {
		super(genericEntity);
	}

	@Override
	public OMSVGGElement getRepresentation() {
		return edgeGroup;
	}

	@Override
	public void setX(double x) {
		this.x1 = x;
		edge.setAttribute("d",linePath());
		hover.setAttribute("d",linePath());
		adaptGroup();
	}

	@Override
	public double getX() {
//		try{
//			return Double.parseDouble(edge.getAttribute("x1"));
//		}catch(Exception ex){
//			return 0;
//		}
		
		return x1;
	}

	@Override
	public void setY(double y) {
		this.y1 = y;
		edge.setAttribute("d",linePath());
		hover.setAttribute("d",linePath());
		adaptGroup();
	}

	@Override
	public double getY() {
////		try{
//			return Double.parseDouble(edge.getAttribute("y1"));
//		}catch(Exception ex){
//			return 0;
//		}
		return y1;
	}

	@Override
	public void setWidth(double width) {
		getRepresentation().setAttribute("width", width+"");
//		adaptGroup();
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
//		adaptGroup();
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
		ProcessElementStyling edgeStyle = ProcessElementStylingUtil.getInstance().getImplicitEdgeStyle(implicitEdgeMode);
		ProcessElementStyling arrowStyle = ProcessElementStylingUtil.getInstance().getImplicitArrowStyle(implicitEdgeMode);
		if(selected){
			edge.setAttribute("style", edgeStyle.defaultStyle);
			arrow.setAttribute("style", arrowStyle.defaultStyle);						
		}else{
			edge.setAttribute("style", edgeStyle.defaultStyle);
			arrow.setAttribute("style", arrowStyle.defaultStyle);
		}
	}
	
	@Override
	public void handleHoveringChange() {
		ProcessElementStyling edgeStyle = ProcessElementStylingUtil.getInstance().getImplicitEdgeStyle(implicitEdgeMode);
		ProcessElementStyling arrowStyle = ProcessElementStylingUtil.getInstance().getImplicitArrowStyle(implicitEdgeMode);
		if(!selected){
			if(hovered){
				edge.setAttribute("style", edgeStyle.defaultStyle);
				arrow.setAttribute("style", arrowStyle.defaultStyle);
			}else{
				edge.setAttribute("style", edgeStyle.defaultStyle);
				arrow.setAttribute("style", arrowStyle.defaultStyle);
			}
		}
		//if(hovered){
			
		//}else{
			//EdgeRepresentation edgeRepresentation = (EdgeRepresentation) getEntity();
			//Edge edge = edgeRepresentation.getEdge();
//			renderer.showTooltip(tooltip, point);
		//}
	}
	
	@Override
	public void handleActiveChange() {
		//NOP
	}

	@Override
	public void initialize() {
	
		ProcessElementStyling style = ProcessElementStylingUtil.getInstance().getImplicitEdgeStyle(implicitEdgeMode);
		ProcessElementStyling arrowStyle = ProcessElementStylingUtil.getInstance().getImplicitArrowStyle(implicitEdgeMode);
		if(edgeGroup == null){
			edgeGroup = new OMSVGGElement();
			edgeGroup.setAttribute("opacity", "0.3");
		}
		
		if(hover == null){
			hover = new OMSVGPathElement();
			hover.setAttribute("d",linePath());
			getHandlers().add(hover.addMouseMoveHandler(this));
			getHandlers().add(hover.addMouseDownHandler(this));
			getHandlers().add(hover.addMouseUpHandler(this));
			getHandlers().add(hover.addMouseOverHandler(this));
			getHandlers().add(hover.addMouseOutHandler(this));
			getHandlers().add(hover.addClickHandler(this));
			hover.setAttribute("cursor", "pointer");
		}
		
		hover.setAttribute("style", ElementRendering.INSTANCE.style("white", "white", getFloat(pdc.getDockingPointStrokeWidth() * 6))+";opacity:0");		
		
		if(edge == null){
			edge = new OMSVGPathElement();
			edge.setAttribute("d",linePath());
			edge.setAttribute("cursor", "pointer");
		}
		
		edge.setAttribute("style", style.defaultStyle);
		
		if(showArrow){
			if(arrow == null)
				arrow = new OMSVGPolygonElement();			
			arrow.setAttribute("style", arrowStyle.undefinedStyle);
		}	

		if(hover.getParentNode() != edgeGroup)
			edgeGroup.appendChild(hover);
		if(edge.getParentNode() != edgeGroup)
			edgeGroup.appendChild(edge);
		if(showArrow && arrow.getParentNode() != edgeGroup)
			edgeGroup.appendChild(arrow);
	
//		adaptGroup();
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
		//NOP
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		//NOP
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
//		parentEventHandler.hoverElement(this, true, event);
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
//		parentEventHandler.hoverElement(this, false, event);
	}

	@Override
	public void onClick(ClickEvent event) {
		//NOP
	}
	
	@Override
	public boolean doesIntersect(Rect mouseRect) {
		return false;
	}

	@Override
	public void setX2(double x2) {
		this.x2 = x2;
		edge.setAttribute("d",linePath());
		hover.setAttribute("d",linePath());
		adaptGroup();
	}

	@Override
	public double getX2() {
		return x2;
	}

	@Override
	public void setY2(double y2) {
		this.y2 = y2;
		edge.setAttribute("d",linePath());
		hover.setAttribute("d",linePath());
		adaptGroup();
	}

	@Override
	public double getY2() {
		return y2;
	}
	
	private float getFloat(double d){
		return (float)(d);
	}
	
	public void adaptGroup(){	
		GenericEntity from = null;
		GenericEntity to = null;
		
		if(fromElement.getEntity() instanceof NodeRepresentation) {
			from = ((NodeRepresentation)fromElement.getEntity()).getNode();
		}else if(fromElement.getEntity() instanceof EdgeRepresentation) {
			from = ((EdgeRepresentation)fromElement.getEntity()).getEdge();
		}
		
		if(toElement.getEntity() instanceof NodeRepresentation) {
			to = ((NodeRepresentation)toElement.getEntity()).getNode();
		}else if(toElement.getEntity() instanceof EdgeRepresentation) {
			to = ((EdgeRepresentation)toElement.getEntity()).getEdge();
		}		
		
		List<Object> allEdges = renderer.getAllEdges(from,to);
		
		Complex start = new Complex(fromElement.getCenterX(), fromElement.getCenterY());
		Complex end = new Complex(toElement.getCenterX(), toElement.getCenterY());		
		Complex tip = end;		
		Complex centre = Complex.getCentre(start, end);
		Complex toDirection = end.minus(start);	
		Complex perpToDir = new Complex(Math.abs(toDirection.y), -Math.abs(toDirection.x));					
		
		if(hover.getParentNode() == edgeGroup)
			edgeGroup.removeChild(hover);
		if(edge.getParentNode() == edgeGroup)
			edgeGroup.removeChild(edge);
		
		List<Complex> arrowPaths = new ArrayList<Complex>();
		
		if(allEdges.size() > 1) {
			Complex condEdgeStart = null;
			int index = allEdges.indexOf(from);			
			int half = allEdges.size() / 2;
			int factor = 0;
			if(index < half) {
				factor = (index + 1);
				condEdgeStart = centre.plus(perpToDir.normalize().times((pdc.getProcessNodeRadius()*2) * factor));
			}else {	
				factor = ((index + 1) - half);
				condEdgeStart = centre.minus(perpToDir.normalize().times((pdc.getProcessNodeRadius()*2) * factor));
			}
			hover.setAttribute("d", ElementRendering.INSTANCE.quadraticBezierCurve(getX(), getY(), condEdgeStart.x, condEdgeStart.y, getX2(), getY2()));
			edge.setAttribute("d", ElementRendering.INSTANCE.quadraticBezierCurve(getX(), getY(), condEdgeStart.x, condEdgeStart.y, getX2(), getY2()));			
		}
		
		tip = getTip();
		if(tip == null)
			tip = end;
		
		start = new Complex(fromElement.getCenterX(), fromElement.getCenterY());
		end = new Complex(toElement.getCenterX(), toElement.getCenterY());
		
		if(allEdges.size() > 1) {
			OMSVGPoint p = edge.getPointAtLength(edge.getTotalLength() / 2);
			Complex orientation = new Complex(p.getX(), p.getY());		
			arrowPaths = EdgeSvgElement.createArrowPath(tip, end.minus(orientation), 7, 14);
		}else
			arrowPaths = EdgeSvgElement.createArrowPath(tip, end.minus(start), 7, 14);
		
		edgeGroup.appendChild(edge);
		edgeGroup.appendChild(hover);		
		
		adaptArrow(arrowPaths);
	}
	
	public void adaptArrow(List<Complex> arrowPaths){
		String points = "";
		for(int i = 0; i< arrowPaths.size();i++){
			points += arrowPaths.get(i).x + "," + arrowPaths.get(i).y + " ";
		}
		arrow.setAttribute("points", points);
	}
	
	public static List<Complex> createArrowPath(Complex tip, Complex orientation, double width, double length) {
		List<Complex> points = new ArrayList<Complex>(3);
		
		points.add(tip);
		
		Complex normalizedOrientation = orientation.normalize();
		
		Complex scaledOrientation = normalizedOrientation.times(-length);
		Complex scaledPerpendicularOrientation = normalizedOrientation.perpendicular().times(width / 2);
		Complex bottom = tip.plus(scaledOrientation);
		Complex left = bottom.plus(scaledPerpendicularOrientation);
		Complex right = bottom.minus(scaledPerpendicularOrientation);
		
		points.add(left);
		points.add(right);
		
		return points; 
	}
	
	private Complex getTip() {
		if(toElement != null) {
			Rect toNodeRect = null;
			if(toElement instanceof EdgeSvgElement)	
				toNodeRect = new Rect(toElement.getCenterX(), toElement.getCenterY(), 1, 1);
			else {
				toNodeRect = new Rect(toElement.getX(), toElement.getY(), toElement.getWidth(), toElement.getHeight());
			}			
			for(int i = 0 ; i <= edge.getTotalLength(); i++) {
				OMSVGPoint candidate = edge.getPointAtLength(i);
				Rect candidateRect = new Rect(candidate.getX(), candidate.getY(), 1, 1);
				if(candidateRect.intersect(toNodeRect) != null) {
					if(toElement.getEntity() instanceof EdgeRepresentation) {
						EdgeRepresentation edgeRepresentation = (EdgeRepresentation)toElement.getEntity();
						if(edgeRepresentation.getEdge() instanceof ConditionalEdge){
							
							Complex direction = new Complex(toElement.getCenterX(), toElement.getCenterY())
									.minus(new Complex(fromElement.getCenterX(), fromElement.getCenterY()));
							
							return new Complex(candidateRect.getX(), candidateRect.getY()).minus(direction.normalize().times(10));
							
						}else {
							return new Complex(candidateRect.getX(), candidateRect.getY());
						}
					}else
						return new Complex(candidateRect.getX(), candidateRect.getY());
				}
			}			
		}
		return null;
	}
	
	@Override
	public void commit() {
		//NOP
	}
	
	@Override
	public String getDescription() {
//		EdgeRepresentation edgeRepresentation = (EdgeRepresentation)getEntity();
//		Edge edge = edgeRepresentation.getEdge();
//		return edge.getDescription();
		
		/*
		switch (edgeMode) {
		case error:
			return "error node";
		case overdue:
			return "overdue node";
		case restart:
			return "restart edge";
		default:
			return "";
		}
		*/
		
		return edgeMode.name() + " " + fromElement.getDescription() + " " + toElement.getDescription();
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
	
	private String linePath() {
		return "M" + getX() + " " + getY() + " L" + getX2() + " " + getY2() + " Z";
	}

}
