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
package com.braintribe.gwt.gme.propertypanel.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gme.propertypanel.client.field.ExtendedInlineField;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelCss;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.AddExistingEntitiesToCollectionAction;
import com.braintribe.gwt.gmview.action.client.ChangeInstanceAction;
import com.braintribe.gwt.gmview.action.client.WorkWithEntityAction;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.components.client.GmeToolTip;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ListStoreWithStringKey;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.extensiondeployment.meta.DynamicSelectList;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.MapKeyPathElement;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.path.SetItemPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.validation.ValidationKind;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.PropertyPath;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.meta.data.prompt.Embedded;
import com.braintribe.model.meta.data.prompt.EntityCompoundViewing;
import com.braintribe.model.meta.data.prompt.Inline;
import com.braintribe.model.meta.data.prompt.SimplifiedAssignment;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.managed.PropertyQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceManipulationListenerRegistry;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.Style.Side;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent.RowMouseDownHandler;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * The property panel is there to display all properties of a given entity.
 * @author michel.docouto
 *
 */
public class PropertyPanel extends AbstractPropertyPanel {
	
	static {
		PropertyPanelResources.INSTANCE.css().ensureInjected();
	}
	protected static final Logger logger = new Logger(PropertyPanel.class);
	protected static final String FLOW_COLLECTION_INDEX = "flowCollectionIndex";
	protected static final int VALUE_INDEX = 2;
	private static final int MAX_LEVEL_FOR_PROPERTY_NAME_FLOW_CHECK = 4;
	
	private String defaultGroup;
	private PropertyPanelHelperMenu helperMenu;
	private PropertPanelCollectionItemMenu collectionItemMenu;
	private AddExistingEntitiesToCollectionAction addExistingEntitiesToCollectionAction;
	private ChangeInstanceAction shortcutChangeInstanceAction;
	protected NestedTransaction editionNestedTransaction;
	protected Map<TriggerFieldAction, MenuItem> triggerFieldActionItemMap;
	private final Set<GenericEntity> entitiesLoading = new HashSet<>();
	private final Set<GenericEntity> entitiesLoadingCompound = new HashSet<>();
	protected ColumnConfig<PropertyModel, String> groupColumn;
	private boolean expandPending = false;
	private PropertyPanelEditorPreparationContinuation propertyPanelEditorPreparationContinuation;
	private PropertyModel helperMenuPropertyModel;
	private PropertyModel collectionItemMenuPropertyModel;
	private boolean waitingForEditors;
	private ModelMdResolver modelMdResolver;
	private boolean useDialogSettings = false;
	
	/**
	 * This method is responsible for setting the parent {@link GenericEntity}, which will have its properties displayed in the {@link PropertyPanel}.
	 * If a null {@link ModelPath} is set, then the {@link PropertyPanel} is cleared.
	 */
	@Override
	public void setContent(ModelPath modelPath) {
		ProfilingHandle ph = Profiling.start(getClass(), "Setting content in PP", false, true);
		completeEditing();
		
		rollbackTransaction();
		
		if (propertyPanelEditorPreparationContinuation != null)
			propertyPanelEditorPreparationContinuation.cancel();
		
		if (metadataValueReevaluationProperties != null)
			metadataValueReevaluationProperties.clear();
		
		if (parentEntity != null) {
			gmSession.listeners().entity(parentEntity).remove(this);
			if (compoundEntities != null) {
				PersistenceManipulationListenerRegistry listener = gmSession.listeners();
				compoundEntities.forEach(c -> listener.entity(c).remove(PropertyPanel.this));
				compoundEntities.clear();
				compoundEntitiesMap.clear();
			}
		}
		
		parentEntity = modelPath != null ? (GenericEntity) modelPath.last().getValue() : null;
		parentEntityType = parentEntity != null ? parentEntity.entityType() : null;
		modelMdResolver = null;
		
		propertyPanelGrid.getStore().clear();
		if (invisiblePropertyModels == null)
			invisiblePropertyModels = new FastMap<>();
		else
			invisiblePropertyModels.clear();
		
		if (parentEntity != null) {
			// We always use the session attached to the entity being displayed, if that is different from the one currently configured to be used.
			GmSession entitySession = parentEntity.session();
			if (entitySession != this.gmSession && entitySession instanceof PersistenceGmSession)
				configureGmSession((PersistenceGmSession) entitySession);
			defaultGroup = LocalizedText.INSTANCE.base();
			if (gmSession != null)
				gmSession.listeners().entity(parentEntity).add(this);
			preparePropertyModels();
		}
		ph.stop();
	}
	
	public PropertyModel getHelperMenuPropertyModel() {
		return helperMenu == null ? helperMenuPropertyModel : helperMenu.helperMenuPropertyModel;
	}
	
	public void setHelperMenuPropertyModel(PropertyModel helperMenuPropertyModel) {
		this.helperMenuPropertyModel = helperMenuPropertyModel;
		if (helperMenu != null)
			helperMenu.helperMenuPropertyModel = helperMenuPropertyModel;
	}
	
	public PropertyModel getCollectionItemMenuPropertyModel() {
		return collectionItemMenu == null ? collectionItemMenuPropertyModel : collectionItemMenu.menuPropertyModel;
	}
	
	public void setCollectionItemMenuPropertyModel(PropertyModel collectionItemMenuPropertyModel) {
		this.collectionItemMenuPropertyModel = collectionItemMenuPropertyModel;
		if (collectionItemMenu != null)
			collectionItemMenu.menuPropertyModel = collectionItemMenuPropertyModel;
	}
	
	/**
	 * Starts editing the first editable property, if any.
	 */
	public void startEditing() {
		propertyPanelGrid.startEditing();
	}
	
	/**
	 * Completes the edition, if currently editing.
	 */
	public void completeEditing() {
		propertyPanelGrid.completeEditing();
	}
	
	/**
	 * Finishes the edition of the current editor, if currently editing.
	 */
	public void finishEditing() {
		propertyPanelGrid.finishEditing();
	}
	
