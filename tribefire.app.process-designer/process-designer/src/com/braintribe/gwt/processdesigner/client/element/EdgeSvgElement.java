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
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processdefrep.EdgeRepresentation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

import tribefire.extension.process.model.deployment.Edge;

public class EdgeSvgElement extends AbstractProcessSvgElement<EdgeRepresentation> {

	private boolean showArrow = true;
	
	OMSVGGElement edgeGroup;
	OMSVGPathElement hover;
	OMSVGPathElement edge;
	OMSVGPolygonElement arrow;
	
	ProcessDesignerRenderer renderer;
	NodeDivElement toNodeElement;
	NodeDivElement fromNodeElement;
		
	double x1,x2,y1,y2;
	
	public void setRenderer(ProcessDesignerRenderer renderer) {
		this.renderer = renderer;
	}
	
	public void setToNodeElement(NodeDivElement toNodeElement) {
		this.toNodeElement = toNodeElement;
	}
	
	public void setFromNodeElement(NodeDivElement fromNodeElement) {
		this.fromNodeElement = fromNodeElement;
	}
	
	public EdgeSvgElement(EdgeRepresentation genericEntity) {
		super(genericEntity);
	}

	@Override
	public OMSVGGElement getRepresentation() {
		return edgeGroup;
	}

	@Override
	public void setX(double x) {
		this.x1 = x;
		//edge.setAttribute("x1", x+"");
		edge.setAttribute("d",linePath());
		hover.setAttribute("d",linePath());
		adaptGroup();
	}

	@Override
	public double getX() {
		return x1;
//		try{
//			return Double.parseDouble(edge.getAttribute("x1"));
//		}catch(Exception ex){
//			return 0;
//		}		
	}

	@Override
	public void setY(double y) {
		this.y1 = y;
		//edge.setAttribute("y1", y+"");
		edge.setAttribute("d",linePath());
		hover.setAttribute("d",linePath());
		adaptGroup();
	}

	@Override
	public double getY() {
		return y1;
//		try{
//			return Double.parseDouble(edge.getAttribute("y1"));
//		}catch(Exception ex){
//			return 0;
//		}	
	}

