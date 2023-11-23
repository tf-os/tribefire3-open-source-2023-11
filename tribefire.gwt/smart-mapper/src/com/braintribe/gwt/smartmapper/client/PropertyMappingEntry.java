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
package com.braintribe.gwt.smartmapper.client;

import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelResources;
import com.braintribe.gwt.smartmapper.client.element.PropertyAssignmentElement;
import com.braintribe.gwt.smartmapper.client.util.ConversionRendering;
import com.braintribe.gwt.smartmapper.client.util.TypeAndPropertyInfo;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.cmd.extended.PropertyMdDescriptor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.HideMode;
import com.sencha.gxt.widget.core.client.button.TextButton;

public class PropertyMappingEntry extends FlowPanel implements ManipulationListener{
	
	private final boolean readOnly = UrlParameters.getInstance().getParameter("readOnly") != null;
	
	private GmPropertyInfo parentProperty;
	private PropertyAssignmentContext pac;
	
	private Label propertyNameLabel;
	private Label propertyTypeLabel;
	private Label conversion;
	private TextButton actionMenuButton;
	
	private PropertyAssignmentElement propertyAssignmentElement;

	public PropertyMappingEntry() {
		addStyleName("propertyAssignmentEntry");
		
		FlowPanel propertyInfo = new FlowPanel();
		propertyInfo.addStyleName("propertyInfo");
		propertyInfo.add(getPropertyNameLabel());
		propertyInfo.add(getPropertyTypeLabel());
		
		propertyInfo.addDomHandler(event -> {
			System.err.println("click property");
			pac.smartMapper.showDetails(parentProperty);
		}, ClickEvent.getType());
		
		add(propertyInfo);
		
		FlowPanel middleSecionWrapper = new FlowPanel();
		middleSecionWrapper.addStyleName("propertyAssignmentEntryMiddleSectionWrapper");
		
		FlowPanel middleSecion = new FlowPanel();
		middleSecion.addStyleName("propertyAssignmentEntryMiddleSection");
		middleSecion.add(getPlaceHolder());
		middleSecion.add(getConversionElement());
		middleSecion.add(getPlaceHolder());
			
		middleSecionWrapper.add(middleSecion);
		
		middleSecionWrapper.addDomHandler(event -> System.err.println("click conversion"), ClickEvent.getType());
		
		add(middleSecionWrapper);
//		add(getPropertyAssignmentElement().getWidget(), new HorizontalLayoutData(-1, -1));
		if(!readOnly){
			add(getActionMenuButton());
			
			addDomHandler(event -> getActionMenuButton().setVisible(true), MouseMoveEvent.getType());
			addDomHandler(event -> getActionMenuButton().setVisible(false), MouseOutEvent.getType());
			addDomHandler(event -> getActionMenuButton().setVisible(false), MouseOverEvent.getType());
		}
	}
	
	public void setPropertyAssignmentContext(PropertyAssignmentContext pac) {
		this.pac = pac;
		this.parentProperty = pac.parentProperty != null ? pac.parentProperty : TypeAndPropertyInfo.getProperty(pac.entityType, pac.propertyName);
//		pac.smartMapper.modelMetaDataEditor.onEntityType(TypeAndPropertyInfo.getTypeSignature(pac.entityType)).configure(pac.propertyName, (propertyInfo) -> {
//			this.parentProperty = propertyInfo;
//		});
		
		if(parentProperty != null)
			pac.session.listeners().entity(parentProperty).add(this);
		removePropertyAssignmentElement();
		addPropertyAssignmentElement();
		render();
	}
	
	public PropertyAssignmentContext getPropertyAssignmentContext(){
		return pac;
	}
	