	/**
	 * Checks if the {@link PropertyPanel} has editable properties.
	 * Please notice that this must be checked after the editors are loaded.
	 * @see #addPropertyPanelListener(PropertyPanelListener) and {@link #isWaitingForEditors()}.
	 */
	public boolean hasEditableProperty() {
		if (readOnly)
			return false;
		
		if (propertyPanelGrid.getFirstEditableRow() != -1)
			return true;
		
		for (PropertyModel model : propertyPanelGrid.getStore().getAll()) {
			GenericModelType propertyType = model.getValueElementType();
			if (propertyType.isCollection() && PropertyPanelRowExpander.isCollectionPropertyEditable(model, this))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Checks whether the panel is currently in edit mode.
	 */
	public boolean isEditing() {
		return propertyPanelGrid.gridInlineEditing.isEditing();
	}
	
	/**
	 * Returns true for a while when the edition was canceled by an esc.
	 */
	public boolean wasEditionFinishedByEsc() {
		return propertyPanelGrid.gridInlineEditing.wasEditionFinishedByEsc();
	}
	
	/**
	 * Returns true for a while when the edition was canceled by an enter.
	 */
	public boolean wasEditionFinishedByEnter() {
		return propertyPanelGrid.gridInlineEditing.wasEditionFinishedByEnter();
	}
	
	/**
	 * Returns true when waiting for the editors to be prepared. False, otherwise.
	 */
	public boolean isWaitingForEditors() {
		return waitingForEditors;
	}
	
	public void addPropertyPanelListener(PropertyPanelListener listener) {
		if (listener != null) {
			if (propertyPanelListeners == null)
				propertyPanelListeners = new ArrayList<>();
			propertyPanelListeners.add(listener);
		}
	}

	public void removePropertyPanelListener(PropertyPanelListener listener) {
		if (listener != null && propertyPanelListeners != null) {
			propertyPanelListeners.remove(listener);
			if (propertyPanelListeners.isEmpty())
				propertyPanelListeners = null;
		}
	}
	
	public int getGridContentHeight() {
		return propertyPanelGrid == null ? 0 : propertyPanelGrid.getContentHeight();
	}
	
	public void setUseDialogSettings(boolean useDialogSettings) {
		this.useDialogSettings = useDialogSettings;
	}
	
	public boolean getUseDialogSettings() {
		return this.useDialogSettings;
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		propertyPanelGrid.getView().getScroller().addClassName("gmePropertyPanelScroller");
		if (expandPending) {
			propertyPanelGrid.expander.expandAllRows();
			expandPending = false;
		}
	}
	
	@Override
	protected void updatePropertyModelValue(PropertyModel propertyModel, String propertyName, Object value, EntityType<?> entityType,
			GenericEntity compoundParentEntity, boolean handleAutoCommit) {
		int indexInGrid;
		ListStore<PropertyModel> store = propertyPanelGrid.getStore();
		if (propertyModel != null)
			indexInGrid = store.indexOf(propertyModel);
		else {
			if (compoundParentEntity != null)
				propertyModel = getCompoundPropertyModel(compoundParentEntity, propertyName, true);
			else {
				if (store instanceof ListStoreWithStringKey)
					propertyModel = ((ListStoreWithStringKey<PropertyModel>) store).findModelWithKeyEvenIfFiltered(propertyName);
				else
					propertyModel = store.findModelWithKey(propertyName);
			}
			
			if (propertyModel != null)
				indexInGrid = store.indexOf(propertyModel);
			else {
				indexInGrid = -1;
				if (compoundParentEntity != null)
					propertyModel = getCompoundPropertyModel(compoundParentEntity, propertyName, false);
				else
					propertyModel = invisiblePropertyModels.get(propertyName);
			}
		}
		
		if (propertyModel == null)
			return;
		
		if (propertyModel != lastEditedPropertyModel)
			propertyModel.setValidationKind(ValidationKind.none); //RVE - value is updated than clear validation information
		propertyModel.setValue(value);
		
		if (propertyModel.getFlow() || isExtendedInlineFieldAvailable(propertyModel)) {
			if (propertyModel.getFlow())
				propertyModel.setFlowDisplay(propertyPanelGroupingView.prepareFlowDisplay(entityType.getProperty(propertyModel.getPropertyName()), propertyModel));
			Scheduler.get().scheduleDeferred(() -> {
				if (indexInGrid != -1) {					
					refreshRow(propertyPanelGroupingView, indexInGrid);		//RVE - also need this to update validation rectangle, must be called before propertyPanelGrid.expander.refreshRow	
					if (propertyPanelGrid.expander != null)
						propertyPanelGrid.expander.refreshRow(indexInGrid);
				}
			});
		} else if (propertyModel.getExtendedInlineField() != null)
			propertyPanelGrid.expander.removeWidget(propertyModel);
		
		if (!propertyModel.getFlow()) {
			propertyModel.setValueDisplay(propertyPanelGroupingView
					.prepareValueDisplay(entityType == null ? null : entityType.getProperty(propertyModel.getPropertyName()), propertyModel));
			if (indexInGrid != -1)
				refreshRow(propertyPanelGroupingView, indexInGrid);
		}
		
		boolean handlingAbsent = absentProperties.remove(propertyModel);
		
		if (handleAutoCommit && !handlingAbsent && propertyModel != lastEditedPropertyModel)
			handleAutoCommit();
		
		lastEditedPropertyModel = null;
		localManipulation = false;
	}
	
	protected void handleAutoCommit() {
		if (autoCommit && commitAction != null && commitAction.getEnabled()) {
			TriggerInfo triggerInfo = new TriggerInfo();
			triggerInfo.setWidget(this);
			triggerInfo.put("AutoCommit", true);
			commitAction.perform(triggerInfo);
		}
	}
	
	private PropertyModel getCompoundPropertyModel(GenericEntity compoundParentEntity, String propertyName, boolean visible) {
		Collection<PropertyModel> propertyModels;
		if (visible)
			propertyModels = propertyPanelGrid.getStore().getAll();
		else
			propertyModels = invisiblePropertyModels.values();

		return propertyModels.stream().filter(p -> p.getParentEntity() == compoundParentEntity && p.getPropertyName().equals(propertyName)).findAny()
				.orElse(null);
	}
	
	@Override
	protected Grid<PropertyModel> preparePropertyPanelGrid() {
		propertyPanelGrid = new PropertyPanelGrid(this, new PropertyPanelRowExpander(this));
		propertyPanelGroupingView = (PropertyPanelGroupingView) propertyPanelGrid.getView();
				
		propertyPanelGrid.addRowMouseDownHandler(new RowMouseDownHandler() {		
			@Override
			public void onRowMouseDown(RowMouseDownEvent event) {				
				updateValidateToolTip(event.getRowIndex());
			}
		});
		
		
		return propertyPanelGrid;
	}

	protected void updateValidateToolTip(int rowIndex) {
		//RVE - clearing custom hints (fail validation hints)
		if (rowIndex < 0)
			return;
		
		Element row = propertyPanelGrid.getView().getRow(rowIndex);
		if (row == null)
			return;
		
		PropertyModel propertyModel = propertyPanelGrid.getStore().get(rowIndex);
		if (propertyModel != null) {
			propertyModel.setValidationKind(ValidationKind.none);
			propertyModel.setValidationDescription(null);
			//propertyPanelGrid.getView().refresh(false);
		}		
	}
	
	protected void rollbackTransaction() {
		try {
			if (editionNestedTransaction != null) {
				editionNestedTransaction.rollback();
				gmEditionViewControllerSupplier.get().unregisterAsCurrentEditionView(this);
			}
		} catch (TransactionException e) {
			ErrorDialog.show(LocalizedText.INSTANCE.errorRollingEditionBack(), e);
			e.printStackTrace();
		} catch (IllegalStateException ex) {
			//Nothing to do: the PP was used within some widget which rolled back the parent transaction already. This may happen within GIMA when canceling it while editing.
		} finally {
			editionNestedTransaction = null;
		}
	}
	
	private void preparePropertyModels() {
		List<PropertyModel> propertyModels = new ArrayList<>();
		if (absentProperties == null)
			absentProperties = new ArrayList<>();
		else
			absentProperties.clear();
		
		EntityMdResolver entityMdResolver = skipMetadataResolution ? null : GmSessions.getMetaData(parentEntity).lenient(lenient).entity(parentEntity).useCase(useCase);
		
		List<EntityCompoundViewing> entityCompoundViewings = entityMdResolver == null ? Collections.emptyList()
				: entityMdResolver.meta(EntityCompoundViewing.T).list();
		
		ModelMdResolver modelResolver = getMetaData();
		ModelMdResolver mdResolver = modelResolver == null ? modelResolver : modelResolver.lenient(lenient);
		parentEntityType.getProperties().forEach(p -> handleProperty(p, entityCompoundViewings, mdResolver, propertyModels, absentProperties));
		
		Collections.sort(propertyModels, PropertyModel.getPriorityComparator());
		ListStore<PropertyModel> store = propertyPanelGrid.getStore();
		if (forceGroups || hasNoDefaultOrMultipleGroups(propertyModels)) {
			store.clearSortInfo();
			propertyPanelGroupingView.groupBy(groupColumn);
			store.clearSortInfo();
			store.addSortInfo(new StoreSortInfo<>(PropertyModel.getGroupPriorityComparator(), SortDir.ASC));
			store.addSortInfo(new StoreSortInfo<>(PropertyModel.getPriorityComparator(), SortDir.ASC));
		}
		store.addAll(propertyModels);
		
		if (triggerFieldActionModelMap != null)
			removeTriggerFieldActions();
		
		if (!readOnly) {
			waitingForEditors = true;
			prepareEditors().onError(e -> waitingForEditors = false).andThen(result -> {
				waitingForEditors = false;
				fireEditorsReady();
			});
		}
		
		if (propertyPanelGrid.isRendered())
			propertyPanelGrid.expander.expandAllRows();
		else
			expandPending = true;
		
		if (!absentProperties.isEmpty() && entitiesLoading.add(parentEntity)) {
			GenericEntity checkEntity = parentEntity;
			loadAbsentProperties(absentProperties).andThen(result -> entitiesLoading.remove(checkEntity)).onError(e -> {
				entitiesLoading.remove(checkEntity);
				ErrorDialog.show(LocalizedText.INSTANCE.errorLoadingAbsentProperties(), e);
				e.printStackTrace();
			});
		}
	}

	protected void fireEditorsReady() {
		if (propertyPanelListeners != null)
			new ArrayList<>(propertyPanelListeners).forEach(l -> l.onEditorsReady());
	}

	protected void fireEditingDone(boolean canceled) {
		if (propertyPanelListeners != null)
			new ArrayList<>(propertyPanelListeners).forEach(l -> l.onEditingDone(canceled));
	}
	
	private void handleProperty(Property property, List<EntityCompoundViewing> entityCompoundViewings, ModelMdResolver metaDataResolver,
			List<PropertyModel> propertyModels, List<PropertyModel> absentProperties) {
		GenericModelType propertyType = property.getType();
		Object value = property.get(parentEntity);
		String propertyName = property.getName();
		boolean baseTyped = false;
		if (propertyType.isBase()) {
			baseTyped = true;
			BaseType baseType = (BaseType) propertyType;
			if (value != null)
				propertyType = baseType.getActualType(value);
			else
				propertyType = getTypeIfPropertyInitializer(propertyName);
		}
		
		PropertyMdResolver propertyMdResolver = metaDataResolver != null
				? metaDataResolver.entity(parentEntity).property(propertyName).useCase(useCase).lenient(lenient) : null;
		
		boolean isSimpleOrSimplified = PropertyPanelMetadataUtil.isPropertySimpleOrSimplified(this, metaDataResolver, propertyType, value,
				propertyName, parentEntity, parentEntityType, useCase, lenient, simplifiedEntityTypes);

		boolean editable = propertyMdResolver != null ? GMEMetadataUtil.isPropertyEditable(propertyMdResolver, parentEntity) : true;
		boolean visible = PropertyPanelMetadataUtil.isPropertyVisible(this, propertyMdResolver, editable, isSimpleOrSimplified,
				hideNonEditableProperties, hideNonSimpleProperties);
		
		handleMetadataReevaluation(GMEMetadataUtil.getPropertyMdResolverForEntity(propertyMdResolver, parentEntity), Modifiable.T);
		
		Embedded embedded = null;
		if (propertyType.isEntity()) {
			embedded = PropertyPanelMetadataUtil.getEmbedded(metaDataResolver, parentEntity, property, propertyType, useCase, lenient);
			
			if (embedded != null) {
				handleEmbeddedProperty(embedded, property, (EntityType<GenericEntity>) propertyType, propertyModels, isSimpleOrSimplified,
						metaDataResolver, parentEntity, parentEntityType, null, visible);
				if (!embedded.getDisplayParentProperty())
					return;
			}
		}
		
		List<PropertyPath> propertyPaths = PropertyPanelMetadataUtil.getPropertyPaths(embedded, entityCompoundViewings, propertyName);
		if (propertyPaths == null)
			handleNormalProperty(property, metaDataResolver, propertyModels, absentProperties, propertyType, baseTyped, editable, visible);
		else {
			List<PropertyModel> compoundEntityPropertyModelsToUpdate = new ArrayList<>();
			handleCompoundProperty(property, entityCompoundViewings, metaDataResolver, propertyModels, propertyType, propertyPaths, isSimpleOrSimplified,
					compoundEntityPropertyModelsToUpdate, parentEntity);
		}
	}
	
	private GenericModelType getTypeIfPropertyInitializer(String propertyName) {
		if (isPropertyInitializer(propertyName)) {
			GmType type = ((GmProperty) parentEntity).getType();
			if (type != null && type.isGmSimple()) {
				GenericModelType theType = GMF.getTypeReflection().findType(type.getTypeSignature());
				if (theType != null)
					return theType;
			}
		}
		
		return BaseType.INSTANCE;
	}
	
	private boolean isPropertyInitializer(String propertyName) {
		return "initializer".equals(propertyName) && parentEntity instanceof GmProperty;
	}
	
	private void handleNormalProperty(Property property, ModelMdResolver metaDataResolver, List<PropertyModel> propertyModels,
			List<PropertyModel> absentProperties, GenericModelType propertyType, boolean baseTyped, boolean editable, boolean visible) {
		String propertyName = property.getName();
		PropertyMdResolver propertyMdResolver = metaDataResolver != null
				? metaDataResolver.entity(parentEntity).property(propertyName).useCase(useCase).lenient(lenient) : null;
		
		boolean mandatory = propertyMdResolver != null && propertyMdResolver.is(Mandatory.T);
		handleMetadataReevaluation(propertyMdResolver, Mandatory.T);
		
		PropertyModel propertyModel = preparePropertyModel(parentEntity, parentEntityType, property, propertyType, editable, mandatory, baseTyped,
				metaDataResolver);
		if (visible)
			propertyModels.add(propertyModel);
		else
			invisiblePropertyModels.put(propertyName, propertyModel);
		
		if (GMEUtil.isPropertyAbsent(parentEntity, property))
			absentProperties.add(propertyModel);
	}
	
	private void handleEmbeddedProperty(Embedded embedded, Property property, EntityType<GenericEntity> propertyEntityType,
			List<PropertyModel> propertyModels, boolean isSimpleOrSimplified, ModelMdResolver metaDataResolver, GenericEntity parentEntity,
			EntityType<?> parentEntityType, String parentPropertyName, boolean parentVisible) {
		GenericEntity propertyEntity = parentEntity != null ? (GenericEntity) property.get(parentEntity) : null;
		if (propertyEntity != null) {
			compoundEntities.add(propertyEntity);
			gmSession.listeners().entity(propertyEntity).add(this);
			propertyEntityType = propertyEntity.entityType();
		}
		
		List<String> embeddedProperties = new ArrayList<>();
		if (!embedded.getIncludes().isEmpty())
			embeddedProperties.addAll(embedded.getIncludes());
		else {
			List<Property> properties = propertyEntityType.getProperties();
			properties.forEach(subProperty -> embeddedProperties.add(subProperty.getName()));
		}
		
		embeddedProperties.removeAll(embedded.getExcludes());
		
		EntityMdResolver entityMdResolver;
		if (parentEntity != null)
			entityMdResolver = metaDataResolver.entity(parentEntity);
		else
			entityMdResolver = metaDataResolver.entityType(parentEntityType);
		PropertyMdResolver propertyMdResolver = entityMdResolver.useCase(useCase).lenient(lenient).property(property);
		boolean isEditable = GMEMetadataUtil.isPropertyEditable(propertyMdResolver, parentEntity);
		
		EntityMdResolver subEntityMdResolver;
		if (propertyEntity != null)
			subEntityMdResolver = metaDataResolver.entity(propertyEntity).useCase(useCase).lenient(lenient);
		else
			subEntityMdResolver = metaDataResolver.entityType(propertyEntityType).useCase(useCase).lenient(lenient);
		
		List<PropertyModel> compoundEntityPropertyModelsToUpdate = new ArrayList<>();
		for (String embeddedPropertyName : embeddedProperties) {
			
			Property embeddedProperty = propertyEntityType.getProperty(embeddedPropertyName);
			GenericModelType embeddedPropertyType = embeddedProperty.getType();
			Object value = propertyEntity == null ? null : embeddedProperty.get(propertyEntity);
			
			boolean isEmbeddedSimpleOrSimplified = PropertyPanelMetadataUtil.isPropertySimpleOrSimplified(PropertyPanel.this, metaDataResolver, embeddedPropertyType, value,
					embeddedPropertyName, propertyEntity, propertyEntityType, useCase, lenient, simplifiedEntityTypes);
			
			Embedded subEmbedded = null;
			String propertyName = property.getName();
			if (embeddedPropertyType.isEntity()) {
				if (value != null) {
					GenericEntity entity = (GenericEntity) value;
					compoundEntities.add(entity);
					gmSession.listeners().entity(entity).add(this);
					compoundEntitiesMap.put(entity, new Pair<>(propertyEntity, embeddedPropertyName));
				}
				
				subEmbedded = subEntityMdResolver.property(embeddedProperty).meta(Embedded.T).exclusive();
				if (subEmbedded == null)
					subEmbedded = subEntityMdResolver.meta(Embedded.T).exclusive();
				
				if (subEmbedded != null) {
					if (propertyEntity == null) {
						if (!GMEUtil.isPropertyAbsent(parentEntity, property)) {
							//when subEmbedded properties are null, we will instantiate the parent before proceeding
							propertyEntity = gmSession.create((EntityType<?>) property.getType());
							handleCompoundQueryResult(Collections.emptyList(), parentEntity, true, propertyName, parentEntity, propertyEntity, false);
						}
					}
					
					handleEmbeddedProperty(subEmbedded, embeddedProperty, (EntityType<GenericEntity>) embeddedPropertyType, propertyModels,
							isEmbeddedSimpleOrSimplified, metaDataResolver, propertyEntity, propertyEntityType, propertyName, parentVisible);
					if (!subEmbedded.getDisplayParentProperty())
						continue;
				}
			}
			
			if (parentPropertyName != null)
				propertyName = parentPropertyName + "." + propertyName;
			PropertyModel propertyModel = handledEmbeddedSingleProperty(embeddedPropertyName, propertyEntity, propertyEntityType, propertyName,
					isEditable, propertyModels, isSimpleOrSimplified, metaDataResolver, parentEntity, parentEntityType, parentVisible);
			
			if (propertyEntity == null)
				compoundEntityPropertyModelsToUpdate.add(propertyModel);
			else if (GMEUtil.isPropertyAbsent(propertyEntity, embeddedProperty)) {
				propertyModel.setAbsent(true);
				loadCompoundProperty(embeddedProperty, Arrays.asList(propertyModel), metaDataResolver, propertyEntity, false);
			}
		}
		
		if (propertyEntity == null && (parentEntity == null || entitiesLoadingCompound.add(parentEntity)))
			loadCompoundProperty(property, compoundEntityPropertyModelsToUpdate, metaDataResolver, parentEntity);
	}

	private void handleCompoundProperty(Property property, List<EntityCompoundViewing> entityCompoundViewings, ModelMdResolver metaDataResolver,
			List<PropertyModel> propertyModels, GenericModelType propertyType, List<PropertyPath> propertyPaths, boolean isSimpleOrSimplified,
			final List<PropertyModel> compoundEntityPropertyModelsToUpdate, GenericEntity parentEntity) {
		EntityType<GenericEntity> compoundEntityType = (EntityType<GenericEntity>) propertyType;
		EntityMdResolver entityMetaDataContextBuilder = metaDataResolver != null
				? metaDataResolver.entityType(compoundEntityType).useCase(useCase).lenient(lenient)
				: null;
		
		GenericEntity propertyEntity = (GenericEntity) property.get(parentEntity);
		if (propertyEntity != null) {
			compoundEntities.add(propertyEntity);
			gmSession.listeners().entity(propertyEntity).add(this);
		}
		
		for (PropertyPath propertyPath : propertyPaths) {
			handleCompoundPath(property, entityCompoundViewings, metaDataResolver, propertyModels, isSimpleOrSimplified,
					compoundEntityPropertyModelsToUpdate, compoundEntityType, entityMetaDataContextBuilder, propertyEntity, propertyPath);
		}
		
		if (propertyEntity == null && (parentEntity == null || entitiesLoadingCompound.add(parentEntity)))
			loadCompoundProperty(property, compoundEntityPropertyModelsToUpdate, metaDataResolver, parentEntity);
	}
	
	private void loadCompoundProperty(Property property, List<PropertyModel> compoundEntityPropertyModelsToUpdate, ModelMdResolver metaDataResolver,
			GenericEntity parentEntity) {
		loadCompoundProperty(property, compoundEntityPropertyModelsToUpdate, metaDataResolver, parentEntity, true);
	}

	private void loadCompoundProperty(Property property, List<PropertyModel> compoundModelsToUpdate, ModelMdResolver metaDataResolver,
			GenericEntity parentEntity, boolean updateParentEntity) {
		final String propertyName = property.getName();
		if (!GMEUtil.isPropertyAbsent(parentEntity, property)) {
			//if the property is really null, then we instantiate it when handling compound (embedded)
			GenericEntity propertyEntity = gmSession.create((EntityType<?>) property.getType());
			handleCompoundQueryResult(compoundModelsToUpdate, parentEntity, updateParentEntity, propertyName, parentEntity, propertyEntity, false);
			return;
		}
		
		EntityReference entityReference = parentEntity.reference();
		if (!(entityReference instanceof PersistentEntityReference)) {
			logger.error("Invalid parentEntity.reference for: " + propertyName + " is not instance of PersistentEntityReference");
			return;
		}
		
		final PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) entityReference, propertyName, null,
				getSpecialTraversingCriterion(property.getType().getJavaType()), false,
				metaDataResolver, useCase);
		
		final GenericEntity requestedEntity = parentEntity;
		gmSession.query().property(propertyQuery).result(com.braintribe.processing.async.api.AsyncCallback.of( //
				result -> handleCompoundQueryResult(compoundModelsToUpdate, parentEntity, updateParentEntity, propertyName, requestedEntity, result),
				e -> {
					entitiesLoadingCompound.remove(requestedEntity);
					logger.error("Error while loading absent compound property: " + propertyName, e);
					e.printStackTrace();
					handleCompoundQueryResult(compoundModelsToUpdate, parentEntity, updateParentEntity, propertyName, requestedEntity, null);
				}));
	}
	
