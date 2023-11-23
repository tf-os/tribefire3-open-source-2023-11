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
package com.braintribe.gwt.simplepropertypanel.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.vectomatic.dom.svg.ui.SVGResource;

import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.simplepropertypanel.client.validation.GmTypeValidator;
import com.braintribe.gwt.utils.client.FastSet;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.sencha.gxt.core.client.Style.HideMode;
import com.sencha.gxt.widget.core.client.button.TextButton;

public class SimplePropertySection extends FlowPanel implements ManipulationListener/*, DisposableBean*/ {
	boolean readOnly = UrlParameters.getInstance().getParameter("readOnly") != null || UrlParameters.getInstance().getParameter("offline") != null;
	
	private boolean editable = false;
	private boolean simple = true;
	private GenericEntity propertyOrConstant;
	
	private TextBox valueInput;
	private FlowPanel propertyCardinality;
	private FlowPanel keyType;
	private FlowPanel valueType;
	private TextButton remove;
	private SimplePropertyPanelActionMenu simplePropertyPanelActionMenu = new SimplePropertyPanelActionMenu();
	
	private PersistenceGmSession session;
	private SimplePropertyPanel parentPanel;
	
	public SimplePropertySection(boolean editable, boolean property, boolean simple) {		
		this.editable = editable;
		this.simple = simple;
		
		addStyleName("typeProperty");
		addStyleName("clickable");
			
		add(getValueInput());
		
		if(property){	
			add(getPropertyCardinality());
			add(getKeyType());
			add(getValueType());
		}
		
		if(editable)
			add(getRemove());
		
		if(!simple) {
			addDomHandler(event -> {
				System.err.println("fireOut");
				parentPanel.deselectProperty();
			}, MouseOutEvent.getType());
			
			addDomHandler(event -> {
				System.err.println("fireOver");
				parentPanel.selectProperty();
			}, MouseOverEvent.getType());
		}
	}
	