	public PropertyAssignmentElement getPropertyAssignmentElement() {
		if(this.parentProperty != null){
			
			PropertyMdDescriptor mdd = pac.smartMapper.cmdResolver
					.entityTypeSignature(TypeAndPropertyInfo.getTypeSignature(pac.entityType))
					.property(TypeAndPropertyInfo.getPropertyName(this.parentProperty))
					.meta(PropertyAssignment.T).exclusiveExtended();
		
			if(mdd != null) {
				pac.parentEntity = mdd.getResolvedValue();
				pac.inherited = mdd.isInherited();	
			}else {
				pac.parentEntity = null;
				pac.inherited = false;
			}
			
			if(pac.parentEntity == null)
				pac.parentEntity = pac.smartMapper.fallbackResolving(TypeAndPropertyInfo.getTypeSignature(pac.entityType), TypeAndPropertyInfo.getPropertyName(this.parentProperty));
		}
		
		propertyAssignmentElement = PropertyAssignmentElement.preparePropertyAssignmentElement(pac);
		
		return propertyAssignmentElement;
	}
	
	public Label getPropertyNameLabel() {
		if(propertyNameLabel == null){
			propertyNameLabel = new Label();
			propertyNameLabel.addStyleName("assignedPropertyName");
		}
		return propertyNameLabel;
	}
	
	public Label getPropertyTypeLabel() {
		if(propertyTypeLabel == null){
			propertyTypeLabel = new Label();
			propertyTypeLabel.addStyleName("assignedPropertyType");
		}
		return propertyTypeLabel;
	}
	
	private Label getConversionElement(){	
		if(conversion == null){
			conversion = new Label();
			conversion.addStyleName("propertyMappingConversion");
			conversion.setText("?");
		}
		return conversion;
	}
	
	private Widget getPlaceHolder(){	
		FlowPanel wrapper = new FlowPanel();
		wrapper.addStyleName("propertyMappingPlaceHolderWrapper");
		FlowPanel fp = new FlowPanel();
		fp.addStyleName("propertyMappingPlaceHolder");
		wrapper.add(fp);
		return wrapper;
	}
	
	public TextButton getActionMenuButton() {
		if(actionMenuButton == null){
			actionMenuButton = new TextButton();
			actionMenuButton.setIcon(AssemblyPanelResources.INSTANCE.blackMenu());
			actionMenuButton.setSize("32px", "32px");
			actionMenuButton.addSelectHandler(event -> pac.smartMapper.showMenu(actionMenuButton, pac));
			actionMenuButton.setHideMode(HideMode.VISIBILITY);
			actionMenuButton.setVisible(false);
		}
		return actionMenuButton;
	}
	
	public void render(){
		GmPropertyInfo renderInfo = parentProperty;
		if(renderInfo == null)
			renderInfo = TypeAndPropertyInfo.getProperty(pac.entityType, pac.propertyName);	
		
		getPropertyNameLabel().setText(renderInfo != null ? TypeAndPropertyInfo.getPropertyName(renderInfo) : "?");
		getPropertyTypeLabel().setText(renderInfo != null ? TypeAndPropertyInfo.getPropertyType(renderInfo) != null ? TypeAndPropertyInfo.getPropertyType(renderInfo).getTypeSignature() : "?" : "?");
		getConversionElement().setText(pac != null && pac.parentEntity != null ? ConversionRendering.renderConversion(pac.parentEntity.entityType()) : "?");
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if(this.pac.parentProperty != null)
			this.pac.propertyName = TypeAndPropertyInfo.getPropertyName(this.pac.parentProperty);
		pac.smartMapper.initMetaModelTools();
		removePropertyAssignmentElement();
		addPropertyAssignmentElement();
		render();
	}
	
	private void removePropertyAssignmentElement(){
		if(propertyAssignmentElement != null){
			propertyAssignmentElement.dispose();
			remove(propertyAssignmentElement.getWidget());
		}
	}
	
	private void addPropertyAssignmentElement(){
		propertyAssignmentElement = getPropertyAssignmentElement();
		insert(propertyAssignmentElement.getWidget(),2);
	}
	
	public void dispose(){
		removePropertyAssignmentElement();
	}
	
	public void validate(){
		propertyAssignmentElement.validate();
	}
	
}