	private void handleCompoundQueryResult(List<PropertyModel> compoundEntityPropertyModelsToUpdate, GenericEntity parentEntity,
			boolean updateParentEntity, final String propertyName, final GenericEntity requestedEntity,
			PropertyQueryResultConvenience propertyQueryResult) {
		Object propertyValue = null;
		if (propertyQueryResult != null) {
			PropertyQueryResult result = propertyQueryResult.result();
			propertyValue = result != null ? result.getPropertyValue() : null;
		}
		
		handleCompoundQueryResult(compoundEntityPropertyModelsToUpdate, parentEntity, updateParentEntity, propertyName, requestedEntity,
				propertyValue);
	}
	
	private void handleCompoundQueryResult(List<PropertyModel> compoundEntityPropertyModelsToUpdate, GenericEntity parentEntity,
			boolean updateParentEntity, final String propertyName, final GenericEntity requestedEntity, Object propertyValue) {
		handleCompoundQueryResult(compoundEntityPropertyModelsToUpdate, parentEntity, updateParentEntity, propertyName, requestedEntity,
				propertyValue, true);
	}

	private void handleCompoundQueryResult(List<PropertyModel> compoundEntityPropertyModelsToUpdate, GenericEntity parentEntity,
			boolean updateParentEntity, final String propertyName, final GenericEntity requestedEntity, Object propertyValue,
			boolean suspendHistory) {
		EntityType<GenericEntity> propertyEntityType = null;
		if (propertyValue != null) {
			try {
				if (suspendHistory)
					gmSession.suspendHistory();
				if (propertyValue instanceof GenericEntity) {
					GenericEntity propertyEntity = (GenericEntity) propertyValue;
					if (requestedEntity == parentEntity) {
						propertyEntityType = propertyEntity.entityType();
						compoundEntities.add(propertyEntity);
						gmSession.listeners().entity(propertyEntity).add(PropertyPanel.this);
					}
				}
				requestedEntity.entityType().getProperty(propertyName).set(requestedEntity, propertyValue);
			} catch (GmSessionException e) {
				logger.error("Error while loading absent compound property: " + propertyName, e);
				e.printStackTrace();
			} finally {
				if (suspendHistory)
					gmSession.resumeHistory();
			}
		}
	
		if (requestedEntity == parentEntity) {
			for (PropertyModel propertyModel : compoundEntityPropertyModelsToUpdate) {
				propertyModel.setAbsent(false);
				if (updateParentEntity && (propertyValue instanceof GenericEntity || propertyValue == null)) {
					GenericEntity propertyEntity = (GenericEntity) propertyValue;
					propertyModel.setParentEntity((GenericEntity) propertyValue);
					updatePropertyModelValue(propertyModel, null,
							propertyEntity == null ? null : propertyEntityType.getProperty(propertyModel.getPropertyName()).get(propertyEntity),
							propertyEntityType, propertyEntity, false);
				} else if (propertyValue == null) {
					int indexInGrid = propertyPanelGrid.getStore().indexOf(propertyModel);
					if (indexInGrid != -1)
						refreshRow(propertyPanelGroupingView, indexInGrid);
				}
			}
		}
		
		entitiesLoadingCompound.remove(requestedEntity);
	}
	
