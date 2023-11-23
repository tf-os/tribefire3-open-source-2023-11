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

import java.util.Set;

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.event.AbstractEventHandler;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public interface ProcessSvgElement<G extends GenericEntity> extends ManipulationListener, MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOverHandler, MouseOutHandler, ClickHandler{
	
	public void setEntity(G genericEntity);
	public GenericEntity getEntity();
	public void setSession(PersistenceGmSession session);
	public PersistenceGmSession getSession();
	public Object getRepresentation();
	public Set<HandlerRegistration> registerHandlers(AbstractEventHandler... handlers);
	public Set<HandlerRegistration> getHandlers();
	
	public void setX(double x);
	public double getX();
	public void setY(double y);	
	public double getY();
	
	public void setX2(double x2);
	public double getX2();
	public void setY2(double y2);	
	public double getY2();
	
	public void setCenterX(double centerX);
	public double getCenterX();
	public void setCenterY(double centerY);
	public double getCenterY();
	
	public void setWidth(double width);
	public double getWidth();
	public void setHeight(double height);
	public double getHeight();
	
	public void handleSelectionChange();
	public void handleHoveringChange();
	public void handleActiveChange();
	
	public void initialize();
	public void dispose();
	
	public boolean doesIntersect(Rect mouseRect);
	
	public void commit();
	
	public String getDescription();
	public ElementKind getElementKind();
	
	public boolean canBeConnected();
}
