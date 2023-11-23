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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.data.prompt.SimplifiedAssignment;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;

/**
 * This action is responsible for inserting an existing item to a list, but in the position
 * exactly before as the current selected one.
 * @author michel.docouto
 *
 */
public class InsertExistingBeforeToListAction extends ModelAction implements ManipulationListener, LocalManipulationAction /*, MetaDataReevaluationHandler*/ {
	
	//private boolean configureReevaluationTrigger = true;
	private Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> entitySelectionFutureProviderProvider;
	private Function<SelectionConfig, ? extends Future<InstanceSelectionData>> entitySelectionFutureProvider;
	private int maxEntriesToAdd = Integer.MAX_VALUE;
	private PropertyRelatedModelPathElement collectionElement;
	private EntityType<?> collectionElementType;
	private ListItemPathElement listItemElement;
	private PersistenceGmSession gmSession;
	private PersistenceGmSession workbenchSession;
	private InstantiateEntityAction instantiateEntityAction;
	private Map<GenericEntity, InstantiatedEntityListener> instantiatedEntityAndListenerMap;
	private LocalManipulationListener listener;
	
	public InsertExistingBeforeToListAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.insertBefore());
		setIcon(GmViewActionResources.INSTANCE.add());
		setHoverIcon(GmViewActionResources.INSTANCE.addBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	/**
	 * Configures the required workbench {@link PersistenceGmSession}.
	 */
	@Required
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configure an required provider which provides an entity selection future provider.
	 */
	@Required
	public void setEntitySelectionFutureProvider(Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> entitySelectionFutureProvider) {
		this.entitySelectionFutureProviderProvider = entitySelectionFutureProvider;
	}
	
	public void configureInstantiateEntityAction(InstantiateEntityAction instantiateEntityAction) {
		this.instantiateEntityAction = instantiateEntityAction;
	}
	
	@Override
	public void configureListener(LocalManipulationListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		try {
			if (entitySelectionFutureProvider == null) {
				entitySelectionFutureProvider = entitySelectionFutureProviderProvider.get();
			}
			PersistenceGmSession gmSession = getGmSession(collectionElementType);
			final NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
			boolean simplified = false;
			boolean useDetail = true;
			boolean instantiable = true;
			boolean referenceable = true;
			GenericEntity parentEntity = collectionElement.getEntity();
			try {
				ModelMdResolver modelMdResolver = gmSession.getModelAccessory().getMetaData();
				PropertyMdResolver propertyMdResolver = modelMdResolver.lenient(true).entity(parentEntity)
						.property(collectionElement.getProperty());
				SimplifiedAssignment sa = propertyMdResolver.meta(SimplifiedAssignment.T).exclusive();
				simplified = sa != null;
				useDetail = sa.getShowDetails();
				instantiable = GMEMetadataUtil.isInstantiable(propertyMdResolver, modelMdResolver);
				referenceable = GMEMetadataUtil.isReferenceable(propertyMdResolver, modelMdResolver);
			} catch(Exception ex) {
				simplified = false;
			}
			
			if (!simplified && collectionElementType.isSimple())
				simplified = true;
			
			entitySelectionFutureProvider
					.apply(new SelectionConfig(collectionElementType, maxEntriesToAdd,
							GMEUtil.prepareQueryEntityProperty(parentEntity, collectionElement.getProperty()), gmSession, workbenchSession,
							instantiable, referenceable, simplified, useDetail, collectionElement)) //
					.andThen(instanceSelectionData -> {
						List<GMTypeInstanceBean> result = instanceSelectionData == null ? null : instanceSelectionData.getSelections();
						if (result != null) {
							GMEUtil.insertToListOrSet(collectionElement, result, listItemElement.getIndex());
							nestedTransaction.commit();

							if (result.size() == 1 && result.get(0).isHandleInstantiation()) {
								if (instantiateEntityAction != null)
									instantiateEntityAction.handleInstantiation(result.get(0), true);
								else
									handleInstantiation(result.get(0));
							}
							fireListener();
						} else {
							try {
								nestedTransaction.rollback();
							} catch (TransactionException e) {
								e.printStackTrace();
								ErrorDialog.show(LocalizedText.INSTANCE.errorRollingBack(), e);
							}
						}
					}).onError(e -> {
						try {
							nestedTransaction.rollback();
						} catch (TransactionException e1) {
							e1.printStackTrace();
						}

						e.printStackTrace();
						ErrorDialog.show(LocalizedText.INSTANCE.errorAddingEntries(), e);
					});
		} catch (RuntimeException e) {
			ErrorDialog.show(LocalizedText.INSTANCE.errorAddingEntries(), e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (instantiatedEntityAndListenerMap == null || (!(manipulation instanceof DeleteManipulation) && !(manipulation instanceof InstantiationManipulation)))
			return;
		
		GenericEntity entity = manipulation instanceof DeleteManipulation ?
				((DeleteManipulation) manipulation).getEntity() : ((InstantiationManipulation) manipulation).getEntity();
		InstantiatedEntityListener listener = instantiatedEntityAndListenerMap.get(entity);
		if (listener != null) {
			RootPathElement rootPathElement = new RootPathElement(entity.entityType(), entity);
			if (manipulation instanceof DeleteManipulation)
				listener.onEntityUninstantiated(rootPathElement);
			else
				listener.onEntityInstantiated(rootPathElement, false, false, null);
		}
	}
	
	private void handleInstantiation(GMTypeInstanceBean bean) {
		InstantiatedEntityListener listener = GMEUtil.getInstantiatedEntityListener(gmContentView);
		if (listener != null) {
			listener.onEntityInstantiated(new RootPathElement(bean.getGenericModelType(), bean.getInstance()), true, true, null);
			if (instantiatedEntityAndListenerMap == null)
				instantiatedEntityAndListenerMap = new HashMap<GenericEntity, InstantiatedEntityListener>();
			instantiatedEntityAndListenerMap.put((GenericEntity) bean.getInstance(), listener);
		}
	}
	
	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		updateVisibility(selectorContext);
	}*/

	@Override
	protected void updateVisibility(/*SelectorContext selectorContext*/) {
		collectionElement = null;
		collectionElementType = null;
		listItemElement = null;
		
		if (modelPaths == null || modelPaths.size() != 1) {
			setHidden(true, true);
			return;
		}
		
		List<ModelPath> selection = modelPaths.get(0);
		for (ModelPath modelPath : selection) {
			ModelPathElement element = modelPath.get(modelPath.size() - 1);
			if (!(element instanceof ListItemPathElement))
				continue;
			
			listItemElement = (ListItemPathElement) element;
			collectionElement = (PropertyRelatedModelPathElement) modelPath.get(modelPath.size() - 2);
			if (!collectionElement.getType().isCollection())
				continue;
			
			if (((CollectionType) collectionElement.getType()).getCollectionElementType().isEntity()) {
				collectionElementType = (EntityType<?>) ((CollectionType) collectionElement.getType()).getCollectionElementType();
				//The checkSharesEntitiesCollectionActionVisibility method is handling the visibility
				maxEntriesToAdd = ActionUtil.checkSharesEntitiesCollectionActionVisibility(collectionElement, /* selectorContext, configureReevaluationTrigger, this, */
						this, gmContentView.getUseCase());
				//configureReevaluationTrigger = false; //Configure only once
				if (maxEntriesToAdd > -1)
					return;
			}
		}
		
		setHidden(true, true);
	}
	
	protected PersistenceGmSession getGmSession(GenericModelType type) {
		PersistenceGmSession session = GMEUtil.getSessionOrAlternativeSessionFromViewBasedOnType(gmContentView, type);
		if (gmSession == null || gmSession != session) {
			if (gmSession != null)
				gmSession.listeners().remove(this);
			
			gmSession = session;
			if (instantiateEntityAction == null)
				gmSession.listeners().add(this);
		}
		
		return gmSession;
	}
	
	private void fireListener() {
		if (listener != null)
			listener.onManipulationPerformed();
	}
	
}
