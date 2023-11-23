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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.InstantiationData;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.MapKeyPathElement;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
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
import com.braintribe.model.meta.data.prompt.SimplifiedAssignment;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * This action is responsible for changing an object instance by another instance of the same {@link GenericModelType}.
 * @author michel.docouto
 *
 */
public class ChangeInstanceAction extends ModelAction implements ManipulationListener, LocalManipulationAction/*, MetaDataReevaluationHandler*/ {
	
	private boolean enableIfParentIsCollection = false;
	private boolean enableIfParentIsMap = true;
	//private boolean configureReevaluationTrigger = true;
	private Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> instanceSelectionFutureProviderProvider;
	private Function<SelectionConfig, ? extends Future<InstanceSelectionData>> instanceSelectionFutureProvider;
	//private ModelPath selectedValue;
	private ModelPathElement parentElement;
	private PropertyRelatedModelPathElement instanceElement;
	private PropertyRelatedModelPathElement mapKeyOrValuePathElement;
	private PersistenceGmSession gmSession;
	private PersistenceGmSession workbenchSession;
	private InstantiateEntityAction instantiateEntityAction;
	private Map<GenericEntity, InstantiatedEntityListener> instantiatedEntityAndListenerMap;
	private boolean instantiable = true;
	private boolean referenceable = true;
	private boolean simplified = false;//fetch via MD;
	private ModelOracle modelOracle;
	private ManipulationListener propertyInitializerListener;
	private LocalManipulationListener listener;
	