	public void setParentPanel(SimplePropertyPanel parentPanel) {
		this.parentPanel = parentPanel;
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
		simplePropertyPanelActionMenu.setSession(session);
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public TextBox getValueInput() {
		if (valueInput != null)
			return valueInput;
		
		valueInput = new TextBox();
		valueInput.addStyleName("propertyName");
		valueInput.setEnabled(editable && !readOnly);
		valueInput.getElement().setAttribute("placeholder", "propertyName");
		valueInput.addChangeHandler(event -> changeValue(valueInput.getValue()));
		
		return valueInput;
	}
	
	public FlowPanel getPropertyCardinality() {
		if (propertyCardinality != null)
			return propertyCardinality;
		
		propertyCardinality = new FlowPanel();
		propertyCardinality.addStyleName("propertyCardinality");
		
		if (editable && !readOnly && simple)
			propertyCardinality.addDomHandler(event -> simplePropertyPanelActionMenu.showCardinalities(this, propertyCardinality), ClickEvent.getType());
		
		return propertyCardinality;
	}
	
	public FlowPanel getKeyType() {
		if (keyType != null)
			return keyType;
		
		keyType = new FlowPanel();
		keyType.addStyleName("propertyType");
		
		if (editable && !readOnly && simple)
			keyType.addDomHandler(event -> simplePropertyPanelActionMenu.showSimpleTypes(this, keyType, true), ClickEvent.getType());
		
		return keyType;
	}
	
	public FlowPanel getValueType() {
		if (valueType != null)
			return valueType;
		
		valueType = new FlowPanel();
		valueType.addStyleName("propertyType");
		valueType.setVisible(false);
		
		if (editable && !readOnly && simple)
			valueType.addDomHandler(event -> simplePropertyPanelActionMenu.showSimpleTypes(this, valueType, false), ClickEvent.getType());
		
		return valueType;
	}
	
	public TextButton getRemove() {
		if (remove != null)
			return remove;
		
		remove = new TextButton("x");			
		remove.setHideMode(HideMode.VISIBILITY);
		remove.setVisible(editable && !readOnly);
		remove.addSelectHandler(event -> removeElement());
		
		return remove;
	}
	
	public void setElement(GenericEntity propertyOrConstant) {
		if (this.propertyOrConstant != null && this.propertyOrConstant != propertyOrConstant) {
			session.listeners().entity(this.propertyOrConstant).remove(this);
			this.propertyOrConstant = null;
		}
		
		if (this.propertyOrConstant == null){			
			this.propertyOrConstant = propertyOrConstant;
			session.listeners().entity(this.propertyOrConstant).add(this);
		}
		
		if (this.propertyOrConstant instanceof GmProperty)
			handleProperty();
		else if (this.propertyOrConstant instanceof GmEnumConstant)
			handleConstant();
	}
	
	public GmTypeKind getCurrentCollectionTypeKind(){
//		if(getPropertyCardinality().getElement().getInnerText().equals("SINGLE"))
//			return null;
//		else
//			return GmTypeKind.valueOf(getPropertyCardinality().getElement().getInnerText());
		GmProperty property = (GmProperty) propertyOrConstant;
		
		if (property.getType() instanceof GmListType)
			return GmTypeKind.LIST;
		if (property.getType() instanceof GmSetType)
			return GmTypeKind.SET;
		if (property.getType() instanceof GmMapType)
			return GmTypeKind.MAP;
		
		return null;
	}
	
	public GmTypeKind getCurrentKeyType(){
		try{
			String keyType = getValueType().getElement().getInnerText();
//			if(keyType.equals("object"))
//				keyType = "base";
			return GmTypeKind.valueOf(keyType.toUpperCase());
		}catch(Exception ex){
			return GmTypeKind.STRING;
		}
	}
	
	public GmTypeKind getCurrentValueType(){
		try{
			String valueType = getValueType().getElement().getInnerText();
//			if(valueType.equals("object"))
//				valueType = "base";
			return GmTypeKind.valueOf(valueType.toUpperCase());
		}catch(Exception ex){
			return GmTypeKind.STRING;
		}
	}
	
	public GmProperty getProperty() {
		return (GmProperty) propertyOrConstant;
	}
	
	protected void handleProperty(){
		if (propertyOrConstant == null)
			return;
		
		GmProperty property = (GmProperty) propertyOrConstant;
		
		getValueInput().setValue(property.getName(), false);			
		
//		getPropertyCardinality().add();
		
		getPropertyCardinality().clear();
		String cardinality = GmTypeRendering.getCardinality(property.getType());
		if(cardinality != null){
			SVGResource icon = simplePropertyPanelActionMenu.getIcon(cardinality);
			getPropertyCardinality().add(new HTML(icon.getSvg().getElement().getString()));
		}
//		getPropertyCardinality().getElement().setInnerText(GmTypeRendering.getCardinality(property.getType()));
		
		getKeyType().getElement().setInnerText(GmTypeRendering.getPropertyType(property.getType(), true));
		
		getValueType().setVisible(property.getType() instanceof GmMapType);
		getValueType().getElement().setInnerText(GmTypeRendering.getPropertyType(property.getType(), false));
		
		parentPanel.validate();
	}
	
	protected void handleConstant(){
		if (propertyOrConstant == null)
			return;
		
		GmEnumConstant constant = (GmEnumConstant) propertyOrConstant;
		getValueInput().setValue(constant.getName(), false);	
		parentPanel.validate();
	}
	
	protected void changeValue(String newValue){
		if(this.propertyOrConstant instanceof GmProperty)
			changePropertyName(newValue);
		else if(this.propertyOrConstant instanceof GmEnumConstant)
			changeConstant(newValue);
	}
	
	protected void changePropertyName(String newPropertyName){
		if (propertyOrConstant == null)
			return;
		
		GmProperty property = (GmProperty) propertyOrConstant;
		if (newPropertyName != null && !newPropertyName.isEmpty() && !hasDuplicates(newPropertyName) && GmTypeValidator.isValidPropertyName(newPropertyName)) {
			property.setName(newPropertyName);
			handleAutoCommit();
		} else
			handleProperty();
	}
	
	protected void handleAutoCommit() {
		if (parentPanel != null)
			parentPanel.handleAutoCommit();
	}

	protected void changeConstant(String newConstant){
		if (propertyOrConstant == null)
			return;
		
		GmEnumConstant constant = (GmEnumConstant) propertyOrConstant;
		if (newConstant != null && !newConstant.isEmpty() && GmTypeValidator.isValidPropertyName(newConstant)) {
			constant.setName(newConstant);
			handleAutoCommit();
		} else
			handleConstant();
	}
	
	public void removeElement(){
		if(this.propertyOrConstant instanceof GmProperty)
			removeProperty();
		else if(this.propertyOrConstant instanceof GmEnumConstant)
			removeConstant();
		
		handleAutoCommit();
	}
	
	public void removeProperty(){
		GmProperty property = (GmProperty) propertyOrConstant;
		GmEntityType entityType = property.getDeclaringType();
		
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		entityType.getProperties().remove(property);
		nt.commit();
	}
	
	public void removeConstant(){
		GmEnumConstant constant = (GmEnumConstant) propertyOrConstant;
		GmEnumType enumType = constant.getDeclaringType();
		
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		enumType.getConstants().remove(constant);
		nt.commit();
	}
	
	public void selectValue(){
		getValueInput().setFocus(true);
		Scheduler.get().scheduleFixedDelay(() -> {
			getValueInput().selectAll();
			return false;
		}, 50);
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if(propertyOrConstant instanceof GmProperty)
			handleProperty();
		else if(propertyOrConstant instanceof GmEnumConstant)
			handleConstant();
	}
	
	/*
	@Override
	public void disposeBean() throws Exception {
		
	}*/
	
	private boolean hasDuplicates(String value){
		if (!(propertyOrConstant instanceof GmProperty))
			return false;
		
		GmProperty gmProperty = (GmProperty) propertyOrConstant;
		for (GmProperty property : getAllProperties(gmProperty.getDeclaringType(), false)) {
			if (property.getName().equalsIgnoreCase(value))
				return true;
		}
		
		return false;
	}	
	private Set<String> tfReservedProperties = new FastSet(Arrays.asList("id", "globalid", "partition"));
	
	/**
	 * @param isSuperType - currently not used
	 */
	public List<GmProperty> getAllProperties(GmEntityType entityType, boolean isSuperType) {		
		List<GmProperty> gmProperties = new ArrayList<>();
//		if(isSuperType)
//			gmProperties.addAll(entityType.getProperties());
//		else{
		if(entityType.getProperties() != null) {
			gmProperties.addAll(entityType.getProperties().stream().filter((property) -> {
				return !tfReservedProperties.contains(property.getName().toLowerCase());
			}).collect(Collectors.toList()));
		}
//		}
		if(entityType.getSuperTypes() != null){
			for(GmEntityType superType : entityType.getSuperTypes()){
				gmProperties.addAll(getAllProperties(superType, true));
			}
		}
		return gmProperties;
	}

}
