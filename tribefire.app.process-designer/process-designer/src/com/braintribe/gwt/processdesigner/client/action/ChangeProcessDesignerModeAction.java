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
package com.braintribe.gwt.processdesigner.client.action;

import org.vectomatic.dom.svg.OMSVGGElement;

import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.processdesigner.client.ProcessDesigner;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerConfiguration;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerMode;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class ChangeProcessDesignerModeAction extends ProcessDesignerActionMenuElement{
	
	private PersistenceGmSession session;
	private ProcessDesignerMode mode;
	private ProcessDesignerConfiguration configuration;
	private ProcessDesigner processDesigner;
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public void setMode(ProcessDesignerMode mode) {
		this.mode = mode;
	}
	
	public void setConfiguration(ProcessDesignerConfiguration configuration) {
		this.configuration = configuration;
	}	
	
	public void setProcessDesigner(ProcessDesigner processDesigner) {
		this.processDesigner = processDesigner;
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if(manipulation instanceof ChangeValueManipulation){
			ChangeValueManipulation propertyManipulation = (ChangeValueManipulation)manipulation;
			LocalEntityProperty owner = (LocalEntityProperty)propertyManipulation.getOwner();
			GenericEntity entity = owner.getEntity();
			String propertyName = owner.getPropertyName();
			if(entity == configuration && propertyName.equals("processDesignerMode")){
				setActive(propertyManipulation.getNewValue() == mode);
			}
		}
	}
	
	@Override
	public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
		//NOP
	}
	
	@Override
	public void handleDipose() {
		if (session != null)
			session.listeners().entity(configuration).remove(this);
	}
	
	@Override
	public void configure() {
		if (session != null)
			session.listeners().entity(configuration).add(this);
	}
	
	@Override
	public void perform() {
		configuration.setProcessDesignerMode(mode);
		processDesigner.getProcessDesignerRenderer().showMode(mode);
	}
	
	@Override
	public OMSVGGElement prepareIconElement() {
		return null;
	}
}
