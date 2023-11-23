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
package com.braintribe.gwt.gme.propertypanel.client.action;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.propertypanel.client.LocalizedText;
import com.braintribe.gwt.gme.propertypanel.client.PropertyModel;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.LocalManipulationAction;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.meta.data.prompt.SimplifiedAssignment;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;

public class ChangeBasedTypeToExistingAction extends Action implements LocalManipulationAction {
	
	private PropertyPanel propertyPanel;
	private Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionFutureProviderProvider;
	private Function<SelectionConfig, Future<InstanceSelectionData>> selectionFutureProvider;
	private LocalManipulationListener listener;
	
	public ChangeBasedTypeToExistingAction(PropertyPanel propertyPanel, Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionFutureProviderProvider) {
		this.propertyPanel = propertyPanel;
		this.selectionFutureProviderProvider = selectionFutureProviderProvider;
		setName(LocalizedText.INSTANCE.assign());
		setTooltip(LocalizedText.INSTANCE.changeNewExistingDescription());
		setIcon(PropertyPanelResources.INSTANCE.changeExisting());
	}
	
	@Override
	public void configureListener(LocalManipulationListener listener) {
		this.listener = listener;
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (selectionFutureProvider == null)
			selectionFutureProvider = selectionFutureProviderProvider.get();
		
		GenericEntity parentEntity = propertyPanel.getHelperMenuPropertyModel().getParentEntity();
		final NestedTransaction nestedTransaction;
		if (parentEntity == null)
			nestedTransaction = null;
		else {
			GmSession session = parentEntity.session();
			if (session instanceof PersistenceGmSession)
				nestedTransaction = ((PersistenceGmSession) session).getTransaction().beginNestedTransaction();
			else
				nestedTransaction = null;
		}
		
		boolean instantiable = true;
		boolean referenceable = true;
		boolean simplified = false;
		boolean useDetail = true;
		if (!propertyPanel.isSkipMetadataResolution()) {
			ModelMdResolver modelMdResolver = GmSessions.getMetaData(parentEntity).useCase(propertyPanel.getUseCase());
			PropertyMdResolver propertyMdResolver = modelMdResolver.entity(parentEntity)
					.property(propertyPanel.getHelperMenuPropertyModel().getPropertyName());
			
			instantiable = GMEMetadataUtil.isInstantiable(propertyMdResolver, modelMdResolver);
			referenceable = GMEMetadataUtil.isReferenceable(propertyMdResolver, modelMdResolver);
			
			SimplifiedAssignment sa = propertyMdResolver.meta(SimplifiedAssignment.T).exclusive();
			if (sa != null) {
				useDetail = sa.getShowDetails();
				simplified = true;
			}
		}
		
		selectionFutureProvider
				.apply(new SelectionConfig(GMF.getTypeReflection().getBaseType(), 1, null, propertyPanel.getGmSession(), null, instantiable,
						referenceable, simplified, useDetail, null)) //
				.andThen(result -> changeEntityInstance(result == null ? null : result.getSelections(), nestedTransaction)) //
				.onError(e -> {
					if (nestedTransaction != null) {
						try {
							nestedTransaction.rollback();
						} catch (TransactionException e1) {
							e1.printStackTrace();
						}
					}

					e.printStackTrace();
					ErrorDialog.show(LocalizedText.INSTANCE.errorChangingPropertyValue(), e);
				});
	}
	
	private void changeEntityInstance(List<GMTypeInstanceBean> result, NestedTransaction nestedTransaction) {
		if (result != null) {
			GMTypeInstanceBean instanceBean = result.get(0);
			PropertyModel propertyModel = propertyPanel.getHelperMenuPropertyModel();
			EntityType<GenericEntity> parentEntityType = propertyModel.getParentEntityType();
			parentEntityType.getProperty(propertyModel.getPropertyName()).set(propertyModel.getParentEntity(), instanceBean.getInstance());
			if (nestedTransaction != null)
				nestedTransaction.commit();
			
			fireListener();
			return;
		}
		
		if (nestedTransaction != null) {
			try {
				nestedTransaction.rollback();
			} catch (TransactionException e) {
				e.printStackTrace();
				ErrorDialog.show(LocalizedText.INSTANCE.errorRollingEditionBack(), e);
			}
		}
	}
	
	private void fireListener() {
		if (listener != null)
			listener.onManipulationPerformed();
	}

}
