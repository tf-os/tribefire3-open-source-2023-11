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

import java.util.ArrayList;
import java.util.List;

import org.vectomatic.dom.svg.OMSVGGElement;

import com.braintribe.gwt.modeller.client.GmModellerRenderer;
import com.braintribe.model.modellergraph.graphics.Node;
import com.braintribe.model.processing.modellergraph.common.Complex;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.user.client.Timer;

public class RelationshipChoices extends OMSVGGElement implements DragLeaveHandler{
	
	private GmModellerRenderer renderer;
//	private OMSVGSVGElement svg = OMSVGParser.currentDocument().createSVGSVGElement();
//	private OMSVGGElement g = OMSVGParser.currentDocument().createSVGGElement();
//	private OMSVGCircleElement innerCircle = new OMSVGCircleElement();
	private List<RelationshipChoice> choices = new ArrayList<RelationshipChoice>();
	RelationshipChoice activeRelationshipChoice;
	
	Timer t = new Timer() {		
		@Override
		public void run() {
			renderer.hideConnectionChoices();
		}
	};
	
	public RelationshipChoices() {
//		innerCircle.setAttribute("style", "fill:white;stroke:#7d7d7d;stroke-width:2");
		addDomHandler(this, DragLeaveEvent.getType());
//		getElement().appendChild(svg.getElement());
		getElement().setAttribute("class", "relationshipChoices");
	}
	
	public void setRenderer(GmModellerRenderer renderer) {
		this.renderer = renderer;
	}
	
	public void setChoices(List<RelationshipChoice> choices) {
		this.choices = choices;
	}
	
	public List<RelationshipChoice> getChoices() {
		return choices;
	}
	
//	public OMSVGGElement getG() {
//		return g;
//	}
	
//	public OMSVGSVGElement getSvg() {
//		return svg;
//	}
	
	public void addChoice(RelationshipChoice choice){
		choice.setRelationshipChoices(this);
		choice.setAttribute("style", "fill:#d9d9d9;stroke:#7d7d7d;stroke-width:2");
		choices.add(choice);
	}
	
	public void removeChoice(RelationshipChoice actionCakeSlice){
		choices.remove(actionCakeSlice);
	}
	
	public void clear() {
		choices.clear();
	}
	
	public void adapt(NodeElement nodeElement){
		//Style style = getElement().getStyle();
		//Style nodeStyle = nodeElement.getDecoration().getElement().getStyle();
		Node node = nodeElement.getNode();
		
		/*
		style.setPosition(Position.ABSOLUTE);
		style.setTop(node.getCenter().getY()-node.getRadius(), Unit.PX);
		style.setLeft(node.getCenter().getX()-node.getRadius(), Unit.PX);
		style.setWidth(nodeElement.getDecoration().getOffsetWidth(), Unit.PX);
		style.setHeight(nodeElement.getDecoration().getOffsetHeight(), Unit.PX);
		style.setProperty("pointerEvents", "all");
		
		Style svgStyle = svg.getElement().getStyle();
		svgStyle.setWidth(nodeElement.getDecoration().getOffsetWidth(), Unit.PX);
		svgStyle.setHeight(nodeElement.getDecoration().getOffsetHeight(), Unit.PX);*/
		
		getElement().setInnerHTML("");
		Complex nodeCenter = new Complex(node.getCenter().getX(), node.getCenter().getY());
		//Complex.getComplex(node.getCenter());
		double radius = node.getRadius();
		
		float cx = (float)nodeCenter.x;
		float cy = (float)nodeCenter.y;
		
//		innerCircle.setAttribute("cx", cx + "");
//		innerCircle.setAttribute("cy", cy + "");			
//		innerCircle.setAttribute("r", radius / 4 + "");
		
		double angle = Math.PI * 2 / choices.size();
		
		List<Complex> points = new ArrayList<Complex>();
		for (int i = 1; i <= choices.size(); i++) {
			double a1 = angle * (i - 1);
			double a2 = a1 + angle;

			double y = Math.sin(a2) * radius + cy;
			double x = Math.cos(a2) * radius + cx;

			points.add(new Complex(x, y));
		}
		
		for(Complex point : points){
			int firstIndex = points.indexOf(point);
			
			RelationshipChoice choice = choices.get(firstIndex);
				
			int newIndex = firstIndex+1;
			if(newIndex == points.size())
				newIndex = 0;
			
			Complex nextPoint = points.get(newIndex);
			
			String path = "M" + cx + "," + cy + " " +
			"L " + point.x + "," + point.y + " " +
			"A " + radius + " " + radius + " 0 0 1 " + nextPoint.x + " " + nextPoint.y + " " +
			"z";				
			
			choice.setAttribute("d", path);
			
			Complex center = Complex.getCentre(point, nextPoint);
			int size = (int)(radius * 0.5);
			Complex position = nodeCenter.plus(center.minus(nodeCenter).normalize().times(radius/2 + size/4));
			
			appendChild(choice);
			choice.adaptIcon(position, radius);
			appendChild(choice.getIconElement());
		}
		
//		svg.appendChild(innerCircle);
	}
	
	public void dispose(){
		for(RelationshipChoice choice : choices)
			choice.dispose();		
		choices.clear();
	}
	
	public void update(RelationshipChoice choice){
		for(RelationshipChoice choice2 : choices){
			if(choice != choice2)
				choice2.setAttribute("style", "fill:#efefef;stroke:#7d7d7d;stroke-width:2");
		}
	}
	
	@Override
	public void onDragLeave(DragLeaveEvent event) {
		System.err.println("dragLeave choices");
		activeRelationshipChoice = null;
		/*Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
			
			@Override
			public boolean execute() {
				if(activeRelationshipChoice == null) {
					renderer.hideConnectionChoices();
				}
				return false;
			}
		}, 250);	*/
		event.stopPropagation();
	}

	public void setActiveChoice(RelationshipChoice relationshipChoice) {
		this.activeRelationshipChoice = relationshipChoice;
		if(activeRelationshipChoice == null) {
			t.schedule(250);
		}else {
			t.cancel();
		}
	}

	public void showConnectionInfo(String relation) {
		renderer.showConnectionInfo(relation);
	}

	public void hideConnectionInfo() {
		renderer.hideTooltip();
	}
	
	public void hideConnection() {
		renderer.onDragEnd(null);
	}
}