	protected void instantiateEmbeddedParent(PropertyModel model) {
		EntityType<?> propertyEntityType = model.getParentEntityType();
		GenericEntity propertyEntity = gmSession.create(propertyEntityType);
		compoundEntities.add(propertyEntity);
		gmSession.listeners().entity(propertyEntity).add(this);
		
		String normalizedPropertyName = model.getNormalizedPropertyName();
		String[] nameArray = normalizedPropertyName.split("\\.");
		GenericEntity parentEntity = this.parentEntity;
		String parentPropertyName = model.getNormalizedPropertyName().substring(0, model.getNormalizedPropertyName().indexOf("."));
		String propertyNameCheck;
		if (nameArray.length > 2) {
			for (int i = 0; i < nameArray.length - 2; i++) {
				parentEntity = parentEntity.entityType().getProperty(nameArray[i]).get(parentEntity);
				parentPropertyName = nameArray[i + 1];
			}
			propertyNameCheck = model.getNormalizedPropertyName().substring(0, model.getNormalizedPropertyName().lastIndexOf("."));
		} else
			propertyNameCheck = parentPropertyName;
		
		parentEntity.entityType().getProperty(parentPropertyName).set(parentEntity, propertyEntity);
		
		propertyPanelGrid.getStore().getAll().stream()
				.filter(m -> m.getParentEntity() == null && m.getNormalizedPropertyName().startsWith(propertyNameCheck + "."))
				.forEach(m -> m.setParentEntity(propertyEntity));
	}
	
