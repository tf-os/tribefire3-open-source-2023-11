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
package com.braintribe.gwt.smartmapper.client.action;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentChangeContext;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentContext;
import com.braintribe.gwt.smartmapper.client.util.TypeAndPropertyInfo;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.OrderedLinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAsIs;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class SmartMapperActionMenu {
	
	private PersistenceGmSession session;
	private ModelMetaDataEditor modelMetaDataEditor;
	private List<SmartMapperAction> smartMapperActions;
	
	ClearAssignmentAction clear = new ClearAssignmentAction() {
		@Override
		public boolean isVisible(PropertyAssignmentContext pac) {
			return !pac.inherited;
		}
	};
	
	RemoveAssignmentAction remove = new RemoveAssignmentAction() {
		@Override
		public boolean isVisible(PropertyAssignmentContext pac) {
			return !pac.inherited;
		}
	};
	
	ChangeAssignmentAction asIs = new ChangeAssignmentAction() {
		@Override
		public void postProcessNewAssignment(PropertyAssignmentChangeContext pacc) {
			//NOP
		}
		
		@Override
		public boolean isVisible(PropertyAssignmentContext pac) {
			GmPropertyInfo property = pac.parentProperty;
			if(property == null)
				property = TypeAndPropertyInfo.getProperty(pac.entityType, pac.propertyName);
			
			return /*PropertyInfo.isSimpleOrEnumType(PropertyInfo.getPropertyType(property)) && */ TypeAndPropertyInfo.isAsIsAvailable(pac);
		}
	};
	
	public void setModelMetaDataEditor(ModelMetaDataEditor modelMetaDataEditor) {
		this.modelMetaDataEditor = modelMetaDataEditor;
		
		for(SmartMapperAction action : getSmartMapperActions()){
			action.setModelMetaDataEditor(modelMetaDataEditor);
		}
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public List<SmartMapperAction> getSmartMapperActions() {
		if(smartMapperActions == null){
			smartMapperActions = new ArrayList<SmartMapperAction>();			
			
			asIs.setAssignmentType(PropertyAsIs.T);
			asIs.setName("Assign as is");
			asIs.setModelMetaDataEditor(modelMetaDataEditor);
			
			ChangeAssignmentAction qualified = new ChangeAssignmentAction() {
				@Override
				public void postProcessNewAssignment(PropertyAssignmentChangeContext pacc) {
					//NOP
				}
				
				@Override
				public boolean isVisible(PropertyAssignmentContext pac) {
					return true;
					
					/*
					GmPropertyInfo property = pac.parentProperty;
					if(property == null)
						property = PropertyInfo.getProperty(pac.entityType, pac.propertyName);
					
					return PropertyInfo.isSimpleOrEnumType(PropertyInfo.getPropertyType(property));
					*/
				}
			};
			qualified.setAssignmentType(QualifiedPropertyAssignment.T);
			qualified.setName("Qualified");
			qualified.setModelMetaDataEditor(modelMetaDataEditor);
			
			ChangeAssignmentAction join = new ChangeAssignmentAction() {
				@Override
				public boolean isVisible(PropertyAssignmentContext pac) {
					GmPropertyInfo property = pac.parentProperty;
					if(property == null)
						property = TypeAndPropertyInfo.getProperty(pac.entityType, pac.propertyName);
					
					return TypeAndPropertyInfo.isEntityType(TypeAndPropertyInfo.getPropertyType(property));
				}
				
				@Override
				public void postProcessNewAssignment(PropertyAssignmentChangeContext pacc) {
					//NOP
				}
			};
			join.setAssignmentType(KeyPropertyAssignment.T);
			join.setName("Join");
			join.setModelMetaDataEditor(modelMetaDataEditor);
			
			ChangeAssignmentAction inverse = new ChangeAssignmentAction() {
				@Override
				public boolean isVisible(PropertyAssignmentContext pac) {
					GmPropertyInfo property = pac.parentProperty;
					if(property == null)
						property = TypeAndPropertyInfo.getProperty(pac.entityType, pac.propertyName);
					
					return TypeAndPropertyInfo.isEntityType(TypeAndPropertyInfo.getPropertyType(property));
				}
				
				@Override
				public void postProcessNewAssignment(PropertyAssignmentChangeContext pacc) {
					//NOP
				}
			};
			inverse.setAssignmentType(InverseKeyPropertyAssignment.T);
			inverse.setName("Inverse");
			inverse.setModelMetaDataEditor(modelMetaDataEditor);
			
			ChangeAssignmentAction link = new ChangeAssignmentAction() {
				@Override
				public boolean isVisible(PropertyAssignmentContext pac) {
					GmPropertyInfo property = pac.parentProperty;
					if(property == null)
						property = TypeAndPropertyInfo.getProperty(pac.entityType, pac.propertyName);
					
					return TypeAndPropertyInfo.isEntityType(TypeAndPropertyInfo.getPropertyType(property));
				}
				
				@Override
				public void postProcessNewAssignment(PropertyAssignmentChangeContext pacc) {
					//NOP
				}
			};
			link.setAssignmentType(LinkPropertyAssignment.T);
			link.setName("Link");
			link.setModelMetaDataEditor(modelMetaDataEditor);
			
			ChangeAssignmentAction orderedLink = new ChangeAssignmentAction() {
				@Override
				public boolean isVisible(PropertyAssignmentContext pac) {
					GmPropertyInfo property = pac.parentProperty;
					if(property == null)
						property = TypeAndPropertyInfo.getProperty(pac.entityType, pac.propertyName);
					
					return TypeAndPropertyInfo.isEntityType(TypeAndPropertyInfo.getPropertyType(property));
				}
				
				@Override
				public void postProcessNewAssignment(PropertyAssignmentChangeContext pacc) {
					//NOP
				}
			};
			orderedLink.setAssignmentType(OrderedLinkPropertyAssignment.T);
			orderedLink.setName("Ordered Link");
			orderedLink.setModelMetaDataEditor(modelMetaDataEditor);
			
			ChangeAssignmentAction composite = new ChangeAssignmentAction() {
				@Override
				public boolean isVisible(PropertyAssignmentContext pac) {
					GmPropertyInfo property = pac.parentProperty;
					if(property == null)
						property = TypeAndPropertyInfo.getProperty(pac.entityType, pac.propertyName);
					
					return TypeAndPropertyInfo.isEntityType(TypeAndPropertyInfo.getPropertyType(property));
				}
				
				@Override
				public void postProcessNewAssignment(PropertyAssignmentChangeContext pacc) {
					//NOP
				}
			};
			composite.setAssignmentType(CompositeKeyPropertyAssignment.T);
			composite.setName("Composite");
			composite.setModelMetaDataEditor(modelMetaDataEditor);
			
			ChangeAssignmentAction compositeInverse = new ChangeAssignmentAction() {
				@Override
				public boolean isVisible(PropertyAssignmentContext pac) {
					GmPropertyInfo property = pac.parentProperty;
					if(property == null)
						property = TypeAndPropertyInfo.getProperty(pac.entityType, pac.propertyName);
					
					return TypeAndPropertyInfo.isEntityType(TypeAndPropertyInfo.getPropertyType(property));
				}
				
				@Override
				public void postProcessNewAssignment(PropertyAssignmentChangeContext pacc) {
					//NOP
				}
			};
			compositeInverse.setAssignmentType(CompositeInverseKeyPropertyAssignment.T);
			compositeInverse.setName("Composite Inverse");
			compositeInverse.setModelMetaDataEditor(modelMetaDataEditor);
			
			remove.setModelMetaDataEditor(modelMetaDataEditor);
			
			smartMapperActions.add(asIs);
			smartMapperActions.add(qualified);
			smartMapperActions.add(join);
			smartMapperActions.add(inverse);
			smartMapperActions.add(link);
			smartMapperActions.add(orderedLink);
			smartMapperActions.add(composite);
			smartMapperActions.add(compositeInverse);
			smartMapperActions.add(clear);
			smartMapperActions.add(remove);
		}
		return smartMapperActions;
	}
	
	public void showMenu(Widget widget, PropertyAssignmentContext pac){
		Menu menu = new Menu();
		for(SmartMapperAction sma : getSmartMapperActions()){
			if(sma.isVisible(pac)){
				sma.setPropertyAssignmentContext(pac);
				MenuItem mi = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem(sma, mi);
				menu.add(mi);
			}
		}
		menu.show(widget);
	}
	
	public void assignAllAsIs(List<PropertyAssignmentContext> assignmentContexts){
		getSmartMapperActions();
		NestedTransaction nt = null;
		for(PropertyAssignmentContext pac : assignmentContexts){
			if(asIs.isVisible(pac)){
				if(nt == null)
					nt = session.getTransaction().beginNestedTransaction();
				asIs.setPropertyAssignmentContext(pac);				
				asIs.setModelMetaDataEditor(modelMetaDataEditor);
				asIs.perform(null);
			}
		}
		if(nt != null)
			nt.commit();
	}
	
	public void removeAll(List<PropertyAssignmentContext> assignmentContexts) {
		getSmartMapperActions();
		NestedTransaction nt = null;
		for(PropertyAssignmentContext pac : assignmentContexts){
			if(remove.isVisible(pac)){
				if(nt == null)
					nt = session.getTransaction().beginNestedTransaction();
				remove.setPropertyAssignmentContext(pac);				
				remove.setModelMetaDataEditor(modelMetaDataEditor);
				remove.perform(null);
			}
		}
		if(nt != null)
			nt.commit();
	}

}
