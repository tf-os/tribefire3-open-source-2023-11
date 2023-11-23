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

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.metadata.client.MetaDataEditorPanelHandler;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil.KeyAndValueGMTypeInstanceBean;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.meta.data.prompt.SimplifiedAssignment;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.meta.data.prompt.VirtualEnumConstant;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.google.gwt.core.client.Scheduler;

/**
 * This action provides a PopUp for selecting available entities to be added to a collection.
 * @author michel.docouto
 *
 */
public class AddExistingEntitiesToCollectionAction extends ModelAction implements ManipulationListener, LocalManipulationAction/*, MetaDataReevaluationHandler*/ {
	
	private int maxEntriesToAdd = Integer.MAX_VALUE;
	//private boolean configureReevaluationTrigger = true;
	private Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> entitySelectionFutureProviderProvider;
	private Function<SelectionConfig, ? extends Future<InstanceSelectionData>> entitySelectionFutureProvider;
	//private ModelPath selectedValue;
	private PropertyRelatedModelPathElement collectionElement;
	private PersistenceGmSession gmSession;
	private PersistenceGmSession workbenchSession;
	private InstantiateEntityAction instantiateEntityAction;
	private Map<GenericEntity, InstantiatedEntityListener> instantiatedEntityAndListenerMap;
	private LocalManipulationListener listener;
	
	public AddExistingEntitiesToCollectionAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.add());
		setIcon(GmViewActionResources.INSTANCE.newInstance());
		setHoverIcon(GmViewActionResources.INSTANCE.newInstanceBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	/**
	 * Configure an required provider which provides an entity selection future provider.
	 */
	@Required
	public void setEntitySelectionFutureProvider(
			Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> entitySelectionFutureProvider) {
		this.entitySelectionFutureProviderProvider = entitySelectionFutureProvider;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession} workbench session.
	 */
	@Required
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
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
		if (entitySelectionFutureProvider == null)
			entitySelectionFutureProvider = entitySelectionFutureProviderProvider.get();
		
		if (isSettingGmPropertyInitializer(collectionElement.getProperty().getName(), collectionElement.getEntity())) {
			handleGmPropertyInitializer((GmProperty) collectionElement.getEntity());
			return;
		}
		
		CollectionType collectionType = (CollectionType) collectionElement.getType();
		NestedTransaction nestedTransaction = getGmSession(collectionType.getCollectionElementType()).getTransaction().beginNestedTransaction();
		
		if (CollectionKind.map.equals(collectionType.getCollectionKind()))
			addToMap(nestedTransaction, collectionType);
		else
			addToCollection(nestedTransaction, collectionType);
	}
	
	private void handleGmPropertyInitializer(GmProperty gmProperty) {
		GmCollectionType gmCollectionType = (GmCollectionType) gmProperty.getType();
		GmType gmType = gmCollectionType.collectionElementType();
		List<Object> possibleValues = null;
		
		TypeCondition tc = getTypeCondition(gmType);
		
		if (gmType != null && gmType.isGmEnum())
			possibleValues = ((GmEnumType) gmType).getConstants().stream().collect(Collectors.toList());
		
		GenericModelType genericModelType = gmType == null ? null : GMF.getTypeReflection().findType(gmType.getTypeSignature());
		if (genericModelType == null)
			genericModelType = BaseType.INSTANCE;
		
		SelectionConfig sc = new SelectionConfig(genericModelType, maxEntriesToAdd, null, gmSession, workbenchSession, false,
				true, true, true, collectionElement);
		sc.setTypeCondition(tc);
		if (possibleValues != null)
			sc.setPossibleValues(possibleValues);
		sc.setParentContentView(gmContentView);
		sc.setAddingToSet(gmCollectionType.typeKind().equals(GmTypeKind.SET));
		
		PersistenceGmSession gmSession = getGmSession(BaseType.INSTANCE);
		final NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
		
		entitySelectionFutureProvider.apply(sc) //
				.andThen(result -> handleReturn(result, nestedTransaction)) //
				.onError(e -> handleFailure(nestedTransaction, e));
	}
	
	private TypeCondition getTypeCondition(GmType gmType) {
		if (gmType == null || gmType.isGmEntity())
			return TypeConditions.isKind(TypeKind.simpleType);
		
		return TypeConditions.isType(gmType.getTypeSignature());
	}

	private void addToCollection(final NestedTransaction nestedTransaction, final CollectionType type) {
		getElementToAdd(type.getCollectionElementType(), maxEntriesToAdd, type.getCollectionKind().equals(CollectionKind.set), null, null) //
				.andThen(result -> handleReturn(result, nestedTransaction)) //
				.onError(e -> handleFailure(nestedTransaction, e));
	}
	
	private void handleReturn(InstanceSelectionData result, NestedTransaction nestedTransaction) {
		boolean rollback = true;
		List<GMTypeInstanceBean> beans = result != null ? result.getSelections() : null;
		if (beans != null) {
			GMEUtil.insertToListOrSet(collectionElement, beans, -1);
			nestedTransaction.commit();
			rollback = false;

			if (beans.size() == 1 && beans.get(0).isHandleInstantiation()) {
				if (instantiateEntityAction != null)
					instantiateEntityAction.handleInstantiation(beans.get(0), true);
				else
					handleInstantiation(beans.get(0));
			}
		}

		if (rollback)
			performRollback(nestedTransaction);
		else
			fireAddToCollectionListener();
	}

	private void addToMap(final NestedTransaction nestedTransaction, final CollectionType type) {
		LocalizedText localizedText = LocalizedText.INSTANCE;
		getElementToAdd(type.getParameterization()[0], maxEntriesToAdd, true, localizedText.addingMapKey(), localizedText.toMapKey()) //
				.andThen(result -> {
					if (result == null) {
						performRollback(nestedTransaction);
						return;
					}

					Scheduler.get().scheduleDeferred(() -> handleAddToMap(nestedTransaction, type, new ArrayList<>(result.getSelections())));
				}).onError(e -> handleFailure(nestedTransaction, e));
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (instantiatedEntityAndListenerMap == null || !(manipulation instanceof DeleteManipulation || manipulation instanceof InstantiationManipulation))
			return;
		
		GenericEntity entity = manipulation instanceof DeleteManipulation ? ((DeleteManipulation) manipulation).getEntity()
				: ((InstantiationManipulation) manipulation).getEntity();
		InstantiatedEntityListener listener = instantiatedEntityAndListenerMap.get(entity);
		if (listener != null) {
			RootPathElement rootPathElement = new RootPathElement(entity.entityType(), entity);
			if (manipulation instanceof DeleteManipulation)
				listener.onEntityUninstantiated(rootPathElement);
			else
				listener.onEntityInstantiated(rootPathElement, false, false, null);
		}
	}
	
	private void handleAddToMap(NestedTransaction nestedTransaction, CollectionType type, List<GMTypeInstanceBean> keyBeans) {
		LocalizedText localizedText = LocalizedText.INSTANCE;
		getElementToAdd(type.getParameterization()[1], keyBeans.size(), false, localizedText.addingMapValue(), localizedText.toMapValue()) //
				.andThen(result -> {
					boolean rollback = true;
					List<GMTypeInstanceBean> valueBeans = result == null ? null : result.getSelections();
					if (valueBeans == null)
						GlobalState.showProcess(localizedText.differentAmountOfKeysAndValues(keyBeans.size(), 0));
					else {
						if (keyBeans.size() != valueBeans.size())
							GlobalState.showProcess(localizedText.differentAmountOfKeysAndValues(keyBeans.size(), valueBeans.size()));
						else {
							List<KeyAndValueGMTypeInstanceBean> keysAndValues = new ArrayList<>();
							for (int i = 0; i < keyBeans.size(); i++)
								keysAndValues.add(new KeyAndValueGMTypeInstanceBean(keyBeans.get(i), valueBeans.get(i)));
							GMEUtil.insertOrRemoveToCollection(collectionElement, keysAndValues, true);
							nestedTransaction.commit();
							rollback = false;
						}
					}

					if (rollback)
						performRollback(nestedTransaction);
					else
						fireAddToCollectionListener();
				}).onError(e -> handleFailure(nestedTransaction, e));
	}
	
	private void handleInstantiation(GMTypeInstanceBean bean) {
		InstantiatedEntityListener listener = GMEUtil.getInstantiatedEntityListener(gmContentView);
		if (listener != null) {
			listener.onEntityInstantiated(new RootPathElement(bean.getGenericModelType(), bean.getInstance()), true, true, null);
			if (instantiatedEntityAndListenerMap == null)
				instantiatedEntityAndListenerMap = new HashMap<>();
			instantiatedEntityAndListenerMap.put((GenericEntity) bean.getInstance(), listener);
		}
	}
	
	private Future<InstanceSelectionData> getElementToAdd(GenericModelType type, int maxEntriesToAdd, boolean addingToSetOrMapKey, String title,
			String subTitleSufix) {
		GenericEntity parentEntity = collectionElement.getEntity();
		Property property = collectionElement.getProperty();
		
		boolean simplified = false;
		boolean useDetail = true;
		String typeName = type.getTypeName();
		boolean instantiable = true;
		boolean referenceable = true;
		if (parentEntity != null) {
			ModelMdResolver modelMdResolver = getMetaData(parentEntity).lenient(true);
			EntityMdResolver entityMdResolver = modelMdResolver.entity(parentEntity);
			PropertyMdResolver propertyMdResolver = entityMdResolver.property(property);
			
			SimplifiedAssignment sa = propertyMdResolver.meta(SimplifiedAssignment.T).exclusive();
			if (sa != null) {
				useDetail = sa.getShowDetails();
				simplified = true;
			}
			
			String propertyName = property.getName();
			if (title == null)
				title = LocalizedText.INSTANCE.addProperty(GMEMetadataUtil.getPropertyDisplay(propertyName, propertyMdResolver),
						SelectiveInformationResolver.resolve(parentEntity, entityMdResolver));
			
			if (type.isEntity())
				typeName = GMEMetadataUtil.getEntityNameMDOrShortName((EntityType<?>) type, modelMdResolver, gmContentView.getUseCase());
			
			instantiable = GMEMetadataUtil.isInstantiable(propertyMdResolver, modelMdResolver);
			referenceable = GMEMetadataUtil.isReferenceable(propertyMdResolver, modelMdResolver);
		}
		
		if (!simplified && (type.isSimple()) || type.isEnum())
			simplified = true;
		
		String subTitle = LocalizedText.INSTANCE.selectType(typeName);
		if (subTitleSufix != null)
			subTitle += " " + subTitleSufix;
		
		SelectionConfig config = new SelectionConfig(type, maxEntriesToAdd,
				GMEUtil.prepareQueryEntityProperty(parentEntity, property), getGmSession(type), workbenchSession, 
				instantiable, referenceable, simplified, useDetail, collectionElement);
		config.setSubTitle(subTitle);
		config.setParentContentView(gmContentView);
		config.setAddingToSet(addingToSetOrMapKey);
		config.setTitle(title);
		
		if (type.isEnum()) {
			List<Object> possibleValues = Arrays.asList((Object[]) ((EnumType) type).getEnumValues());
			config.setPossibleValues(possibleValues);
		}
		
		return entitySelectionFutureProvider.apply(config);
	}
	
	private void handleFailure(NestedTransaction nestedTransaction, Throwable e) {
		try {
			nestedTransaction.rollback();
		} catch (TransactionException e1) {
			e1.printStackTrace();	
		}
		
		ErrorDialog.show(LocalizedText.INSTANCE.errorAddingEntries(), e);
		e.printStackTrace();
	}
	
	private void performRollback(NestedTransaction nestedTransaction) {
		try {
			nestedTransaction.rollback();
		} catch (TransactionException e) {
			e.printStackTrace();
			ErrorDialog.show(LocalizedText.INSTANCE.errorRollingBack(), e);
		}
	}
	
	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		updateVisibility(selectorContext);
	}*/
	
	@Override
	protected void updateVisibility(/*SelectorContext selectorContext*/) {
		//selectedValue = null;
		if 	(this.gmContentView instanceof MetaDataEditorPanelHandler) {
			setHidden(true, true);
			return;
		}
				
		if (!canAddForModelPaths(modelPaths, true)) {
			setHidden(true, true);
			return;			
		}			
	}
	
	public boolean canAddForModelPaths(List<List<ModelPath>> modelPaths) {
		return canAddForModelPaths(modelPaths, false);
	}

	private boolean canAddForModelPaths(List<List<ModelPath>> modelPaths, boolean checkForceSelection) {
		collectionElement = null;

		if (modelPaths == null || modelPaths.size() != 1) {
			return false;
		}
		
		List<ModelPath> selection = modelPaths.get(0);
		for (ModelPath selectedValue : selection) {
			ModelPathElement element = selectedValue.get(selectedValue.size() - 1);
			if (element instanceof PropertyRelatedModelPathElement && isCollection(element.getType(),
					((PropertyRelatedModelPathElement) element).getProperty().getName(), ((PropertyRelatedModelPathElement) element).getEntity())) {
				CollectionType ct = null;
				if (element.getType().isCollection())
					ct = (CollectionType) element.getType();
				
				if (ct == null || ct.getCollectionElementType() != VirtualEnumConstant.T) {										
					this.collectionElement = (PropertyRelatedModelPathElement) element;
					
					PropertyRelatedModelPathElement propertyElement = (PropertyRelatedModelPathElement) element;
					GenericEntity parentEntity = propertyElement.getEntity();
					Property property = propertyElement.getProperty();
					ModelMdResolver modelMdResolver = GmSessions.getMetaData(parentEntity).useCase(gmContentView.getUseCase());
					PropertyMdResolver propertyMdResolver = modelMdResolver.entity(parentEntity).property(property);
					
					if (checkForceSelection) {
						VirtualEnum virtualEnum = propertyMdResolver.meta(VirtualEnum.T).exclusive();
						if (virtualEnum != null)
							if (virtualEnum.getForceSelection())
								continue;
					}
					
					maxEntriesToAdd = ActionUtil.checkSharesEntitiesCollectionActionVisibility(this.collectionElement, this, gmContentView.getUseCase());
					//configureReevaluationTrigger = false; //Configure only once
					if (maxEntriesToAdd > -1) {
						//this.selectedValue = selectedValue;
						return true;
					}
				}
			}
			
			//checkReevaluation();
		}
		return false;
	}	
	
	private boolean isCollection(GenericModelType propertyType, String propertyName, GenericEntity parentEntity) {
		if (propertyType.isCollection())
			return true;
		
		return isSettingGmPropertyInitializer(propertyName, parentEntity) && ((GmProperty) parentEntity).getType() != null
				&& ((GmProperty) parentEntity).getType().isGmCollection();
	}
	
	private boolean isSettingGmPropertyInitializer(String propertyName, GenericEntity parentEntity) {
		return "initializer".equals(propertyName) && parentEntity instanceof GmProperty;
	}
	
	/*private void checkReevaluation() {
		delegateModel = selectedModel.getDelegate();
		if (selectedModel instanceof PropertyEntryModelInterface) {
			delegateModel = ((PropertyEntryModelInterface) selectedModel).getPropertyDelegate();
		}
		if ((delegateModel instanceof ListTreeModelInterface || delegateModel instanceof SetTreeModel || delegateModel instanceof CondensedEntityTreeModel) &&
				delegateModel.getElementType() instanceof EntityType) {
			if (delegateModel instanceof SetTreeModel) {
				ignoreEntitiesInSet = new HashSet<GenericEntity>();
				for (ModelData model : delegateModel.getChildren()) {
					ignoreEntitiesInSet.add((GenericEntity) ((AbstractGenericTreeModel) model).getModelObject());
				}
			} else
				ignoreEntitiesInSet = null;
			
			EntityTreeModel entityTreeModel;
			if (selectedModel instanceof EntityModelInterface) {
				entityTreeModel = ((EntityModelInterface) selectedModel).getEntityTreeModel();
			} else {
				if (delegateModel instanceof CondensedEntityTreeModel) {
					entityTreeModel = ((CondensedEntityTreeModel) delegateModel).getEntityTreeModel();
					delegateModel = ((CondensedEntityTreeModel) delegateModel).getPropertyDelegate();
				} else
					entityTreeModel = ((EntityModelInterface) delegateModel.getParent()).getEntityTreeModel();
			}
			
			if (delegateModel != null) { //it may be absent
				maxEntriesToAdd = AssemblyUtil.checkSharesEntitiesCollectionActionVisibility(delegateModel, entityTreeModel, selectorContext,
						configureReevaluationTrigger, this, this, metaDataResolver, false);
				configureReevaluationTrigger = false; //Configure only once
				return;
			}
		}
	}*/
	
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
	
	private void fireAddToCollectionListener() {
		if (listener != null)
			listener.onManipulationPerformed();
	}
	
}