	private PropertyModel handledEmbeddedSingleProperty(String propertyName, GenericEntity propertyEntity,
			EntityType<GenericEntity> propertyEntityType, String parentPropertyName, boolean isEditable, List<PropertyModel> propertyModels,
			boolean isSimpleOrSimplified, ModelMdResolver metaDataResolver, GenericEntity parentEntity, EntityType<?> parentEntityType,
			boolean parentVisible) {
		Property property = propertyEntityType.getProperty(propertyName);
		GenericModelType propertyType = property.getType();
		boolean isBaseTyped = propertyType.isBase();
		if (isBaseTyped)
			propertyType = getActualType(property, propertyEntity);

		EntityMdResolver entityMdResolver = null;
		if (metaDataResolver != null) {
			if (propertyEntity != null)
				entityMdResolver = metaDataResolver.entity(propertyEntity).useCase(useCase).lenient(lenient);
			else
				entityMdResolver = metaDataResolver.entityType(propertyEntityType).useCase(useCase).lenient(lenient);
		}
		
		PropertyMdResolver propertyMdResolver = null;
		if (entityMdResolver != null)
			propertyMdResolver = entityMdResolver.property(propertyName);
		
		if (isEditable)
			isEditable = GMEMetadataUtil.isPropertyEditable(propertyMdResolver, propertyEntity);
		
		boolean isMandatory = propertyMdResolver == null ? false : propertyMdResolver.is(Mandatory.T);
		
		PropertyModel propertyModel = preparePropertyModel(propertyEntity, propertyEntityType, property, propertyType, isEditable, isMandatory,
				isBaseTyped, metaDataResolver);
		propertyModel.setCompoundPropertyName(parentPropertyName + "." + propertyName);
		
		if (metaDataResolver != null) {
			String currentPropertyName = parentPropertyName.contains(".") ? parentPropertyName.substring(parentPropertyName.lastIndexOf(".") + 1) : parentPropertyName;
			PropertyPanelMetadataUtil.handleGroupAssignment(currentPropertyName, metaDataResolver, propertyModel, parentEntity, parentEntityType,
					useCase, lenient, defaultGroup, gmSession);
		}
		
		boolean visible = PropertyPanelMetadataUtil.isPropertyVisible(this, propertyMdResolver == null ? null : propertyMdResolver,
				isEditable, isSimpleOrSimplified, hideNonEditableProperties, hideNonSimpleProperties);
		if (visible && parentVisible)
			propertyModels.add(propertyModel);
		else
			invisiblePropertyModels.put(propertyName, propertyModel);
		
		return propertyModel;
	}
	
	private GenericModelType getActualType(Property property, GenericEntity parentEntity) {
		GenericModelType propertyType = property.getType();
		if (!propertyType.isBase() || parentEntity == null)
			return propertyType;
		
		Object propertyValue = property.get(parentEntity);
		if (propertyValue == null)
			return propertyType;
		
		return propertyType.getActualType(propertyValue);
	}

	private void handleCompoundPath(Property property, List<EntityCompoundViewing> entityCompoundViewings, ModelMdResolver metaDataResolver,
			List<PropertyModel> propertyModels, boolean isSimpleOrSimplified, final List<PropertyModel> compoundEntityPropertyModelsToUpdate,
			EntityType<GenericEntity> compoundEntityType, EntityMdResolver entityMetaDataContextBuilder, GenericEntity propertyEntity,
			PropertyPath propertyPath) {
		GmProperty viewGmProperty = propertyPath.getProperties().get(0); //TODO: prepare for the whole path...
		Property viewProperty = compoundEntityType.getProperty(viewGmProperty.getName());
		
		GenericModelType viewPropertyType = viewProperty.getType();
		boolean viewBaseTyped = false;
		Object propertyValue = viewProperty.get(propertyEntity);
		if (viewPropertyType.isBase() && propertyEntity != null) {
			viewBaseTyped = true;
			BaseType baseType = (BaseType) viewPropertyType;
			if (propertyValue != null)
				viewPropertyType = baseType.getActualType(propertyValue);
		}
		
		PropertyMdResolver propMdResolver = null;
		if (entityMetaDataContextBuilder != null)
			propMdResolver = entityMetaDataContextBuilder.property(viewGmProperty);
		
		boolean editable = propMdResolver != null
				? GMEMetadataUtil.isPropertyEditable(propMdResolver, propertyValue instanceof GenericEntity ? (GenericEntity) propertyValue : null)
				: true;
		
		boolean mandatory = propMdResolver != null && propMdResolver.is(Mandatory.T);
		
		PropertyModel propertyModel = preparePropertyModel(propertyEntity, compoundEntityType, viewProperty, viewPropertyType, editable, mandatory,
				viewBaseTyped, metaDataResolver);
		if (propertyEntity == null)
			compoundEntityPropertyModelsToUpdate.add(propertyModel);

		String propertyName = property.getName();
		String compoundPropertyName = propertyName;
		for (GmProperty gmProperty : propertyPath.getProperties())
			compoundPropertyName += "." + gmProperty.getName();
		propertyModel.setCompoundPropertyName(compoundPropertyName);
		
		if (metaDataResolver != null) {
			PropertyPanelMetadataUtil.handleGroupAssignment(propertyName, metaDataResolver, propertyModel, parentEntity, parentEntityType,
					useCase, lenient, defaultGroup, gmSession);
		}
		
		if (defaultGroup.equals(propertyModel.getGroupName()) && !preventGroup(entityCompoundViewings, propertyName, viewProperty.getName())) {
			propertyModel.setGroupName(metaDataResolver != null
					? GMEMetadataUtil.getPropertyDisplay(propertyName, metaDataResolver.entity(parentEntity).property(propertyName))
					: propertyName);
		}
		
		boolean visible = PropertyPanelMetadataUtil.isPropertyVisible(this,
				metaDataResolver == null ? null : metaDataResolver.entity(parentEntity).property(propertyName).useCase(useCase).lenient(lenient),
				editable, isSimpleOrSimplified, hideNonEditableProperties, hideNonSimpleProperties);
		if (visible)
			propertyModels.add(propertyModel);
		else
			invisiblePropertyModels.put(propertyName, propertyModel);
	}
	
	private boolean preventGroup(List<EntityCompoundViewing> entityCompoundViewings, String compoundPropertyname, String viewPropertyName) {
		if (entityCompoundViewings == null)
			return false;
		
		for (EntityCompoundViewing entityCompoundViewing : entityCompoundViewings) {
			PropertyPath propertyPath = entityCompoundViewing.getPropertyPath();
			if (propertyPath != null) {
				List<GmProperty> properties = propertyPath.getProperties();
				if (properties != null && properties.size() > 1) {
					GmProperty compoundProperty = properties.get(0);
					GmProperty viewProperty = properties.get(1);
					if (compoundPropertyname.equals(compoundProperty.getName()) && viewPropertyName.equals(viewProperty.getName()))
						return entityCompoundViewing.getPreventGroup();
				}
			}
		}
		
		return false;
	}
	
	private boolean hasNoDefaultOrMultipleGroups(List<PropertyModel> propertyModels) {
		String groupName = null;
		for (PropertyModel model : propertyModels) {
			if (!defaultGroup.equals(model.getGroupName()))
				return true;
			
			if (groupName == null)
				groupName = model.getGroupName();
			else if (!groupName.equals(model.getGroupName()))
				return true;
		}
		
		return false;
	}
	
	private Future<Void> loadAbsentProperties(List<PropertyModel> absentProperties) {
		final Future<Void> future = new Future<>();
		
		MultiLoader multiLoader = new MultiLoader();
		multiLoader.setParallel(false);
		int i = 0;
		List<PropertyModel> singleValueProperties = new ArrayList<>();
		for (PropertyModel propertyModel : absentProperties) {
			if (!propertyModel.getValueElementType().isCollection())
				singleValueProperties.add(propertyModel);
			else {
				multiLoader.add(Integer.toString(i++),
						loadAbsentProperty(propertyModel, isRestricted((CollectionType) propertyModel.getValueElementType())));
			}
		}
		
		if (!singleValueProperties.isEmpty())
			multiLoader.add(Integer.toString(i), loadSingleAbsentProperties(parentEntity, parentEntityType, singleValueProperties));
		
		multiLoader.load().onError(future::onFailure).andThen(result -> future.onSuccess(null));
		return future;
	}
	
