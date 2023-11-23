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
package com.braintribe.gwt.modeller.client.element;

import java.util.List;

import org.vectomatic.dom.svg.OMSVGCircleElement;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.OMSVGPolygonElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.model.modellergraph.graphics.AggregationKind;
import com.braintribe.model.modellergraph.graphics.Edge;
import com.braintribe.model.modellergraph.graphics.GeneralizationKind;
import com.braintribe.model.modellergraph.graphics.Point;
import com.braintribe.model.processing.modellergraph.common.ArrowTools;
import com.braintribe.model.processing.modellergraph.common.ColorPalette;
import com.braintribe.model.processing.modellergraph.common.Complex;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

public class EdgeSvg {
	
	private Edge edge;
	private OMSVGPathElement path;
	private OMSVGPathElement phantomPath;
	private OMSVGGElement g;
	private OMSVGGElement startAggregation = OMSVGParser.currentDocument().createSVGGElement();
	private OMSVGGElement endAggregation = OMSVGParser.currentDocument().createSVGGElement();
	private OMSVGGElement generalization = OMSVGParser.currentDocument().createSVGGElement();
	private boolean inverse;
	
	public EdgeSvg() {
		startAggregation.getElement().setAttribute("class", "relationDecoration");
		endAggregation.getElement().setAttribute("class", "relationDecoration");
		generalization.getElement().setAttribute("class", "relationDecoration");
	}
	
	public void adapt(Edge edge, boolean inverse) {
		this.edge = edge;
		this.inverse = inverse;
		
		getPath().setAttribute("d", quadraticBezier());
		getPath().setAttribute("stroke", ColorPalette.getColor(edge.getColor()));
		getPath().setAttribute("opacity", edge.getColor().getAlpha()+"");
		path.setAttribute("id", edge.getName());
		
		getPhantomPath().setAttribute("d", quadraticBezier());
		phantomPath.setAttribute("id", edge.getName()+"-phantom");
		
		startAggregation.getElement().setInnerHTML("");
		if(edge.getStartAggregationKind() != null) {
			OMSVGElement s = aggregationElement(edge, inverse ? false : true);
			if(s != null)
				startAggregation.appendChild(s);
		}
		
		endAggregation.getElement().setInnerHTML("");
		if(edge.getStartAggregationKind() != null) {			
			OMSVGElement e = aggregationElement(edge, inverse ? true : false);		
			if(e != null)
				endAggregation.appendChild(e);
		}
		
		generalization.getElement().setInnerHTML("");
		if(edge.getGeneralizationKind() != null && edge.getGeneralizationKind() != GeneralizationKind.none) {
			OMSVGElement g = generalization(edge, edge.getGeneralizationKind() != GeneralizationKind.generalization);
			if(g != null) {
				generalization.appendChild(g);
			}
		}
	}
	
	public OMSVGGElement getG() {
		if(g == null) {
			g = OMSVGParser.currentDocument().createSVGGElement();
			g.appendChild(getPhantomPath());
			g.appendChild(getPath());
		}
		return g;
	}
	
	private OMSVGPathElement getPath() {
		if(path == null) {
			path = OMSVGParser.currentDocument().createSVGPathElement();
			path.setAttribute("style", "stroke-width:2; fill: none; cursor: pointer");
//			path.setAttribute("id", edge.getName());
		}
		return path;
	}
	
	public OMSVGPathElement getPhantomPath() {
		if(phantomPath == null) {
			phantomPath = OMSVGParser.currentDocument().createSVGPathElement();
			phantomPath.setAttribute("style", "stroke-width:24; stroke:black; fill: none; cursor: pointer; opacity: 0");
//			phantomPath.setAttribute("id", edge.getName()+"-phantom");
		}
		return phantomPath;
	}
	
	public OMSVGElement getStartAggregation() {
		return startAggregation;
	}
	
	public OMSVGElement getEndAggregation() {
		return endAggregation;
	}
	
	public OMSVGGElement getGeneralization() {
		return generalization;
	}
	