	@Override
	public void setWidth(double width) {
		getRepresentation().setAttribute("width", width+"");
		adaptGroup();
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
		adaptGroup();
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
			edge.setAttribute("style", ProcessElementStylingUtil.getInstance().getEdgeElementStyling().selectedStyle);
			arrow.setAttribute("style", ProcessElementStylingUtil.getInstance().getArrowElementStyling().selectedStyle);						
		}else{
			edge.setAttribute("style", ProcessElementStylingUtil.getInstance().getEdgeElementStyling().defaultStyle);
			arrow.setAttribute("style", ProcessElementStylingUtil.getInstance().getArrowElementStyling().defaultStyle);
		}
	}
	
	@Override
	public void handleHoveringChange() {
		if(!selected){
			if(hovered){
				edge.setAttribute("style", ProcessElementStylingUtil.getInstance().getEdgeElementStyling().hoveredStyle);				
				arrow.setAttribute("style", ProcessElementStylingUtil.getInstance().getArrowElementStyling().hoveredStyle);
			}else{
				edge.setAttribute("style", ProcessElementStylingUtil.getInstance().getEdgeElementStyling().defaultStyle);
				arrow.setAttribute("style", ProcessElementStylingUtil.getInstance().getArrowElementStyling().defaultStyle);
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
		if(edgeGroup == null){
			edgeGroup = new OMSVGGElement();
			edgeGroup.setAttribute("opacity", "0");
		}
		
		if(hover == null){
			hover = new OMSVGPathElement();
			hover.setAttribute("d", linePath());
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
			edge.setAttribute("d", linePath());
			edge.setAttribute("cursor", "pointer");
		}
		
		edge.setAttribute("style", ProcessElementStylingUtil.getInstance().getEdgeElementStyling().defaultStyle);
		
		if(showArrow){
			if(arrow == null)
				arrow = new OMSVGPolygonElement();			
			arrow.setAttribute("style", ProcessElementStylingUtil.getInstance().getEdgeElementStyling().undefinedStyle);
		}	

		if(hover.getParentNode() != edgeGroup)
			edgeGroup.appendChild(hover);
		if(edge.getParentNode() != edgeGroup)
			edgeGroup.appendChild(edge);
		if(showArrow && arrow.getParentNode() != edgeGroup)
			edgeGroup.appendChild(arrow);
	
		adaptGroup();
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
	public boolean doesIntersect(Rect mouseRect) {
		return false;
	}
	
	/*private Rect getRect(OMSVGRect omsvgRect){
		return new Rect(omsvgRect.getX(), omsvgRect.getY(), omsvgRect.getWidth(), omsvgRect.getHeight());
	}*/

	@Override
	public void setX2(double x2) {
		this.x2 = x2;
		//edge.setAttribute("x2", x2+"");
		edge.setAttribute("d",linePath());
		hover.setAttribute("d",linePath());
		adaptGroup();
	}

	@Override
	public double getX2() {
		return x2;
//		try{
//			return Double.parseDouble(edge.getAttribute("x2"));
//		}catch(Exception ex){
//			return 0;
//		}	
	}

	@Override
	public void setY2(double y2) {
		this.y2 = y2;
		//edge.setAttribute("y2", y2+"");
		edge.setAttribute("d",linePath());
		hover.setAttribute("d",linePath());
		adaptGroup();
	}

	@Override
	public double getY2() {
		return y2;
//		try{
//			return Double.parseDouble(edge.getAttribute("y2"));
//		}catch(Exception ex){
//			return 0;
//		}	
	}
	
	public void adaptGroup(){
		EdgeRepresentation edgeRepresentation = (EdgeRepresentation)getEntity();
		Edge edgeCandidate = edgeRepresentation.getEdge();
		List<Object> allEdges = renderer.getAllEdges(edgeCandidate.getFrom(), edgeCandidate.getTo());
		
		Complex start = new Complex(fromNodeElement.getCenterX(), fromNodeElement.getCenterY());
		Complex end = new Complex(toNodeElement.getCenterX(), toNodeElement.getCenterY());
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
			int index = allEdges.indexOf(edgeCandidate);			
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
		
		start = new Complex(fromNodeElement.getCenterX(), fromNodeElement.getCenterY());
		end = new Complex(toNodeElement.getCenterX(), toNodeElement.getCenterY());
		
		if(allEdges.size() > 1) {
			OMSVGPoint p = edge.getPointAtLength(edge.getTotalLength() / 2);
			Complex orientation = new Complex(p.getX(), p.getY());		
			arrowPaths = EdgeSvgElement.createArrowPath(tip, end.minus(orientation), 7, 14);
		}else
			arrowPaths = EdgeSvgElement.createArrowPath(tip, end.minus(start), 7, 14);
		
		edgeGroup.appendChild(edge);
		edgeGroup.appendChild(hover);		
		
		adaptArrow(arrowPaths);
		
		/*
		double diffX = getX2() - getX();
		double diffY = getY2() - getY();
		String newX = "", newY = "", newWidth = "", newHeight = "";
		if(diffX > 0 && diffY > 0){
			newX = getX()+"";
			newY = getY()+"";
			newWidth = diffX+"";
			newHeight = diffY+"";
		}else if(diffX < 0 && diffY < 0){
			newX = getX2() + "";
			newY = getY2() + "";
			newWidth = (diffX * -1)+"";
			newHeight = (diffY * -1)+"";
		}else if(diffX < 0){
			newX = (getX() + diffX) + "";
			newY = getY()+"";
			newWidth = (-1 * diffX) +"";
			newHeight = diffY+"";
		}else if(diffY < 0){
			newX = getX()+"";
			newY = (getY() + diffY) + "";
			newWidth = diffX+"";
			newHeight = (-1 * diffY) +"";
		}else if(diffY == 0){
			newX = getX()+"";
			newY = getY()+"";
			newWidth = diffX+"";
			newHeight = getString(pdc.getDockingPointRadius()*2);
		}else if(diffX == 0){
			newX = getX()+"";
			newY = getY()+"";
			newWidth = getString(pdc.getDockingPointRadius()*2);
			newHeight = diffY+"";
		}	
		
		edgeGroup.setAttribute("x", newX);
		edgeGroup.setAttribute("y", newY);
		edgeGroup.setAttribute("width", newWidth);
		edgeGroup.setAttribute("height", newHeight);
		*/
				
	}
	
	/*private String getString(double d){
		return d + "";
	}*/
	
	private float getFloat(double d){
		return (float)(d);
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
		if(toNodeElement != null) {
			Rect toNodeRect = new Rect(toNodeElement.getX(), toNodeElement.getY(), toNodeElement.getWidth(), toNodeElement.getHeight());					
			for(int i = 0 ; i <= edge.getTotalLength(); i++) {
				OMSVGPoint candidate = edge.getPointAtLength(i);
				Rect candidateRect = new Rect(candidate.getX(), candidate.getY(), 1, 1);
				if(candidateRect.intersect(toNodeRect) != null) {					
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
		//EdgeRepresentation edgeRepresentation = (EdgeRepresentation)getEntity();
		//Edge edge = edgeRepresentation.getEdge();
		return "edge " +  fromNodeElement.getDescription() + " " + toNodeElement.getDescription();
	}

	@Override
	public void setCenterX(double centerX) {
		//NOP
	}

	@Override
	public double getCenterX() {
		try{	
			EdgeRepresentation edgeRepresentation = (EdgeRepresentation)getEntity();
			Edge edgeEntity = edgeRepresentation.getEdge();
			List<Object> allEdges = renderer.getAllEdges(edgeEntity.getFrom(), edgeEntity.getTo());
			if(allEdges.size() > 1)
				return edge.getPointAtLength(edge.getTotalLength() / 2).getX();
			else {
				return Complex.getCentre(new Complex(x1, y1), new Complex(x2, y2)).x;
			}
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
			EdgeRepresentation edgeRepresentation = (EdgeRepresentation)getEntity();
			Edge edgeEntity = edgeRepresentation.getEdge();
			List<Object> allEdges = renderer.getAllEdges(edgeEntity.getFrom(), edgeEntity.getTo());
			if(allEdges.size() > 1)
				return edge.getPointAtLength(edge.getTotalLength() / 2).getY();
			else {
				return Complex.getCentre(new Complex(x1, y1), new Complex(x2, y2)).y;
			}
		}catch(Exception ex){
			return 0;
		}
	}
	
	@Override
	public ElementKind getElementKind() {
		return ElementKind.edge;
	}
	
	@Override
	public boolean canBeConnected() {
		return false;
	}
	
	private String linePath() {
		return "M" + getX() + " " + getY() + " L" + getX2() + " " + getY2() + " Z";
	}
	
}
