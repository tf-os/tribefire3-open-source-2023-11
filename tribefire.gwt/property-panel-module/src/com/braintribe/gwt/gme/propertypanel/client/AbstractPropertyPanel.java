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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.genericmodelgxtsupport.client.GMEditorSupport;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gme.propertypanel.client.field.ExtendedInlineField;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelCss;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.LocalManipulationAction;
import com.braintribe.gwt.gmview.action.client.LocalManipulationAction.LocalManipulationListener;
import com.braintribe.gwt.gmview.client.AlternativeGmSessionHandler;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmEditionView;
import com.braintribe.gwt.gmview.client.GmEditionViewController;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmPropertyView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.metadata.client.MetaDataReevaluationHelper;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesResources;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesStyle;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.validation.Validation;
import com.braintribe.model.generic.validation.log.ValidationLogView;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.prompt.AutoCommit;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.shared.FastSet;
import com.sencha.gxt.data.shared.Store.StoreFilter;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Menu;

/**
 * Abstract class, which is extended by the {@link PropertyPanel}, and have implementations for all its extended interfaces.
 * @author michel.docouto
 *
 */
public abstract class AbstractPropertyPanel extends ContentPanel
		implements InitializableBean, ManipulationListener, /* MetaDataReevaluationHandler, */ GmEntityView, GmEditionView, GmInteractionSupport,
		GmActionSupport, DisposableBean, AlternativeGmSessionHandler, ValidationLogView, GmPropertyView {
	
	protected List<Pair<ActionTypeAndName, ModelAction>> externalActions;
	protected GmContentViewActionManager actionManager;
	protected List<GmSelectionListener> gmSelectionListeners;
	protected ModelPath selectedModelPath;
	protected PropertyPanelGrid propertyPanelGrid;
	protected List<GmInteractionListener> gmInteractionListeners;
	protected PersistenceGmSession gmSession;
	protected PersistenceGmSession resourceGmSession;
	protected IconProvider iconProvider;
	protected String useCase;
	protected boolean lenient = true;
	protected boolean showGridLines = true;
	protected ValueRendering valueRendering = ValueRendering.none;
	protected GenericEntity parentEntity;
	protected EntityType<GenericEntity> parentEntityType;
	protected Set<GenericEntity> compoundEntities = new HashSet<>();
	protected Map<GenericEntity, Pair<GenericEntity, String>> compoundEntitiesMap = new HashMap<>();
	protected List<String> specialUiElementsStyles;
	protected Set<Class<?>> specialFlowClasses;
	protected GMEditorSupport gmEditorSupport;
	protected Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionFutureProviderProvider;
	protected boolean readOnly = false;
	protected Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion;
	protected CodecRegistry<String> codecRegistry;
	protected CodecRegistry<String> specialFlowCodecRegistry;
	protected int maxCollectionSize = 3;
	protected boolean forceGroups = false;
	protected boolean hideNonEditableProperties = false;
	protected boolean hideNonSimpleProperties = false;
	protected Set<Class<?>> simplifiedEntityTypes;
	protected int propertyNameColumnWidth = 150;
	protected boolean navigationEnabled = true;
	protected boolean enableMandatoryFieldConfiguration = true;
	protected PersistenceGmSession alternativeGmSession;
	
	protected Map<String, PropertyModel> invisiblePropertyModels;
	protected Map<PropertyModel, TriggerFieldAction> triggerFieldActionModelMap;
	protected boolean skipMetadataResolution;
	protected boolean defaultSkipMetadataResolution;
	protected boolean filterExternalActions = true;
	protected List<PropertyPanelListener> propertyPanelListeners;
	protected Map<Class<?>, Supplier<? extends ExtendedInlineField>> extendedInlineFields;
	protected TransientPersistenceGmSession transientSession;
	protected Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier;
	protected Supplier<? extends NotificationFactory> notificationFactorySupplier;
	protected Supplier<GmEditionViewController> gmEditionViewControllerSupplier;
	protected boolean autoCommit;
	protected Action commitAction;
	protected List<PropertyModel> absentProperties;
	protected PropertyModel lastEditedPropertyModel;
	protected boolean localManipulation;
	protected boolean useWorkWithEntityExpert = true;
	protected Set<String> metadataValueReevaluationProperties;
	protected PropertyPanelGroupingView propertyPanelGroupingView;
	protected Validation validation;
	private LocalManipulationListener localManipulationListener;
	private List<LocalManipulationAction> localManipulationActions;
	private StoreFilter<PropertyModel> propertiesFilter;
	
	public enum ValueRendering {
		none, gridlines, gridlinesForEmptyValues
	}
	
	
	public AbstractPropertyPanel() {
		setHeaderVisible(false);
		setBorders(false);
		setBodyBorder(false);
		addStyleName("gmePropertyPanelPanel");
		PropertyPanelCss css = PropertyPanelResources.INSTANCE.css();
		specialUiElementsStyles = Arrays.asList(css.propertyNameFlowExpanderCollapsed(), css.propertyNameFlowExpanderExpanded(),
				PropertyPanelCss.EXTERNAL_PROPERTY_MENU, css.checkedValue(), css.uncheckedValue(), css.checkNullValue(),
				PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_COLLECTION_ADD, PropertyPanelCss.EXTERNAL_PROPERTY_VALUE_ENTITY_ASSIGN,
				css.clickableTrigger());
		
		specialFlowClasses = new HashSet<Class<?>>();
		specialFlowClasses.add(LocalizedString.class);
	}
	
	/**
	  * Configures the required provider which will provide icons.
	  */
	@Required
	public void setIconProvider(IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}
	
	/**
	 * Configures the required {@link GMEditorSupport}, used for preparing the properties' fields.
	 */
	@Required
	public void setGMEditorSupport(GMEditorSupport gmEditorSupport) {
		this.gmEditorSupport = gmEditorSupport;
	}
	
	/**
	 * Configures the required {@link TransientPersistenceGmSession} used for running the OnEditFired request.
	 */
	@Required
	public void setTransientSession(TransientPersistenceGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	/**
	 * Configures the required Supplier for transient sessions, used for running the OnEditFired request.
	 */
	@Required
	public void setTransientSessionSupplier(Supplier<? extends TransientPersistenceGmSession> transientSessionSupplier) {
		this.transientSessionSupplier = transientSessionSupplier;
	}
	
	/**
	 * Configures the required {@link NotificationFactory} used for running the OnEditFired request.
	 */
	@Required
	public void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}
	
	/**
	 * Configure a provider which provides a selection future provider.
	 * If the read only property was not set to false via {@link #setReadOnly(boolean)} then this is required.
	 */
	@Configurable
	public void setSelectionFutureProvider(Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionFutureProvider) {
		this.selectionFutureProviderProvider = selectionFutureProvider;
	}
	
	/**
	 * Configures if the PropertyPanel will be shown as readOnly.
	 * Defaults to false (users can also edit the properties).
	 */
	@Override
	@Configurable
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	/**
	 * Configures a map containing special traversing criterion for the given entities.
	 * This is used when loading an absent property. Special entities (such as {@link LocalizedString}) require some properties to be loaded.
	 */
	@Configurable
	public void setSpecialEntityTraversingCriterion(Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion) {
		this.specialEntityTraversingCriterion = specialEntityTraversingCriterion;
	}
	
	/**
	 * Configures a {@link CodecRegistry} containing special flow renderers.
	 */
	@Configurable
	public void setSpecialFlowCodecRegistry(CodecRegistry<String> specialFlowCodecRegistry) {
		this.specialFlowCodecRegistry = specialFlowCodecRegistry;
	}
	
	/**
	 * Configures the {@link CodecRegistry} used as renderers.
	 */
	@Configurable
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	/**
	 * Configures the maximum size of collection' items that are displayed.
	 * Defaults to 3.
	 */
	@Configurable
	public void setMaxCollectionSize(int maxCollectionSize) {
		this.maxCollectionSize = maxCollectionSize;
	}
	
	/**
	 * Configures whether we should force the exhibition of groups, even if we only have the defaultGroup.
	 * Defaults to false (groups are always displayed).
	 * If false, groups will be only shown if we have different groups other than the defaultGroup.
	 */
	@Configurable
	public void setForceGroups(boolean forceGroups) {
		this.forceGroups = forceGroups;
	}
	
	/**
	 * Configures which classes should be handled specially when displayed in flow. There will be no link in the property name, and also none in the value.
	 * Defaults to a set containing only the {@link LocalizedString} class. Must not be null.
	 */
	@Configurable
	public void setSpecialFlowClasses(Set<Class<?>> specialFlowClasses) {
		this.specialFlowClasses = specialFlowClasses;
	}
	
	/**
	 * Configures whether non editable properties should be hidden. Defaults to false.
	 * If there is an explicit {@link Visible} or {@link Hidden} metadata set to the property, then that metadata is respected.
	 */
	@Configurable
	public void setHideNonEditableProperties(boolean hideNonEditableProperties) {
		this.hideNonEditableProperties = hideNonEditableProperties;
	}
	
	/**
	 * Configures whether non simple (or simplified) properties should be hidden. Defaults to false.
	 * If there is an explicit {@link Visible} or {@link Hidden} metadata set to the property, then that metadata is respected.
	 */
	@Configurable
	public void setHideNonSimpleProperties(boolean hideNonSimpleProperties) {
		this.hideNonSimpleProperties = hideNonSimpleProperties;
	}
	
	/**
	 * Configures a set of {@link EntityType}s that act as simplified by default.
	 */
	@Configurable
	public void setSimplifiedEntityTypes(Set<Class<?>> simplifiedEntityTypes) {
		this.simplifiedEntityTypes = simplifiedEntityTypes;
	}
	
	/**
	 * Configures the width (in pixels) of the property name column.
	 * Defaults to 150.
	 */
	@Configurable
	public void setPropertyNameColumnWidth(int propertyNameColumnWidth) {
		this.propertyNameColumnWidth = propertyNameColumnWidth;
	}
	
	/**
	 * Configures whether navigation (either via property name clicks, or the Open action) is enabled.
	 * Defaults to true.
	 */
	@Configurable
	public void setNavigationEnabled(boolean navigationEnabled) {
		this.navigationEnabled = navigationEnabled;
	}
	
	/**
	 * Configures whether the fields can be configured as mandatory. Defaults to true.
	 * If false, the mandatory metadata is ignored within the field configuration.
	 */
	@Configurable
	public void setEnableMandatoryFieldConfiguration(boolean enableMandatoryFieldConfiguration) {
		this.enableMandatoryFieldConfiguration = enableMandatoryFieldConfiguration;
	}
	
	/**
	 * Configures whether to show grid lines. Defaults to true.
	 */
	@Configurable
	public void setShowGridLines(boolean showGridLines) {
		this.showGridLines = showGridLines;
	}
	
	@Configurable
	public void setValueRendering(ValueRendering valueRendering) {
		this.valueRendering = valueRendering;
	}
	
	/**
	 * Configures whether to filter the external actions based on the actions defined in the root folder.
	 * Defaults to true.
	 */
	@Configurable
	public void setFilterExternalActions(boolean filterExternalActions) {
		this.filterExternalActions = filterExternalActions;
	}
	
	/**
	 * Configures whether we should skip the metadata resolution within this instance of the PropertyPanel.
	 * If true, then no metadata is resolved.
	 */
	@Configurable
	public void setSkipMetadataResolution(boolean skipMetadataResolution) {
		this.defaultSkipMetadataResolution = skipMetadataResolution;
	}
	
	/**
	 * Configures extended fields that should be placed under collections of a given type.
	 */
	@Configurable
	public void setExtendedInlineFields(Map<Class<?>, Supplier<? extends ExtendedInlineField>> extendedInlineFields) {
		this.extendedInlineFields = extendedInlineFields;
	}
	
	/**
	 * Configures the {@link Action} used for committing when {@link AutoCommit} is available.
	 */
	@Configurable
	public void setCommitAction(Action commitAction) {
		this.commitAction = commitAction;
	}
	
	/**
	 * Configures whether to use the WorkWithEntityExpert when dealing with the WorkWithEntityAction.
	 * Defaults to true.
	 */
	@Configurable
	public void setUseWorkWithEntityExpert(boolean useWorkWithEntityExpert) {
		this.useWorkWithEntityExpert = useWorkWithEntityExpert;
	}
	
	@Override
	public void intializeBean() throws Exception {
		add(preparePropertyPanelGrid());
		
		iconProvider.configureUseCase(useCase);
		if (!showGridLines) {
			GridWithoutLinesStyle style = GWT.<GridWithoutLinesResources>create(GridWithoutLinesResources.class).css();
			style.ensureInjected();
			propertyPanelGrid.addStyleName(style.gridWithoutLines());
			addStyleName(PropertyPanelResources.INSTANCE.css().propertyPanelWithoutLines());
		}
	}
	
	@Override
	public void setValidation(Validation validation) {
		this.validation = validation;
		if (gmEditorSupport != null)
			gmEditorSupport.setValidation(this.validation);
	}
	
	public boolean isSkipMetadataResolution() {
		return skipMetadataResolution;
	}

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		Widget actionMenu = actionManager.getActionMenu(this, null, filterExternalActions);
		if (actionMenu instanceof Menu) {
			Menu menu = (Menu) actionMenu;
			if (menu.getWidgetCount() > 0) {
				Menu helperMenu = getHelperMenu(false);
				
				List<Widget> items = new ArrayList<>();
				for (int i = 0; i < menu.getWidgetCount(); i++)
					items.add(menu.getWidget(i));
				items.forEach(item -> helperMenu.add(item));
			}
		}
	}

	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (externalActions == null)
			this.externalActions = null;
		else {
			this.externalActions = new ArrayList<>(externalActions);
			externalActions.stream().filter(p -> !actionManager.isActionAvailable(p.first)).forEach(pair -> this.externalActions.remove(pair));
		}
		
		if (this.externalActions != null) {
			List<? extends Widget> itemsAdded = actionManager.addExternalActions(this, this.externalActions);
			if (itemsAdded != null) {
				Menu helperMenu = getHelperMenu(false);
				itemsAdded.forEach(item -> helperMenu.add(item));
			}
		}
	}

	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return externalActions;
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;
	}

	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		return this.actionManager;
	}	
	
	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (sl != null) {
			if (gmSelectionListeners == null)
				gmSelectionListeners = new ArrayList<>();
			
			if (!gmSelectionListeners.contains(sl))
				gmSelectionListeners.add(sl);
		}
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		if (gmSelectionListeners != null) {
			gmSelectionListeners.remove(sl);
			if (gmSelectionListeners.isEmpty())
				gmSelectionListeners = null;
		}
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return selectedModelPath;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return Collections.singletonList(selectedModelPath);
	}

	@Override
	public boolean isSelected(Object element) {
		return propertyPanelGrid.getSelectionModel().getSelectedItems().stream().anyMatch(m -> m.getValue() == element);
	}

	@Override
	public void select(int index, boolean keepExisting) {
		propertyPanelGrid.getSelectionModel().select(index, keepExisting);
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public void addInteractionListener(GmInteractionListener il) {
		if (il != null) {
			if (gmInteractionListeners == null)
				gmInteractionListeners = new ArrayList<>();
			gmInteractionListeners.add(il);
		}
	}

	@Override
	public void removeInteractionListener(GmInteractionListener il) {
		if (gmInteractionListeners != null) {
			gmInteractionListeners.remove(il);
			if (gmInteractionListeners.isEmpty())
				gmInteractionListeners = null;
		}
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		if (this.gmSession != gmSession) {
			this.gmSession = gmSession;			
			if (!(gmSession instanceof TransientPersistenceGmSession))
				this.resourceGmSession = gmSession;
			MetaDataReevaluationHelper.setGmSession(gmSession);
			iconProvider.configureGmSession(gmSession);
			if (actionManager != null)
				Scheduler.get().scheduleDeferred(() -> {
					actionManager.connect(this);
					addListenersToCollections(actionManager.getActionsForView(this));
				});
			
			if (gmSession instanceof ModelEnvironmentDrivenGmSession && ((ModelEnvironmentDrivenGmSession) gmSession).getModelEnvironment() == null)
				skipMetadataResolution = true;
			else
				skipMetadataResolution = defaultSkipMetadataResolution;
			
			if (!skipMetadataResolution && gmSession != null) {
				ModelMdResolver mdResolver = gmSession.getModelAccessory().getMetaData();
				autoCommit = mdResolver == null ? false : mdResolver.is(AutoCommit.T);
			}
		}
	}
	
	/**
	 * Configures the optional session to be as an alternative session for the editors and actions.
	 */
	@Override
	public void configureAlternativeGmSession(PersistenceGmSession alternativeGmSession) {
		this.alternativeGmSession = alternativeGmSession;
	}
	
	@Override
	public PersistenceGmSession getAlternativeGmSession() {
		return alternativeGmSession;
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	public boolean isLenient() {
		return lenient;
	}

	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
		if (iconProvider != null)
			iconProvider.configureUseCase(useCase);
	}

	@Override
	public String getUseCase() {
		return useCase;
	}

	@Override
	public void noticeManipulation(final Manipulation manipulation) {
		if (parentEntity == null || !(manipulation instanceof PropertyManipulation))
			return;
		
		new Timer() {
			@Override
			public void run() {
				Object parentObject = GMEUtil.getParentObject(manipulation);
				GenericEntity entity = null;
				String propertyName = null;
				Owner manipulationOwner = ((PropertyManipulation) manipulation).getOwner();
				if (manipulationOwner instanceof LocalEntityProperty) {
					entity = ((LocalEntityProperty) manipulationOwner).getEntity();
					propertyName = ((LocalEntityProperty) manipulationOwner).getPropertyName();
				}
				
				if (parentEntity == parentObject || (parentEntity == entity && entity != null)) {
					if (propertyName != null) {
						updatePropertyModelValue(null, propertyName, parentEntityType.getProperty(propertyName).get(parentEntity), parentEntityType,
								null, localManipulation);
					}
				} else if (compoundEntities != null) {
					if ((compoundEntities.contains(parentObject) || compoundEntities.contains(entity)) && propertyName != null) {
						GenericEntity compoundEntity = compoundEntities.contains(parentObject) ? (GenericEntity) parentObject : entity;
						
						Pair<GenericEntity, String> pair = compoundEntitiesMap.get(compoundEntity);
						if (pair != null) {
							propertyName = pair.getSecond();
							compoundEntity = pair.getFirst();
						}
						
						EntityType<GenericEntity> entityType = compoundEntity.entityType();
						updatePropertyModelValue(null, propertyName, entityType.getProperty(propertyName).get(compoundEntity), entityType,
								compoundEntity, localManipulation);
					}
				}
				
				if (metadataValueReevaluationProperties != null && metadataValueReevaluationProperties.contains(propertyName))
					refreshPropertyPanel();
			}
		}.schedule(10); //needed, so the value in the entity was the correct one
	}
	
	@Override
	public void stopEditing() {
		propertyPanelGrid.gridInlineEditing.cancelEditing();
	}
	
	@Override
	public void setGmEditionViewController(Supplier<GmEditionViewController> controllerSupplier) {
		this.gmEditionViewControllerSupplier = controllerSupplier;
	}
	
	@Override
	public void hideProperties(List<String> properties) {
		unhideProperties();
		propertyPanelGrid.getStore().addFilter(getPropertiesFilter(properties));
	}
	
	@Override
	public void unhideProperties() {
		if (propertiesFilter != null) {
			propertyPanelGrid.getStore().removeFilter(propertiesFilter);
			propertiesFilter = null;
		}
	}
	
	private StoreFilter<PropertyModel> getPropertiesFilter(List<String> propertiesToHide) {
		if (propertiesFilter != null)
			return propertiesFilter;
		
		propertiesFilter = (store, parent, item) -> !propertiesToHide.contains(item.getNormalizedPropertyName());
		return propertiesFilter;
	}

	@Override
	public void disposeBean() throws Exception {
		setContent(null);
		
		if (compoundEntities != null) {
			compoundEntities.clear();
			compoundEntities = null;
		}
		
		compoundEntitiesMap.clear();
		//compoundEntitiesMap = null;
		
		if (gmInteractionListeners != null) {
			gmInteractionListeners.clear();
			gmInteractionListeners = null;
		}
		
		if (gmSelectionListeners != null) {
			gmSelectionListeners.clear();
			gmSelectionListeners = null;
		}
		
		if (actionManager != null)
			actionManager.notifyDisposedView(this);
		
		if (externalActions != null) {
			externalActions.clear();
			externalActions = null;
		}
		
		if (invisiblePropertyModels != null) {
			invisiblePropertyModels.clear();
			invisiblePropertyModels = null;
		}
		
		if (triggerFieldActionModelMap != null) {
			triggerFieldActionModelMap.clear();
			triggerFieldActionModelMap = null;
		}
		
		if (propertyPanelListeners != null) {
			propertyPanelListeners.clear();
			propertyPanelListeners = null;
		}
		
		if (absentProperties != null) {
			absentProperties.clear();
			absentProperties = null;
		}
		
		if (metadataValueReevaluationProperties != null) {
			metadataValueReevaluationProperties.clear();
			metadataValueReevaluationProperties = null;
		}
		
		if (localManipulationActions != null) {
			localManipulationActions.forEach(action -> action.configureListener(null));
			localManipulationActions.clear();
			localManipulationActions = null;
		}
	}
	
	protected void handleMetadataReevaluation(EntityMdResolver entityMdResolver, EntityType<? extends MetaData> metadataType) {
		if (entityMdResolver == null)
			return;
		
		EntityMdResolver ignoreSelectorsResolver = entityMdResolver.fork();
		MetaData metadata = ignoreSelectorsResolver.ignoreSelectors().meta(metadataType).exclusive();
		Set<String> propertiesToBeReevaluated = GMEMetadataUtil.getPropertyPathsForMetadataWithPropertyValueComparator(metadata);
		if (propertiesToBeReevaluated != null)
			propertiesToBeReevaluated.forEach(property -> addToMetadataReevaluation(property));
	}
	
	protected void handleMetadataReevaluation(PropertyMdResolver propertyMdResolver, EntityType<? extends MetaData> metadataType) {
		if (propertyMdResolver == null)
			return;
		
		PropertyMdResolver ignoreSelectorsResolver = propertyMdResolver.fork();
		MetaData metadata = ignoreSelectorsResolver.ignoreSelectors().meta(metadataType).exclusive();
		Set<String> propertiesToBeReevaluated = GMEMetadataUtil.getPropertyPathsForMetadataWithPropertyValueComparator(metadata);
		if (propertiesToBeReevaluated != null)
			propertiesToBeReevaluated.forEach(property -> addToMetadataReevaluation(property));
	}
	
	private void addToMetadataReevaluation(String propertyName) {
		if (metadataValueReevaluationProperties == null)
			metadataValueReevaluationProperties = new FastSet();
		
		metadataValueReevaluationProperties.add(propertyName);
	}
	
	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		if (selectorContext instanceof PropertySelectorContext && metaData instanceof PropertyMetaData) {
			if (((PropertySelectorContext) selectorContext).getEntity() == parentEntity) {
				EntityType<?> entityType = GMF.getTypeReflection().getEntityType(owner.getEntityTypeSignature());
				String propertyName = owner.getPropertyName();
				
				MetaDataAndTrigger<PropertyVisibility> visibleData = metaDataResolver.getPropertyVisibility(entityType, propertyName, selectorContext);
				boolean visible = visibleData == null || !visibleData.isValid() ? true : visibleData.getMetaData().getVisible();
				
				MetaDataAndTrigger<PropertyEditable> editableData = metaDataResolver.getPropertyEditable(entityType, propertyName, selectorContext);
				boolean editable = editableData == null || !editableData.isValid() ? true : editableData.getMetaData().getEditable();
				
				MetaDataAndTrigger<MandatoryProperty> mandatoryData = metaDataResolver.getMandatoryProperty(entityType, propertyName, selectorContext);
				boolean mandatory = mandatoryData == null || !mandatoryData.isValid() ? false : mandatoryData.getMetaData().getMandatory();
				
				PropertyModel editedModel = null;
				for (PropertyModel model : propertyPanelGrid.getStore().getModels()) {
					if (model.getPropertyName().equals(propertyName)) {
						editedModel = model;
						break;
					}
				}
				if (editedModel == null) {
					for (PropertyModel model : invisiblePropertyModels) {
						if (model.getPropertyName().equals(propertyName)) {
							editedModel = model;
							break;
						}
					}
				}
				editedModel.setEditable(editable);
				editedModel.setMandatory(mandatory);
				
				boolean added = false;
				if (visible) {
					if (invisiblePropertyModels.contains(editedModel)) {
						invisiblePropertyModels.remove(editedModel);
						int indexToInsert = getIndexToInsert(editedModel, propertyPanelGrid.getStore().getModels());
						insertToGrid(editedModel, indexToInsert);
						prepareCustomEditor(editedModel, entityType, propertyPanelGrid.getColumnModel().getColumn(VALUE_INDEX), indexToInsert);
						added = true;
					}
				} else {
					if (propertyPanelGrid.getStore().contains(editedModel)) {
						invisiblePropertyModels.add(editedModel);
						propertyPanelGrid.getStore().remove(editedModel);
					}
				}
				
				if (!added && propertyPanelGrid.getStore().contains(editedModel)) {
					refreshRow(propertyPanelGrid.getView(), propertyPanelGrid.getStore().indexOf(editedModel));
				}
			}
		}
	}*/
	
	public void saveScrollState() {
		propertyPanelGroupingView.saveScrollState();
	}
	
	public void restoreScrollState() {
		propertyPanelGroupingView.restoreScrollState();
	}
	
	private void refreshPropertyPanel() {
		saveScrollState();
		
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(parentEntity));
		setContent(modelPath);
		
		restoreScrollState();
	}
	
	private void addListenersToCollections(List<Action> actions) {
		for (Action action : actions) {
			if (action instanceof LocalManipulationAction) {
				if (localManipulationActions == null)
					localManipulationActions = new ArrayList<>();
				((LocalManipulationAction) action).configureListener(getLocalManipulationListener());
				localManipulationActions.add((LocalManipulationAction) action);
			}
		}
	}
	
	private LocalManipulationListener getLocalManipulationListener() {
		if (localManipulationListener != null)
			return localManipulationListener;
		
		localManipulationListener = () -> localManipulation = true;
		return localManipulationListener;
	}
	
	protected abstract Menu getHelperMenu(boolean update);

	protected abstract Menu getCollectionItemMenu(boolean update);
	
	protected abstract Grid<PropertyModel> preparePropertyPanelGrid();
	
	protected abstract void updatePropertyModelValue(PropertyModel propertyModel, String propertyName, Object value, EntityType<?> entityType,
			GenericEntity compoundParentEntity, boolean handleAutoCommit);

}