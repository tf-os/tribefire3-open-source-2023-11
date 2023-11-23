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
package com.braintribe.gwt.smartmapper.client.element;

import java.util.Set;

import com.braintribe.gwt.smartmapper.client.PropertyAssignmentContext;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentInput;
import com.braintribe.gwt.smartmapper.client.experts.AbstractMappingElementsProvider;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.OrderedLinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAsIs;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
//import com.braintribe.model.processing.am.AssemblyMonitoring;
//import com.braintribe.model.processing.am.EntityMigrationListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class PropertyAssignmentElement implements ManipulationListener, /* EntityMigrationListener, */ ClickHandler{
	
	protected PropertyAssignmentContext propertyAssignmentContext;
	protected Set<PropertyAssignmentInput> inputElements;
//	private boolean inherited = false;
//	protected AssemblyMonitoring am;
	
//	public void setInherited(boolean inherited){
//		this.inherited = inherited;
//	}
		
	public void setPropertyAssignmentContext(PropertyAssignmentContext propertyAssignmentContext) {
		this.propertyAssignmentContext = propertyAssignmentContext;
		initialise();
		adapt();
		render();
	}
	
	public PropertyAssignmentElement() {
		
	}
	
	public void adapt(){
		getWidget().getElement().getStyle().setOpacity(propertyAssignmentContext.inherited ? 0.5 : 1);
		for(PropertyAssignmentInput input : getInputElements()){
			input.setPropertyAssignmentContext(propertyAssignmentContext);
			input.setEnabled(!propertyAssignmentContext.inherited);
		}
		for(AbstractMappingElementsProvider provider : getTypesOrPropertiesProviders()){
			provider.setPropertyAssignmentContext(propertyAssignmentContext);
		}
		handleAdaption();
	}
	
	public abstract void handleAdaption();

	public void render(){
		for(PropertyAssignmentInput input : getInputElements()){
			input.render();
		}
		handleRender();
	}
	
	public abstract void handleRender();
	
	public void dispose(){
		/*if(am != null && am.getEntities() != null && am.getEntities().size() > 0){
			for(GenericEntity entity : am.getEntities()){
				propertyAssignmentContext.session.listeners().entity(entity).remove(this);
				for(PropertyAssignmentInput input : getInputElements()){
					propertyAssignmentContext.session.listeners().entity(entity).remove(input);
				}				
			}
		} */
		for(PropertyAssignmentInput input : getInputElements()){
			input.dispose();
		}
		handleDisposal();
	}
	
	public abstract void handleDisposal();
	
	public void initialise(){
		getWidget().addDomHandler(this, ClickEvent.getType());
		if(propertyAssignmentContext.parentEntity != null){
			propertyAssignmentContext.session.listeners().entity(propertyAssignmentContext.parentEntity).add(this);
			for(PropertyAssignmentInput input : getInputElements()){
				propertyAssignmentContext.session.listeners().entity(propertyAssignmentContext.parentEntity).add(input);
			}
		}
		/*
		if(am == null && propertyAssignmentContext.parentEntity != null){
			am = AssemblyMonitoring.newInstance().build(propertyAssignmentContext.session, propertyAssignmentContext.parentEntity);
			am.addEntityMigrationListener(this);
//			am.addManpiulationListener(this);
			
			if(am.getEntities() != null && am.getEntities().size() > 0){
				for(GenericEntity entity : am.getEntities()){
					propertyAssignmentContext.session.listeners().entity(entity).add(this);
					for(PropertyAssignmentInput input : getInputElements()){
						propertyAssignmentContext.session.listeners().entity(entity).add(input);
					}					
				}
			}
		} */
		
		handleInitialisation();
	}
	
	public abstract void handleInitialisation();
	
	public abstract void validate();
	
	public abstract Set<PropertyAssignmentInput> getInputElements();
	
	public abstract Set<AbstractMappingElementsProvider> getTypesOrPropertiesProviders();
	
	public abstract Widget getWidget();
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		handleNoticeManipulation();
		adapt();
		render();
	}
	
//	@Override
//	public void onJoin(GenericEntity entity) {
//		if(entity instanceof QualifiedProperty){
//			propertyAssignmentContext.session.listeners().entity(entity).add(this);
//			for(PropertyAssignmentInput input : getInputElements()){
//				propertyAssignmentContext.session.listeners().entity(entity).add(input);
//			}
//		}
//	}
//	
//	@Override
//	public void onLeave(GenericEntity entity) {
//		propertyAssignmentContext.session.listeners().entity(entity).remove(this);
//		for(PropertyAssignmentInput input : getInputElements()){
//			propertyAssignmentContext.session.listeners().entity(entity).remove(input);
//		}		
//	}
	
	public abstract void handleNoticeManipulation();
	
	public Label getSeparatorElement(String label){
		Label se = new Label(label);
		se.addStyleName("separatorElement");
		return se;
	}
	
	@Override
	public void onClick(ClickEvent event) {
//		System.err.println("click propertyAssignment");
		propertyAssignmentContext.smartMapper.showDetails(propertyAssignmentContext.parentEntity);
	}
	
	public static PropertyAssignmentElement preparePropertyAssignmentElement(PropertyAssignmentContext pac){
		PropertyAssignmentElement propertyAssignmentElement = new MockupPropertyAssignmentElement();
		if(pac.parentEntity == null)
			propertyAssignmentElement = new MockupPropertyAssignmentElement();
		
		if(pac.parentEntity instanceof PropertyAsIs)
			propertyAssignmentElement = new PropertyAsIsElement();
		
		if(pac.parentEntity instanceof QualifiedPropertyAssignment)
			propertyAssignmentElement = new QualifiedPropertyAssignmentElement();
		
		if(pac.parentEntity instanceof KeyPropertyAssignment){
			propertyAssignmentElement = new KeyPropertyAssignmentElement(pac.parentEntity instanceof InverseKeyPropertyAssignment);
		}
		
		if(pac.parentEntity instanceof LinkPropertyAssignment)
			propertyAssignmentElement = new LinkPropertyAssignmentElement();
		
		if(pac.parentEntity instanceof OrderedLinkPropertyAssignment)
			propertyAssignmentElement = new OrderedLinkPropertyAssignmentElement();
		
		if(pac.parentEntity instanceof CompositeKeyPropertyAssignment)
			propertyAssignmentElement = new CompositeKeyPropertyAssignmentElement(false);
		
		if(pac.parentEntity instanceof CompositeInverseKeyPropertyAssignment)
			propertyAssignmentElement = new CompositeKeyPropertyAssignmentElement(true);
		
		propertyAssignmentElement.setPropertyAssignmentContext(pac);
		
		propertyAssignmentElement.getWidget().addStyleName("propertyAssignmentElement");
		
		return propertyAssignmentElement;
	}
	
	public String getTypeName(String typeSignature){
		return typeSignature.substring(typeSignature.lastIndexOf(".")+1, typeSignature.length());
	}
	
}
