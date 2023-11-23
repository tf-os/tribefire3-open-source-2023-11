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
package com.braintribe.gwt.templateeditor.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.Variable;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class AddManipulationRepresentation extends ManipulationRepresentation<AddManipulation> {

	private List<FlowPanel> keyElements = new ArrayList<>();
	private List<FlowPanel> valueElements = new ArrayList<>();
	private Map<Object, FlowPanel> variableToElements = new HashMap<>();
	private Map<FlowPanel, Object> elementsToVariable = new HashMap<>();
	
	@Override
	public Future<Widget> renderManipulation(final AddManipulation manipulation) {
		final Future<Widget> future = new Future<>();
	
		prepareOwnerElement(manipulation).andThen(result -> {
			FlowPanel manipulationEntry = result;
			
			FlowPanel delimiterElement = new FlowPanel();
			delimiterElement.setStyleName(DELIMITER_ELEMENT);
			delimiterElement.getElement().setInnerText(ADD_DELIMITER);
			
			manipulationEntry.add(delimiterElement);
			
			EntityType<GenericEntity> entityType = entity.entityType();
			CollectionType collectionType = entityType.getProperty(propertyName).getType().cast();
			boolean useKey = collectionType.getCollectionKind() == CollectionKind.list || collectionType.getCollectionKind() == CollectionKind.map;
			
			for(Object key : manipulation.getItemsToAdd().keySet()){
				if(useKey){
					final FlowPanel keyElement = new FlowPanel();
					keyElement.setStyleName(VALUE_ELMENT_CLASS);
					manipulationEntry.add(keyElement);
					keyElement.getElement().setInnerText(key.toString());
					
					manipulationEntry.add(keyElement);
					
					FlowPanel keyValueDelimiter = new FlowPanel();
					keyValueDelimiter.setStyleName(DELIMITER_ELEMENT);
					keyValueDelimiter.getElement().setInnerText(KEY_VALUE_DELIMITER);
					
					manipulationEntry.add(keyValueDelimiter);
					
					keyElements.add(keyElement);
				}
				
				final Object value = manipulation.getItemsToAdd().get(key);
				
				final FlowPanel valueElement = new FlowPanel();
				boolean isVariable = value instanceof Variable;
				valueElement.setStyleName(isVariable ? VARIABLE_ELMENT_CLASS : VALUE_ELMENT_CLASS);
				if(isVariable){
					valueElement.addDomHandler(event -> {
						setCurrentSelectedEntity((GenericEntity) elementsToVariable.get(valueElement));
						System.err.println(valueElement.getElement().getInnerText());
						event.preventDefault();
						event.stopPropagation();
					}, MouseDownEvent.getType());
				}
				valueElement.addDomHandler(event -> valueElement.addStyleName("selected"), MouseOverEvent.getType());
				valueElement.addDomHandler(event -> {
					Variable variable = (Variable) elementsToVariable.get(valueElement);
					if(!manipulationRepresentationListener.isElementSelected(variable))
						valueElement.removeStyleName("selected");
				},MouseOutEvent.getType());		
				String valueText = isVariable ? ((Variable)value).getName() : value.toString();
				valueElement.getElement().setInnerText(valueText);
				
				manipulationEntry.add(valueElement);
				
				valueElements.add(valueElement);
				if(value instanceof Variable){
					Variable variable = (Variable) value;
					session.listeners().entityProperty(variable, "name").add(AddManipulationRepresentation.this);
					manipulationRepresentationListener.putEntityElement(variable, ownerElement);
				}
				
				variableToElements.put(value, valueElement);
				elementsToVariable.put(valueElement, value);
				
				FlowPanel valuesDelimiter = new FlowPanel();
				valuesDelimiter.setStyleName(DELIMITER_ELEMENT);
				valuesDelimiter.getElement().setInnerText(VALUES_DELIMITER);
				
				manipulationEntry.add(valuesDelimiter);
			}
			//?
			manipulationEntry.remove(manipulationEntry.getWidgetCount()-1);
			future.onSuccess(manipulationEntry);
		}).onError(future::onFailure);
		
		return future;
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		ChangeValueManipulation changeValueManipulation = (ChangeValueManipulation) manipulation;
		LocalEntityProperty localEntityProperty = (LocalEntityProperty) changeValueManipulation.getOwner();
		
		GenericEntity candidate = localEntityProperty.getEntity();
		if(candidate == entity){
			EntityType<GenericEntity> entityType = entity.entityType();
			String ownerName = entity.toSelectiveInformation();
			Object id = entity.getId();
			if(ownerName == null || ownerName.equals("")) ownerName = entityType.getShortName() + " (" + id.toString() + ")";
			ownerElement.getElement().setInnerText(ownerName);
		}else if(candidate instanceof Variable){
			Variable variable = (Variable) candidate;
			FlowPanel valueElement = variableToElements.get(variable);
			valueElement.getElement().setInnerText(variable.getName());
		}		
	}
	
	@Override
	public void changeValue(Object oldValue, Object newValue) {
		boolean isVariable = newValue instanceof Variable;
		FlowPanel valueElement = variableToElements.get(oldValue);
		valueElement.setStyleName(isVariable ? VARIABLE_ELMENT_CLASS : VALUE_ELMENT_CLASS);
		String valueText = newValue != null ? (isVariable ? ((Variable)newValue).getName() : newValue.toString()) : "null";
		if(isVariable)
			session.listeners().entityProperty((GenericEntity) newValue, "name").add(this);
		valueElement.getElement().setInnerText(valueText);
		variableToElements.remove(oldValue);
		variableToElements.put(newValue, valueElement);
	}

	@Override
	public String getManipulationType() {
		return "Bulk Insert To Collection";
	}
}
