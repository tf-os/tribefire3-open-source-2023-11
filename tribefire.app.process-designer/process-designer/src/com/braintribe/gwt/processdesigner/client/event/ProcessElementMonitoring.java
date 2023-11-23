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
package com.braintribe.gwt.processdesigner.client.event;

import com.braintribe.gwt.processdesigner.client.ProcessDesignerRenderer;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.notifying.NotifyingGmSession;

public class ProcessElementMonitoring {
	
	GenericEntity parentEntity;
	GenericEntity entity;
	ManipulationListener manipulationListener;
	ProcessDesignerRenderer renderer;
	NotifyingGmSession session;
	
	public ProcessElementMonitoring(GenericEntity entity, GenericEntity parentEntity) {
		this.entity = entity;
		this.parentEntity = parentEntity;
	}
	
	public void setSession(NotifyingGmSession session) {
		this.session = session;
	}
	
	public void setRenderer(ProcessDesignerRenderer renderer) {
		this.renderer = renderer;
	}
	
	public GenericEntity getParentEntity() {
		return parentEntity;
	}
	
	public void init(){
		manipulationListener = new ManipulationListener() {
			@Override
			public void noticeManipulation(Manipulation manipulation) {
				renderer.noticeManipulation(manipulation, entity);	
			}
		};
		if(entity != null)
			session.listeners().entity(entity).add(manipulationListener);
	}
	
	public void dispose(){
		if(entity != null)
			session.listeners().entity(entity).remove(manipulationListener);
	}

}