	private Loader<?> loadSingleAbsentProperties(GenericEntity parentEntity, EntityType<GenericEntity> parentEntityType,
			List<PropertyModel> singleValueProperties) {
		return new Loader<Void>() {
			@Override
			public void load(final AsyncCallback<Void> asyncCallback) {
				if (parentEntity.getId() == null) {
					asyncCallback.onSuccess(null);
					return;
				}
				
				SelectQueryBuilder builder = new SelectQueryBuilder().from(parentEntityType, "e").where().entity("e").eq().entity(parentEntity);
				singleValueProperties.forEach(p -> builder.select("e", p.getPropertyName()));
				
				GenericEntity requestedParentEntity = parentEntity;
				ProfilingHandle ph = Profiling.start(getClass(), "Querying non collection absent properties in PP", true);
				gmSession.query().select(builder.done()).result(com.braintribe.processing.async.api.AsyncCallback.of( //
						selectQueryResult -> {
							if (parentEntity != requestedParentEntity)
								return; // Nothing to do. The parent entity was already changed, so we ignore the return

							ph.stop();
							Exception exception = null;
							gmSession.suspendHistory();
							try {
								SelectQueryResult result = selectQueryResult.result();
								List<Object> results = result.getResults();
								if (results.isEmpty())
									return;

								Object theResult = results.get(0);
								if (singleValueProperties.size() > 1) {
									ListRecord listRecord = (ListRecord) theResult;
									Iterator<Object> it = listRecord.getValues().iterator();
									for (PropertyModel propertyModel : singleValueProperties) {
										Property property = parentEntityType.getProperty(propertyModel.getPropertyName());
										Object value = it.next();
										if (!GMEUtil.isPropertyAbsent(parentEntity, property))
											continue;
										property.set(parentEntity, value);
									}
								} else {
									Property property = parentEntityType.getProperty(singleValueProperties.get(0).getPropertyName());
									if (!GMEUtil.isPropertyAbsent(parentEntity, property))
										return;
									property.set(parentEntity, theResult);
								}
							} catch (Exception e) {
								exception = e;
							} finally {
								gmSession.resumeHistory();

								if (exception == null)
									asyncCallback.onSuccess(null);
								else
									asyncCallback.onFailure(exception);
							}
						}, e -> {
							ph.stop();
							asyncCallback.onFailure(e);
						}));
			}
		};
	}

	private boolean isRestricted(CollectionType collectionType) {
		CollectionKind collectionKind = collectionType.getCollectionKind();
		return collectionKind.equals(CollectionKind.set) && collectionType.getCollectionElementType().isEntity();
	}
	
	private Loader<Void> loadAbsentProperty(final PropertyModel propertyModel, final boolean isRestricted) {
		return asyncCallback -> {
			if (parentEntity.getId() == null) {
				asyncCallback.onSuccess(null);
				return;
			}
			
			String propertyName = propertyModel.getPropertyName();
			Property property = parentEntityType.getProperty(propertyName);
			PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) parentEntity.reference(), propertyName,
					isRestricted ? maxCollectionSize + 1 : null, getSpecialTraversingCriterion(propertyModel.getValueElementType().getJavaType()),
					isRestricted, getMetaData(), useCase);
			
