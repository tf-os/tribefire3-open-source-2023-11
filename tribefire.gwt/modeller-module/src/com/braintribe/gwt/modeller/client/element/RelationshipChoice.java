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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.ui.SVGResource;

import com.braintribe.model.processing.modellergraph.common.Complex;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public abstract class RelationshipChoice extends OMSVGPathElement implements DragOverHandler, DragEnterHandler, DragLeaveHandler, DropHandler{
	
	private final static String HOVER_COLOR = "#ffd688";
	private final static String NORMAL_COLOR = "#d9d9d9";
	
	Logger logger = Logger.getLogger("RelationshipChoice");
	
	RelationshipChoices relationshipChoices;
	String text;
	OMSVGElement iconElement;
	List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();
	
	
	
	public RelationshipChoice(String text) {
		setText(text);
		addDomHandler(this, DragOverEvent.getType());
		addDomHandler(this, DragEnterEvent.getType());
		addDomHandler(this, DragLeaveEvent.getType());
		addDomHandler(this, DropEvent.getType());
		getElement().setAttribute("class", "relationshipChoice");
	}

	public void setText(String text){
		this.text = text;
	}
	
	public RelationshipChoice(String text, SVGResource image){
		this(text);
		iconElement = image.getSvg();
		iconElement.addDomHandler(this, DragOverEvent.getType());
		iconElement.addDomHandler(this, DragEnterEvent.getType());
		iconElement.addDomHandler(this, DragLeaveEvent.getType());
		iconElement.addDomHandler(this, DropEvent.getType());
	}
	
	public void setRelationshipChoices(RelationshipChoices relationshipChoices) {
		this.relationshipChoices = relationshipChoices;
	}
	
	public void adaptIcon(Complex complex, double radius){
		int size = (int)(radius * 0.5);
		iconElement.setAttribute("x", (int)(complex.x - size/2) + "");
		iconElement.setAttribute("y", (int)(complex.y - size/2) + "");
		iconElement.setAttribute("width",  size + "");
		iconElement.setAttribute("height", size + "");
		iconElement.setAttribute("opacity", "0.6");
	}

	public OMSVGElement getIconElement() {
		return iconElement;
	}
	
	public void dispose(){
		for(HandlerRegistration handlerRegistration : handlerRegistrations)
			handlerRegistration.removeHandler();		
		handlerRegistrations.clear();
	}
	
	public abstract void perform();
	
	public abstract boolean isVisible();
	
	@Override
	public void onDragLeave(DragLeaveEvent event) {
		System.err.println("dragLeave choice " + text);
		logger.log(Level.SEVERE, "dragLeave choice " + text);
		out();
		relationshipChoices.setActiveChoice(null);
	}
	
	@Override
	public void onDragEnter(DragEnterEvent event) {
		System.err.println("dragEnter choice " + text);	
	}
	
	@Override
	public void onDragOver(DragOverEvent event) {
//		System.err.println("dragOver choice");
		relationshipChoices.setActiveChoice(this);
		over();
	}
	
	@Override
	public void onDrop(DropEvent event) {
		event.preventDefault();		
		System.err.println("drop choice " + text);	
		out();
		perform();
		relationshipChoices.hideConnection();
	}
	
	public void out() {
		setAttribute("style", "fill:"+NORMAL_COLOR+";stroke:#7d7d7d;stroke-width:2");
		relationshipChoices.hideConnectionInfo();
	}
	
	public void over() {
		setAttribute("style", "fill:"+HOVER_COLOR+";stroke:#7d7d7d;stroke-width:2");
		relationshipChoices.showConnectionInfo(text);
	}
		
}
