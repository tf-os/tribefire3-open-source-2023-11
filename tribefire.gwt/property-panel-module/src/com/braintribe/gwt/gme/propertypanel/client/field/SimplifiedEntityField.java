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
package com.braintribe.gwt.gme.propertypanel.client.field;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gme.propertypanel.client.LocalizedText;
import com.braintribe.gwt.gme.propertypanel.client.PropertyModel;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanelGrid;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField.QuickAccessTriggerFieldListener;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.client.SelectionTabConfig;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ExtendedStringCellDefaultAppearance;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.NoBlurWhileEditingField;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.ConfigurableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.qc.api.client.EntityFieldListener;
import com.braintribe.gwt.qc.api.client.EntityFieldSource;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.TriggerFieldCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;

/**
 * This field is responsible for showing a dialog for selecting entities of the given entity type.
 * @author michel.docouto
 *
 */
public class SimplifiedEntityField extends TriggerField<GenericEntity> implements NoBlurWhileEditingField, ManipulationListener, /*MetaDataReevaluationHandler,*/
		ConfigurableBean<SimplifiedEntityFieldConfiguration>, EntityFieldSource, TriggerFieldAction {
	private static Logger logger = new Logger(SimplifiedEntityField.class);
	private static final int DEFAULT_MIN_CHARS_FOR_FILTER = 1;
	private static int KEY_CODE_SPACE = 32;
	
	private EntityType<? extends GenericEntity> entityType;
	private List<SelectionTabConfig> tabsConfig;
	private boolean editingField = false;
	//private boolean configureReevaluationTrigger = true;
	private PersistenceGmSession gmSession;
	private List<EntityFieldListener> listeners;
	private PropertyModel propertyModel;
	private GenericEntity propertyModelRootEntity;
	private Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionConstellationDialogProvider;
	private Function<SelectionConfig, Future<InstanceSelectionData>> selectionConstellationDialog;
	private Action triggerAction;
	private AbstractGridEditing<?> gridEditing;
	private GridCell cellInGrid;
	private Map<GenericEntity, InstantiatedEntityListener> instantiatedEntityAndListenerMap;
	private GmContentView gmContentView;
	private boolean useQueryTabAsDefault;
	private GenericEntity internalValue;
	private SimplifiedEntityRendererCodec rendererCodec;
	private boolean disableClickAction = false;
	private boolean instantiable = true;
	private boolean referenceable = true;
	private boolean simplified = false;
	private boolean useDetail = true;
	private boolean manipulationListenerAdded;
	private String title;
	private int minCharsForFilter = DEFAULT_MIN_CHARS_FOR_FILTER;
	private Timer keyPressedTimer;
	private TypeCondition typeCondition;
	private SpotlightPanel quickAccessPanel;
	private QuickAccessDialog quickAccessDialog;
	private Supplier<SpotlightPanel> quickAccessPanelSupplier;
	private List<QuickAccessTriggerFieldListener> quickAccessTriggerFieldListeners;
	private boolean handleDetach;
	private SimplifiedEntityFieldConfiguration simplifiedEntityFieldConfiguration;

	/**
	 * Configures a required SimplifiedEntityRendererCodec for rendering the values in the not manually editable editor.
	 */
	public SimplifiedEntityField(final SimplifiedEntityRendererCodec rendererCodec) {
		super(new TriggerFieldCell<>(new ExtendedStringCellDefaultAppearance()));
		this.rendererCodec = rendererCodec;
		setPropertyEditor(new PropertyEditor<GenericEntity>() {
			@Override
			public String render(GenericEntity entity) {
				try {
					return rendererCodec.encode(entity);
				} catch (CodecException ex) {
					logger.error("Error while decoding entity.", ex);
					ex.printStackTrace();
					return "";
				}
			}

			@Override
			public GenericEntity parse(CharSequence text) throws ParseException {
				return internalValue;
			}
		});
		//setEditable(false);
		
		addTriggerClickHandler(event -> {
			if (!disableClickAction)
				handleTriggerClick();
		});
		
		addAttachHandler(event -> {
			if (event.isAttached())
				return;
			
			if (quickAccessDialog != null && handleDetach) {
				handleDetach = false;
				quickAccessDialog.handleEscape();
			}
			
			disposeBean();
		});
		
		init();
	}
	
	/**
	 * Configures the required provider for SelectionConstellationDialog.
	 */
	@Required
	public void setSelectionConstellationDialogProvider(Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionConstellationDialogProvider) {
		this.selectionConstellationDialogProvider = selectionConstellationDialogProvider;
	}
	
	/**
	 * Configures whether to disable the dialog from being shown when clicking in the field.
	 * Defaults to false.
	 */
	@Configurable
	public void setDisableClickAction(boolean disableClickAction) {
		this.disableClickAction = disableClickAction;
	}
	
	/**
	 * Configures the minimum number of chars entered for the filter to take place. Defaults to 1.
	 */
	@Configurable
	public void setMinCharsForFilter(int minCharsForFilter) {
		this.minCharsForFilter = minCharsForFilter;
	}
	
	/**
	 * Configures the useCase where this field is used on.
	 */
	public void configureUseCase(String useCase) {
		rendererCodec.configureUseCase(useCase);
	}
	
	/**
	 * Configures the {@link PersistenceGmSession}.
	 */
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		//TODO: are we ever removing this listener?
		if (this.gmSession != null) {
			this.gmSession.listeners().remove(this);
			manipulationListenerAdded = false;
		}
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the required {@link SpotlightPanel} to be connected with this field.
	 */
	@Required
	public void setQuickAccessPanel(Supplier<SpotlightPanel> quickAccessPanelSupplier) {
		this.quickAccessPanelSupplier = quickAccessPanelSupplier;
	}
	
	/**
	 * Configures the TypeCondition to be used within the Quick Access Dialog. If no type condition is set, then a default one is prepared,
	 * which gets all GenericEntity.
	 */
	@Configurable
	public void setTypeCondition(TypeCondition typeCondition) {
		this.typeCondition = typeCondition;
	}
	
	/**
	 * Configures the {@link GmContentView} where this field is being used on.
	 */
	public void configureGmContentView(GmContentView gmContentView) {
		this.gmContentView = gmContentView;
	}
	 
	/**
	 * This property must be set for this field. Unless {@link #setInitialConfiguration(SimplifiedEntityFieldConfiguration)} is called.
	 * It must be configured after the session via {@link #configureGmSession(PersistenceGmSession)} and the useCase via {@link #configureUseCase(String)}.
	 */
	public void configurePropertyModel(PropertyModel propertyModel, boolean instantiable, boolean simplified, boolean useDetail) {
		this.propertyModel = propertyModel;
		this.propertyModelRootEntity = propertyModel.getParentEntity();
		this.instantiable = instantiable;
		this.referenceable = propertyModel.isReferenceable();
		this.simplified = simplified;
		this.useDetail = useDetail;
		this.title = null;
		
		boolean readOnly = !instantiable && !referenceable;
		
		/*MetaDataAndTrigger<PropertySharesEntities> propertySharesData = MetaDataReevaluationHelper.getPropertySharesEntitiesData(new EntityInfo(
				GMF.getTypeReflection().getEntityType(propertyModelRootEntity), propertyModelRootEntity), propertyModel.getPropertyName(), null);
		boolean sharesEntities = propertySharesData == null || !propertySharesData.isValid() ? true : propertySharesData.getMetaData().getShares();*/
		setReadOnly(readOnly);
		
		if (readOnly)
			setDisableClickAction(readOnly);
		
		/*if (propertySharesData != null && propertySharesData.getReevaluationTrigger() != null && configureReevaluationTrigger) {
			MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(propertySharesData.getReevaluationTrigger(), this);
			configureReevaluationTrigger = false;
		}*/
	}
	
	@Override
	public Action getTriggerFieldAction() {
		if (triggerAction != null)
			return triggerAction;
		
		triggerAction = new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				handleTriggerClick();
			}
		};
		
		triggerAction.setIcon(PropertyPanelResources.INSTANCE.changeExisting());
		triggerAction.setName(LocalizedText.INSTANCE.assign());
		triggerAction.setTooltip(LocalizedText.INSTANCE.changeDescription());
		
		return triggerAction;
	}
	
	@Override
	public void setGridInfo(AbstractGridEditing<?> gridEditing, GridCell cellInGrid) {
		this.gridEditing = gridEditing;
		this.cellInGrid = cellInGrid;
	}
	
	@Override
	public XElement getInputEl() {
		return super.getInputEl();
	}
	
	private void init() {
		//setHeight(22); //synchronize with BT logo
		setEmptyText(LocalizedText.INSTANCE.typeToShowValues());
		
		this.addFocusHandler(event -> {
			if (getText().trim().length() >= minCharsForFilter)
				handleQuickAccess();
		});
		
		this.addKeyPressHandler(event -> Scheduler.get().scheduleDeferred(() -> {
			if (getText().trim().length() >= minCharsForFilter)
				getKeyPressedTimer().schedule(100);
		}));
		
		this.addKeyDownHandler(event -> {
			if (event.getNativeKeyCode() == KEY_CODE_SPACE && event.isControlKeyDown()) {
				event.stopPropagation();
				event.preventDefault();
				handleQuickAccess();
			}
		});
	}
	
	protected void handleTriggerClick() {
		if (isReadOnly())
			return;
		
		SelectionConfig selectionConfig;
		GenericModelType type;
		if (propertyModel != null) {
			String propertyName = propertyModel.getPropertyName();
			PropertyPathElement propertyPathElement = null;
			if (propertyModelRootEntity != null) {
				EntityType<GenericEntity> propertyEntityType = propertyModelRootEntity.entityType();
				Property property = propertyEntityType.getProperty(propertyName);
				propertyPathElement = new PropertyPathElement(propertyModelRootEntity, property, property.get(propertyModelRootEntity));
			}
			
			if (!simplified && propertyModel.getValueElementType().isSimple())
				simplified = true;
			
			type = propertyModel.getValueElementType();
			selectionConfig = new SelectionConfig(type, 1,
					GMEUtil.newEntityProperty(propertyModelRootEntity, propertyName), gmSession, null, instantiable, referenceable, simplified,
					useDetail, propertyPathElement);
		} else {
			type = entityType;
			selectionConfig = new SelectionConfig(type, tabsConfig, 1, null, gmSession, null, useQueryTabAsDefault, instantiable,
					referenceable, simplified, useDetail, null);
		}
		selectionConfig.setTitle(title);
		selectionConfig.setSubTitle(LocalizedText.INSTANCE.select(type instanceof EntityType ? ((EntityType<?>) type).getShortName() : type.getTypeName()));
		
		if (simplifiedEntityFieldConfiguration != null) {
			selectionConfig.setDialogWidth(simplifiedEntityFieldConfiguration.getDialogWidth());
			selectionConfig.setDialogHeight(simplifiedEntityFieldConfiguration.getDialogHeight());
		}
		
		getSelectionConstellationDialog().apply(selectionConfig) //
				.andThen(instanceSelectionData -> {
					int delay = 100;
					List<GMTypeInstanceBean> result = instanceSelectionData == null ? null : instanceSelectionData.getSelections();

					new Timer() {
						@Override
						public void run() {
							Scheduler.get().scheduleDeferred(() -> {
								if (gridEditing != null)
									gridEditing.startEditing(cellInGrid);

								if (result != null && !result.isEmpty()) {
									GMTypeInstanceBean bean = result.get(0);
									GenericEntity entity = (GenericEntity) bean.getInstance();
									populateField(entity, true, bean.isHandleInstantiation());
								}
								setEditing(false);

								if (gridEditing != null)
									gridEditing.completeEditing();

								if (result != null && !result.isEmpty() && result.get(0).isHandleInstantiation())
									handleInstantiation(result.get(0));
							});

						}
					}.schedule(delay);
				}).onError(e -> {
					ErrorDialog.show(LocalizedText.INSTANCE.errorChangingPropertyValue(), e);
					e.printStackTrace();
				});
		
		setEditing(true);
		if (isRendered())
			getInputEl().focus();
	}
	
	private void setEditing(boolean editing) {
		this.editingField = editing;
	}
	
	@Override
	public void clear() {
		super.clear();
		setAndFireValueChange(null, false);
	}
	
	@Override
	public void setValue(GenericEntity value) {
		setAndFireValueChange(value, false);
	}
	
	private void setAndFireValueChange(GenericEntity value, boolean isNewInstance) {
		super.setValue(value);
		internalValue = value;
		fireValueChanged(value, isNewInstance);
	}
	
	/**
	 * Populates the field with values.
	 */
	private void populateField(GenericEntity genericEntity, boolean focus, boolean isNewInstance) {
		if (focus)
			focus();
		
		setAndFireValueChange(genericEntity, isNewInstance);
	}
	
	@Override
	public boolean isEditingField() {
		return editingField;
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (instantiatedEntityAndListenerMap == null
				|| (!(manipulation instanceof DeleteManipulation)) && !(manipulation instanceof InstantiationManipulation)) {
			return;
		}
		
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
		if (gmContentView == null)
			return;
		
		InstantiatedEntityListener listener = GMEUtil.getInstantiatedEntityListener(gmContentView);
		if (listener != null) {
			if (!manipulationListenerAdded) {
				gmSession.listeners().add(this);
				manipulationListenerAdded = true;
			}
			
			listener.onEntityInstantiated(new RootPathElement(bean.getGenericModelType(), bean.getInstance()), true, false, null);
			if (instantiatedEntityAndListenerMap == null)
				instantiatedEntityAndListenerMap = new HashMap<GenericEntity, InstantiatedEntityListener>();
			instantiatedEntityAndListenerMap.put((GenericEntity) bean.getInstance(), listener);
		}
	}
	
	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		if (selectorContext instanceof PropertySelectorContext && metaData instanceof PropertyMetaData) {
			EntityType<?> entityType = GMF.getTypeReflection().getEntityType(owner.getEntityTypeSignature());
			String propertyName = owner.getPropertyName();
			
			MetaDataAndTrigger<PropertySharesEntities> propertySharesEntities = metaDataResolver.getPropertySharesEntities(entityType, propertyName, selectorContext);
			boolean sharesEntities = propertySharesEntities == null || !propertySharesEntities.isValid() ? true : propertySharesEntities.getMetaData().getShares();
			setReadOnly(!sharesEntities);
		}
	}*/
	
	/**
	 * Prepares the entityType and the session to be used when no Model is set via {@link #configurePropertyModel(PropertyModel, boolean, boolean, boolean)}
	 */
	@Override
	public void setInitialConfiguration(SimplifiedEntityFieldConfiguration simplifiedEntityFieldConfiguration) {
		this.simplifiedEntityFieldConfiguration = simplifiedEntityFieldConfiguration;
		this.entityType = simplifiedEntityFieldConfiguration.getEntityType();
		this.gmSession = simplifiedEntityFieldConfiguration.getGmSession();
		this.useQueryTabAsDefault = simplifiedEntityFieldConfiguration.isUseQueryTabAsDefault() && simplifiedEntityFieldConfiguration.getTabsConfig() != null;
		this.title = simplifiedEntityFieldConfiguration.getTitle();
		
		List<SelectionTabConfig> selectionTabsConfig = new ArrayList<>();
		if (simplifiedEntityFieldConfiguration.getTabsConfig() != null) {
			for (SimplifiedEntityFieldTabConfiguration fieldTabConfig : simplifiedEntityFieldConfiguration.getTabsConfig())
				selectionTabsConfig.add(new SelectionTabConfig(fieldTabConfig.getEntityQuery(), fieldTabConfig.getTabName()));
		}
		
		this.tabsConfig = selectionTabsConfig;
	}

	@Override
	public void addListener(EntityFieldListener listener) {
		if (listeners == null)
			listeners = new ArrayList<>();
		listeners.add(listener);
	}

	@Override
	public void fireValueChanged(GenericEntity entity, boolean isNewInstance) {
		if (listeners != null)
			listeners.forEach(listener -> listener.onValueChanged(entity, isNewInstance));
	}

	@Override
	public void removeListener(EntityFieldListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty())
				listeners = null;
		}
	}
	
	private Function<SelectionConfig, Future<InstanceSelectionData>> getSelectionConstellationDialog() {
		if (selectionConstellationDialog == null)
			selectionConstellationDialog = selectionConstellationDialogProvider.get();
		
		return selectionConstellationDialog;
	}
	
	private Timer getKeyPressedTimer() {
		if (keyPressedTimer == null) {
			keyPressedTimer = new Timer() {
				@Override
				public void run() {
					handleQuickAccess();
				}
			};
		}
		
		return keyPressedTimer;
	}
	
	protected void handleQuickAccess() {
		if (editingField)
			return;
		
		editingField = true;
		if (typeCondition == null)
			typeCondition = getQuickAccessPanel().prepareTypeCondition(GenericEntity.T);
		handleDetach = true;
		getQuickAccessDialog().getQuickAccessResult(typeCondition, this, getText(), false, true) //
				.andThen(result -> {
					handleDetach = false;
					quickAccessDialog.configureHandleKeyPress(false);
					fireQuickAccessResult(result);

					Widget parent = getParent();
					if (!(parent instanceof PropertyPanelGrid))
						return;

					PropertyPanelGrid grid = (PropertyPanelGrid) parent;
					boolean cancelPerformed = false;
					if (result != null) {
						GridCell gridCell = grid.getActiveCell();
						if (gridCell != null) {
							IsField<?> editor = grid.getEditor(gridCell.getCol());
							grid.handleCompleteEdit(result.getObject(), gridCell, editor);
							grid.markAsFinishedByEnter();
						}
					}

					if (!cancelPerformed)
						grid.cancelEditing();
				});
		
		quickAccessDialog.configureHandleKeyPress(true);
		getInputEl().focus();
	}
	
	private SpotlightPanel getQuickAccessPanel() {
		if (quickAccessPanel != null)
			return quickAccessPanel;
		
		quickAccessPanel = quickAccessPanelSupplier.get();
		return quickAccessPanel;
	}
	
	private Supplier<SpotlightPanel> getQuickAccessPanelProvider() {
		return () -> {
			SpotlightPanel quickAccessPanel = getQuickAccessPanel();
			quickAccessPanel.setMinCharsForFilter(minCharsForFilter);
			quickAccessPanel.setEnableAutoWidth(true);
			quickAccessPanel.setTextField(SimplifiedEntityField.this);
			return quickAccessPanel;
		};
	}
	
	private QuickAccessDialog getQuickAccessDialog() {
		if (quickAccessDialog != null)
			return quickAccessDialog;
		
		SpotlightPanel quickAccessPanel = getQuickAccessPanel();
		
		quickAccessDialog = new QuickAccessDialog();
		quickAccessDialog.setQuickAccessPanelProvider(getQuickAccessPanelProvider());
		quickAccessDialog.addStyleName(PropertyPanelResources.INSTANCE.css().border());
		quickAccessDialog.configureUseCase(KnownUseCase.selectionUseCase.getDefaultValue());
		
		quickAccessDialog.setFocusWidget(this);
		quickAccessDialog.setUseApplyButton(quickAccessPanel.getUseApplyButton());
		quickAccessDialog.setUseNavigationButtons(false);
		
		//if (instantiationLabel != null)
			//quickAccessDialog.setInstantiateButtonLabel(instantiationLabel);
		
		quickAccessDialog.addHideHandler(event -> {
			clear();
			new Timer() {
				@Override
				public void run() {
					Scheduler.get().scheduleDeferred(() -> {
						editingField = false;
						blur();
					});
				}
			}.schedule(501);
		});
		
		try {
			quickAccessDialog.intializeBean();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return quickAccessDialog;
	}
	
	private void fireQuickAccessResult(QuickAccessResult result) {
		if (quickAccessTriggerFieldListeners != null)
			quickAccessTriggerFieldListeners.forEach(listener -> listener.onQuickAccessResult(result));
	}
	
	public void addQuickAccessTriggerFieldListener(QuickAccessTriggerFieldListener listener) {
		if (quickAccessTriggerFieldListeners == null)
			quickAccessTriggerFieldListeners = new ArrayList<>();
		
		quickAccessTriggerFieldListeners.add(listener);
	}
	
	public void removeQuickAccessTriggerFieldListener(QuickAccessTriggerFieldListener listener) {
		if (quickAccessTriggerFieldListeners != null)
			quickAccessTriggerFieldListeners.remove(listener);
	}
	
	private void disposeBean() {
		if (manipulationListenerAdded) {
			gmSession.listeners().remove(SimplifiedEntityField.this);
			manipulationListenerAdded = false;
		}
		
		if (instantiatedEntityAndListenerMap != null) {
			instantiatedEntityAndListenerMap.clear();
			instantiatedEntityAndListenerMap = null;
		}
	}
	
	public class PropertyConfiguration {
		private String propertyName;
		private GenericEntity propertyValue;
		private EntityType<GenericEntity> propertyEntityType;
		private GenericEntity parentEntity;
		private GenericEntity rootEntity;
		
		public PropertyConfiguration(String propertyName, GenericEntity propertyValue, EntityType<GenericEntity> propertyEntityType, GenericEntity parentEntity,
				GenericEntity rootEntity) {
			this.propertyName = propertyName;
			this.propertyValue = propertyValue;
			this.propertyEntityType = propertyEntityType;
			this.parentEntity = parentEntity;
			this.rootEntity = rootEntity;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}
		
		public GenericEntity getPropertyValue() {
			return propertyValue;
		}
		
		public void setPropertyValue(GenericEntity propertyValue) {
			this.propertyValue = propertyValue;
		}

		public EntityType<GenericEntity> getPropertyEntityType() {
			return propertyEntityType;
		}

		public void setPropertyEntityType(EntityType<GenericEntity> propertyEntityType) {
			this.propertyEntityType = propertyEntityType;
		}

		public GenericEntity getParentEntity() {
			return parentEntity;
		}

		public void setParentEntity(GenericEntity parentEntity) {
			this.parentEntity = parentEntity;
		}
		
		public GenericEntity getRootEntity() {
			return rootEntity;
		}
		
		public void setRootEntity(GenericEntity rootEntity) {
			this.rootEntity = rootEntity;
		}
	}

}
