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

import com.braintribe.gwt.smartmapper.client.PropertyAssignmentAccessInput;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentInput;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentPropertyInput;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentTypeInput;
import com.braintribe.gwt.smartmapper.client.experts.AbstractMappingElementsProvider;
import com.braintribe.gwt.smartmapper.client.experts.AbstractMappingElementsProvider.MappingElementKind;
import com.braintribe.gwt.smartmapper.client.experts.LinkPropertyElementsProvider;
import com.braintribe.gwt.smartmapper.client.experts.LinkPropertyElementsProvider.Mode;
import com.braintribe.gwt.smartmapper.client.util.TypeAndPropertyInfo;
import com.braintribe.model.meta.GmEntityType;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class LinkPropertyAssignmentElement extends PropertyAssignmentElement{

	FlowPanel wrapper;
	FlowPanel keyPanel;
	FlowPanel otherKeyPanel;
	FlowPanel linkPanel;
	
	PropertyAssignmentTypeInput keyTypeInput;
	PropertyAssignmentPropertyInput keyPropertyInput;
	PropertyAssignmentTypeInput otherKeyTypeInput;
	PropertyAssignmentPropertyInput otherKeyPropertyInput;
	
	PropertyAssignmentTypeInput linkTypeInput;
	PropertyAssignmentPropertyInput linkPropertyInput;
	PropertyAssignmentTypeInput linkTypeInput2;
	PropertyAssignmentPropertyInput otherLinkPropertyInput;
	
	PropertyAssignmentAccessInput linkAccessInput;
	
	LinkPropertyElementsProvider keyTypeInputProvider = new LinkPropertyElementsProvider(MappingElementKind.types, Mode.key);
	LinkPropertyElementsProvider keyPropertyInputProvider = new LinkPropertyElementsProvider(MappingElementKind.properties, Mode.key);
	LinkPropertyElementsProvider otherKeyTypeInputProvider = new LinkPropertyElementsProvider(MappingElementKind.types, Mode.otherKey);
	LinkPropertyElementsProvider otherKeyPropertyProvider = new LinkPropertyElementsProvider(MappingElementKind.properties, Mode.otherKey);
	
	LinkPropertyElementsProvider linkTypeInputProvider = new LinkPropertyElementsProvider(MappingElementKind.types, Mode.linkKey);
	LinkPropertyElementsProvider linkPropertyInputProvider = new LinkPropertyElementsProvider(MappingElementKind.properties, Mode.linkKey);
	LinkPropertyElementsProvider linkTypeInputProvider2 = new LinkPropertyElementsProvider(MappingElementKind.properties, Mode.linkOtherKey);
	LinkPropertyElementsProvider otherLinkPropertyInputProvider = new LinkPropertyElementsProvider(MappingElementKind.properties, Mode.linkOtherKey);
	
	@Override
	public void handleAdaption() {
//		getKeyPropertyInput().getElement().setAttribute("placeholder", "entityType");
		
		GmEntityType entityType = TypeAndPropertyInfo.getMappedEntityTypeOfProperty(propertyAssignmentContext.smartMapper.cmdResolver, TypeAndPropertyInfo.getPropertyType(propertyAssignmentContext.parentProperty));
		String keyPropertyEntityType = entityType != null ? getTypeName(entityType.getTypeSignature()) : "???";		
		getOtherKeyTypeInput().getElement().setAttribute("placeholder", keyPropertyEntityType);
		
//		getPropertyInput().getElement().setAttribute("placeholder", propertyEntityType);
		
		String propertyEntityType = propertyAssignmentContext.mappedToEntityType != null 
				? getTypeName(propertyAssignmentContext.mappedToEntityType.getTypeSignature()) : "???";
		getKeyTypeInput().getElement().setAttribute("placeholder", propertyEntityType);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public Widget getWidget() {
		return getWrapper();
	}

	@Override
	public void handleNoticeManipulation() {
		//NOP
	}
	
	@Override
	public Set<PropertyAssignmentInput> getInputElements() {
		if(inputElements == null){
			inputElements = new HashSet<>();
			inputElements.add(getKeyTypeInput());
			inputElements.add(getKeyPropertyInput());
			inputElements.add(getOtherKeyTypeInput());
			inputElements.add(getOtherKeyPropertyInput());
			inputElements.add(getLinkTypeInput());
			inputElements.add(getLinkPropertyInput());
			inputElements.add(getLinkTypeInput2());
			inputElements.add(getOtherLinkPropertyInput());
			inputElements.add(getLinkAccessInput());
		}
		return inputElements;
	}

	@Override
	public Set<AbstractMappingElementsProvider> getTypesOrPropertiesProviders() {
		Set<AbstractMappingElementsProvider> providers = new HashSet<>();
		providers.add(keyTypeInputProvider);
		providers.add(keyPropertyInputProvider);
		providers.add(otherKeyTypeInputProvider);
		providers.add(otherKeyPropertyProvider);
		providers.add(linkTypeInputProvider);
		providers.add(linkPropertyInputProvider);
		providers.add(linkTypeInputProvider2);
		providers.add(otherLinkPropertyInputProvider);
		return providers;
	}
	
	public FlowPanel getWrapper() {
		if(wrapper == null){
			wrapper = new FlowPanel();
			wrapper.add(getKeyPanel());
			wrapper.add(getOtherKeyPanel());
			wrapper.add(getLinkPanel());
			wrapper.addStyleName("flexColumn");
		}
		return wrapper;
	}
	
	public FlowPanel getKeyPanel() {
		if(keyPanel == null){
			keyPanel = new FlowPanel();
//			keyPanel.add(PropertyAssignmentTypeInput.clear(getKeyTypeInput()));
			keyPanel.add(getKeyTypeInput());
//			keyPanel.add(PropertyAssignmentInput.clear(getKeyTypeInput()));
			keyPanel.add(getSeparatorElement("."));
			keyPanel.add(getKeyPropertyInput());
//			keyPanel.add(PropertyAssignmentTypeInput.clear(getKeyPropertyInput()));
			keyPanel.add(getSeparatorElement("="));
//			keyPanel.add(PropertyAssignmentTypeInput.clear(getLinkTypeInput()));
			keyPanel.add(getLinkTypeInput());
			keyPanel.add(getSeparatorElement("."));
			keyPanel.add(getLinkPropertyInput());
//			keyPanel.add(PropertyAssignmentTypeInput.clear(getLinkPropertyInput()));
			keyPanel.addStyleName("propertyAssignmentElementWrapper");
		}
		return keyPanel;
	}
	
	public FlowPanel getOtherKeyPanel() {
		if(otherKeyPanel == null){
			otherKeyPanel = new FlowPanel();
//			otherKeyPanel.add(PropertyAssignmentTypeInput.clear(getOtherKeyTypeInput()));
			otherKeyPanel.add(getOtherKeyTypeInput());
			otherKeyPanel.add(getSeparatorElement("."));
			otherKeyPanel.add(getOtherKeyPropertyInput());
//			otherKeyPanel.add(PropertyAssignmentTypeInput.clear(getOtherKeyPropertyInput()));
			otherKeyPanel.add(getSeparatorElement("="));
//			otherKeyPanel.add(PropertyAssignmentTypeInput.clear(getLinkTypeInput2()));
			otherKeyPanel.add(getLinkTypeInput2());
			otherKeyPanel.add(getSeparatorElement("."));
			otherKeyPanel.add(getOtherLinkPropertyInput());
//			otherKeyPanel.add(PropertyAssignmentTypeInput.clear(getOtherLinkPropertyInput()));
			otherKeyPanel.addStyleName("propertyAssignmentElementWrapper");
		}
		return otherKeyPanel;
	}
	
	public FlowPanel getLinkPanel() {
		if(linkPanel == null){
			linkPanel = new FlowPanel();
			linkPanel.add(getLinkAccessInput());
//			linkPanel.add(PropertyAssignmentTypeInput.clear(getLinkAccessInput()));
			linkPanel.addStyleName("propertyAssignmentElementWrapper");
		}
		return linkPanel;
	}
	
	public PropertyAssignmentTypeInput getKeyTypeInput() {
		if(keyTypeInput == null){
			keyTypeInput = new PropertyAssignmentTypeInput();
			keyTypeInput.setPropertyNameOfAssignment("key");
			keyTypeInput.getElement().setAttribute("placeholder", "???");
			keyTypeInput.setTypesProvider(keyTypeInputProvider);
		}
		return keyTypeInput;
	}
	
	public PropertyAssignmentPropertyInput getKeyPropertyInput() {
		if(keyPropertyInput == null){
			keyPropertyInput = new PropertyAssignmentPropertyInput();
			keyPropertyInput.setPropertyNameOfAssignment("key");
			keyPropertyInput.getElement().setAttribute("placeholder", "key");
			keyPropertyInput.setPropertiesProvider(keyPropertyInputProvider);
		}
		return keyPropertyInput;
	}
	
	public PropertyAssignmentTypeInput getOtherKeyTypeInput() {
		if(otherKeyTypeInput == null){
			otherKeyTypeInput = new PropertyAssignmentTypeInput();
			otherKeyTypeInput.setPropertyNameOfAssignment("otherKey");
			otherKeyTypeInput.getElement().setAttribute("placeholder", "???");
			otherKeyTypeInput.setTypesProvider(otherKeyTypeInputProvider);
		}
		return otherKeyTypeInput;
	}
	
	public PropertyAssignmentPropertyInput getOtherKeyPropertyInput() {
		if(otherKeyPropertyInput == null){
			otherKeyPropertyInput = new PropertyAssignmentPropertyInput();
			otherKeyPropertyInput.setPropertyNameOfAssignment("otherKey");
			otherKeyPropertyInput.getElement().setAttribute("placeholder", "otherKey");
			otherKeyPropertyInput.setPropertiesProvider(otherKeyPropertyProvider);
		}
		return otherKeyPropertyInput;
	}
	
	public PropertyAssignmentTypeInput getLinkTypeInput() {
		if(linkTypeInput == null){
			linkTypeInput = new PropertyAssignmentTypeInput();
			linkTypeInput.setPropertyNameOfAssignment("linkEntityType");
			linkTypeInput.getElement().setAttribute("placeholder", "???");
//			linkTypeInput.setTypesProvider(linkTypeInputProvider);
		}
		return linkTypeInput;
	}
	
	public PropertyAssignmentPropertyInput getLinkPropertyInput() {
		if(linkPropertyInput == null){
			linkPropertyInput = new PropertyAssignmentPropertyInput();
			linkPropertyInput.setPropertyNameOfAssignment("linkKey");
			linkPropertyInput.getElement().setAttribute("placeholder", "linkKey");
			linkPropertyInput.setPropertiesProvider(linkPropertyInputProvider);
		}
		return linkPropertyInput;
	}
	
	public PropertyAssignmentTypeInput getLinkTypeInput2() {
		if(linkTypeInput2 == null){
			linkTypeInput2 = new PropertyAssignmentTypeInput();
			linkTypeInput2.setPropertyNameOfAssignment("linkEntityType");
			linkTypeInput2.getElement().setAttribute("placeholder", "???");
//			linkTypeInput2.setTypesProvider(linkTypeInputProvider2);
		}
		return linkTypeInput2;
	}
	
	public PropertyAssignmentPropertyInput getOtherLinkPropertyInput() {
		if(otherLinkPropertyInput == null){
			otherLinkPropertyInput = new PropertyAssignmentPropertyInput();
			otherLinkPropertyInput.setPropertyNameOfAssignment("linkOtherKey");
			otherLinkPropertyInput.getElement().setAttribute("placeholder", "linkOtherKey");
			otherLinkPropertyInput.setPropertiesProvider(otherLinkPropertyInputProvider);
		}
		return otherLinkPropertyInput;		
	}
	
	public PropertyAssignmentAccessInput getLinkAccessInput() {
		if(linkAccessInput == null){
			linkAccessInput = new PropertyAssignmentAccessInput();
			linkAccessInput.setPropertyNameOfAssignment("linkAccess");
			linkAccessInput.getElement().setAttribute("placeholder", "linkAccess");
		}
		return linkAccessInput;
	}

}
