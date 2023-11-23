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

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.smartmapper.client.PropertyAssignmentContext;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;

public class ChangeCompositeKeyPropertyAssignmentAction extends SmartMapperAction{
	
	protected boolean inverse = false;
	protected GenericEntity parent;
	private boolean remove = true;
	
	public ChangeCompositeKeyPropertyAssignmentAction(boolean remove, boolean inverse, GenericEntity parent){
		this.parent = parent;
		this.inverse = inverse;
		this.remove = remove;
	}
	
	@Override
	public boolean isVisible(PropertyAssignmentContext pac) {
		return true;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		PersistenceGmSession session = propertyAssignmentContext.session;
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		if(remove){
			if(inverse){
				CompositeInverseKeyPropertyAssignment cikpa = (CompositeInverseKeyPropertyAssignment) parent;
				cikpa.getInverseKeyPropertyAssignments().remove(propertyAssignmentContext.parentEntity);
			}else{
				CompositeKeyPropertyAssignment ckpa = (CompositeKeyPropertyAssignment) parent;
				ckpa.getKeyPropertyAssignments().remove(propertyAssignmentContext.parentEntity);
			}			
		}else{
			if(inverse){
				CompositeInverseKeyPropertyAssignment cikpa = (CompositeInverseKeyPropertyAssignment) parent;
				InverseKeyPropertyAssignment kpa = session.create(InverseKeyPropertyAssignment.T);
				cikpa.getInverseKeyPropertyAssignments().add(kpa);
			}else{
				CompositeKeyPropertyAssignment ckpa = (CompositeKeyPropertyAssignment) parent;
				KeyPropertyAssignment kpa = session.create(KeyPropertyAssignment.T);
				ckpa.getKeyPropertyAssignments().add(kpa);
			}
		}
		nt.commit();
	}

}