	public void ensureMouseHandlers(EdgeElement edgeElement) {
		/*
		OMSVGElement path = getPath();
		path.addDomHandler(edgeElement, MouseOverEvent.getType());
		path.addDomHandler(edgeElement, MouseOutEvent.getType());
		path.addDomHandler(edgeElement, MouseMoveEvent.getType());
		path.addDomHandler(edgeElement, MouseDownEvent.getType());
		path.addDomHandler(edgeElement, MouseUpEvent.getType());
		path.addDomHandler(edgeElement, ClickEvent.getType());
		path.addDomHandler(edgeElement, DoubleClickEvent.getType());
		
		OMSVGElement phantom = getPhantomPath();
		phantom.addDomHandler(edgeElement, MouseOverEvent.getType());
		phantom.addDomHandler(edgeElement, MouseOutEvent.getType());
		phantom.addDomHandler(edgeElement, MouseMoveEvent.getType());
		phantom.addDomHandler(edgeElement, MouseDownEvent.getType());
		phantom.addDomHandler(edgeElement, MouseUpEvent.getType());
		phantom.addDomHandler(edgeElement, ClickEvent.getType());
		phantom.addDomHandler(edgeElement, DoubleClickEvent.getType());
		*/
		
		startAggregation.addDomHandler(edgeElement, MouseOverEvent.getType());
		startAggregation.addDomHandler(edgeElement, MouseOutEvent.getType());
		startAggregation.addDomHandler(edgeElement, MouseMoveEvent.getType());
		startAggregation.addDomHandler(edgeElement, MouseDownEvent.getType());
		startAggregation.addDomHandler(edgeElement, MouseUpEvent.getType());
		startAggregation.addDomHandler(edgeElement, ClickEvent.getType());
		startAggregation.addDomHandler(edgeElement, DoubleClickEvent.getType());
		
		endAggregation.addDomHandler(edgeElement, MouseOverEvent.getType());
		endAggregation.addDomHandler(edgeElement, MouseOutEvent.getType());
		endAggregation.addDomHandler(edgeElement, MouseMoveEvent.getType());
		endAggregation.addDomHandler(edgeElement, MouseDownEvent.getType());
		endAggregation.addDomHandler(edgeElement, MouseUpEvent.getType());
		endAggregation.addDomHandler(edgeElement, ClickEvent.getType());
		endAggregation.addDomHandler(edgeElement, DoubleClickEvent.getType());
		
		generalization.addDomHandler(edgeElement, MouseOverEvent.getType());
		generalization.addDomHandler(edgeElement, MouseOutEvent.getType());
		generalization.addDomHandler(edgeElement, MouseMoveEvent.getType());
		generalization.addDomHandler(edgeElement, MouseDownEvent.getType());
		generalization.addDomHandler(edgeElement, MouseUpEvent.getType());
		generalization.addDomHandler(edgeElement, ClickEvent.getType());
		generalization.addDomHandler(edgeElement, DoubleClickEvent.getType());		
		
		
		OMSVGGElement g = getG();
		g.addDomHandler(edgeElement, MouseOverEvent.getType());
		g.addDomHandler(edgeElement, MouseOutEvent.getType());
		g.addDomHandler(edgeElement, MouseMoveEvent.getType());
		g.addDomHandler(edgeElement, MouseDownEvent.getType());
		g.addDomHandler(edgeElement, MouseUpEvent.getType());
		g.addDomHandler(edgeElement, ClickEvent.getType());
		g.addDomHandler(edgeElement, DoubleClickEvent.getType());
	}
	
	public void reset() {
		getG().removeAttribute("opacity");
		startAggregation.removeAttribute("opacity");
		endAggregation.removeAttribute("opacity");
		generalization.removeAttribute("opacity");
	}
	
	public void downplay() {
		String opacity = GmModellerElement.DOWNPLAY_OPACITY + "";
		getG().setAttribute("opacity", opacity);
		startAggregation.setAttribute("opacity", opacity);
		endAggregation.setAttribute("opacity", opacity);
		generalization.setAttribute("opacity", opacity);
	}
	
	private String quadraticBezier() {
		StringBuilder sb = new StringBuilder();
		Point start = inverse ? edge.getEnd() : edge.getStart();
		sb.append("M" + start.getX() + "," + start.getY() + " ");
		
		Point turning = edge.getTurning();
		sb.append("Q" + turning.getX() + "," + turning.getY() + " ");
		
		Point end = inverse ? edge.getStart() : edge.getEnd();
		sb.append(end.getX() + "," + end.getY());
		return sb.toString();
	}