	public ChangeInstanceAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.assign());
		setIcon(GmViewActionResources.INSTANCE.changeInstance());
		setHoverIcon(GmViewActionResources.INSTANCE.changeInstanceBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession} workbench session.
	 * May be null.
	 */
	@Configurable
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configure an required provider which provides an instance selection future provider.
	 */
	@Required
	public void setInstanceSelectionFutureProvider(
			Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> instanceSelectionFutureProvider) {
		this.instanceSelectionFutureProviderProvider = instanceSelectionFutureProvider;
	}

	/**
	 * Configures whether to enable this action if the parent is a collection.
	 * Defaults to false (the action is disabled if the parent of the selected model is a collection).
	 */
	@Configurable
	public void setEnableIfParentIsCollection(Boolean enableIfParentIsCollection) {
		this.enableIfParentIsCollection = enableIfParentIsCollection;
	}
	
	/**
	 * Configures whether to enable this action if the parent is a map, and the selected entry is a map value.
	 * Defaults to true).
	 */
	@Configurable
	public void setEnableIfParentIsMap(Boolean enableIfParentIsMap) {
		this.enableIfParentIsMap = enableIfParentIsMap;
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
			if (instanceSelectionFutureProvider == null)
				instanceSelectionFutureProvider = instanceSelectionFutureProviderProvider.get();
			
			GenericModelType gmType;
			GenericEntity parentEntity;
			Property property;
			boolean useDetail = true;
			boolean sia = false;
			if (instanceElement != null) {
				property = instanceElement.getProperty();
				gmType = property.getType();
				parentEntity = instanceElement.getEntity();
			} else {
				gmType = mapKeyOrValuePathElement.getType();
				parentEntity = mapKeyOrValuePathElement.getEntity();
				property = mapKeyOrValuePathElement.getProperty();
				instantiable = true;
				referenceable = true;
			}
			
			if (isSettingGmPropertyInitializer(property, parentEntity)) { //We need a special handling for this case
				handleGmPropertyInitializer((GmProperty) parentEntity);
				return;
			}
			
			ModelMdResolver modelMdResolver = getMetaData(parentEntity).lenient(true);
			String useCase = gmContentView.getUseCase();
			EntityMdResolver entityMdResolver = modelMdResolver.entity(parentEntity).useCase(useCase);
			PropertyMdResolver propertyMetaDataBuilder = entityMdResolver.property(property).useCase(useCase);
			
			if (parentEntity != null) {
				SimplifiedAssignment sa = propertyMetaDataBuilder.meta(SimplifiedAssignment.T).exclusive();
				if (sa != null) {
					useDetail = sa.getShowDetails();
					if (gmSession != null) {
						try {
							if(gmType.isEntity()) {
								EntityType<?> entityType = (EntityType<?>)gmType;
								modelOracle = gmSession.getModelAccessory().getOracle();
								EntityTypeOracle entityTypeOracle = modelOracle.findEntityTypeOracle(gmType.getTypeSignature());
								boolean hasSubTypes = !entityTypeOracle.getSubTypes().asGmTypes().isEmpty();
								sia = !entityType.isAbstract() && !hasSubTypes && !referenceable && instantiable;
							}
						}catch(Exception ex) {
							sia = false;
						}
					}
					simplified = true;
				}
			}
			
			PersistenceGmSession gmSession = getGmSession(gmType);
			final NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
			
			if (!simplified && gmType.isSimple())
				simplified = true;
			
			SelectionConfig sc = new SelectionConfig(gmType, 1, GMEUtil.prepareQueryEntityProperty(parentEntity, property),
					gmSession, workbenchSession, instantiable, referenceable, simplified, useDetail, instanceElement);
			
			sc.setTitle(LocalizedText.INSTANCE.assignProperty(GMEMetadataUtil.getPropertyDisplay(property.getName(), propertyMetaDataBuilder),
					SelectiveInformationResolver.resolve(parentEntity, entityMdResolver)));
			String typeName;
			if (gmType.isEntity())
				typeName = GMEMetadataUtil.getEntityNameMDOrShortName((EntityType<?>) gmType, modelMdResolver, useCase);
			else
				typeName = gmType.getTypeName();
			sc.setSubTitle(LocalizedText.INSTANCE.selectType(typeName));
			sc.setParentContentView(gmContentView);
			sc.setSingleInstantiationAndAssignment(sia);
			
			instanceSelectionFutureProvider.apply(sc).get(prepareSelectionFutureProviderCallback(nestedTransaction));
		} catch (RuntimeException e) {
			ErrorDialog.show(LocalizedText.INSTANCE.errorChangingProperty(), e);
			e.printStackTrace();
		}
	}

	private boolean isSettingGmPropertyInitializer(Property property, GenericEntity parentEntity) {
		return "initializer".equals(property.getName()) && parentEntity instanceof GmProperty;
	}

	private AsyncCallback<InstanceSelectionData> prepareSelectionFutureProviderCallback(final NestedTransaction nestedTransaction) {
		return AsyncCallbacks.of( //
				result -> changeInstance(result, nestedTransaction), //
				e -> {
					try {
						nestedTransaction.rollback();
					} catch (TransactionException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
					ErrorDialog.show(LocalizedText.INSTANCE.errorChangingProperty(), e);
				});
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (instantiatedEntityAndListenerMap == null || !(manipulation instanceof DeleteManipulation || manipulation instanceof InstantiationManipulation))
			return;
		
		GenericEntity entity = manipulation instanceof DeleteManipulation ? ((DeleteManipulation) manipulation).getEntity()
				: ((InstantiationManipulation) manipulation).getEntity();
		InstantiatedEntityListener listener = instantiatedEntityAndListenerMap.get(entity);
		if (listener == null)
			return;
		
		ModelPathElement modelPathElement;
		if (instanceElement == null)
			modelPathElement = new RootPathElement(entity.entityType(), entity);
		else {
			modelPathElement = instanceElement.copy();
			modelPathElement.setValue(entity);
		}
		
		if (manipulation instanceof DeleteManipulation)
			listener.onEntityUninstantiated(modelPathElement);
		else
			listener.onEntityInstantiated(modelPathElement, false, false, null);
	}
	
	private void handleInstantiation(GMTypeInstanceBean bean, ModelPathElement instanceElement, Widget parentWidget) {
		InstantiatedEntityListener listener = GMEUtil.getInstantiatedEntityListener(gmContentView);
		if (listener == null)
			return;
		
		ModelPathElement modelPathElement;
		if (instanceElement == null)
			modelPathElement = new RootPathElement(bean.getGenericModelType(), bean.getInstance());
		else {
			modelPathElement = instanceElement.copy();
			modelPathElement.setValue(bean.getInstance());
		}
		
		InstantiationData instantiationData = new InstantiationData(modelPathElement, true, true, null, false, false);
		instantiationData.setParentWidget(parentWidget);
		listener.onEntityInstantiated(instantiationData);
		if (instantiatedEntityAndListenerMap == null)
			instantiatedEntityAndListenerMap = new HashMap<>();
		instantiatedEntityAndListenerMap.put((GenericEntity) bean.getInstance(), listener);
	}
	
	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		updateVisibility(selectorContext);
	}*/
	
	protected PropertyRelatedModelPathElement getEntityElement() {
		return instanceElement;
	}
	
	@Override
	protected void updateVisibility(/*SelectorContext selectorContext*/) {
		instanceElement = null;
		//selectedValue = null;
		mapKeyOrValuePathElement = null;
		
		if (modelPaths == null || modelPaths.size() != 1) {
			setHidden(true, true);
			return;
		}
		
		List<ModelPath> selection = modelPaths.get(0);
		for (ModelPath selectedValue : selection) {
			if (selectedValue.size() <= 1) //does not have parent
				continue;
			
			ModelPathElement parentElement = selectedValue.get(selectedValue.size() - 2);
			ModelPathElement last = selectedValue.last();
			if (parentElement instanceof MapValuePathElement && enableIfParentIsMap && (last instanceof MapValuePathElement || last instanceof MapKeyPathElement)) {
				boolean changingKey = last instanceof MapKeyPathElement;
				MapValuePathElement mapValuePathElement = (MapValuePathElement) parentElement;
				if (!(mapValuePathElement.getType().isCollection())) {
					this.mapKeyOrValuePathElement = changingKey ? (PropertyRelatedModelPathElement) last : mapValuePathElement;
					//this.selectedValue = selectedValue;
					setHidden(false);
					return;
				}
			} else if (parentElement.getType().isCollection() && !enableIfParentIsCollection)
				continue;
		
			this.parentElement = parentElement;
			ModelPathElement element = selectedValue.get(selectedValue.size() - 1);
			
			if (!(element instanceof PropertyRelatedModelPathElement) || isPropertyTypeCollection(
					(((PropertyRelatedModelPathElement) element).getProperty()), ((PropertyRelatedModelPathElement) element).getEntity())) {
				continue;
			}
			
			instanceElement = (PropertyRelatedModelPathElement) element;
			
			Property property = instanceElement.getProperty();
			Class<?> propertyJavaType = property.getType().getJavaType();
			if (propertyJavaType == Boolean.class || (!parentElement.getType().isCollection() && propertyJavaType == String.class))
				continue;
			
			String propertyName = property.getName();
			GenericEntity parentEntity = instanceElement.getEntity();
			
			ModelMdResolver modelMdResolver = getMetaData(parentEntity).useCase(gmContentView.getUseCase());
			PropertyMdResolver propertyMdResolver = modelMdResolver.entity(parentEntity).property(propertyName);
			boolean editable = GMEMetadataUtil.isPropertyEditable(propertyMdResolver, parentEntity);
			
			if (!editable)
				continue;
			
			//handleReevaluation();
			
			instantiable = GMEMetadataUtil.isInstantiable(propertyMdResolver, modelMdResolver);
			referenceable = GMEMetadataUtil.isReferenceable(propertyMdResolver, modelMdResolver);
			
			try {
				SimplifiedAssignment sa = propertyMdResolver.meta(SimplifiedAssignment.T).exclusive();
				simplified = sa != null;
			}catch(Exception ex) {
				simplified = false;
			}
			
			if (instantiable || referenceable) {
				//this.selectedValue = selectedValue;
				setHidden(false);
				return;
			}
		}
		
		setHidden(true, true);
	}
	
	private boolean isPropertyTypeCollection(Property property, GenericEntity parentEntity) {
		if (property.getType().isCollection())
			return true;
		
		if (isSettingGmPropertyInitializer(property, parentEntity)) {
			Property typeProperty = parentEntity.entityType().getProperty("type");
			if (GMEUtil.isPropertyAbsent(parentEntity, typeProperty)) {
				propertyInitializerListener = manipulation -> {
					((PersistenceGmSession) parentEntity.session()).listeners().entity(parentEntity).property(typeProperty)
							.remove(propertyInitializerListener);
					propertyInitializerListener = null;
					if (gmContentView != null) {
						ModelPath modelPath = new ModelPath();
						modelPath.add(new RootPathElement(parentEntity));
						gmContentView.setContent(modelPath);
					}
				};
				
				((PersistenceGmSession) parentEntity.session()).listeners().entity(parentEntity).property(typeProperty)
						.add(propertyInitializerListener);
				return false;
			}
			GmType gmType = ((GmProperty) parentEntity).getType();
			return gmType != null && gmType.isGmCollection();
		}
		
		return false;
	}
	
	/*protected String getChangeName(EntityType<?> entityType) {
		return LocalizedText.INSTANCE.changeEntityType(
				GMEUtil.getEntityDisplayInfoOrShortName((EntityType<?>) instanceElement.getType(), getGmSession().getModelAccessory().getCascadingMetaDataResolver(), useCase));
	}*/
	
	protected void changeInstance(InstanceSelectionData result, NestedTransaction nestedTransaction) {
		if (result == null) {
			try {
				nestedTransaction.rollback();
			} catch (TransactionException e) {
				e.printStackTrace();
				ErrorDialog.show(LocalizedText.INSTANCE.errorRollingBack(), e);
			}
			return;
		}
		
		GMTypeInstanceBean instanceBean = result.getSelections().get(0);
		if (mapKeyOrValuePathElement != null) {
			PropertyRelatedModelPathElement mapElement = (PropertyRelatedModelPathElement) mapKeyOrValuePathElement.getPrevious();
			if (!(mapElement.getValue() instanceof Map))
				mapElement = (PropertyRelatedModelPathElement) mapElement.getPrevious();
			GMEUtil.replaceInCollection(mapElement, mapKeyOrValuePathElement, instanceBean.getInstance());
		} else if (parentElement.getType().isEntity()) {
			GMEUtil.changeEntityPropertyValue(instanceElement.getEntity(), instanceElement.getProperty(), instanceBean.getInstance());
		} else if (parentElement instanceof PropertyRelatedModelPathElement)
			GMEUtil.replaceInCollection((PropertyRelatedModelPathElement) parentElement, instanceElement, instanceBean.getInstance());
		
		nestedTransaction.commit();
		if (instanceBean.isHandleInstantiation()) {
			 if (instantiateEntityAction != null)
				 instantiateEntityAction.handleInstantiation(instanceBean, false);
			 else {
				 ModelPathElement instanceElement = this.instanceElement.copy();
				 Scheduler.get().scheduleDeferred(() -> handleInstantiation(instanceBean, instanceElement, result.getParentWidget()));
			 }
		}

		fireListener();
	}
	
	protected void changeCollectionInstance(Object newValue) {
		GMEUtil.replaceInCollection((PropertyRelatedModelPathElement) parentElement, instanceElement, newValue);
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
	
	private void handleGmPropertyInitializer(GmProperty gmProperty) {
		GmType gmType = gmProperty.getType();
		List<Object> possibleValues = null;
		
		boolean isCollection = gmType != null && gmType.isGmCollection();
		if (isCollection)
			gmType = ((GmCollectionType) gmType).collectionElementType();
		
		TypeCondition tc = getTypeCondition(gmType);
		
		if (gmType != null && gmType.isGmEnum())
			possibleValues = ((GmEnumType) gmType).getConstants().stream().collect(Collectors.toList());
		
		GenericModelType genericModelType = gmType == null ? null : GMF.getTypeReflection().findType(gmType.getTypeSignature());
		if (genericModelType == null)
			genericModelType = BaseType.INSTANCE;
		
		SelectionConfig sc = new SelectionConfig(genericModelType, isCollection ? Integer.MAX_VALUE : 1, null, gmSession, workbenchSession, false,
				false, true, true, instanceElement);
		sc.setTypeCondition(tc);
		if (possibleValues != null)
			sc.setPossibleValues(possibleValues);
		
		PersistenceGmSession gmSession = getGmSession(BaseType.INSTANCE);
		final NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
		
		instanceSelectionFutureProvider.apply(sc).get(prepareSelectionFutureProviderCallback(nestedTransaction));
		
	}
	
	private TypeCondition getTypeCondition(GmType gmType) {
		if (gmType == null || gmType.isGmEntity())
			return TypeConditions.isKind(TypeKind.simpleType);
		
		return TypeConditions.isType(gmType.getTypeSignature());
	}
	
	private void fireListener() {
		if (listener != null)
			listener.onManipulationPerformed();
	}
	
	/*private void handleReevaluation() {
		EntityInfo entityInfo = new EntityInfo(GMF.getTypeReflection().getEntityType(parentEntity), parentEntity);
		MetaDataAndTrigger<PropertyEditable> editableData = MetaDataReevaluationHelper.getPropertyEditableData(entityInfo, propertyName, selectorContext);
		boolean editable = editableData == null || !editableData.isValid() ? true : editableData.getMetaData().getEditable();
		
		MetaDataAndTrigger<PropertySharesEntities> sharesEntitiesData = MetaDataReevaluationHelper.getPropertySharesEntitiesData(entityInfo, propertyName,
				selectorContext);
		boolean sharesEntities = sharesEntitiesData == null || !sharesEntitiesData.isValid() ? true : sharesEntitiesData.getMetaData().getShares();
		
		if (configureReevaluationTrigger) {
			if (editableData != null && editableData.getReevaluationTrigger() != null) {
				MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(editableData.getReevaluationTrigger(), this);
			}
			if (sharesEntitiesData != null && sharesEntitiesData.getReevaluationTrigger() != null) {
				MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(sharesEntitiesData.getReevaluationTrigger(), this);
			}
			configureReevaluationTrigger = false;
		}
	}*/

}
