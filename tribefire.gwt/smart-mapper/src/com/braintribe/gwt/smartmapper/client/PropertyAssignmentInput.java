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

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog;
import com.braintribe.gwt.gme.workbench.client.resources.WorkbenchResources;
import com.braintribe.gwt.gmview.action.client.EntitiesProviderResult;
import com.braintribe.gwt.gmview.action.client.ParserResult;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.client.parse.ParserArgument;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.query.EntityQuery;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

public abstract class PropertyAssignmentInput extends TextBox implements ManipulationListener, /* EntityMigrationListener,  */ KeyUpHandler, KeyDownHandler, ChangeHandler{
	
	protected static final EntityType<QualifiedProperty> qualifiedPropertyType = GMF.getTypeReflection().getEntityType(QualifiedProperty.class);
	protected static final EntityType<GmProperty> gmPropertyType = GMF.getTypeReflection().getEntityType(GmProperty.class);
	protected static final EntityType<GmEntityType> gmEntityType = GMF.getTypeReflection().getEntityType(GmEntityType.class);
	protected static final EntityType<IncrementalAccess> accessType = GMF.getTypeReflection().getEntityType(IncrementalAccess.class);
	
	protected String propertyNameOfAssignment;
	protected String internalPropertyName;
	protected PropertyAssignmentContext pac;
//	protected AssemblyMonitoring am;
	protected QuickAccessDialog quickAccessDialog;
	protected boolean requiresRefresh = false;
	
	public PropertyAssignmentInput() {
		addKeyUpHandler(this);
		addKeyDownHandler(this);
		addChangeHandler(this);				
	}
	
	public void setRequiresRefresh(boolean requiresRefresh) {
		this.requiresRefresh = requiresRefresh;
	}

	public void setPropertyAssignmentContext(PropertyAssignmentContext pac){
		this.pac = pac;
		if(pac.parentEntity != null)
			pac.session.listeners().entity(pac.parentEntity).add(this);
		/*
		if(am == null){
			am = AssemblyMonitoring.newInstance().build(pac.session, pac.parentEntity);
			am.addEntityMigrationListener(this);
//			am.addManpiulationListener(this);
			
			if(am.getEntities() != null && am.getEntities().size() > 0){
				for(GenericEntity entity : am.getEntities()){
					pac.session.listeners().entity(entity).add(this);	
				}
			}
		} */
//		setEnabled(pac.inherited);
		render();
	}
		
	public void setPropertyNameOfAssignment(String propertyNameOfAssignment) {
		this.propertyNameOfAssignment = propertyNameOfAssignment;
//		getElement().setAttribute("placeholder", propertyNameOfAssignment);
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		render();
	}
	
//	@Override
//	public void onJoin(GenericEntity entity) {
//		System.err.println(entity);
//	}
//	
//	@Override
//	public void onLeave(GenericEntity entity) {
//		System.err.println(entity);
//	}
	
	@Override
	public void onKeyUp(KeyUpEvent event) {
		if(event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE || event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
			if(quickAccessDialog != null) { quickAccessDialog.forceHide(); quickAccessDialog = null; }
			setFocus(false);
			render();
		}else{
			try{
				if(event.getNativeKeyCode() != KeyCodes.KEY_DOWN && event.getNativeKeyCode() != KeyCodes.KEY_UP && event.getNativeKeyCode() != KeyCodes.KEY_LEFT && event.getNativeKeyCode() != KeyCodes.KEY_RIGHT)
					showQuickAccess();				
			}catch(Exception ex){
				ErrorDialog.show("Error while providing quickAccessDialog", ex);
			}
		}	
	}
	
	@Override
	public void onKeyDown(KeyDownEvent event) {
		if(event.getNativeKeyCode() == KeyCodes.KEY_TAB){
			if(quickAccessDialog != null) { quickAccessDialog.forceHide(); quickAccessDialog = null; }
			setFocus(false);
			render();
		}
	}
	
	@Override
	public void onChange(ChangeEvent event) {
//		System.err.println("onChange");
//		if(quickAccessDialog != null) { quickAccessDialog.forceHide(); quickAccessDialog = null; }
//		setFocus(false);
		if(quickAccessDialog != null && !quickAccessDialog.isVisible())
			render();
	}
	
	public abstract void render();
	
	public void dispose() {
		//NOP
	}
	
	public GenericEntity prepareEntity(){
		return pac.parentEntity;
	}
	
	private void refresh(GenericEntity entity) {
		EntityType<?> type = entity.entityType();
		Object id = type.getIdProperty().get(entity);		
		EntityQuery query = EntityQueryBuilder.from(type).where().property(type.getIdProperty().getName()).eq(id).tc().negation().joker().done();
		pac.session.query().entities(query).result(Future.async(this::error, this::handleResult));
	}
	
	public void handleResult(EntityQueryResultConvenience result) {
		handleInput(result.first());
	}
	