	private OMSVGElement aggregationElement(Edge edge, boolean start){	
		AggregationKind aggregationKind = start ? edge.getEndAggregationKind() : edge.getStartAggregationKind();
		OMSVGElement element = null;
//		com.braintribe.model.modellergraph.graphics.Color color2 = ColorPalette.getColor(aggregationKind, false);
		String color = ColorPalette.toHex(edge.getColor());
		double alpha = edge.getColor().getAlpha();
		switch (aggregationKind != null ? aggregationKind : AggregationKind.none) {
			case simple_aggregation:				
				element = renderCircle(edge, start, 6);	
				element.setAttribute("style", "stroke:" + color +";stroke-width:2;fill:white; opacity:" + alpha);
//				return circle;
				break;
			case unordered_aggregation: case ordered_aggregation:
				element = new OMSVGGElement();
				element.appendChild(renderCircle(edge, start, 10));
				element.appendChild(renderCircle(edge, start, 5));
				element.setAttribute("style", "stroke:" + color +";stroke-width:2;fill:white; opacity:" + alpha);
//				return element;
				break;
			case multiple_aggregation:
				element = new OMSVGGElement();
				OMSVGCircleElement circle1 = renderCircle(edge, start, 10);
				element.appendChild(circle1);
				circle1.setAttribute("style", "stroke:" + color + ";stroke-width:1;fill:white");
				OMSVGCircleElement circle2 = renderCircle(edge, start, 8);
				circle2.setAttribute("style", "stroke:" + color + ";stroke-width:0;fill:" + color);
				element.appendChild(circle2);
				element.setAttribute("style", "opacity:" + alpha);
//				return element;
				break;
//				return createCakeSlices(edge, start, edgeGroup, 10);
			case key_association:
				Complex complex = Complex.getComplex(start ? edge.getStart() : edge.getEnd());
				element = new OMSVGGElement();				
				OMSVGCircleElement circle = new OMSVGCircleElement((float)complex.x,(float)complex.y,10);				
				element.appendChild(circle);
				OMSVGPolygonElement arrow = new OMSVGPolygonElement();				
				element.setAttribute("style", "stroke:" + color +";stroke-width:3;fill:white; opacity: " + alpha/* + getColor(edge.getColor())*/);

				Complex tip = complex;
				Complex direction = Complex.getComplex(edge.getTurning()).minus(tip);
				List<Complex> arrowPaths = ArrowTools.createArrowPath(tip.plus(direction.normalize().times(5)), direction, 8, 8);
				
				String points = "";
				for(int i = 0; i< arrowPaths.size();i++){
					points += arrowPaths.get(i).x + "," + arrowPaths.get(i).y + " ";
				}
				arrow.setAttribute("points", points);
				element.appendChild(arrow);
				break;
			case value_association:
				complex = Complex.getComplex(start ? edge.getEnd() : edge.getStart());
				element = new OMSVGGElement();
				circle = new OMSVGCircleElement((float)complex.x,(float)complex.y,10);				
				element.appendChild(circle);
				arrow = new OMSVGPolygonElement();				
				element.setAttribute("style", "stroke:" + color +";stroke-width:3;fill:white; opacity: " + alpha/* + getColor(edge.getColor())*/);
								
				tip = complex;
				direction = tip.minus(Complex.getComplex(edge.getTurning()));
				arrowPaths = ArrowTools.createArrowPath(tip.plus(direction.normalize().times(5)), direction, 8, 8);
				
				points = "";
				for(int i = 0; i< arrowPaths.size();i++){
					points += arrowPaths.get(i).x + "," + arrowPaths.get(i).y + " ";
				}
				arrow.setAttribute("points", points);
				element.appendChild(arrow);
				break;
			default:
//				return null;
				break;
		}
//		if(element != null && modelGraphConfigurations.modellerMode == GmModellerMode.condensed)
		if(element != null)
			element.setAttribute("cursor", "pointer");
		return element;
	}
	
	private OMSVGElement generalization(Edge edge, boolean start){
		OMSVGPolygonElement element = new OMSVGPolygonElement();
	
		element.setAttribute("style", "stroke:#dbdbdb; stroke-width:3;fill:white; opacity: " + edge.getColor().getAlpha()/* + getColor(edge.getColor())*/);
		
		Complex point = Complex.getComplex(start ? edge.getStart() : edge.getEnd());
		List<Complex> arrowPaths = ArrowTools.createArrowPath(point, point.minus(Complex.getComplex(edge.getTurning())), 8, 13);
		String points = "";
		for(int i = 0; i< arrowPaths.size();i++){
			points += arrowPaths.get(i).x + "," + arrowPaths.get(i).y + " ";
		}
		element.setAttribute("points", points);
		
		element.setId(edge.getName() + " " + start);
		return element;
	}
	
	private OMSVGCircleElement renderCircle(Edge edge, boolean inverse, double radius){
		float x = 0, y = 0;
		x = inverse ? edge.getEnd().getX().floatValue() : edge.getStart().getX().floatValue();
		y = inverse ? edge.getEnd().getY().floatValue() : edge.getStart().getY().floatValue();
			
		OMSVGCircleElement element = new OMSVGCircleElement(x,y,(float)radius);

		return element;
	}
}
