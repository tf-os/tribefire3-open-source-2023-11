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

import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * This action is responsible for instantiating a new transient entity.
 * @author michel.docouto
 *
 */
public class InstantiateTransientEntityAction extends AbstractInstantiateEntityAction {
	
	public InstantiateTransientEntityAction() {
		setName(LocalizedText.INSTANCE.newTransientEntity());
	}
	
	/**
	 * Configures the session used for creating new transient instances. This is required when the action is available.
	 */
	@Configurable
	public void setTransientSession(PersistenceGmSession transientSession) {
		this.gmSession = transientSession;
		transientSession.listeners().add(InstantiateTransientEntityAction.this);
	}
	
	/**
	 * Configures the useCase to be used within this action.
	 * Notice that this is used only if there is no {@link GmContentView} configured via {@link #configureGmContentView(com.braintribe.gwt.gmview.client.GmContentView)}.
	 */
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}		
	
	/**
	 * Configures an {@link InstantiatedEntityListener} to be called when instantiating.
	 * Notice that this is used only if there is no {@link GmContentView} configured via {@link #configureGmContentView(com.braintribe.gwt.gmview.client.GmContentView)}.
	 */
	@Override
	public void configureInstantiatedEntityListener(InstantiatedEntityListener instantiatedEntityListener) {
		this.instantiatedEntityListener = instantiatedEntityListener;
	}	
		
	/**
	 * Configures the {@link EntityType} that will be instantiated.
	 */
	@Override
	public void configureEntityType(EntityType<?> entityType) {
		this.entityType = entityType;
		setHidden(entityType == null || !isInstantiable(entityType));
		if (entityType != null) {
			String actionName = LocalizedText.INSTANCE.createEntity(GMEMetadataUtil.getEntityNameMDOrShortName(entityType,
					getGmSession().getModelAccessory().getMetaData(), gmContentView != null ? gmContentView.getUseCase() : useCase));
			if (displayEntityNameInAction)
				setName(actionName);
			setTooltip(actionName);
		}
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	@Override
	protected void updateVisibility() {
		if (entityType == null)
			entityType = GenericEntity.T;
		if (gmSession.getModelAccessory() != null) {
			setHidden(!isInstantiable(entityType));
			return;
		}
		
		setHidden(true, true);
	}
	
	//@Override
	protected boolean isInstantiable(EntityType<?> entityType) {
		if (gmSession.getModelAccessory() != null) {
			EntityMdResolver entityMetaDataContextBuilder = gmSession.getModelAccessory().getMetaData().entityType(entityType)
					.useCase(gmContentView != null ? gmContentView.getUseCase() : useCase);
			if (!entityMetaDataContextBuilder.is(Instantiable.T))
				return false;
		}
		
		return true;
	}
	
}
