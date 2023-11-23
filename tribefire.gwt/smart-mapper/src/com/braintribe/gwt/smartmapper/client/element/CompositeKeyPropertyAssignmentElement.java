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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.gwt.smartmapper.client.PropertyAssignmentContext;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentInput;
import com.braintribe.gwt.smartmapper.client.action.ChangeCompositeKeyPropertyAssignmentAction;
import com.braintribe.gwt.smartmapper.client.experts.AbstractMappingElementsProvider;
import com.braintribe.gwt.smartmapper.client.util.TypeAndPropertyInfo;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;

public class CompositeKeyPropertyAssignmentElement extends PropertyAssignmentElement{

	protected boolean inverse = false;
	
	FlowPanel main;
	FlowPanel wrapper;
	Set<KeyPropertyAssignmentElement> keyPropertyAssignmentElements = new HashSet<>();
	FlowPanel buttonWrapper;
	ChangeCompositeKeyPropertyAssignmentAction addAssignmentAction;
	
	public CompositeKeyPropertyAssignmentElement(boolean inverse) {
		this.inverse = inverse;
	}
	
	@Override
	public void handleAdaption() {
		
		addAssignmentAction = new ChangeCompositeKeyPropertyAssignmentAction(false, inverse, propertyAssignmentContext.parentEntity);
		addAssignmentAction.setPropertyAssignmentContext(propertyAssignmentContext);
		getWrapper().clear();
		
		Set<? extends KeyPropertyAssignment> kpas;
		if(inverse){
			kpas = ((CompositeInverseKeyPropertyAssignment)propertyAssignmentContext.parentEntity).getInverseKeyPropertyAssignments();
		}else{
			kpas = ((CompositeKeyPropertyAssignment)propertyAssignmentContext.parentEntity).getKeyPropertyAssignments();
		}
		
		for(KeyPropertyAssignment kpa : kpas){
			
			PropertyAssignmentContext pac = preparePropertyAssignmentContext(propertyAssignmentContext, kpa);
			
			PropertyAssignmentElement pae = PropertyAssignmentElement.preparePropertyAssignmentElement(pac);
			
			pae.getWidget().removeStyleName("propertyAssignmentElement");
			
			ChangeCompositeKeyPropertyAssignmentAction removeAction = new ChangeCompositeKeyPropertyAssignmentAction(true, inverse, propertyAssignmentContext.parentEntity);
			removeAction.setPropertyAssignmentContext(pac);
			FlowPanel element = new FlowPanel();			
			element.add(pae.getWidget());
			TextButton remove = new TextButton("x");
			remove.addSelectHandler(event -> removeAction.perform(null));
			element.add(remove);
			element.getElement().setAttribute("style", "display:flex; justify-content: center; align-items: center");
			getWrapper().add(element);
			keyPropertyAssignmentElements.add((KeyPropertyAssignmentElement) pae);
		}
	}

	@Override
	public void handleRender() {
		//NOP
	}

	@Override
	public void handleDisposal() {
		//NOP
	}

	@Override
	public void handleInitialisation() {
		//NOP
	}
	
	@Override
	public void validate() {
		//NOP
	}

	@Override
	public Widget getWidget() {
		return getMain();
	}

	@Override
	public void handleNoticeManipulation() {
		//NOP
	}
	
	@Override
	public Set<PropertyAssignmentInput> getInputElements() {
		return Collections.emptySet();
//		Set<PropertyAssignmentInput> inputElements = new HashSet<PropertyAssignmentInput>();
//		for(KeyPropertyAssignmentElement kpae : keyPropertyAssignmentElements){
//			inputElements.addAll(kpae.getInputElements());
//		}
//		return inputElements;
	}
	
	@Override
	public Set<AbstractMappingElementsProvider> getTypesOrPropertiesProviders() {
		return Collections.emptySet();
	}
	
	public FlowPanel getMain() {
		if(main == null){
			main = new FlowPanel();
			main.addStyleName("flexColumn");
			main.add(getWrapper());
			main.add(getButtonWrapper());
		}
		return main;
	}
	
	public FlowPanel getWrapper() {
		if(wrapper == null){
			wrapper = new FlowPanel();
			wrapper.addStyleName("compositeElement");
		}
		return wrapper;
	}
	
	public FlowPanel getButtonWrapper() {
		if(buttonWrapper == null){
			buttonWrapper = new FlowPanel();
			
			buttonWrapper.getElement().setAttribute("style", "display:flex; justify-content: center; align-items: center");
			
			TextButton add = new TextButton("+");
			add.addSelectHandler(event -> addAssignmentAction.perform(null));
			buttonWrapper.add(add);
		}
		return buttonWrapper;
	}
	
	private PropertyAssignmentContext preparePropertyAssignmentContext(PropertyAssignmentContext parentContext, GenericEntity parentEntity){
		PropertyAssignmentContext pac = new PropertyAssignmentContext();
		
		pac.parentEntity = parentEntity;
		pac.propertyName = TypeAndPropertyInfo.getPropertyName(parentContext.parentProperty);
		pac.parentProperty = parentContext.parentProperty;
		
		pac.smartMapper = parentContext.smartMapper;
		pac.session = parentContext.session;
		pac.spotlightPanelProvider = parentContext.spotlightPanelProvider;
		pac.entityType = parentContext.entityType;		
		pac.mappedToEntityType = parentContext.mappedToEntityType;
		
		pac.inherited = parentContext.inherited;
		
		return pac;
	}

}
