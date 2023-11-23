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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.InstantiationData;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.workbench.InstantiationAction;

public abstract class AbstractInstantiateEntityAction extends ModelAction implements ManipulationListener {
	
	protected EntityType<?> entityType;
	protected PersistenceGmSession gmSession;
	private Map<GenericEntity, InstantiatedEntityListener> instantiatedEntityAndListenerMap;
	protected boolean displayEntityNameInAction = false;
	private List<ModelActionPosition> positions = new ArrayList<>();
	protected String useCase;
	protected InstantiatedEntityListener instantiatedEntityListener;
	protected InstantiationActionHandler instantiationActionHandler;
	protected Function<EntityType<?>, Future<List<InstantiationAction>>> instantiationActionsProvider;
	
	public AbstractInstantiateEntityAction() {
		setIcon(GmViewActionResources.INSTANCE.add());
		setHoverIcon(GmViewActionResources.INSTANCE.addBig());
		setHidden(true);
		positions.add(ModelActionPosition.ActionBar);
		positions.add(ModelActionPosition.ContextMenu);
		put(ModelAction.PROPERTY_POSITION, positions);
	}
	
	/**
	 * Configures the required handle that will handle InstantiationActions.
	 */
	@Required
	public void setInstantiationActionHandler(InstantiationActionHandler instantiationActionHandler) {
		this.instantiationActionHandler = instantiationActionHandler;
	}
	
	/**
	 * Configures the required provider which will provide available {@link InstantiationAction}s for the given type.
	 */
	@Required
	public void setInstantiationActionsProvider(Function<EntityType<?>, Future<List<InstantiationAction>>> instantiationActionsProvider) {
		this.instantiationActionsProvider = instantiationActionsProvider;
	}
	
	/**
	 * Configures whether to display the entity name in the action name.
	 * Defaults to false (entity name not shown).
	 */
	@Configurable
	public void setDisplayEntityNameInAction(boolean displayEntityNameInAction) {
		this.displayEntityNameInAction = displayEntityNameInAction;
	}
	
	/**
	 * Configures whether to use this action in the context menu.
	 * Defaults to true;
	 */
	@Configurable
	public void setUseInContextMenu(boolean useInContextMenu) {
		if (useInContextMenu) {
			if (!positions.contains(ModelActionPosition.ContextMenu)) {
				positions.add(ModelActionPosition.ContextMenu);
				put(ModelAction.PROPERTY_POSITION, positions);
			}
		} else if (positions.contains(ModelActionPosition.ContextMenu)) {
			positions.remove(ModelActionPosition.ContextMenu);
			put(ModelAction.PROPERTY_POSITION, positions);
		}
	}
	
	/**
	 * Configures the {@link EntityType} that will be instantiated.
	 */
	public abstract void configureEntityType(EntityType<?> entityType);
	
	
	/**
	 * Configures an {@link InstantiatedEntityListener} to be called when instantiating.
	 * Notice that this is used only if there is no {@link GmContentView} configured via {@link #configureGmContentView(com.braintribe.gwt.gmview.client.GmContentView)}.
	 */
	public abstract void configureInstantiatedEntityListener(InstantiatedEntityListener instantiatedEntityListener);
	
	
	/**
	 * Configures the useCase to be used within this action.
	 * Notice that this is used only if there is no {@link GmContentView} configured via {@link #configureGmContentView(com.braintribe.gwt.gmview.client.GmContentView)}.
	 */
	public abstract void configureUseCase(String useCase);
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		String type = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, getGmSession().getModelAccessory().getMetaData(),
				gmContentView != null ? gmContentView.getUseCase() : useCase);
		
		instantiationActionsProvider.apply(entityType) //
				.andThen(instantiationActions -> {
					if (instantiationActions.size() == 1) {
						instantiationActionHandler.handleInstantiationAction(instantiationActions.get(0), null);
						return;
					}

					handleInstantiation(entityType, instantiationActions, true, LocalizedText.INSTANCE.newType(type));
				}).onError(e -> {
					ErrorDialog.show(LocalizedText.INSTANCE.errorInstantiatingEntity(), e);
					e.printStackTrace();
				});
	}
	
	public abstract PersistenceGmSession getGmSession();
	
	/**
	 * Returns the current configured entity type.
	 */
	public EntityType<?> getEntityType() {
		return entityType;
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (instantiatedEntityAndListenerMap == null
				|| !(manipulation instanceof DeleteManipulation || manipulation instanceof InstantiationManipulation)) {
			return;
		}
		
		GenericEntity entity = manipulation instanceof DeleteManipulation ? ((DeleteManipulation) manipulation).getEntity()
				: ((InstantiationManipulation) manipulation).getEntity();
		InstantiatedEntityListener listener = instantiatedEntityAndListenerMap.get(entity);
		if (listener != null) {
			RootPathElement rootPathElement = new RootPathElement(entity.entityType(), entity);
			if (manipulation instanceof DeleteManipulation)
				listener.onEntityUninstantiated(rootPathElement);
			else
				listener.onEntityInstantiated(new InstantiationData(rootPathElement, false, false, null, false, getGmSession() instanceof TransientPersistenceGmSession));
		}
	}
	
	protected void handleInstantiation(GMTypeInstanceBean bean, boolean isFreeInstantiation) {
		InstantiatedEntityListener listener = null;
		if (gmContentView != null)
			listener = GMEUtil.getInstantiatedEntityListener(gmContentView);
		else
			listener = instantiatedEntityListener;
		
		if (listener != null) {
			listener.onEntityInstantiated(new InstantiationData(new RootPathElement(bean.getGenericModelType(), bean.getInstance()), true,
					isFreeInstantiation, null, false, getGmSession() instanceof TransientPersistenceGmSession));
			if (instantiatedEntityAndListenerMap == null)
				instantiatedEntityAndListenerMap = new HashMap<GenericEntity, InstantiatedEntityListener>();
			instantiatedEntityAndListenerMap.put((GenericEntity) bean.getInstance(), listener);
		}
	}
	
	protected void handleInstantiation(GenericModelType type, List<InstantiationAction> instantiationActions, boolean isFreeInstantiation, String title) {
		InstantiatedEntityListener listener = null;
		if (gmContentView != null)
			listener = GMEUtil.getInstantiatedEntityListener(gmContentView);
		else
			listener = instantiatedEntityListener;

 		if (listener != null)
			listener.onEntityInstantiated(new InstantiationData(type, instantiationActions, true, isFreeInstantiation, null, title, false, getGmSession() instanceof TransientPersistenceGmSession));
	}
	
	//protected abstract boolean isInstantiable(EntityType<?> entityType);

}
