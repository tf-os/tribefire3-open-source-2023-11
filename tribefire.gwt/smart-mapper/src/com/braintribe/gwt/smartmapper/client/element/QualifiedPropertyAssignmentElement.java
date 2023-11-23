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

import java.util.HashSet;
import java.util.Set;

import com.braintribe.gwt.smartmapper.client.PropertyAssignmentInput;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentPropertyInput;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentTypeInput;
import com.braintribe.gwt.smartmapper.client.experts.AbstractMappingElementsProvider;
import com.braintribe.gwt.smartmapper.client.experts.AbstractMappingElementsProvider.MappingElementKind;
import com.braintribe.gwt.smartmapper.client.experts.QualifiedPropertyElementsProvider;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class QualifiedPropertyAssignmentElement extends PropertyAssignmentElement{

	FlowPanel main;
	PropertyAssignmentTypeInput entityTypeInput;
	PropertyAssignmentPropertyInput propertyInput;	
	QualifiedPropertyElementsProvider propertiesProvider = new QualifiedPropertyElementsProvider(MappingElementKind.properties);
	QualifiedPropertyElementsProvider typesProvider = new QualifiedPropertyElementsProvider(MappingElementKind.types);
	
	@Override
	public void handleAdaption() {
		String entityType = propertyAssignmentContext.mappedToEntityType != null 
				? getTypeName(propertyAssignmentContext.mappedToEntityType.getTypeSignature()) : "???";
		getEntityTypeInput().getElement().setAttribute("placeholder", entityType);	
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
		if(inputElements == null){
			inputElements = new HashSet<>();
			inputElements.add(getEntityTypeInput());
			inputElements.add(getPropertyInput());
		}
		return inputElements;
	}
	
	@Override
	public Set<AbstractMappingElementsProvider> getTypesOrPropertiesProviders() {
		Set<AbstractMappingElementsProvider> providers = new HashSet<>();
		providers.add(propertiesProvider);
		providers.add(typesProvider);
		return providers;
	}
	
	public FlowPanel getMain() {
		if(main == null){
			main = new FlowPanel();
//			main.add(PropertyAssignmentTypeInput.clear(getEntityTypeInput()));
			main.add(getEntityTypeInput());
			main.add(getSeparatorElement("."));
			main.add(getPropertyInput());
//			main.add(PropertyAssignmentTypeInput.clear(getPropertyInput()));
			main.addStyleName("propertyAssignmentElementWrapper");
		}
		return main;
	}
	
	public PropertyAssignmentTypeInput getEntityTypeInput() {
		if(entityTypeInput == null){
			entityTypeInput = new PropertyAssignmentTypeInput();
			entityTypeInput.setPropertyNameOfAssignment("entityType");
			entityTypeInput.getElement().setAttribute("placeholder", "???");
			entityTypeInput.setTypesProvider(typesProvider);
		}
		return entityTypeInput;
	}
	
	public PropertyAssignmentPropertyInput getPropertyInput() {
		if(propertyInput == null){
			propertyInput = new PropertyAssignmentPropertyInput();
			propertyInput.setPropertyNameOfAssignment("property");
			propertyInput.getElement().setAttribute("placeholder", "property");
			propertyInput.setPropertiesProvider(propertiesProvider);
		}
		return propertyInput;
	}

}