	public void handleInput(Object input){
		NestedTransaction nt = pac.session.getTransaction().beginNestedTransaction();
		if(pac.parentEntity == null)
			pac.parentEntity = prepareEntity();
		
		if (pac.parentEntity == null || !(input instanceof GenericEntity)) {
			setText("");
			nt.commit();
			return;
		}
		
		GenericEntity ge = (GenericEntity)input;
		if (!getType().isAssignableFrom(ge.entityType())) {
			nt.commit();
			return;
		}
		
		Property property = pac.parentEntity.entityType().getProperty(propertyNameOfAssignment);			
		if (getType().isAssignableFrom(property.getType())) {
			setPropertyValue(property, pac.parentEntity, ge); //set direct
			nt.commit();
			return;
		}
		
		GenericEntity currentValue = property.get(pac.parentEntity);
		if (!qualifiedPropertyType.isAssignableFrom(property.getType())) {
			nt.commit();
			return;
		}
		
		if (currentValue == null) {
			QualifiedProperty qp = pac.session.create((EntityType<? extends QualifiedProperty>) property.getType());
			setPropertyValue(property, pac.parentEntity, qp);
			currentValue = qp;
			pac.session.listeners().entity(qp).add(this);
		}
		
		Property p = currentValue.entityType().getProperty(internalPropertyName);
		setPropertyValue(p, currentValue, ge);
		render();
		
		nt.commit();
	}
	
	private void setPropertyValue(Property p, GenericEntity parentEntity, Object value){
		Object oldValue = p.get(parentEntity);
		if(oldValue != value)
			p.set(parentEntity, value);
	}
	
	public void clearInput(){
		if (pac.parentEntity == null)
			return;
		
		Property property = pac.parentEntity.entityType().getProperty(propertyNameOfAssignment);			
		if (getType().isAssignableFrom(property.getType())){
			property.set(pac.parentEntity, null); //set direct
			return;
		}
		
		GenericEntity currentValue = property.get(pac.parentEntity);
		if (currentValue != null && qualifiedPropertyType.isAssignableFrom(property.getType()))						
			currentValue.entityType().getProperty(internalPropertyName).set(currentValue, null);					
	}
	
	public abstract EntityType<? extends GenericEntity> getType();
	
	public abstract boolean loadExisitingValues();
	
	public abstract boolean loadTypes();
	
	public abstract Function<ParserArgument, List<ParserResult>> simpleTypesValuesProvider();
	
	public abstract Function<ParserArgument, Future<EntitiesProviderResult>> entitiesFutureProvider();
	
	private void showQuickAccess() throws RuntimeException{
		QuickAccessDialog quickAccessDialog = getQuickAccessDialog();
//		quickAccessDialog.getQuickAccessPanel().prepareTypeCondition(getType())
		quickAccessDialog.getQuickAccessResult(quickAccessDialog.getQuickAccessPanel().prepareTypeCondition(getType()), this, getText()) //
		.andThen(result -> {
			if (result == null) {
				render();
				return;
			}
			
			Object input = result.getObject() != null ? result.getObject() : result.getType();
			if (requiresRefresh)
				refresh((GenericEntity) input);
			else
				handleInput(input);
		}).onError(e -> ErrorDialog.show("Error while providing showQuickAccess", e));
	}
	
	private QuickAccessDialog getQuickAccessDialog() throws RuntimeException {
//		if(quickAccessDialog != null) quickAccessDialog.forceHide();
		if(quickAccessDialog != null)
			return quickAccessDialog;
		
		quickAccessDialog = new QuickAccessDialog();
		quickAccessDialog.setShadow(false);
		quickAccessDialog.setUseApplyButton(false);
		quickAccessDialog.setUseNavigationButtons(false);
		quickAccessDialog.setInstantiateButtonLabel("Add");			
		quickAccessDialog.setFocusWidget(PropertyAssignmentInput.this);
		quickAccessDialog.setQuickAccessPanelProvider(getQuickAccessPanelProvider());
		quickAccessDialog.addStyleName(WorkbenchResources.INSTANCE.css().border());
			
		try {
			quickAccessDialog.intializeBean();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return quickAccessDialog;
	}
	
	private Supplier<SpotlightPanel> getQuickAccessPanelProvider() {
		return () -> {
			SpotlightPanel spotlightPanel = pac.spotlightPanelProvider.get();
			spotlightPanel.setTextField(PropertyAssignmentInput.this);
			spotlightPanel.setMinCharsForFilter(0);
			spotlightPanel.setUseApplyButton(false);
			spotlightPanel.setLoadTypes(false);
			spotlightPanel.setLoadExistingValues(true);
//				spotlightPanel.setLoadExistingValues(loadExisitingValues());
//				spotlightPanel.setSimpleTypesValuesProvider(simpleTypesValuesProvider());
			if(entitiesFutureProvider() != null)
				spotlightPanel.setEntitiesFutureProvider(entitiesFutureProvider());
			return spotlightPanel;
		};
	}
	
	public static TextButton clear(PropertyAssignmentInput input){
		TextButton clear = new TextButton("x");
		clear.addSelectHandler(new ClearInputAction(input));
		return clear;
	}
	
	private void error(Throwable t) {
		t.printStackTrace();
	}
	
	static class ClearInputAction implements SelectHandler{
		protected PropertyAssignmentInput input;
		
		public ClearInputAction(PropertyAssignmentInput input) {
			setInput(input);
		}
		
		public void setInput(PropertyAssignmentInput input) {
			this.input = input;
		}
		
		public void clear(){
			input.clearInput();
		}
		
		@Override
		public void onSelect(SelectEvent event) {
			clear();
		}
	}
}
