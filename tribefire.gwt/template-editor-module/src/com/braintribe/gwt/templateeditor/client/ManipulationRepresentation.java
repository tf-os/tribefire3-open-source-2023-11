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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.templateeditor.client.resources.TemplateEditorResources;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public abstract class ManipulationRepresentation<M extends Manipulation> implements GmSelectionSupport, ManipulationListener{
	
	protected final static String ASSIGN_DELIMITER = "=";
	protected final static String ADD_DELIMITER = "+";
	protected final static String KEY_VALUE_DELIMITER = ":";
	protected final static String VALUES_DELIMITER = ",";
	
	protected final static String MANIPULATION_ENTRY_CLASS = "manipulationEntry";
	protected final static String MANIPULATION_SUB_ENTRY_CLASS = "manipulationSubEntry";
	protected final static String OWNER_ELEMENT = "ownerElement";
	protected final static String PROPERTY_ELEMENT = "propertyElement";
	protected final static String VALUE_ELMENT_CLASS = "valueElement";
	protected final static String VARIABLE_ELMENT_CLASS = "variableElement";
	protected final static String OWNER_PROPERTY_DELIMITER_WRAPPER = "ownerPropertyDelimiterWrapper";
	protected final static String OWNER_PROPERTY_DELIMITER = "ownerPropertyDelimiter";
	
	protected final static String DELIMITER_ELEMENT = "delimiterElement";
	protected final static String TYPE_ELEMENT = "manipulationType";
	
	protected boolean showTitle = true;
	protected FlowPanel titleElement;
	protected FlowPanel ownerElement;
	protected Image ownerPropertyDelimiter;
	private Owner owner;
	private FlowPanel propertyElement;
	//private Widget currentSelectedElement;
	protected GenericEntity currentSelectedEntity;
	protected GenericEntity entity;
	protected String propertyName;
	
	protected PersistenceGmSession session;
	private List<GmSelectionListener> gmSelectionListeners = new ArrayList<>();
	protected ManipulationRepresentationListener manipulationRepresentationListener;
	protected Map<GenericEntity, FlowPanel> entitiesToElements = new HashMap<>();
	
	public abstract Future<Widget> renderManipulation(M manipulation);
	public abstract void changeValue(Object oldValue, Object newValue);
	public abstract String getManipulationType();
	
	protected Future<FlowPanel> prepareOwnerElement(final PropertyManipulation manipulation){
		owner = manipulation.getOwner();
		final Future<FlowPanel> future = new Future<>();
//		if(owner instanceof LocalEntityProperty){
			entity = ((LocalEntityProperty) owner).getEntity();
			propertyName = ((LocalEntityProperty) owner).getPropertyName();
			future.onSuccess(prepareOwner(entity, manipulation));
//		}
//		else if(owner instanceof EntityProperty){
//			propertyName = ((EntityProperty) owner).getPropertyName();
//			EntityReference entityReference = ((EntityProperty) owner).getReference();
//			if(entityReference instanceof PreliminaryEntityReference){
//				entity = entityReference.getLocalReference();
//				future.onSuccess(prepareOwner(entity, manipulation));
//			}else if(entityReference instanceof PersistentEntityReference){
//				EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entityReference.getTypeSignature());
//				EntityQuery entityQuery = EntityQueryBuilder.from(entityType).where().property(entityType.getIdProperty().getPropertyName()).eq(entityReference.getId()).done();
//				session.query().entities(entityQuery).result(new AsyncCallback<EntityQueryResultConvenience>() {
//					public void onSuccess(EntityQueryResultConvenience result) {
//						try {
//							entity = result.first();
//						} catch (GmSessionException e) {
//							future.onFailure(e);
//						}
//						future.onSuccess(prepareOwner(entity, manipulation));
//					}
//					
//					public void onFailure(Throwable t) {
//						future.onFailure(t);
//					}
//				});
//			}
//		}
		return future;
	}
	
	private FlowPanel prepareOwner(final GenericEntity entity, final Manipulation manipulation){
		session.listeners().entity(entity).add(this);
		
		final FlowPanel manipulationEntry = new FlowPanel();
		manipulationEntry.setStyleName(MANIPULATION_ENTRY_CLASS);
		manipulationEntry.addDomHandler(event -> setCurrentSelectedEntity(manipulation), MouseDownEvent.getType());
		manipulationEntry.addDomHandler(event -> manipulationEntry.addStyleName("selected"), MouseOverEvent.getType());
		manipulationEntry.addDomHandler(event -> {
			if(!manipulationRepresentationListener.isElementSelected(manipulation))
				manipulationEntry.removeStyleName("selected");
		},MouseOutEvent.getType());
		
		manipulationRepresentationListener.putEntityElement(manipulation, manipulationEntry);
		
		if(showTitle){
			titleElement = new FlowPanel();
			titleElement.setStyleName(TYPE_ELEMENT);
			titleElement.getElement().setInnerText(getManipulationType());
			manipulationEntry.add(titleElement);
		}
		
		ownerElement = new FlowPanel();
		ownerElement.setStyleName(OWNER_ELEMENT);
		ownerElement.addDomHandler(event -> {
			setCurrentSelectedEntity(entity);
			event.preventDefault();
			event.stopPropagation();
		}, MouseDownEvent.getType());
		ownerElement.addDomHandler(event -> ownerElement.addStyleName("selected"), MouseOverEvent.getType());
		ownerElement.addDomHandler(event -> {
			if(!manipulationRepresentationListener.isElementSelected(entity))
				ownerElement.removeStyleName("selected");
		},MouseOutEvent.getType());
		EntityType<GenericEntity> entityType = entity.entityType();
		String ownerName = entity.toSelectiveInformation();
		Object id = entity.getId();
		if(ownerName == null || ownerName.equals(""))
			ownerName = entityType.getShortName() + " (" + id.toString() + ")";
		ownerElement.getElement().setInnerText(ownerName);
		
		manipulationEntry.add(ownerElement);
		
		manipulationRepresentationListener.putEntityElement(entity, ownerElement);
		
		FlowPanel ownerPropertyDelimiterWrapper = new FlowPanel();
		ownerPropertyDelimiterWrapper.setStyleName(OWNER_PROPERTY_DELIMITER_WRAPPER);
		ownerPropertyDelimiter = new Image(TemplateEditorResources.INSTANCE.arrow());
		ownerPropertyDelimiter.setStyleName(OWNER_PROPERTY_DELIMITER);
		ownerPropertyDelimiterWrapper.add(ownerPropertyDelimiter);
		manipulationEntry.add(ownerPropertyDelimiterWrapper);
		
		propertyElement = new FlowPanel();
		propertyElement.setStyleName(PROPERTY_ELEMENT);
		Name propertyDisplayName = session.getModelAccessory().getMetaData().entity(entity).property(propertyName).meta(Name.T).exclusive();
		String propertyText = propertyDisplayName != null ? I18nTools.getDefault(propertyDisplayName.getName(), "") : propertyName;
		propertyElement.getElement().setInnerText(propertyText);
		
		manipulationEntry.add(propertyElement);
		return manipulationEntry;
	}
	
	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}
	
	public void setManipulationRepresentationListener(ManipulationRepresentationListener manipulationRepresentationListener) {
		this.manipulationRepresentationListener = manipulationRepresentationListener;
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public void setGmSelectionListeners(List<GmSelectionListener> gmSelectionListeners) {
		this.gmSelectionListeners = gmSelectionListeners;
	}	
	
	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		if(currentSelectedEntity != null){
			ModelPath modelPath = new ModelPath();
			RootPathElement rootPathElement = new RootPathElement(currentSelectedEntity.entityType(), currentSelectedEntity);
			modelPath.add(rootPathElement);
			return modelPath;
		}
		return null;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return Arrays.asList(getFirstSelectedItem());
	}

	@Override
	public boolean isSelected(Object element) {
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		//NOP
	}

	@Override
	public GmContentView getView() {
		return null;
	}
	
	public void setCurrentSelectedEntity(GenericEntity currentSelectedEntity) {
		this.currentSelectedEntity = currentSelectedEntity;
		manipulationRepresentationListener.setCurrentSelectedElement(currentSelectedEntity);
		fireSelectionChanged();
	}
	
	private void fireSelectionChanged(){
		for(GmSelectionListener gmSelectionListener : gmSelectionListeners)
			gmSelectionListener.onSelectionChanged(this);
	}

}
