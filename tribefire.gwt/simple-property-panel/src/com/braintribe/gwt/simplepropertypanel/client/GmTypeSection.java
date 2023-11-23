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

import java.util.HashSet;
import java.util.Set;

import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.simplepropertypanel.client.resources.LocalizedText;
import com.braintribe.gwt.simplepropertypanel.client.validation.GmTypeValidator;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class GmTypeSection extends FlowPanel implements ManipulationListener/*, DisposableBean*/ {
	
	boolean readOnly = UrlParameters.getInstance().getParameter("readOnly") != null || UrlParameters.getInstance().getParameter("offline") != null;
	private final String STRING_TYPE = "type:string";
	
	private boolean editable = false;
	private boolean visible = true;
	private boolean hasComplexProperties = false;
	private GmType type;
	
	private FlowPanel expandWrapper;
	private Image expandIcon;
	private TextBox typeName;
	private TextBox typePackage;
	private FlowPanel simpleProperties;
	private Label complexPropertiesTitle;
	private FlowPanel complexProperties;
	private Button add;
	private GenericEntity elementToSelect;
	
	private FlowPanel abstractWrapper;
	//private FlowPanel complexPropertiesWrapper;
	private CheckBox abstractCheckBox;
	private CheckBox showComplexPropertiesCheckBox;
	
	private Set<SimplePropertySection> simplePropertySections = new HashSet<>();
	private PersistenceGmSession session;
	private SimplePropertyPanel parentPanel;
		
	public GmTypeSection(boolean editable, boolean main) {		
		this.editable = editable;
		visible = main;
		
		addStyleName("typeSection");
		
		expandWrapper = new FlowPanel();
		expandWrapper.addStyleName("typeNameWrapper");
		
		expandIcon = new Image(visible ? PropertyPanelResources.INSTANCE.expandedArrow() : PropertyPanelResources.INSTANCE.collapsedArrow());
		
		expandIcon.addClickHandler(event -> {
			visible = !visible;
			getTypePackage().setVisible(visible);
			getSimpleProperties().setVisible(visible);
			getComplexPropertiesTitle().setVisible(visible && hasComplexProperties);
			getComplexProperties().setVisible(visible && hasComplexProperties);
			if (type != null && type.isEntity() && editable && !readOnly)
				abstractWrapper.setVisible(visible);
			expandIcon.setUrl(visible ? PropertyPanelResources.INSTANCE.expandedArrow().getSafeUri().asString() : PropertyPanelResources.INSTANCE.collapsedArrow().getSafeUri().asString());
		});
		
		expandWrapper.add(expandIcon);
		expandWrapper.add(getTypeName());
		
		add(expandWrapper);
		add(getTypePackage());
		
		if (editable && !readOnly) {
			abstractWrapper = new FlowPanel();
			abstractWrapper.addStyleName("abstractWrapper");			
			abstractWrapper.add(getAbstractCheckBox());
			abstractWrapper.add(new Label(LocalizedText.INSTANCE.abstractText()));
			add(abstractWrapper);
		}
		
//		complexPropertiesWrapper = new FlowPanel();
//		complexPropertiesWrapper.addStyleName("abstractWrapper");			
//		complexPropertiesWrapper.add(getShowComplexPropertiesCheckBox());
//		complexPropertiesWrapper.add(new Label("Show complex properties"));
//		add(complexPropertiesWrapper);
		
		add(getSimpleProperties());
		
		add(getComplexPropertiesTitle());
		getComplexPropertiesTitle().setVisible(false);
		add(getComplexProperties());
		getComplexProperties().setVisible(false);
//		if(editable && !readOnly){
//			FlowPanel buttonWrapperPanel = new FlowPanel();
//			buttonWrapperPanel.addStyleName("simplePropertyPanelButtonWrapper");
//			
//			buttonWrapperPanel.add(getAdd());
//			
//			getTypeProperties().add(buttonWrapperPanel);
//		}
			
	}
	
	public void setParentPanel(SimplePropertyPanel parentPanel) {
		this.parentPanel = parentPanel;
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public CheckBox getAbstractCheckBox() {
		if(abstractCheckBox == null) {
			abstractCheckBox = new CheckBox();
			abstractCheckBox.addStyleName("gmModellerDefaultCheckBox");
			abstractCheckBox.addValueChangeHandler(event -> {
				((GmEntityType) type).setIsAbstract(event.getValue());
				if (parentPanel != null)
					parentPanel.handleAutoCommit();
			});
		}
		return abstractCheckBox;
	}
	
	public CheckBox getShowComplexPropertiesCheckBox() {
		if(showComplexPropertiesCheckBox == null) {
			showComplexPropertiesCheckBox = new CheckBox();
			showComplexPropertiesCheckBox.addStyleName("gmModellerDefaultCheckBox");
			/*showComplexPropertiesCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {				
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					
				}
			});*/
		}
		return showComplexPropertiesCheckBox;
	}
	
	public Button getAdd() {
		if (add == null) {
			add = new Button(LocalizedText.INSTANCE.addProperty());
			add.addStyleName("simplePropertyPanelButton");
			add.addClickHandler(event -> {
				if (type.isGmEntity())
					addProperty();
				else if (type.isGmEnum())
					addConstant();
			});
		}
		return add;
	}
	
	public TextBox getTypeName() {
		if (typeName == null) {
			typeName = new TextBox();
			typeName.addStyleName("typeName");
			typeName.setEnabled(editable && !readOnly);	
			typeName.addChangeHandler(event -> changeTypeSignature(typePackage.getValue(), typeName.getValue()));
		}
		return typeName;
	}
	
	public TextBox getTypePackage() {
		if (typePackage == null) {
			typePackage = new TextBox();
			typePackage.addStyleName("typePackage");
			typePackage.setEnabled(editable && !readOnly);
			typePackage.setVisible(visible);
			typePackage.addChangeHandler(event -> changeTypeSignature(typePackage.getValue(), typeName.getValue()));
		}
		return typePackage;
	}
	
	public FlowPanel getSimpleProperties() {
		if(simpleProperties == null){
			simpleProperties = new FlowPanel();
			simpleProperties.addStyleName("typeProperties");
			simpleProperties.setVisible(visible);
//			typeProperties.addStyleName("clickable");
		}
		return simpleProperties;
	}
	
	public Label getComplexPropertiesTitle() {
		if (complexPropertiesTitle == null) {
			complexPropertiesTitle = new Label();
			complexPropertiesTitle.addStyleName("complexPropertiesTitle");
			complexPropertiesTitle.setVisible(visible);
			
			complexPropertiesTitle.addDomHandler(event -> {
				boolean isVisible = getComplexProperties().isVisible();
				getComplexProperties().setVisible(!isVisible);
			}, ClickEvent.getType());
			
			complexPropertiesTitle.setText(LocalizedText.INSTANCE.relations());
		}
		return complexPropertiesTitle;
	}
	
	public FlowPanel getComplexProperties() {
		if(complexProperties == null) {
			complexProperties = new FlowPanel();
			complexProperties.addStyleName("typeProperties");
			complexProperties.setVisible(visible);
		}
		return complexProperties;
	}
	
	public void setType(GmType type) {
		if(this.type != null){
			if(this.type != type){
				session.listeners().entity(this.type).remove(this);
				this.type = null;
			}
		}
		
		if(this.type == null){			
			this.type = type;
			session.listeners().entity(this.type).add(this);
		}		
		
		if(this.type.isGmEntity())
			handleEntityType((GmEntityType) this.type);
		else if(this.type.isGmEnum())
			handleEnumType((GmEnumType) this.type);
	}
	
	protected void setName(GmType type){
		String name = GmTypeRendering.getTypeName(type.getTypeSignature());
		getTypeName().setValue(name);
		getTypeName().setTitle(name);
		
		String packaging = GmTypeRendering.getTypePackage(type.getTypeSignature());
		getTypePackage().setValue(packaging);
		getTypePackage().setTitle(packaging);
	}
	
	protected void handleEntityType(GmEntityType entityType){
		setName(entityType);
		
		if(abstractWrapper != null) abstractWrapper.setVisible(true);
		getAbstractCheckBox().setValue(entityType.getIsAbstract(), false);
		
		getSimpleProperties().clear();
		getComplexProperties().clear();
		simplePropertySections.clear();
		
		if(entityType.getProperties() != null) {
			for(GmProperty property : entityType.getProperties()){
				boolean simple = isSimpleProperty(property.getType());
				if(isSimpleProperty(property.getType())){
					
					final SimplePropertySection simplePropertySection = new SimplePropertySection(editable, true, simple);
					simplePropertySection.setSession(session);
					simplePropertySection.setParentPanel(parentPanel);
					simplePropertySection.setElement(property);				
					simplePropertySections.add(simplePropertySection);
					
					getSimpleProperties().add(simplePropertySection);
					
					if(property == elementToSelect){
						simplePropertySection.selectValue();
						elementToSelect = null;					
					}
				}
			}
		}
		
		if(editable && !readOnly){
			FlowPanel buttonWrapperPanel = new FlowPanel();
			buttonWrapperPanel.addStyleName("simplePropertyPanelButtonWrapper");
			
			buttonWrapperPanel.add(getAdd());
			
			getSimpleProperties().add(buttonWrapperPanel);
		}
		
		getComplexPropertiesTitle().setVisible(false);
		getComplexProperties().setVisible(false);
		
		if(entityType.getProperties() != null) {
			for(GmProperty property : entityType.getProperties()){
				boolean simple = isComplexProperty(property.getType());
				if(isComplexProperty(property.getType())){
					hasComplexProperties = true;
					getComplexPropertiesTitle().setVisible(visible);
					getComplexProperties().setVisible(visible);
					
					final SimplePropertySection simplePropertySection = new SimplePropertySection(editable, true, simple);
					simplePropertySection.setSession(session);
					simplePropertySection.setParentPanel(parentPanel);
					simplePropertySection.setElement(property);				
					simplePropertySections.add(simplePropertySection);
					
					getComplexProperties().add(simplePropertySection);
				}
			}
		}
	}
	
	private boolean isSimpleProperty(GmType gmType){
//		return true;
		
		if(gmType instanceof GmSimpleType || gmType instanceof GmBaseType)
			return true;
		else if(gmType instanceof GmLinearCollectionType){
			return isSimpleProperty(((GmLinearCollectionType) gmType).getElementType());
		}else if(gmType instanceof GmMapType){
			return isSimpleProperty(((GmMapType) gmType).getKeyType()) && isSimpleProperty(((GmMapType) gmType).getValueType());
		}
		return false;
		
	}
	
	private boolean isComplexProperty(GmType gmType){
//		return true;
		
		if(gmType instanceof GmEnumType || gmType instanceof GmEntityType)
			return true;
		else if(gmType instanceof GmLinearCollectionType){
			return isComplexProperty(((GmLinearCollectionType) gmType).getElementType());
		}else if(gmType instanceof GmMapType){
			return isComplexProperty(((GmMapType) gmType).getKeyType()) && isSimpleProperty(((GmMapType) gmType).getValueType());
		}
		return false;
		
	}

	protected void handleEnumType(GmEnumType enumType){
		setName(enumType);
		if(abstractWrapper != null) abstractWrapper.setVisible(false);
		
		getSimpleProperties().clear();
		getComplexProperties().clear();
		simplePropertySections.clear();
		
		getComplexPropertiesTitle().setVisible(false);
		getComplexProperties().setVisible(false);		
		
		if(enumType.getConstants() != null) {
			for(GmEnumConstant constant : enumType.getConstants()){				
				final SimplePropertySection simplePropertySection = new SimplePropertySection(editable, true, true);
				simplePropertySection.setSession(session);
				simplePropertySection.setParentPanel(parentPanel);
				simplePropertySection.setElement(constant);				
				simplePropertySections.add(simplePropertySection);
				
				getSimpleProperties().add(simplePropertySection);
				
				if(constant == elementToSelect){
					simplePropertySection.selectValue();
					elementToSelect = null;					
				}			
			}
		}
		
		if(editable && !readOnly){
			FlowPanel buttonWrapperPanel = new FlowPanel();
			buttonWrapperPanel.addStyleName("simplePropertyPanelButtonWrapper");
			
			buttonWrapperPanel.add(getAdd());
			
			getSimpleProperties().add(buttonWrapperPanel);
		}
	}
	
	private void changeTypeSignature(String packageName, String typeName){
		boolean handleCommit = false;
		String newName = packageName + "." + typeName;
		if (GmTypeValidator.isValidTypeName(typeName)) {
			this.type.setTypeSignature(newName);
			handleCommit = true;
		} else {
			if (this.type.isGmEntity()) {
				handleEntityType((GmEntityType) this.type);
				handleCommit = true;
			} else if(this.type.isGmEnum()) {
				handleEnumType((GmEnumType) this.type);
				handleCommit = true;
			}
		}

		if (handleCommit && parentPanel != null)
			parentPanel.handleAutoCommit();
	}
	
	int count = 0;
	
	public void addProperty(){
		GmEntityType entityType = (GmEntityType)type;
		
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		
		GmProperty newProperty = session.create(GmProperty.T);
		newProperty.setDeclaringType(entityType);
		newProperty.setName("newProperty" + count++);
		
		GmType stringType = session.findEntityByGlobalId(STRING_TYPE);
		
		newProperty.setType(stringType);		
		
		elementToSelect = newProperty;
		
		entityType.getProperties().add(newProperty);
		
		nt.commit();
		
		if (parentPanel != null)
			parentPanel.handleAutoCommit();
	}
	
	public void addConstant(){
		GmEnumType enumType = (GmEnumType)type;
		
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		
		GmEnumConstant newConstant = session.create(GmEnumConstant.T);
		newConstant.setDeclaringType(enumType);
		newConstant.setName("newConstant");
		
		elementToSelect = newConstant;
		
		enumType.getConstants().add(newConstant);
		
		nt.commit();
		
		if (parentPanel != null)
			parentPanel.handleAutoCommit();
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if(this.type.isGmEntity())
			handleEntityType((GmEntityType) this.type);
		else if(this.type.isGmEnum())
			handleEnumType((GmEnumType) this.type);
		
		parentPanel.validate();
	}
	
	/*
	@Override
	public void disposeBean() throws Exception {
	}*/	

}
