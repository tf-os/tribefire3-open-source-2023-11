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
package com.braintribe.gwt.metadataeditor.client.action;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.ActionUtil;
import com.braintribe.gwt.gmview.action.client.InstantiateEntityAction;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.metadataeditor.client.MetaDataEditorPanel;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.DeclaredPropertyOverviewExpert;
import com.braintribe.gwt.metadataeditor.client.experts.MetaDataEditorBaseExpert;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorProvider;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmCustomTypeInfo;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;

/**
* This action provides a PopUp for selecting MetaData to be added MetaData directly to declared owner or as Overriden to not declared owner.
*
*/

public class AddDeclaredMetaDataEditorAction extends ModelAction implements ManipulationListener/*, MetaDataReevaluationHandler*/ {
	
	private int maxEntriesToAdd = Integer.MAX_VALUE;
	//private boolean configureReevaluationTrigger = true;
	private Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> entitySelectionFutureProviderProvider;
	private Function<SelectionConfig, ? extends Future<InstanceSelectionData>> entitySelectionFutureProvider;
	//private ModelPath selectedValue;
	private PropertyRelatedModelPathElement collectionElement;
	private Set<ModelPath> modelPaths1;
	private PersistenceGmSession gmSession;
	private PersistenceGmSession workbenchSession;
	private InstantiateEntityAction instantiateEntityAction;
	private Map<GenericEntity, InstantiatedEntityListener> instantiatedEntityAndListenerMap;
	private MetaDataEditorBaseExpert metaDataExpert;
	private ModelMetaDataEditor modelMetaDataEditor = null;
	
	public AddDeclaredMetaDataEditorAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.addMetaData());
		setIcon(GmViewActionResources.INSTANCE.newInstance());
		setHoverIcon(GmViewActionResources.INSTANCE.newInstanceBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	/**
	 * Configure an required provider which provides an entity selection future provider.
	 * @param entitySelectionFutureProvider
	 */
	@SuppressWarnings("javadoc")
	@Required
	public void setEntitySelectionFutureProvider(
			Supplier<? extends Function<SelectionConfig, ? extends Future<InstanceSelectionData>>> entitySelectionFutureProvider) {
		this.entitySelectionFutureProviderProvider = entitySelectionFutureProvider;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession} workbench session.
	 * @param workbenchSession
	 */
	@SuppressWarnings("javadoc")
	@Required
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession} session.
	 * @param gmSession
	 */
	@SuppressWarnings("javadoc")
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	
	public void configureInstantiateEntityAction(InstantiateEntityAction instantiateEntityAction) {
		this.instantiateEntityAction = instantiateEntityAction;
	}
	
	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		updateVisibility(selectorContext);
	}*/
	
	@Override
	protected void updateVisibility() {
		/*
		collectionElement = null;
		
		if (modelPaths != null && (modelPaths.size() == 1 || hasAmbiguousSingleSelection)) {
			for (ModelPath selectedValue : modelPaths) {
				ModelPathElement element = selectedValue.get(selectedValue.size() - 1);
				if (element instanceof PropertyRelatedModelPathElement && element.getType().isCollection()) {
					collectionElement = (PropertyRelatedModelPathElement) element;
					maxEntriesToAdd = ActionUtil.checkSharesEntitiesCollectionActionVisibility(collectionElement,  this,
							gmContentView.getUseCase());
					if (maxEntriesToAdd > -1) {
						return;
					}
				}
				
			}
		}
		
		setHidden(true, true);
		*/
		
		Boolean useHidden = true;
		this.collectionElement = null;
		
		//get MetaData Collection
		if 	(this.gmContentView instanceof MetaDataEditorPanel) {
			MetaDataEditorProvider metaDataEditorProvider = ((MetaDataEditorPanel) this.gmContentView).getEditorProvider();
			if (metaDataEditorProvider != null) {
				this.modelPaths1 = metaDataEditorProvider.getContent();
				this.metaDataExpert = metaDataEditorProvider.getModelExpert();
				if (this.modelPaths1 != null) {
					for (ModelPath modelPath : this.modelPaths1) {
						if (modelPath != null) {
							ModelPathElement modelPathElement =  modelPath.last();
							if (modelPathElement !=null && modelPathElement instanceof PropertyRelatedModelPathElement && modelPathElement.getType() instanceof CollectionType) {
								this.collectionElement = (PropertyRelatedModelPathElement) modelPathElement;
								this.maxEntriesToAdd = ActionUtil.checkSharesEntitiesCollectionActionVisibility(this.collectionElement, /*selectorContext, configureReevaluationTrigger, this,*/ this,
										this.gmContentView.getUseCase());
								
								if (this.metaDataExpert instanceof DeclaredOverviewExpert || this.metaDataExpert instanceof DeclaredPropertyOverviewExpert) {										
									useHidden = false;
									break;
								}								
							}							
						}
					}
				}
			}
		}
		setHidden(useHidden);
	}
	
	
	@Override
	public void perform(TriggerInfo triggerInfo) {		
		final NestedTransaction nestedTransaction = getGmSession().getTransaction().beginNestedTransaction();
		try {
			if (entitySelectionFutureProvider == null) {
				entitySelectionFutureProvider = entitySelectionFutureProviderProvider.get();
			}
			final CollectionType collectionType = (CollectionType) collectionElement.getType();
			
			addToCollection(nestedTransaction, collectionType);
			
		} catch (RuntimeException e) {
			handleFailure(nestedTransaction, e);
		}
	}

	private void addToCollection(final NestedTransaction nestedTransaction, final CollectionType collectionType) {
		getElementToAdd(collectionType.getCollectionElementType(), maxEntriesToAdd).andThen(result -> {
			boolean rollback = true;
			List<GMTypeInstanceBean> beans = result == null ? null : result.getSelections();
			
			if (beans == null) {
				if (rollback)
					performRollback(nestedTransaction);
				return;
			}
			
			GenericEntity entity = collectionElement.getEntity();
			
			if (entity instanceof GmMetaModel) {
				GMEUtil.insertToListOrSet(collectionElement, beans, -1);
				
				nestedTransaction.commit();
				rollback = false;

				if (rollback)
					performRollback(nestedTransaction);
				return;
			}
			
			GmMetaModel editingMetaModel = null;
			GmEntityTypeInfo editingEntityType = null;
			GmEnumTypeInfo editingEnumType = null;
			for (ModelPath modelPath : modelPaths1) {
				if (modelPath == null)
					continue;
				
				for (ModelPathElement modelPathElement : modelPath) {
					if (modelPathElement.getValue() instanceof GmMetaModel)
						editingMetaModel = modelPathElement.getValue();
					if (modelPathElement.getValue() instanceof GmEntityTypeInfo)
						editingEntityType = modelPathElement.getValue();
					if (modelPathElement.getValue() instanceof GmEnumTypeInfo)
						editingEnumType = modelPathElement.getValue();
				}
			}
			
			//Save metaData for GmEntityType and GmEnumType
			if (entity instanceof GmCustomTypeInfo) {
				GmMetaModel declaringModel = ((GmCustomTypeInfo) entity).getDeclaringModel();
				if (editingMetaModel == null || editingMetaModel.equals(declaringModel)) {
					GMEUtil.insertToListOrSet(collectionElement, beans, -1);
				} else {
					ModelMetaDataEditor editor = getModelMetaDataEditor(editingMetaModel);
					for (GMTypeInstanceBean bean : beans) {
						if (bean.getInstance() instanceof MetaData) {
							MetaData metaData = (MetaData) bean.getInstance();	
							if (entity instanceof GmEntityType) {
								if (metaDataExpert instanceof DeclaredPropertyOverviewExpert)
									editor.onEntityType(((GmEntityType) entity).getTypeSignature()).addPropertyMetaData(metaData);												
								else
									editor.onEntityType(((GmEntityType) entity).getTypeSignature()).addMetaData(metaData);
							}
							if (entity instanceof GmEnumType)
								editor.onEnumType(((GmEnumType) entity).getTypeSignature()).addMetaData(metaData);
																	
						}
					}
				}
			} else if (entity instanceof GmProperty) {
				//save metaData for GmProperty
				GmProperty elementProperty = (GmProperty) entity;
				if (editingEntityType == null)
					GMEUtil.insertToListOrSet(collectionElement, beans, -1);
				else {
					//need add Overriden or normal
					if (editingMetaModel == null && editingEntityType.getDeclaringModel() == null) {
						GMEUtil.insertToListOrSet(collectionElement, beans, -1);
					} else {
						if (editingMetaModel == null)
							editingMetaModel = editingEntityType.getDeclaringModel();
						ModelMetaDataEditor editorMetaModel = getModelMetaDataEditor(editingMetaModel);
						for (GMTypeInstanceBean bean : beans) {
							if (bean.getInstance() instanceof MetaData) {
								editorMetaModel.onEntityType(editingEntityType).addPropertyMetaData(elementProperty, (MetaData) bean.getInstance());
							}
						}
					}
				}							
			} else if (entity instanceof GmEnumConstant) {
				//save metaData for GmProperty
				GmEnumConstant elementEnumConstant = (GmEnumConstant) entity;
				if (editingEnumType == null) {
					GMEUtil.insertToListOrSet(collectionElement, beans, -1);
				} else {								
					//need add Overriden or normal to GmEnumTypeOverride
					if (editingMetaModel == null && editingEnumType.getDeclaringModel() == null) {
						GMEUtil.insertToListOrSet(collectionElement, beans, -1);
					} else {								
						if (editingMetaModel == null)
							editingMetaModel = editingEnumType.getDeclaringModel(); 
						ModelMetaDataEditor editorMetaModel = getModelMetaDataEditor(editingMetaModel);
						for (GMTypeInstanceBean bean : beans) {
							if (bean.getInstance() instanceof MetaData) {
								MetaData metaData = (MetaData) bean.getInstance();
								editorMetaModel.onEnumType(editingEnumType).addConstantMetaData(elementEnumConstant, metaData);
							}
						}
					}
				}
			}
													
			nestedTransaction.commit();
			rollback = false;

			/*
			if (beans.size() == 1 && beans.get(0).isHandleInstantiation()) {
				if (instantiateEntityAction != null)
					instantiateEntityAction.handleInstantiation(beans.get(0), true);
				else
					handleInstantiation(beans.get(0));
			}
			*/
			
			if (rollback)
				performRollback(nestedTransaction);
		}).onError(e -> handleFailure(nestedTransaction, e));
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (instantiatedEntityAndListenerMap == null) //TODO: currently, this is ALWAYS null
			return;
		
		if (manipulation instanceof DeleteManipulation || manipulation instanceof InstantiationManipulation) {
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
	}
	
	private Future<InstanceSelectionData> getElementToAdd(GenericModelType type, int maxEntriesToAdd) throws RuntimeException {
		SelectionConfig selectionConfig = new SelectionConfig(type, maxEntriesToAdd,
				GMEUtil.prepareQueryEntityProperty(collectionElement.getEntity(), collectionElement.getProperty()), getGmSession(), workbenchSession,
				true, true, false, true, collectionElement);
		selectionConfig.setTitle(LocalizedText.INSTANCE.addMetaData());
		selectionConfig.setSubTitle(LocalizedText.INSTANCE.select("metadata"));
		
		return entitySelectionFutureProvider.apply(selectionConfig);
	}
	
	private static void handleFailure(NestedTransaction nestedTransaction, Throwable e) {
		try {
			nestedTransaction.rollback();
		} catch (TransactionException e1) {
			e1.printStackTrace();
		}
		
		ErrorDialog.show(LocalizedText.INSTANCE.errorAddingEntries(), e);
		e.printStackTrace();
		//window.getGenericModelEditorPanel().getPerformEntityAndPropertyQueryAction().configureEntitiesToIgnore(null); TODO
	}
	
	private static void performRollback(NestedTransaction nestedTransaction) {
		try {
			nestedTransaction.rollback();
		} catch (TransactionException e) {
			e.printStackTrace();
			ErrorDialog.show(LocalizedText.INSTANCE.errorRollingBack(), e);
		}
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
	
	private ModelMetaDataEditor getModelMetaDataEditor(GmMetaModel editingMetaModel) {
		if (this.modelMetaDataEditor == null || this.modelMetaDataEditor.getMetaModel() != editingMetaModel) {
			//this.modelMetaDataEditor = new BasicModelMetaDataEditor(editingMetaModel, getGmSession()::create, GlobalIdFactory.noGlobalId);
			//this.modelMetaDataEditor = new BasicModelMetaDataEditor(editingMetaModel, getGmSession()::create); Deprecated
			this.modelMetaDataEditor = BasicModelMetaDataEditor.create(editingMetaModel).withSession(getGmSession()).done();
		}
		return this.modelMetaDataEditor;			
	}

	protected PersistenceGmSession getGmSession() {
		if (gmSession == null) {
			gmSession = gmContentView.getGmSession();
			
			if (instantiateEntityAction == null)
				gmSession.listeners().add(this);
		}
		
		return gmSession;
	}
	
}
