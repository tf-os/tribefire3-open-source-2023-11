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

import java.util.HashSet;
import java.util.Set;

import org.vectomatic.dom.svg.OMNode;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGRect;

import com.braintribe.gwt.browserfeatures.client.Console;
import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerConfiguration;
import com.braintribe.gwt.processdesigner.client.event.AbstractEventHandler;
import com.braintribe.gwt.processdesigner.client.event.ProcessDefintionElementEventHandler;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;

public abstract class AbstractProcessSvgElement<G extends GenericEntity> implements ProcessSvgElement<G>{
	
	protected G genericEntity;
	protected PersistenceGmSession session;
	protected ProcessDesignerConfiguration pdc;
	protected boolean selected = false;
	protected boolean hovered = false;
	protected boolean active = true;
	protected Set<HandlerRegistration> handlers = new HashSet<>();
	protected ProcessDefintionElementEventHandler parentEventHandler;
		
	public AbstractProcessSvgElement(G genericEntity) {
		setEntity(genericEntity);
	}
	
	public void setParentEventHandler(ProcessDefintionElementEventHandler parentEventHandler) {
		this.parentEventHandler = parentEventHandler;
	}
	
	public void setProcessDesignerConfiguration(ProcessDesignerConfiguration processDesignerConfiguration) {
		this.pdc = processDesignerConfiguration;
	}
	
	@Override
	public void setEntity(G genericEntity) {
		this.genericEntity = genericEntity;
	}
	
	@Override
	public GenericEntity getEntity() {
		return this.genericEntity;
	}
	
	@Override
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	@Override
	public PersistenceGmSession getSession() {
		return session;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
		handleSelectionChange();
	}
	
	public boolean getSelected() {
		return selected;
	}
	
	public void setHovered(boolean hovered) {
		this.hovered = hovered;
		handleHoveringChange();
	}
	
	public boolean getHovered() {
		return hovered;
	}
	
	public void setActive(boolean active) {
		this.active = active;
		handleActiveChange();
	}
	
	public boolean getActive() {
		return active;
	}

	
	@Override
	public Set<HandlerRegistration> registerHandlers(AbstractEventHandler... eventHandlers) {		
		if(eventHandlers != null && eventHandlers.length > 0){
			for(AbstractEventHandler handler : eventHandlers){
				if(getRepresentation() instanceof OMNode) {
					OMNode node = (OMNode)getRepresentation();
					handlers.add(node.addDomHandler(handler, MouseDownEvent.getType()));
					handlers.add(node.addDomHandler(handler, MouseUpEvent.getType()));
					handlers.add(node.addDomHandler(handler, MouseMoveEvent.getType()));
					handlers.add(node.addDomHandler(handler, MouseOverEvent.getType()));
					handlers.add(node.addDomHandler(handler, MouseOutEvent.getType()));
					handlers.add(node.addDomHandler(handler, ClickEvent.getType()));
				}else if(getRepresentation() instanceof FlowPanel) {
					Console.log("flowPanel");
					FlowPanel node = (FlowPanel)getRepresentation();
					handlers.add(node.addDomHandler(handler, MouseDownEvent.getType()));
					handlers.add(node.addDomHandler(handler, MouseUpEvent.getType()));
					handlers.add(node.addDomHandler(handler, MouseMoveEvent.getType()));
					handlers.add(node.addDomHandler(handler, MouseOverEvent.getType()));
					handlers.add(node.addDomHandler(handler, MouseOutEvent.getType()));
					handlers.add(node.addDomHandler(handler, ClickEvent.getType()));
				}
			}
		}
		return handlers;
	}
	
	@Override
	public Set<HandlerRegistration> getHandlers() {
		return handlers;
	}
	
	@Override
	public void dispose() {
		for(HandlerRegistration registration : getHandlers()){
			registration.removeHandler();
		}
		getHandlers().clear();
	}
	
	@Override
	public void onMouseOut(MouseOutEvent event) {
		OMSVGPoint point = parentEventHandler.createPoint(event).scale((float)(1/pdc.getScaleLevel()));		
		Rect mouseRect = new Rect(point.getX(), point.getY(), 1, 1);
		Rect elementRect = null;
		if(getRepresentation() instanceof OMSVGGElement) {
			OMSVGRect svgRect = ((OMSVGGElement)getRepresentation()).getBBox();
			elementRect = new Rect(svgRect.getX(), svgRect.getY(), svgRect.getWidth(), svgRect.getHeight());
		}else if(getRepresentation() instanceof FlowPanel) {
			FlowPanel svgRect = ((FlowPanel)getRepresentation());
			elementRect = new Rect(svgRect.getAbsoluteLeft(), svgRect.getAbsoluteLeft(), svgRect.getOffsetWidth(), svgRect.getOffsetHeight());
		}
		parentEventHandler.hoverElement(this, elementRect != null && elementRect.intersect(mouseRect) != null, event);
	}

}
