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
package com.braintribe.gwt.gmview.action.client;

import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.client.EntityFieldDialog;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class FieldDialogOpenerAction<E extends GenericEntity> extends Action {
	
	private Supplier<? extends EntityFieldDialog<E>> entityFieldDialogSupplier;
	private EntityFieldDialog<E> entityFieldDialog;
	private PersistenceGmSession gmSession;
	private E entityValue;
	private Boolean isFreeInstantiation;
	
	/**
	 * Configures the required supplier for the EntityFieldDialog, used for editing the GenericEntity configured via
	 * {@link #configureEntityValue(GenericEntity)}.
	 */
	@Required
	public void setEntityFieldDialogSupplier(Supplier<? extends EntityFieldDialog<E>> entityFieldDialogSupplier) {
		this.entityFieldDialogSupplier = entityFieldDialogSupplier;
		entityFieldDialog = getEntityFieldDialog();
		if (entityFieldDialog == null)
			return;
		
		entityFieldDialog.addHideHandler(event -> {
			boolean hasChanges = FieldDialogOpenerAction.this.entityFieldDialog.hasChanges();
			if (hasChanges) {
				FieldDialogOpenerAction.this.entityFieldDialog.performManipulations();
				/*try {
					parentPanel.getManipulationContext().doManipulation(manipulation);
				} catch (ManipulationException e) {
					ErrorDialog.show("Error while performing field manipulation.", e);
					e.printStackTrace();
				}*/
			}
		});
	}
	
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		
		if (entityFieldDialog != null)
			entityFieldDialog.configureGmSession(gmSession);
	}
	
	/**
	 * Sets the entity to be edited within this action.
	 */
	public void configureEntityValue(E entityValue) {
		this.entityValue = entityValue;
		
		if (entityFieldDialog != null)
			entityFieldDialog.setEntityValue(entityValue);
	}
	
	public void setIsFreeInstantiation(Boolean isFreeInstantiation) {
		this.isFreeInstantiation = isFreeInstantiation;
		
		if (entityFieldDialog != null)
			entityFieldDialog.setIsFreeInstantiation(isFreeInstantiation);
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		getEntityFieldDialog().show();
	}
	
	private EntityFieldDialog<E> getEntityFieldDialog() {
		if (entityFieldDialog != null)
			return entityFieldDialog;
		
		entityFieldDialog = entityFieldDialogSupplier.get();
		if (gmSession != null)
			entityFieldDialog.configureGmSession(gmSession);
		if (entityValue != null)
			entityFieldDialog.setEntityValue(entityValue);
		if (isFreeInstantiation != null)
			entityFieldDialog.setIsFreeInstantiation(isFreeInstantiation);
			
		return entityFieldDialog;
	}
	
}
