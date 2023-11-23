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

import java.util.function.Predicate;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentChangeContext;
import com.braintribe.gwt.smartmapper.client.util.TypeAndPropertyInfo;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.core.shared.GWT;

public abstract class ChangeAssignmentAction extends SmartMapperAction{
	
	protected EntityType<? extends PropertyAssignment> assignmentType;
		
	public void setAssignmentType(EntityType<? extends PropertyAssignment> assignmentType) {
		this.assignmentType = assignmentType;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		changeAssignment();
	}
	
	public void changeAssignment(){
			
		GWT.debugger();
		PersistenceGmSession session = propertyAssignmentContext.session;
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		
		modelMetaDataEditor.onEntityType(TypeAndPropertyInfo.getTypeSignature(propertyAssignmentContext.entityType)).removePropertyMetaData(
				propertyAssignmentContext.propertyName, (Predicate<MetaData>) metaData -> metaData == propertyAssignmentContext.parentEntity);
//		property.getMetaData().remove(propertyAssignmentContext.entity);	
		
		PropertyAssignment pa = session.create(assignmentType);
		
//		UseCaseSelector selector = session.create(UseCaseSelector.T);
//		selector.setUseCase(myAccess.getExternalId());
//		pa.setSelector(selector);
		
		postProcessNewAssignment(prepareChangeContext(pa));
		
		modelMetaDataEditor.onEntityType(TypeAndPropertyInfo.getTypeSignature(propertyAssignmentContext.entityType))
				.addPropertyMetaData(propertyAssignmentContext.propertyName, pa);
//		property.getMetaData().add(pa);	
		
		nt.commit();		
	}
	
	public abstract void postProcessNewAssignment(PropertyAssignmentChangeContext pacc);
	
	public PropertyAssignmentChangeContext prepareChangeContext(PropertyAssignment pa){
		PropertyAssignmentChangeContext changeContext = new PropertyAssignmentChangeContext(propertyAssignmentContext);
		changeContext.newPropertyAssignment = pa;
		return changeContext;
	}

}