			ProfilingHandle ph = Profiling.start(getClass(), "Querying property '" + propertyName + "' in PP", true);
			GenericEntity requestedParentEntity = parentEntity;
			gmSession.query().property(propertyQuery).result(com.braintribe.processing.async.api.AsyncCallback.of( //
					propertyQueryResult -> {
						if (parentEntity != requestedParentEntity)
							return; // Nothing to do. The parent entity was already changed, so we ignore the return

						ph.stop();
						Exception exception = null;
						gmSession.suspendHistory();
						try {
							if (!GMEUtil.isPropertyAbsent(parentEntity, property))
								return; // Nothing to do. It was already loaded somewhere else, we then simply ignore
							PropertyQueryResult result = propertyQueryResult.result();
							Object value = result != null ? result.getPropertyValue() : null;
							value = GMEUtil.transformIfSet(value, propertyName, parentEntityType);

							if (value instanceof EnhancedCollection)
								((EnhancedCollection) value).setIncomplete(result.getHasMore());

							property.set(parentEntity, GMEUtil.sortIfSet(value, propertyQuery, gmSession, useCase, codecRegistry));
						} catch (Exception e) {
							exception = e;
						} finally {
							gmSession.resumeHistory();

							if (exception == null)
								asyncCallback.onSuccess(null);
							else
								asyncCallback.onFailure(exception);
						}
					}, e -> {
						ph.stop();
						asyncCallback.onFailure(e);
					}));
		};
	}
	
	private TraversingCriterion getSpecialTraversingCriterion(Class<?> clazz) {
		return specialEntityTraversingCriterion == null ? null : specialEntityTraversingCriterion.get(clazz);
	}
	
	private PropertyModel preparePropertyModel(GenericEntity entity, EntityType<GenericEntity> entityType, Property property,
			GenericModelType propertyType, boolean isEditable, boolean isMandatory, boolean baseTyped, ModelMdResolver metaDataResolver) {
		String propertyName = property.getName();
		PropertyModel model = new PropertyModel();
		model.setParentEntity(entity);
		model.setParentEntityType(entityType);
		model.setEditable(isEditable);
		model.setMandatory(isMandatory);
		model.setPropertyName(propertyName);
		model.setNullable(property.isNullable());
		model.setValue(entity != null ? property.get(entity) : null);
		model.setValueElementType(propertyType);
		model.setBaseTyped(baseTyped);
		model.setPropertyGroup(null);
		
		EntityMdResolver entityMdResolver = null;
		PropertyMdResolver propertyMdResolver = null;
		if (metaDataResolver != null) {
			if (entity != null)
				entityMdResolver = metaDataResolver.entity(entity).useCase(useCase).lenient(lenient);
			else
				entityMdResolver = metaDataResolver.entityType(entityType).useCase(useCase).lenient(lenient);
			propertyMdResolver = entityMdResolver.property(propertyName);
			
			model.setVirtualEnum(propertyMdResolver.meta(VirtualEnum.T).exclusive());
			
			PropertyPanelMetadataUtil.handleGroupAssignment(propertyName, metaDataResolver, model, entity, entityType,
					useCase, lenient, defaultGroup, resourceGmSession);
		
			model.setDynamicSelectList(propertyMdResolver.meta(DynamicSelectList.T).exclusive());
			handleMetadataReevaluation(propertyMdResolver, DynamicSelectList.T);
		}
		
		if (model.getPropertyGroup() == null)
			model.setGroupName(defaultGroup);
		
		Double priority = GMEMetadataUtil.getPropertyPriority(propertyMdResolver);
		if (priority == null)
			priority = Double.NEGATIVE_INFINITY;
		model.setPriority(priority);
		
		model.setPassword(GMEMetadataUtil.isPropertyPassword(propertyMdResolver));
		prepareExtendedInlineField(model);
		model.setFlow(PropertyPanelMetadataUtil.isPropertyFlow(this, propertyMdResolver, propertyType, model.getExtendedInlineField(),
				skipMetadataResolution, useCase, lenient, gmSession, logger, simplifiedEntityTypes, model.getPropertyName(),
				model.getParentEntity()));
		
		Pair<String, String> nameAndDescription = propertyMdResolver != null
				? GMEMetadataUtil.getPropertyDisplayAndDescription(propertyName, propertyMdResolver) : new Pair<>(propertyName, null);
		model.setDisplayName(nameAndDescription.getFirst());
		model.setDescription(nameAndDescription.getSecond() == null ? null : nameAndDescription.getSecond());
		model.setPlaceHolder(GMEMetadataUtil.getPlaceholder(propertyMdResolver));
		model.setHideLabel(PropertyPanelMetadataUtil.isHideLabel(propertyMdResolver, propertyType, metaDataResolver));
		model.setReferenceable(GMEMetadataUtil.isReferenceable(propertyMdResolver, metaDataResolver));
		model.setSimplifiedAssignment(propertyMdResolver == null ? null : propertyMdResolver.meta(SimplifiedAssignment.T).exclusive());
		model.setInline(propertyMdResolver == null ? false : propertyMdResolver.is(Inline.T));
		
		handleMetadataReevaluation(propertyMdResolver, SimplifiedAssignment.T);
		handleMetadataReevaluation(propertyMdResolver, Inline.T);
		
		if (model.getFlow())
			model.setFlowDisplay(propertyPanelGroupingView.prepareFlowDisplay(property, model));
		else
			model.setValueDisplay(propertyPanelGroupingView.prepareValueDisplay(property, model));
		
		if (model.getFlow() && model.isInline() && model.getValueElementType().isCollection())
			model.setValueDisplay(propertyPanelGroupingView.prepareValueDisplay(property, model, false));
		
		return model;
	}
	
	protected void prepareExtendedInlineField(PropertyModel propertyModel) {
		if (extendedInlineFields == null)
			return;
		
		Class<?> javaType;
		GenericModelType type = propertyModel.getValueElementType();
		if (!type.isEntity() && !type.isCollection())
			return;
		
		if (type.isEntity())
			javaType = type.getJavaType();
		else
			javaType = ((CollectionType) type).getCollectionElementType().getJavaType();
		
		Supplier<? extends ExtendedInlineField> supplier = extendedInlineFields.get(javaType);
		if (supplier == null)
			return;
		
		ExtendedInlineField field = supplier.get();
		field.configurePropertyPanel(this);
		
		propertyModel.setExtendedInlineField(field);
	}
	
	protected boolean isExtendedInlineFieldAvailable(PropertyModel propertyModel) {
		return isExtendedInlineFieldAvailable(propertyModel, false);
	}
	
	protected boolean isExtendedInlineFieldAvailable(PropertyModel propertyModel, boolean inline) {
		ExtendedInlineField field = propertyModel.getExtendedInlineField();
		if (field == null/* || propertyModel.getParentEntity() == null*/)
			return false;
		
		PropertyPathElement propertyPathElement = new PropertyPathElement(propertyModel.getParentEntity(),
				propertyModel.getParentEntityType().getProperty(propertyModel.getPropertyName()), propertyModel.getValue());
		
		return inline ? field.isAvailableInline(propertyPathElement) : field.isAvailable(propertyPathElement);
	}
	
	protected Future<Void> prepareEditors() {
		final Future<Void> future = new Future<>();
		final PropertyPanelEditorPreparationContinuation editorPreparation = getPropertyPanelEditorPreparationContinuation();
		Scheduler.get().scheduleDeferred(() -> editorPreparation.start(propertyPanelGrid.getStore().getAll()).get(future));
		
		return future;
	}
	
	private PropertyPanelEditorPreparationContinuation getPropertyPanelEditorPreparationContinuation() {
		if (propertyPanelEditorPreparationContinuation == null)
			propertyPanelEditorPreparationContinuation = new PropertyPanelEditorPreparationContinuation(this);
		
		return propertyPanelEditorPreparationContinuation;
	}
	
	protected native void refreshRow(GridView<PropertyModel> view, int row) /*-{
		view.@com.sencha.gxt.widget.core.client.grid.GridView::refreshRow(I)(row);
	}-*/;
	
	protected void fireCollectionElementSelected(int index, PropertyModel propertyModel) {
		if (!navigationEnabled || gmSelectionListeners == null)
			return;
		
		ModelPathElement collectionElement = getCollectionItemPathElement(index, propertyModel, false);
		if (collectionElement == null)
			return;
		
		ModelPath modelPath = getModelPath(propertyModel);
		modelPath.add(collectionElement);
		selectedModelPath = modelPath;
		
		fireGmSelectionListeners(propertyModel.isEditable());
	}

	public ModelPathElement getCollectionItemPathElement(int index, PropertyModel propertyModel, boolean allowSimple) {
		Collection<?> collection = propertyModel.getValue();
		String propertyName = propertyModel.getPropertyName();
		Property collectionProperty = propertyModel.getParentEntityType().getProperty(propertyName);
		
		GenericModelType collectionElementType = null;
		boolean isSimpleCollection;
		if (isPropertyInitializer(propertyName)) {
			collectionElementType = getTypeIfPropertyInitializer(propertyName);
			isSimpleCollection = collectionElementType.isSimple();
		} else {
			collectionElementType = ((CollectionType) collectionProperty.getType()).getCollectionElementType();
			isSimpleCollection = collectionElementType.isSimple();
		}

		if (!allowSimple && isSimpleCollection) 
			return null;
		
		Object element = null;
		ModelPathElement collectionElement;
		if (collection instanceof List) {
			element = ((List<?>) collection).get(index);
			if (element instanceof GenericEntity)
				collectionElementType = ((GenericEntity) element).entityType();
			collectionElement = new ListItemPathElement(propertyModel.getParentEntity(), collectionProperty, index, collectionElementType, element);
		} else {
			int i = 0;
			for (Object setElement : ((Set<?>) collection)) {
				if (i == index) {
					element = setElement;
					break;
				}
				i++;
			}
			if (element instanceof GenericEntity)
				collectionElementType = ((GenericEntity) element).entityType();
			
			collectionElement = new SetItemPathElement(propertyModel.getParentEntity(), collectionProperty, collectionElementType, element);
		}
		return collectionElement;
	}
	
	protected void fireMapElementSelected(int index, boolean isKey, PropertyModel propertyModel) {
		if (!navigationEnabled || gmSelectionListeners == null)
			return;
		
		Map<Object, Object> map = propertyModel.getValue();
		int i = 0;
		Map.Entry<Object, Object> entry = null;
		for (Map.Entry<Object, Object> mapEntry : map.entrySet()) {
			if (i == index) {
				entry = mapEntry;
				break;
			}
			i++;
		}
		
		if (entry == null)
			return;
		
		CollectionType collectionType = (CollectionType) propertyModel.getValueElementType();
		GenericModelType keyType = collectionType.getParameterization()[0];
		GenericModelType valueType = collectionType.getParameterization()[1];
		if (entry.getKey() instanceof GenericEntity)
			keyType = ((GenericEntity) entry.getKey()).entityType();
		if (entry.getValue() instanceof GenericEntity)
			valueType = ((GenericEntity) entry.getValue()).entityType();
		
		String propertyName = propertyModel.getPropertyName();
		GenericEntity modelParentEntity = propertyModel.getParentEntity();
		Property property = propertyModel.getParentEntityType().getProperty(propertyName);
		MapKeyPathElement mapKeyPathElement = new MapKeyPathElement(modelParentEntity, property, keyType, entry.getKey(), valueType,
				entry.getValue());
		ModelPathElement mapElement;
		if (isKey)
			mapElement = mapKeyPathElement;
		else {
			mapElement = new MapValuePathElement(modelParentEntity, property, keyType, entry.getKey(), valueType, entry.getValue(),
					mapKeyPathElement);
		}
		
		ModelPath modelPath = getModelPath(propertyModel);
		modelPath.add(mapElement);
		selectedModelPath = modelPath;
		
		fireGmSelectionListeners(propertyModel.isEditable());
	}
	
	protected void fireEntityPropertySelected(PropertyModel propertyModel) {
		if (navigationEnabled && gmSelectionListeners != null) {
			selectedModelPath = getModelPath(propertyModel);
			fireGmSelectionListeners(propertyModel.isEditable());
		}
	}
	
	protected void fireCollectionPropertySelected(PropertyModel propertyModel) {
		fireEntityPropertySelected(propertyModel);
	}
	
	protected Boolean fireStringPropertySelected(PropertyModel propertyModel, int index) {
		IsField<Object> field = propertyPanelGrid.gridInlineEditing.getEditor(propertyPanelGrid.getColumnModel().getColumn(PropertyPanel.VALUE_INDEX),
				propertyPanelGrid.getStore().indexOf(propertyModel));
		if (!navigationEnabled)
			return false;
		
		if (field instanceof TriggerFieldAction && !(propertyModel.getValueElementType().isCollection() && propertyModel.isInline())) {
			((TriggerFieldAction) field).getTriggerFieldAction().perform(null);
			return true;
		}
		
		PropertyPanelHelperMenu helperMenu = (PropertyPanelHelperMenu) getHelperMenu(false);
		return helperMenu.fireMultilineAction(propertyModel, index);
	}
	
	private void removeTriggerFieldActions() {
		if (helperMenu != null && triggerFieldActionItemMap != null) {
			for (TriggerFieldAction action : triggerFieldActionModelMap.values()) {
				MenuItem item = triggerFieldActionItemMap.get(action);
				if (item != null)
					helperMenu.remove(item);
			}
		}
		
		triggerFieldActionModelMap.clear();
		if (triggerFieldActionItemMap != null)
			triggerFieldActionItemMap.clear();
	}
	
	@Override
	protected Menu getHelperMenu(boolean update) {
		if (helperMenu == null) {
			helperMenu = new PropertyPanelHelperMenu(this);
			helperMenu.helperMenuPropertyModel = this.helperMenuPropertyModel;
		}
		
		if (update)
			helperMenu.updateMenu();
		
		return helperMenu;
	}
	
	@Override
	protected Menu getCollectionItemMenu(boolean update) {
		if (collectionItemMenu == null) {
			collectionItemMenu = new PropertPanelCollectionItemMenu(this);
			collectionItemMenu.menuPropertyModel = this.collectionItemMenuPropertyModel;
		}
		
		if (update)
			collectionItemMenu.updateMenu();
		
		return collectionItemMenu;
	}
	
	protected AddExistingEntitiesToCollectionAction getAddExistingEntitiesToCollectionAction() {
		if (addExistingEntitiesToCollectionAction == null) {
			addExistingEntitiesToCollectionAction = new AddExistingEntitiesToCollectionAction();
			addExistingEntitiesToCollectionAction.configureGmContentView(this);
			addExistingEntitiesToCollectionAction.setEntitySelectionFutureProvider(selectionFutureProviderProvider);
			addExistingEntitiesToCollectionAction.configureListener(() -> localManipulation = true);
		}
		
		return addExistingEntitiesToCollectionAction;
	}
	
	protected ChangeInstanceAction getShortcutChangeInstanceAction() {
		if (shortcutChangeInstanceAction == null) {
			shortcutChangeInstanceAction = new ChangeInstanceAction();
			shortcutChangeInstanceAction.setInstanceSelectionFutureProvider(selectionFutureProviderProvider);
			shortcutChangeInstanceAction.configureGmContentView(this);
			shortcutChangeInstanceAction.configureListener(() -> localManipulation = true);
		}
		
		return shortcutChangeInstanceAction;
	}
	
	private void fireGmSelectionListeners(boolean editable) {
		if (actionManager != null) {
			ModelAction action = actionManager.getWorkWithEntityAction(this);
			if (action != null) {
				TriggerInfo triggerInfo = null;			
				if (!editable) {
					triggerInfo = new TriggerInfo();
					triggerInfo.put(ModelAction.PROPERTY_READONLY, !editable);
				}
				
				if (action instanceof WorkWithEntityAction)
					((WorkWithEntityAction) action).configureUseWorkWithEntityExpert(useWorkWithEntityExpert);
				
				action.updateState(Collections.singletonList(Collections.singletonList(selectedModelPath)));
				action.perform(triggerInfo);
			}
		}
		
		if (gmSelectionListeners != null) {
			List<GmSelectionListener> listenersCopy = new ArrayList<>(gmSelectionListeners);
			listenersCopy.forEach(l -> l.onSelectionChanged(PropertyPanel.this));
		}
	}
	
	protected static ModelPath getModelPath(PropertyModel propertyModel)  {
		if (propertyModel == null)
			return null;
		
		ModelPath modelPath = new ModelPath();
			
		GenericEntity modelParentEntity = propertyModel.getParentEntity();
		EntityType<GenericEntity> modelParentEntityType = propertyModel.getParentEntityType();
		RootPathElement rootPathElement = new RootPathElement(modelParentEntityType, modelParentEntity);
		modelPath.add(rootPathElement);
				
		if (modelParentEntity != null) {
			PropertyPathElement propertyPathElement = new PropertyPathElement(modelParentEntity,
					modelParentEntityType.getProperty(propertyModel.getPropertyName()), propertyModel.getValue());
			modelPath.add(propertyPathElement);
		}
		
		if (propertyModel.getVirtualEnum() != null && !propertyModel.getValueElementType().isCollection()) {
			VirtualEnum ve = propertyModel.getVirtualEnum();
			PropertyPathElement propertyPathElement = new PropertyPathElement(ve, ve.entityType().getProperty("constants"), propertyModel.getValue());
			modelPath.add(propertyPathElement);			
		}		
		
		return modelPath;
	}
	
	protected boolean checkPropertyNameFlow(com.google.gwt.dom.client.Element e, PropertyPanelCss css) {
		if (e.getClassName().contains(css.propertyNameFlow()))
			return true;
		else
			return checkPropertyNameFlow(e.getParentElement(), css, 0);
	}
	
	private boolean checkPropertyNameFlow(com.google.gwt.dom.client.Element e, PropertyPanelCss css, int level) {
		if (e == null)
			return false;
		
		if (e.getClassName().contains(css.propertyNameFlow()))
			return true;
		
		if (level < MAX_LEVEL_FOR_PROPERTY_NAME_FLOW_CHECK)
			return checkPropertyNameFlow(e.getParentElement(), css, ++level);
		
		return false;
	}
	
	protected ModelMdResolver getMetaData() {
		return PropertyPanelMetadataUtil.getMetaData(skipMetadataResolution, gmSession, logger);
	}
	
//	protected ModelMdResolver getMetaData() {
//		if (skipMetadataResolution)
//			return null;
//		
//		if (modelMdResolver == null && parentEntity != null)
//			modelMdResolver = GmSessions.getMetaData(parentEntity);
//		
//		return modelMdResolver;
//	}
	
	protected static native void clearSelection() /*-{
		if ($wnd.document.selection)
        	$wnd.document.selection.empty();
        else if ($wnd.getSelection)
			$wnd.getSelection().removeAllRanges();
	}-*/;
	
	@Override
	public void handleValidationLog(ValidationLog validationLog, ValidationKind validationKind) {
		Scheduler.get().scheduleDeferred(() -> {
			GenericEntity parentEntity = null;
			boolean rowUpdated = false;
				
			List<Pair<PropertyModel, String>> listElementToolTip = new ArrayList<>();
			
			for (Entry<GenericEntity, ArrayList<ValidatorResult>> validationEntry : validationLog.entrySet()) {
				parentEntity = validationEntry.getKey();
				
				for ( ValidatorResult validatorResult : validationEntry.getValue()) {
                    if (validatorResult.getResultMessage() == null)
                        continue;						
					
					String failPropertyName = validatorResult.getPropertyName();
					for ( PropertyModel propertyModel : propertyPanelGrid.getStore().getAll()) {
						if (propertyModel.getParentEntity().equals(parentEntity) && propertyModel.getPropertyName().equals(failPropertyName)) {
							propertyModel.setValidationKind(validationKind);
							propertyModel.setValidationDescription(validatorResult.getResultMessage());						

							Element row = propertyPanelGrid.getView().getRow(propertyModel);
							refreshRow(propertyPanelGrid.getView(), propertyPanelGrid.getView().findRowIndex(row));							
							rowUpdated = true;
									
							listElementToolTip.add(new Pair<>(propertyModel, validatorResult.getResultMessage()));																							
						}
					}						
				}
			}
			
			if (rowUpdated && propertyPanelGrid.isRendered())
				propertyPanelGrid.expander.expandAllRows();								
			
			new Timer() {
				@Override
				public void run() {
					Map<String, GmeToolTip> mapToolCount = new HashMap<>();
					for (Pair<PropertyModel, String> pair : listElementToolTip) {
						PropertyModel propertyModel = pair.getFirst(); 
						GmeToolTip parentToolTip = mapToolCount.get(propertyModel.getPropertyName());

						Element row = propertyPanelGrid.getView().getRow(propertyModel);
						final Element imgElement = Document.get().getElementById("propertyValidationIcon_" + propertyPanelGrid.getView().findRowIndex(row));    
						if (imgElement == null)							
							continue;
						
						Integer propertyToolTipCount = 0;
						if (parentToolTip != null) 
							propertyToolTipCount = parentToolTip.getChildToolTipCount() + 1;
																						
						if (imgElement.getClassName().contains("failPropertyValidationIcon") || imgElement.getClassName().contains("infoPropertyValidationIcon")) {
							ToolTip tip = null;
							ToolTipConfig toolTipConfig = new ToolTipConfig(pair.getSecond());
							toolTipConfig.setAutoHide(false);						
							if (propertyToolTipCount == 0) {
								toolTipConfig.setAnchorArrow(true);
								toolTipConfig.setAnchor(Side.BOTTOM);
								toolTipConfig.setAnchorToTarget(true);
								
								tip = new GmeToolTip(PropertyPanel.this, imgElement, toolTipConfig);
								mapToolCount.put(propertyModel.getPropertyName(), (GmeToolTip) tip);							
							} else {
								tip = new GmeToolTip(PropertyPanel.this, null, toolTipConfig);
								parentToolTip.addChildToolTip((GmeToolTip) tip);
							}
							
							tip.setData("offset", propertyToolTipCount);
							if (imgElement.getClassName().contains("failPropertyValidationIcon")) 
								tip.addStyleName("failValidationPropertyToolTip");
							else
								tip.addStyleName("infoValidationPropertyToolTip");
						}
					}
				}
			}.schedule(500);			
		});
	}
	
	public GenericEntity getParentEntity() {
		return parentEntity;
	}
}