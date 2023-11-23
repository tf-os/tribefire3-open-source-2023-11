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
package com.braintribe.gwt.gme.assemblypanel.client;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.AddToClipboardActionFolderContent;
import com.braintribe.gwt.action.adapter.gxt.client.ButtonActionAdapter;
import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.genericmodelgxtsupport.client.GMEditorSupport;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyUtil.ValueDescriptionBean;
import com.braintribe.gwt.gme.assemblypanel.client.action.CopyEntityToClipboardAction;
import com.braintribe.gwt.gme.assemblypanel.client.action.ExchangeAssemblyPanelDisplayModeAction;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbsentModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.CollectionTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.CompoundTreePropertyModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.CondensedEntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.DelegatingTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.EntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ListTreeModelInterface;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapKeyAndValueTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ObjectAndType;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntry;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntryModelInterface;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.SetTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.TreePropertyModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.factory.ModelFactory;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelResources;
import com.braintribe.gwt.gme.assemblypanel.client.ui.InstanceTypeSelectorDialog;
import com.braintribe.gwt.gme.assemblypanel.client.ui.InstanceTypeSelectorDialog.InstanceTypeSelectorDialogParameters;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.ChangeInstanceAction;
import com.braintribe.gwt.gmview.action.client.CopyTextAction;
import com.braintribe.gwt.gmview.action.client.KnownActions;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmeDragAndDropSupport;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil.KeyAndValueGMTypeInstanceBean;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedColumnHeader;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.AbsentingManipulation;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.MinLength;
import com.braintribe.model.meta.data.prompt.AutoExpand;
import com.braintribe.model.meta.data.prompt.ColumnDisplay;
import com.braintribe.model.meta.data.prompt.CondensationMode;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.ScrollDirection;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.WindowManager;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;

/**
 * The AssemblyPanel display the maximum of information of entities, in a generic way.
 * @author michel.docouto
 *
 */
public class AssemblyPanel extends AbstractAssemblyPanel {
	private static final int MAX_DATA_TO_RENDER = 2000;
	
	protected boolean readOnly = false;
	protected boolean useCheckBoxColumn;
	private boolean isTopLevelMap;
	protected GMEditorSupport gmEditorSupport;
	private Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion;
	private String nodeColumnHeader = LocalizedText.INSTANCE.node();
	private CopyEntityToClipboardAction copyEntityToClipboardAction;
	private MenuItem emptyMenuItem;
	private boolean useDragAndDrop = false;
	private static Set<AbstractGenericTreeModel> currentClipboardModels = new LinkedHashSet<>();
	private boolean showTreeProperties = true;
	protected boolean showNodeTextAsTooltip = false;
	private boolean allowExpandNodes = true;
	private CollectionTreeModel rootCollectionTreeModel;
	private GenericEntity entityForProperties;
	private int autoExpandLevel = 0;
	private HTML emptyPanel;
	private Widget currentWidget;
	private Menu helperMenu;
	protected TreePropertyModel helperMenuPropertyModel;
	protected GridCell helperMenuGridCell;
	private MenuItem setNullItem;
	private MenuItem clearStringItem;
	private MenuItem workWithItem;
	private MenuItem changeInstanceItem;
	private ChangeInstanceAction changeInstanceAction;
	private Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionFutureProviderProvider;
	protected Map<String, TriggerFieldAction> propertyTriggerFieldActionMap;
	private Map<TriggerFieldAction, MenuItem> triggerFieldActionItemMap;
	protected boolean showLinkStyle = false;
	private boolean useCopyEntityToClipboardAction = true;
	private boolean useExchangeAssemblyPanelDisplayModeAction = true;
	protected boolean enableRowSelection = true;
	private String emptyTextMessage = LocalizedText.INSTANCE.noItemsToDisplay();
	private boolean contentSet = false;
	private HandlerRegistration viewReadyRegistration;
	private ManipulationListener rootEnhancedSetManipulationListener;
	protected boolean ignoreSelection;
	private Point savedScrollState;
	private Set<Class<?>> simplifiedEntityTypes;
	private Action defaultContextMenuAction;
	private AutoExpand autoExpand;
	private boolean cancelContinuationRendering;
	private boolean renderEntriesWithContinuation;
	private boolean disableTreeViewResizeHandling;
	private boolean renderingContinuationData;
	
	 /**
	  * Configures the {@link GMEditorSupport}.
	  * If the read only property was not set to false via {@link #setReadOnly(boolean)}, then this is required.
	  */
	 @Configurable
	 public void setGMEditorSupport(GMEditorSupport gmEditorSupport) {
		 this.gmEditorSupport = gmEditorSupport;
	 }
	
	/**
	 * Configures whether the panel is in read only mode.
	 * Defaults to false.
	 */
	@Override
	@Configurable
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	/**
	 * Configures whether to use a check box column.
	 * Defaults to false.
	 */
	@Configurable
	public void setUseCheckBoxColumn(boolean useCheckBoxColumn) {
		this.useCheckBoxColumn = useCheckBoxColumn;
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
	 * Configures the initial header to be used in the node column.
	 * Defaults to the localized entry "node" of the {@link LocalizedText}.
	 */
	@Configurable
	public void setNodeColumnHeader(String nodeColumnHeader) {
		this.nodeColumnHeader = nodeColumnHeader;
	}
	
	/**
	 * Configures whether to use drag and drop.
	 * Defaults to false.
	 */
	@Configurable
	public void setUseDragAndDrop(boolean useDragAndDrop) {
		this.useDragAndDrop = useDragAndDrop;
	}
	
	/**
	 * Configures whether we should show property columns in the tree.
	 * Defaults to true. If false, only the node column will be shown.
	 */
	@Configurable
	public void setShowTreeProperties(boolean showTreeProperties) {
		this.showTreeProperties = showTreeProperties;
	}
	
	/**
	 * Configures whether we should use the text in the tree node as toolTip in case the node hasn't any default toolTip set (via metaData).
	 * Defaults to false. 
	 */
	@Configurable
	public void setShowNodeTextAsTooltip(boolean showNodeTextAsTooltip) {
		this.showNodeTextAsTooltip = showNodeTextAsTooltip;
	}
	
	/**
	 * Configures whether we should disable the recursion dimension (expanding tree nodes) or not.
	 * Defaults to true: recursion dimension (expanding tree nodes) is allowed.
	 */
	@Configurable
	public void setAllowExpandNodes(boolean allowExpandNodes) {
		this.allowExpandNodes = allowExpandNodes;
	}
	
	/**
	 * Configures how many levels should be automatically expanded.
	 * Defaults to 0 (none).
	 */
	@Configurable
	public void setAutoExpandLevel(int autoExpandLevel) {
		this.autoExpandLevel = autoExpandLevel;
	}
	
	/**
	 * Configure a provider which provides a selection future provider.
	 * If {@link #setReadOnly(boolean)} wasn't set to false, nor {@link #setPrepareToolBarActions(boolean)} was set to false,  then this is required.
	 */
	@Configurable
	public void setSelectionFutureProvider(Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> selectionFutureProvider) {
		this.selectionFutureProviderProvider = selectionFutureProvider;
	}
	
	/**
	 * Configures whether the link style should be shown when rendering nodes.
	 * Defaults to false.
	 * Notice that {@link #setPrepareToolBarActions(boolean)} must be set to true as well, in order for the link style to be shown.
	 */
	@Configurable
	public void setShowLinkStyle(boolean showLinkStyle) {
		this.showLinkStyle = showLinkStyle;
	}
	
	/**
	 * Configures whether we should use the {@link CopyEntityToClipboardAction}.
	 * Defaults to true.
	 */
	@Configurable
	public void setUseCopyEntityToClipboardAction(boolean useCopyEntityToClipboardAction) {
		this.useCopyEntityToClipboardAction = useCopyEntityToClipboardAction;
	}
	
	/**
	 * Configures whether to use the {@link ExchangeAssemblyPanelDisplayModeAction}.
	 * Defaults to true.
	 */
	@Configurable
	public void setUseExchangeAssemblyPanelDisplayModeAction(boolean useExchangeAssemblyPanelDisplayModeAction) {
		this.useExchangeAssemblyPanelDisplayModeAction = useExchangeAssemblyPanelDisplayModeAction;
	}
	
	/**
	 * Configures whether to enable row selection within the tree grid.
	 * Defaults to true.
	 */
	@Configurable
	public void setEnableRowSelection(boolean enableRowSelection) {
		this.enableRowSelection = enableRowSelection;
	}
	
	/**
	 * Configures the message shown while displaying the empty data panel.
	 * Defaults to the localized entry of "noItemsToDisplay".
	 */
	@Configurable
	public void setEmptyTextMessage(String emptyTextMessage) {
		this.emptyTextMessage = emptyTextMessage;
	}
	
	/**
	 * Configures a set of {@link EntityType}s that act as simplified by default.
	 */
	@Configurable
	public void setSimplifiedEntityTypes(Set<Class<?>> simplifiedEntityTypes) {
		this.simplifiedEntityTypes = simplifiedEntityTypes;
	}
	
	public void setIgnoreSelection(boolean ignoreSelection) {
		this.ignoreSelection = ignoreSelection;
	}
	
	@Override
	public void addContent(ModelPath modelPath) {
		setContent(modelPath, false);
	}
	
	@Override
	public void setColumnData(ColumnData columnData) {
		this.columnData = columnData;
	}
	
	@Override
	public void setAutoExpand(AutoExpand autoExpand) {
		this.autoExpand = autoExpand;
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		setContent(modelPath, true);
		
		if (rootEnhancedSet != null)
			rootEnhancedSet.removeManipulationListener(getRootEnhancedSetManipulationListener());
		
		if (modelPath != null && !modelPath.isEmpty() && modelPath.last().getValue() instanceof EnhancedSet) {
			rootEnhancedSet = modelPath.last().getValue();
			rootEnhancedSet.addManipulationListener(getRootEnhancedSetManipulationListener());
		} else
			rootEnhancedSet = null;
	}
	
	private void setContent(ModelPath modelPath, boolean initialData) {
		boolean addingInitialElements = initialData ? initialData : rootModelPath == null && (addedModelPaths == null || addedModelPaths.isEmpty());
		
		if (initialData) {
			if (deletedEntities != null) {
				deletedEntities.clear();
				deletedEntities = null;
			}
			
			this.rootModelPath = modelPath;
			if (addedModelPaths != null)
				addedModelPaths.clear();
			
			if (currentSelectedItems == null)
				currentSelectedItems = new ArrayList<>();
			else
				currentSelectedItems.clear();
			
			if (currentExpandedItems == null)
				currentExpandedItems = new ArrayList<>();
			else
				currentExpandedItems.clear();
			
			List<AbstractGenericTreeModel> selectedItems = editorTreeGrid.getSelectionModel().getSelectedItems();
			selectedItems.forEach(selectedItem -> currentSelectedItems.add(selectedItem.getModelObject()));
			
			getExpandedEntries(editorTreeGrid.getTreeStore().getRootItems());
		} else {
			if (addedModelPaths == null)
				addedModelPaths = new ArrayList<>();
			addedModelPaths.add(modelPath);
		}
		
		if (modelPath != null) {
			ProfilingHandle ph = Profiling.start(getClass(), "Configuring top level object", false, true);
			ModelPathElement lastElement = modelPath.last();
			configureTopLevelObject(lastElement.getValue(), lastElement.getType(), addingInitialElements);
			ph.stop();
			
			if (lastElement instanceof PropertyRelatedModelPathElement)
				addManipulationListener((PropertyRelatedModelPathElement) lastElement);

			if (autoExpand == null && (entityTypeForProperties != null || entityForProperties != null)) {
				EntityMdResolver entityMdResolver;
				if (entityForProperties != null) {
					entityMdResolver = getMetaData(entityForProperties).entity(entityForProperties).useCase(useCase);
				} else
					entityMdResolver = gmSession.getModelAccessory().getMetaData().lenient(true).entityType(entityTypeForProperties);
				
				autoExpand = entityMdResolver.meta(AutoExpand.T).exclusive();
			}
			
			if (autoExpandLevel != 0 || autoExpand != null)
				expandEntries();
			
			contentSet = true;
			if (addingInitialElements)
				fireContentSet();
			if (editorTreeGrid.getTreeStore().getRootCount() == 0)
				deferredShowEmptyPanel();
			else {
				if (currentExpandedItems != null && !currentExpandedItems.isEmpty()) {
					expandCurrentExpandedItems(editorTreeGrid.getTreeStore().getRootItems());
					currentExpandedItems.clear();
				}
				
				if (currentSelectedItems != null && !currentSelectedItems.isEmpty()) {
					selectCurrentSelectedItems(editorTreeGrid.getTreeStore().getRootItems());
					currentSelectedItems.clear();
				}
			}
		} else if (initialData) {
			if (editorTreeGrid.getTreeStore().getRootCount() != 0)
				editorTreeGrid.getTreeStore().clear();
			else
				deferredShowEmptyPanel();
		}
	}
	
	private void deferredShowEmptyPanel() {
		Scheduler.get().scheduleDeferred(() -> {
			if (editorTreeGrid.getTreeStore().getRootCount() == 0)
				exchangeCenterWidget(getEmptyPanel());
		});
	}
	
	@Override
	protected void initializeActions() {
		String addToClipboardActionName = KnownActions.ADD_TO_CLIPBOARD.getName();
		boolean addToClipboardAvailable = actionManager
				.isActionAvailable(new ActionTypeAndName(AddToClipboardActionFolderContent.T, addToClipboardActionName));
		Map<ActionTypeAndName, Boolean> actionsAvailability = null;
		if (prepareToolBarActions && buttonsList == null) {
			buttonsList = new ArrayList<>();
			
			if (addToClipboardAvailable && copyEntityToClipboardAction != null) {
				TextButton copyEntityToClipboardButton = new TextButton();
				copyEntityToClipboardButton.setIcon(AssemblyPanelResources.INSTANCE.clipboardBig());
				copyEntityToClipboardButton.setWidth(70);
				copyEntityToClipboardButton.setIconAlign(IconAlign.TOP);
				copyEntityToClipboardButton.setScale(ButtonScale.LARGE);
				ButtonActionAdapter.linkActionToButton(true, copyEntityToClipboardAction, copyEntityToClipboardButton);
				buttonsList.add(new Pair<>(addToClipboardActionName, copyEntityToClipboardButton));
			}
			
			if (externalActions != null) {
				actionsAvailability = new HashMap<>();
				List<Pair<ActionTypeAndName, ModelAction>> externalActionsChecked = new ArrayList<>(externalActions);
				for (Pair<ActionTypeAndName, ModelAction> pair : externalActions) {
					ActionTypeAndName actionTypeAndName = pair.getFirst();
					boolean actionAvailable = actionManager.isActionAvailable(actionTypeAndName);
					actionsAvailability.put(actionTypeAndName, actionAvailable);
					if (!actionAvailable)
						externalActionsChecked.remove(pair);
						
				}
				
				List<Pair<String, TextButton>> externalButtons = GMEUtil.prepareExternalActionButtons(externalActionsChecked);
				if (externalButtons != null)
					buttonsList.addAll(externalButtons);
			}
		}
		
		if (!showContextMenu || menuItemsList != null)
			return;
		
		menuItemsList = new ArrayList<>();
		
		if (defaultContextMenuActionSupplier != null) {
			defaultContextMenuAction = defaultContextMenuActionSupplier.get();
			emptyMenuItem = new MenuItem(defaultContextMenuActionName);
			//emptyMenuItem.setIcon(defaultContextMenuAction.getHoverIcon());
			emptyMenuItem.addSelectionHandler(event -> defaultContextMenuAction.perform(null));
		} else {
			emptyMenuItem = new MenuItem(LocalizedText.INSTANCE.noOptionsAvailable());
			emptyMenuItem.setEnabled(false);
		}
		
		emptyMenuItem.setVisible(false);
		menuItemsList.add(new Pair<>("Not Known", emptyMenuItem));
		
		if (addToClipboardAvailable && copyEntityToClipboardAction != null) {
			MenuItem copyEntityToClipboardItem = new MenuItem();
			MenuItemActionAdapter.linkActionToMenuItem(copyEntityToClipboardAction, copyEntityToClipboardItem);
			menuItemsList.add(new Pair<>(addToClipboardActionName, copyEntityToClipboardItem));
		}
		
		if (externalActions != null) {
			List<Pair<ActionTypeAndName, ModelAction>> externalActionsChecked = new ArrayList<>(externalActions);
			for (Pair<ActionTypeAndName, ModelAction> pair : externalActions) {
				ActionTypeAndName actionTypeAndName = pair.getFirst();
				boolean actionAvailable = actionsAvailability != null ? actionsAvailability.get(actionTypeAndName)
						: actionManager.isActionAvailable(actionTypeAndName);
				if (!actionAvailable)
					externalActionsChecked.remove(pair);
					
			}
			
			List<Pair<String, MenuItem>> externalMenuItems = GMEUtil.prepareExternalMenuItems(externalActionsChecked);
			if (externalMenuItems != null)
				menuItemsList.addAll(externalMenuItems);
		}
		
		//actionManager.addExternalComponents(this, menuItemsList);
		Widget actionMenu = actionManager.getActionMenu(this, menuItemsList, filterExternalActions);
		if (actionMenu instanceof Menu)
			actionsContextMenu = (Menu) actionMenu;
	}
		
	/**
	 * Configures the topLevel object, the one used for building up the AssemblyPanel tree models.
	 * It may be an already existing {@link AbstractGenericTreeModel} node. In this case, that node will be used as root node.
	 * @param object - Either the direct value, or an {@link AbstractGenericTreeModel}.
	 * @param modelType - The {@link GenericModelType} of the object, or null in case the object is an {@link AbstractGenericTreeModel}.
	 * @param initialData - true when adding the initial data (either through setContent, or addContent).
	 */
	private void configureTopLevelObject(Object object, GenericModelType modelType, boolean initialData) {
		if (initialData) {
			if (editorTreeGrid.getTreeStore().getRootCount() != 0)
				editorTreeGrid.getTreeStore().clear();
			isTopLevelMap = object instanceof Map || object instanceof MapTreeModel;
			allRootItemsForRenderLimit.clear();
		}
		
		if (object == null)
			return;
		
		boolean refreshRequired = false; //Checking if we need the refresh when adding the first entry to the AP (this will resize the AP columns).
		if (editorTreeGrid.isViewReady() && editorTreeGrid.getTreeStore().getRootCount() == 0)
			refreshRequired = true;
				
		//if (object instanceof GenericEntity)
			//ensureSessionAttached((GenericEntity) object);
		AbstractGenericTreeModel treeModel = null;
		
		if (object instanceof AbstractGenericTreeModel) {
			treeModel = (AbstractGenericTreeModel) object;
			AbstractGenericTreeModel delegateModel = treeModel.getDelegate();
			EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
			if (entityTreeModel != null) {
				entityForProperties = entityTreeModel.getModelObject();
				entityTypeForProperties = entityTreeModel.getElementType();
			} else if (delegateModel instanceof CollectionTreeModel && !(delegateModel instanceof CondensedEntityTreeModel)) {
				if (delegateModel instanceof MapTreeModel && ((MapTreeModel) delegateModel).getKeyElementType().isEntity()) {
					entityForProperties = null;
					entityTypeForProperties = ((MapTreeModel) delegateModel).getKeyElementType();
				} else if (((CollectionTreeModel) delegateModel).getElementType().isEntity()) {
					entityForProperties = null;
					entityTypeForProperties = ((CollectionTreeModel) delegateModel).getElementType();
				}
			}
			modelFactory.configureEntityTypeForPreparingProperties(entityTypeForProperties);
		} else {
			ObjectAndType objectAndType = new ObjectAndType();
			objectAndType.setObject(object);
			objectAndType.setType(modelType);
			objectAndType.setDepth(ModelFactory.MAX_DEPTH - 1);
			objectAndType.setRoot(true);
			
			if (initialData) {
				entityForProperties = null;
				if (modelType.isEntity()) {
					if (object instanceof GenericEntity)
						entityForProperties = (GenericEntity) object;
					entityTypeForProperties = (EntityType<GenericEntity>) modelType;
				} else if (modelType.isCollection()) {
					if (((CollectionType) modelType).getCollectionKind().equals(CollectionKind.map))
						nodeColumnHeader = LocalizedText.INSTANCE.keyValuePairs();
					else {
						CollectionType collectionType = (CollectionType) modelType;
						if (collectionType.getCollectionElementType().isEntity()) {
							entityTypeForProperties = (EntityType<GenericEntity>) collectionType.getCollectionElementType();
							if (object instanceof Collection) {
								Collection<?> collection = (Collection<?>) object;
								entityForProperties = (GenericEntity) collection.stream().filter(Objects::nonNull).findAny().orElse(null);
							}
						}
					}
				}
				modelFactory.configureEntityTypeForPreparingProperties(entityTypeForProperties);
			}
			
			ProfilingHandle ph = Profiling.start(getClass(), "Building model", false, true);
			treeModel = modelFactory.apply(objectAndType);
			ph.stop();
		}
		
		if (initialData) {
			ModelMdResolver modelMdResolver ;
			if (entityForProperties != null)
				modelMdResolver = getMetaData(entityForProperties);
			else
				modelMdResolver = gmSession.getModelAccessory().getMetaData().lenient(true);
			
			if (entityTypeForProperties != null) {
				if (columnData == null) {
					ColumnDisplay columnDisplay = modelMdResolver.entityTypeSignature(entityTypeForProperties.getTypeSignature()).useCase(useCase)
							.meta(ColumnDisplay.T).exclusive();
					columnData = GMEMetadataUtil.prepareColumnData(columnDisplay);
					
					if (columnData != null)
						logger.debug("ColumnData was prepared from the ColumnDisplay metadata for the '" + entityTypeForProperties.getTypeSignature() + "' entityType.");
				}
				
				if (showTreeProperties)
					editorTreeGrid.prepareTreeGridColumns(entityForProperties, entityTypeForProperties, modelMdResolver, columnData);
			}
			
			String header;
			if (columnData != null && columnData.getNodeTitle() != null)
				header = I18nTools.getLocalized(columnData.getNodeTitle());
			else {
				header = entityTypeForProperties == null ? nodeColumnHeader
						: GMEMetadataUtil.getEntityNameMDOrShortName(entityTypeForProperties, modelMdResolver, useCase);
			}
			editorTreeGrid.nodeColumn.setHeader(header);
		}
		
		if (treeModel != null) {
			int maxEntriesToSelect = this.maxSelectionCount;				
				
			if (useCheckBoxColumn)
				editorTreeGrid.getSelectionModel().setSelectionMode(maxEntriesToSelect > 1 ? SelectionMode.MULTI : SelectionMode.SINGLE);
			
			ProfilingHandle ph = Profiling.start(getClass(), "Adding tree models", false, true);
			if (treeModel instanceof CollectionTreeModel && !(treeModel instanceof CondensedEntityTreeModel)) {
				rootCollectionTreeModel = (CollectionTreeModel) treeModel;
				List<AbstractGenericTreeModel> children = treeModel.getChildren();
				if (children != null) {
					List<AbstractGenericTreeModel> entriesToAdd = new ArrayList<>(treeModel.getChildren());
					allRootItemsForRenderLimit.addAll(entriesToAdd);
					if (entriesToAdd.size() * editorTreeGrid.getColumnModel().getColumnCount() < MAX_DATA_TO_RENDER && !renderEntriesWithContinuation)
						AssemblyUtil.addToTreeWithChildren(this, null, entriesToAdd);
					else {
						if (renderingContinuationData) {
							if (itemsWaitingForRendering == null)
								itemsWaitingForRendering = new ArrayList<>();
							
							itemsWaitingForRendering.addAll(entriesToAdd);
							return;
						}
						
						renderEntriesWithContinuation = true;
						disableViewportCheck = true;
						AbstractGenericTreeModel firstItem = entriesToAdd.remove(0);
						AssemblyUtil.addToTreeWithChildren(this, null, firstItem);
						
						if (!entriesToAdd.isEmpty())
							Scheduler.get().scheduleDeferred(() -> renderDataWithContinuation(entriesToAdd));
					}
				}
			} else {
				rootCollectionTreeModel = null;
				AssemblyUtil.addToTreeWithChildren(this, null, treeModel);
				allRootItemsForRenderLimit.add(treeModel);
			}
			ph.stop();
		}
		
		if (refreshRequired)
			Scheduler.get().scheduleDeferred(editorTreeGrid.getView()::layout);
	}
	
	/*private void ensureSessionAttached(GenericEntity entity) {
		if (entity.session() == null) {
			entity.attach(getGmSession());
		}
	}*/
	
	/**
	 * Clears the checked items.
	 */
	@Override
	public void clearCheckedItems() {
		editorTreeGrid.selectionModel.clearCheckedItems(false);
	}
	
	/**
	 * Returns the auto expand level configured via {@link #setAutoExpandLevel(int)}.
	 */
	public int getAutoExpandLevel() {
		return autoExpandLevel;
	}
	
	/**
	 * Returns the configuration for using the {@link ExchangeAssemblyPanelDisplayModeAction}.
	 */
	public boolean isUseExchangeAssemblyPanelDisplayModeAction() {
		return useExchangeAssemblyPanelDisplayModeAction;
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//ProfilingHandle ph = Profiling.start(getClass(), "Handling manipulation in AP", false, true);
		Object parentObject = GMEUtil.getParentObject(manipulation);
		List<AbstractGenericTreeModel> treeRootItems = editorTreeGrid.getTreeStore().getRootItems();
		
		if (ManipulationType.DELETE.equals(manipulation.manipulationType())) {
			GenericEntity deletedEntity = ((DeleteManipulation) manipulation).getEntity();
			
			TreeStore<AbstractGenericTreeModel> treeStore = editorTreeGrid.getTreeStore();
			int i = 0;
			for (AbstractGenericTreeModel rootModel : treeRootItems) {
				if (rootModel.refersTo(deletedEntity)) {
					if (deletedEntities == null)
						deletedEntities = new HashMap<>();
					deletedEntities.put(deletedEntity, new ModelAndIndex(rootModel, i));
					treeStore.remove(rootModel);
					break;
				}
				i++;
			}
			
			return;
		}
		
		if (ManipulationType.INSTANTIATION.equals(manipulation.manipulationType())
				|| ManipulationType.MANIFESTATION.equals(manipulation.manipulationType())) {
			GenericEntity deletedEntity = ManipulationType.INSTANTIATION.equals(manipulation.manipulationType())
					? ((InstantiationManipulation) manipulation).getEntity() : ((ManifestationManipulation) manipulation).getEntity();
			if (deletedEntities != null) {
				ModelAndIndex modelAndIndex = deletedEntities.remove(deletedEntity);
				if (modelAndIndex != null)
					AssemblyUtil.insertToTreeStore(this, null, modelAndIndex.model, modelAndIndex.index, false, true);
			}
			
			return;
		}
		
		if (!(manipulation instanceof PropertyManipulation))
			return;
		
		Owner owner = ((PropertyManipulation) manipulation).getOwner();
		Property property = null;
		if (owner instanceof LocalEntityProperty) {
			GenericEntity entity = ((LocalEntityProperty) owner).getEntity();
			if (isEntitySimplified(entity)) {
				updateSimplifiedEntityTreeModel(entity);
				return;
			}
			
			if (entity != null)
				property = entity.entityType().findProperty(owner.getPropertyName());
		}
		
		modelFactory.configureEntityTypeForPreparingProperties(entityTypeForProperties);
		
		Set<AbstractGenericTreeModel> parentModels = null;
		if (parentObject != null) {
			parentModels = new LinkedHashSet<>();
			
			for (AbstractGenericTreeModel rootModel : treeRootItems)
				parentModels.addAll(getParentModels(rootModel, parentObject));
			
			if (rootCollectionTreeModel != null && owner instanceof LocalEntityProperty && isRootPathRelatedToEntityProperty((LocalEntityProperty) owner))
				parentModels.add(rootCollectionTreeModel);
		}
		
		if (parentModels != null) {
			for (AbstractGenericTreeModel parentModel : parentModels) {
				parentModel.setLabel(null);
				if (property != null) {
					TreePropertyModel treePropertyModel = parentModel.getDelegate().getTreePropertyModel(property);
					if (treePropertyModel != null)
						treePropertyModel.setLabel(null);
				}
			}
		}
		
		switch (manipulation.manipulationType()) {
		case CHANGE_VALUE:
			if (parentModels != null && !parentModels.isEmpty()) {
				AssemblyUtil.onChangeManipulation((ChangeValueManipulation) manipulation, parentModels, this);
				
				if (parentObject instanceof GenericEntity)
					checkForRefresh((GenericEntity) parentObject, parentModels, property != null ? property : null);
			} else {
				if (owner instanceof LocalEntityProperty)
					updateCompoundProperties((LocalEntityProperty) owner);
				editorTreeGrid.getView().refresh(false);
			}
			updateRootModelPath((ChangeValueManipulation) manipulation);
			break;
		case ABSENTING:
			AssemblyUtil.onChangeManipulation((AbsentingManipulation) manipulation, parentModels, this);
			updateRootModelPath((AbsentingManipulation) manipulation);
			break;
		case ADD:
			/*boolean addToRoot = false;
			if (rootModelPath != null && rootModelPath.last().getValue() == parentObject)
				addToRoot = true;*/
			if (parentModels != null) {
				for (AbstractGenericTreeModel model : parentModels) {
					EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
					if (treeRootItems.contains(model) && entityTreeModel == null) {
						parentModels.remove(model);
						break;
					}
				}
			}
			
			AssemblyUtil.onInsertToCollectionManipulation((PropertyManipulation) manipulation, parentModels, this/*, addToRoot*/);
			break;
		case REMOVE:
			if (rootCollectionTreeModel != null && parentModels.contains(rootCollectionTreeModel)) {
				//parentModels.remove(rootCollectionTreeModel);
				if (rootCollectionTreeModel.getCollectionType().getCollectionKind().equals(CollectionKind.map)) {
					handleRemoveTreeRootMapEntry((RemoveManipulation) manipulation, treeRootItems);
					parentModels.remove(rootCollectionTreeModel);
				}/* else {
					Collection<Object> itemsToRemove = ((RemoveManipulation) manipulation).getItemsToRemove().values();
					for (Object itemToRemove : itemsToRemove) {
						int counter = 0;
						for (AbstractGenericTreeModel model : treeRootItems) {
							if (model.refersTo(itemToRemove)) {
								AssemblyUtil.removeFromTreeStore(editorTreeGrid, null, counter, false);
								break;
							}
							counter++;
						}
					}
				}*/
			}
			/*boolean removeFromRoot = false;
			if (rootModelPath != null && rootModelPath.last().getValue() == parentObject)
				removeFromRoot = true;*/
			if (!parentModels.isEmpty())
				AssemblyUtil.onRemoveFromCollectionManipulation(manipulation, parentModels, editorTreeGrid/*,  removeFromRoot, parentObject*/);
			break;
		case CLEAR_COLLECTION:
			AssemblyUtil.onClearCollectionManipulation(parentObject, parentModels, editorTreeGrid);
			break;
		default:
			break;
		}
		
		//ph.stop();
	}
	
	private void handleRemoveTreeRootMapEntry(RemoveManipulation manipulation, List<AbstractGenericTreeModel> treeRootItems) {
		Collection<Object> itemsToRemove = manipulation.getItemsToRemove().keySet();
		
		for (Object itemToRemove : itemsToRemove) {
			int counter = 0;
			for (AbstractGenericTreeModel model : treeRootItems) {
				if (model.refersTo(itemToRemove) || (model instanceof MapKeyAndValueTreeModel
						&& ((MapKeyAndValueTreeModel) model).getMapKeyEntryTreeModel().refersTo(itemToRemove))) {
					AssemblyUtil.removeFromTreeStore(editorTreeGrid, null, counter, false);
					break;
				}
				counter++;
			}
		}
	}
	
	private void updateSimplifiedEntityTreeModel(GenericEntity entity) {
		List<AbstractGenericTreeModel> treeRootItems = editorTreeGrid.getTreeStore().getRootItems(); //TODO: other entries, and not only root items, may be associated with the changed LS.
		for (AbstractGenericTreeModel rootModel : treeRootItems) {
			EntityTreeModel entityTreeModel = rootModel.getDelegate().getEntityTreeModel();
			if (entityTreeModel == null)
				continue;
			
			Collection<TreePropertyModel> treePropertyModels = entityTreeModel.getTreePropertyModels();
			treePropertyModels.stream().filter(m -> m.getValue() == entity).findFirst().ifPresent(treePropertyModel -> {
				treePropertyModel.setLabel(null);
				editorTreeGrid.getTreeStore().update(rootModel);
				editorTreeGrid.selectionModel.handleSelectionChanged(Collections.singletonList(rootModel), true);
			});
		}
	}

	private void updateCompoundProperties(LocalEntityProperty owner) {
		GenericEntity ownerEntity = owner.getEntity();
		Property property = ownerEntity.entityType().getProperty(owner.getPropertyName());
		for (AbstractGenericTreeModel rootModel : editorTreeGrid.getTreeStore().getRootItems()) {
			AbstractGenericTreeModel delegateModel = rootModel.getDelegate();
			EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
			if (entityTreeModel != null) {
				TreePropertyModel treePropertyModel = entityTreeModel.getTreePropertyModel(property);
				if (treePropertyModel != null && treePropertyModel.getParentEntity() == ownerEntity)
					treePropertyModel.setValue(property.get(ownerEntity));
			}
		}
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		
		if (renderingContinuationData)
			cancelContinuationRendering = true;
		renderEntriesWithContinuation = false;
	}
	
	@Override
	public void saveScrollState() {
		if (savedScrollState == null)
			savedScrollState = editorTreeGrid.getView().getScroller() != null ? editorTreeGrid.getView().getScrollState() : null;
	}
	
	private void restoreScrollState() {
		if (savedScrollState == null)
			return;
		
		XElement scroller = editorTreeGrid.getView().getScroller();
		
		if (savedScrollState.getY() > scroller.getScrollHeight())
			return;
		
		if (savedScrollState.getX() != 0)
			scroller.scrollTo(ScrollDirection.LEFT, savedScrollState.getX());
		if (savedScrollState.getY() != 0)
			scroller.scrollTo(ScrollDirection.TOP, savedScrollState.getY());
		savedScrollState = null;
	}
	
	/**
	 * Disables the tree view resize handling.
	 */
	@Override
	public void disableTreeViewResizeHandling() {
		disableTreeViewResizeHandling = true;
	}
	
	/**
	 * If the number of columns x rows are bigger than MAX_DATA_TO_RENDER, then we render initially only one row, and
	 * then schedule the rest to be rendered in chucks of 100ms (one item added to the store in each run).
	 */
	@Override
	public void limitAmountOfDataToRender() {
		if (allRootItemsForRenderLimit.isEmpty())
			return;
		
		if (allRootItemsForRenderLimit.size() * editorTreeGrid.getColumnModel().getColumnCount() < MAX_DATA_TO_RENDER) {
			Scheduler.get().scheduleDeferred(this::restoreScrollState);
			return;
		}
		
		selectionForRenderLimit.clear();
		selectionForRenderLimit.addAll(editorTreeGrid.getSelectionModel().getSelectedItems());
		
		disableViewportCheck = true;
		editorTreeGrid.getTreeStore().clear();
		
		List<AbstractGenericTreeModel> allItems = new ArrayList<>(allRootItemsForRenderLimit);
		
		AbstractGenericTreeModel firstItem = allItems.remove(0);
		
		AssemblyUtil.addToTreeWithChildren(this, null, firstItem);
		if (!selectionForRenderLimit.isEmpty() && selectionForRenderLimit.remove(firstItem))
			editorTreeGrid.getSelectionModel().select(firstItem, false);
		
		Scheduler.get().scheduleDeferred(() -> {
			editorTreeGrid.getView().layout();
			restoreScrollState();
		});
		
		if (allItems.isEmpty()) {
			disableViewportCheck = false;
			return;
		}
		
		Scheduler.get().scheduleDeferred(() -> renderDataWithContinuation(allItems));
	}
	
	private void renderDataWithContinuation(List<AbstractGenericTreeModel> models) {
		if (cancelContinuationRendering) {
			cancelContinuationRendering = false;
			disableViewportCheck = false;
			
			if (itemsWaitingForRendering != null)
				itemsWaitingForRendering.clear();
			return;
		}
		
		disableViewportCheck = true;
		renderingContinuationData = true;
		
		long start = System.currentTimeMillis();
		
		List<AbstractGenericTreeModel> modelsAdded = new ArrayList<>();
		do {
			AbstractGenericTreeModel model = models.remove(0);
			AssemblyUtil.addToTreeWithChildren(AssemblyPanel.this, null, model);
			modelsAdded.add(model);
		} while (!models.isEmpty() && System.currentTimeMillis() - start < 100);
		
		restoreScrollState();
		
		if (!selectionForRenderLimit.isEmpty()) {
			for (AbstractGenericTreeModel model : modelsAdded) {
				if (selectionForRenderLimit.isEmpty())
					break;
				
				if (selectionForRenderLimit.remove(model))
					editorTreeGrid.getSelectionModel().select(model, true);
			}
		}
		
		if (!models.isEmpty())
			Scheduler.get().scheduleDeferred(() -> renderDataWithContinuation(models));
		else {
			if (itemsWaitingForRendering != null && !itemsWaitingForRendering.isEmpty()) {
				renderDataWithContinuation(new ArrayList<>(itemsWaitingForRendering));
				itemsWaitingForRendering.clear();
				return;
			}
			
			disableViewportCheck = false;
			renderingContinuationData = false;
			editorTreeGrid.fireWindowChanged();
		}
	}
	
	@Override
	protected void onResize(int width, int height) {
		if (renderingContinuationData)
			cancelContinuationRendering = true;
		
		if (!disableTreeViewResizeHandling && !allRootItemsForRenderLimit.isEmpty()) {
			saveScrollState();
			limitAmountOfDataToRender();
		}
		
		disableTreeViewResizeHandling = false;
		
		super.onResize(width, height);
	}
	
	/**
	 * This method refreshes the AP. Those entries that are somehow related to the changed entity.
	 * They may be used within the SI, for example, which would require it to be refreshed.
	 */
	private void checkForRefresh(GenericEntity changedEntity, Set<AbstractGenericTreeModel> modelsAlreadyHandled, Property changedProperty) {
		editorTreeGrid.getTreeStore().getRootItems().stream()
				.filter(m -> !modelsAlreadyHandled.contains(m) && m.getModelObject() instanceof GenericEntity).forEach(rootModel -> {
			GenericEntity entity = (GenericEntity) rootModel.getModelObject();
			EntityType<GenericEntity> entityType = entity.entityType();
			List<Property> properties = entityType.getProperties();
			for (Property property : properties) {
				Object value = property.get(entity);
				if (value != changedEntity)
					continue;
				
				if (changedProperty == null || !(rootModel.getDelegate() instanceof EntityTreeModel)) {
					AssemblyUtil.refreshNode(editorTreeGrid, rootModel);
					break;
				}
				
				EntityTreeModel entityTreeModel = (EntityTreeModel) rootModel.getDelegate();
				TreePropertyModel treePropertyModel = entityTreeModel.getTreePropertyModels().stream()
						.filter(tpm -> tpm.getParentEntity() == changedEntity && tpm.getPropertyName().equals(changedProperty.getName()))
						.findAny().orElse(null);
				
				if (treePropertyModel == null) {
					AssemblyUtil.refreshNode(editorTreeGrid, rootModel);
					break;
				}
				
				Object newValue = changedProperty.get(changedEntity);
				treePropertyModel.setValue(newValue);
				treePropertyModel.setAbsent(newValue instanceof AbsenceInformation);
				treePropertyModel.setLabel(null);
				AssemblyUtil.refreshRow(editorTreeGrid, rootModel);
				break;
			}
		});
	}
	
	private void updateRootModelPath(PropertyManipulation manipulation) {
		if (rootModelPath == null)
			return;
		
		Object newValue = null;
		
		LocalEntityProperty localEntityProperty = (LocalEntityProperty) manipulation.getOwner();
		GenericEntity entity = localEntityProperty.getEntity();
		String propertyName = localEntityProperty.getPropertyName();
		
		if (manipulation instanceof ChangeValueManipulation) {
			if (entity == null)
				newValue = ((ChangeValueManipulation) manipulation).getNewValue();
			else {
				EntityType<GenericEntity> entityType = entity.entityType();
				newValue = entityType.getProperty(propertyName).get(entity);
			}
		} else {
			newValue = ((AbsentingManipulation) manipulation).getAbsenceInformation();
			propertyName = ((AbsentingManipulation) manipulation).getOwner().getPropertyName();
		}
		
		String finalName = propertyName;
		Object finalValue = newValue;
		rootModelPath.stream().filter(e -> e instanceof PropertyPathElement && ((PropertyPathElement) e).getProperty().getName().equals(finalName)
				&& ((PropertyPathElement) e).getEntity() == entity).forEach(e -> e.setValue(finalValue));
	}
	
	private boolean isRootPathRelatedToEntityProperty(LocalEntityProperty owner) {
		if (rootModelPath != null && rootModelPath.last() instanceof PropertyRelatedModelPathElement) {
			PropertyRelatedModelPathElement propertyPathElement = (PropertyRelatedModelPathElement) rootModelPath.last();
			return propertyPathElement.getEntity() == owner.getEntity() && propertyPathElement.getProperty().getName().equals(owner.getPropertyName());
		}
		
		return false;
	}
	
	public void maybeShowContextMenuOnLoadingAbsent() {
		if (!loadedAbsentWithRightClick)
			return;
		
		Scheduler.get().scheduleDeferred(() -> {
			loadedAbsentWithRightClick = false;
			new Timer() {
				@Override
				public void run() {
					updateEmptyMenuItem();
					actionsContextMenu.showAt(loadedAbsentWithRightClickPoint.getX(), loadedAbsentWithRightClickPoint.getY());
				}
			}.schedule(100);
		});
	}
	
	private void addManipulationListener(PropertyRelatedModelPathElement propertyPathElement) {
		GenericEntity parentEntity = propertyPathElement.getEntity();
		//ensureSessionAttached(parentEntity);
		
		if (entitiesWithManipulationListeners.add(parentEntity))
			gmSession.listeners().entity(parentEntity).add(this);

		/*EntityProperty entityProperty = new EntityProperty();
		entityProperty.setPropertyName(propertyPathElement.getProperty().getPropertyName());
		entityProperty.setReference(GMF.getTypeReflection().getEntityType(parentEntity).getReference(parentEntity));
		EntityAndProperty entityAndProperty = new EntityAndProperty(parentEntity, entityProperty.getPropertyName());
		if (!entityPropertiesWithManipulationListeners.contains(entityAndProperty)) {
			gmSession.addHookedManipulationListener(entityProperty, this);
			entityPropertiesWithManipulationListeners.add(entityAndProperty);
		}*/
	}
	
	protected void addManipulationListener(Object object) {
		if (object instanceof GenericEntity) {
			GenericEntity entity = (GenericEntity) object;
			//ensureSessionAttached(entity);
			if (entitiesWithManipulationListeners.add(entity))
				gmSession.listeners().entity(entity).add(this);
		}
	}
	
	@Override
	protected void addManipulationListener(AbstractGenericTreeModel model) {
		AbstractGenericTreeModel delegateModel = model.getDelegate();
		if (delegateModel.getChildCount() > 0)
			delegateModel.getChildren().forEach(child -> addManipulationListener(child));
		
		//Object object = delegateModel.getModelObject();
		//if (object instanceof GenericEntity)
			//ensureSessionAttached((GenericEntity) object);
		
		EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
		if (entityTreeModel != null) {
			GenericEntity entity = entityTreeModel.getModelObject();
			if (entity != null && entitiesWithManipulationListeners.add(entity))
				gmSession.listeners().entity(entity).add(this);
		}
	}
	
	@Override
	protected void removeManipulationListener(AbstractGenericTreeModel model) {
		AbstractGenericTreeModel delegateModel = model.getDelegate();
		if (delegateModel.getChildCount() > 0)
			delegateModel.getChildren().forEach(child -> removeManipulationListener(child));
		
		EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
		if (entityTreeModel != null) {
			GenericEntity entity = entityTreeModel.getModelObject();
			if (entity != null)
				removeManipulationListener(entity);
			
			Collection<TreePropertyModel> propertyModels = entityTreeModel.getTreePropertyModels();
			for (TreePropertyModel propertyModel : propertyModels) {
				if (propertyModel instanceof CompoundTreePropertyModel)
					removeManipulationListener(propertyModel.getParentEntity());
				else {
					Object value = propertyModel.getValue();
					if (value instanceof GenericEntity)
						removeManipulationListener((GenericEntity) value);
				}
			}
		}
	}
	
	@Override
	protected void removeManipulationListener(GenericEntity entity) {
		if (canManipulationListenerBeRemoved(entity)) {
			gmSession.listeners().entity(entity).remove(this);
			entitiesWithManipulationListeners.remove(entity);
		}
		
		/*Set<EntityAndProperty> set = new HashSet<EntityAndProperty>(entityPropertiesWithManipulationListeners);
		for (EntityAndProperty entityAndProperty : set) {
			if (entityAndProperty.getEntity() == entity) {
				EntityProperty entityProperty = new EntityProperty();
				entityProperty.setPropertyName(entityAndProperty.getPropertyName());
				entityProperty.setReference(reference);
				gmSession.removeHookedManipulationListener(entityProperty, this);
				entityPropertiesWithManipulationListeners.remove(entityAndProperty);
			}
		}*/
	}
	
	private boolean canManipulationListenerBeRemoved(GenericEntity entity) {
		if (deletedEntities != null && deletedEntities.containsKey(entity))
			return false;
		
		for (AbstractGenericTreeModel rootModel : editorTreeGrid.getTreeStore().getRootItems()) {
			Set<AbstractGenericTreeModel> parentModels = getParentModels(rootModel, entity);
			if (!parentModels.isEmpty())
				return false;
		}
		
		return rootModelPath == null || !(rootModelPath.last() instanceof PropertyRelatedModelPathElement) ||
				((PropertyRelatedModelPathElement) rootModelPath.last()).getEntity() != null;
	}
	
	@Override
	protected void prepareTreeGrid() {
		editorTreeGrid = new AssemblyPanelTreeGrid(new ColumnConfig<>(new IdentityValueProvider<>()), this);
		editorTreeGrid.getTreeView().setColumnHeader(new ExtendedColumnHeader<>(editorTreeGrid, editorTreeGrid.getColumnModel()));
		editorTreeGrid.setHideHeaders(hideHeaders);
		
		if (useDragAndDrop)
			configureTreeGridDragAndDrop();
		
		configureCopyAndPaste();
		
		if (gmeDragAndDropSupport != null)
			prepareDropTargetWidget(editorTreeGrid, -1);
	}
	
	protected void loadAbsentOrIncompleteModel(final AbstractGenericTreeModel model) {
		if (model == null)
			return;
		
		AbstractGenericTreeModel delegateModel = model.getDelegate();
		if (model instanceof PropertyEntryModelInterface || delegateModel instanceof PropertyEntryModelInterface) {
			PropertyEntryModelInterface propertyEntryModel = AssemblyUtil.isModelAbsent(model);
			if (propertyEntryModel != null) {
				loadAbsentProperty(propertyEntryModel) //
						.andThen(result -> Scheduler.get().scheduleDeferred(() -> ensureAllChildrenAreVisible(model)));
			}
		}
		
		if (AssemblyUtil.isNotComplete(model)) {
			if (delegateModel instanceof CondensedEntityTreeModel && ((CondensedEntityTreeModel) delegateModel).getPropertyDelegate() != null
					&& ((CondensedEntityTreeModel) delegateModel).getPropertyDelegate().getNotCompleted()) {
				AbstractGenericTreeModel collectionModel = ((CondensedEntityTreeModel) delegateModel).getPropertyDelegate();
				AssemblyUtil.updatePropertyEntryTreeModel(this, model, collectionModel, collectionModel.getModelObject(), false, false, false, true);
			} else {
				delegateModel = model instanceof PropertyEntryModelInterface ? ((PropertyEntryModelInterface) model).getPropertyDelegate() : delegateModel;
				Object modelObject = model instanceof CondensedEntityTreeModel ? ((CondensedEntityTreeModel) model).getPropertyDelegate().getModelObject()
						: model.getModelObject();
				AssemblyUtil.updatePropertyEntryTreeModel(this, model, delegateModel, modelObject, false, false, false);
			}
			Scheduler.get().scheduleDeferred(() -> ensureAllChildrenAreVisible(model));
		}
	}
	
	protected boolean isModelCheckable(AbstractGenericTreeModel model) {
		if (!useCheckBoxColumn || typeForCheck == null)
			return false;
		
		boolean modelCheckable = false;
    	AbstractGenericTreeModel delegateModel = model.getDelegate();
    	if (!typeForCheck.isEntity())
    		modelCheckable = typeForCheck.isAssignableFrom(delegateModel.getElementType());
    	else {
    		EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
    		if (entityTreeModel != null) {
		    	EntityType<?> modelEntityType = entityTreeModel.getElementType();
		    	if (delegateModel.getModelObject() != null && (modelEntityType.equals(typeForCheck) || ((EntityType<?>) typeForCheck).isAssignableFrom(modelEntityType)))
		    		modelCheckable = true;
    		}
    	}
    	
	    return modelCheckable;
	}
	
	protected boolean isTopLevelMap() {
		return isTopLevelMap;
	}
	
	protected boolean isAllowExpandNodes() {
		return allowExpandNodes;
	}
	
	private Future<Void> loadAbsentProperty(PropertyEntryModelInterface propertyEntryModel) {
		PropertyEntry propertyEntry = propertyEntryModel.getPropertyEntry();
		Integer maxSize = null;
		boolean loadingRestricted = false;
		if (isSetOfEntity(propertyEntryModel)) {
			maxSize = propertyEntry.getMaxSize();
			loadingRestricted = true;
		}
		if (maxSize != null && maxSize == -1)
			maxSize = null;
		
		List<AbsentModel> absentModels = new ArrayList<>();
		
		EntityTreeModel entityTreeModel = null;
		if (propertyEntryModel instanceof CondensedEntityTreeModel)
			entityTreeModel = ((CondensedEntityTreeModel) propertyEntryModel).getEntityTreeModel();
		else
			entityTreeModel = propertyEntryModel.getPropertyDelegate().getEntityTreeModel();
			
		if (entityTreeModel != null) {
			Collection<TreePropertyModel> treePropertyModels = entityTreeModel.getTreePropertyModels();
			treePropertyModels.stream().filter(m -> m.isAbsent()).forEach(m -> absentModels.add(m));
		}
		
		return loadAbsentProperty(propertyEntry.getEntity(), propertyEntry.getEntityType(), propertyEntry.getPropertyName(), absentModels, propertyEntryModel,
				maxSize, loadingRestricted, true);
	}
	
	private boolean isSetOfEntity(PropertyEntryModelInterface propertyEntryModel) {
		if (propertyEntryModel.getPropertyDelegate() instanceof SetTreeModel
				&& propertyEntryModel.getPropertyDelegate().getElementType().isEntity()) {
			return true;
		}
		
		if (propertyEntryModel instanceof CondensedEntityTreeModel) {
			GenericModelType type = ((CondensedEntityTreeModel) propertyEntryModel).getCollectionProperty().getType();
			if (type.isCollection() && ((CollectionType) type).getCollectionKind().equals(CollectionKind.set))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Loads the given absent property.
	 */
	private Future<Void> loadAbsentProperty(GenericEntity entity, EntityType<GenericEntity> entityType, String propertyName,
			List<AbsentModel> absentModels, PropertyEntryModelInterface propertyEntryModel, Integer maxSize, boolean loadingRestricted,
			boolean showError) {
		Future<Void> future = new Future<>();
		Property property = entityType.getProperty(propertyName);
		PropertyQuery propertyQuery = GMEUtil.getPropertyQuery((PersistentEntityReference) entity.reference(), propertyName,
				maxSize, getSpecialTraversingCriterion(property.getType().getJavaType()),
				loadingRestricted, getMetaData(entity), useCase);
		AbstractGenericTreeModel model = propertyEntryModel instanceof AbstractGenericTreeModel ? (AbstractGenericTreeModel) propertyEntryModel : null;
		Scheduler.get().scheduleDeferred(() -> {
			//The delay is needed because the node is refreshed after selection.
			if (model != null && AssemblyUtil.isModelAbsent(model) != null) {
				markNodeAsLoading(model);
			}
		});
		gmSession.query().property(propertyQuery).result(AsyncCallback.of(propertyQueryResult -> {
			Scheduler.get().scheduleDeferred(() -> {
				absentModels.add(propertyEntryModel.getPropertyEntry());
				
				GmSessionException exception = null;
				try {
					gmSession.suspendHistory();
					absentModels.forEach(m -> m.setAbsent(false));
					if (!GMEUtil.isPropertyAbsent(entity, property))
						return; //Nothing to do, the property was loaded and set somewhere else
					
					PropertyQueryResult result = propertyQueryResult.result();
					
					Object value = result != null ? result.getPropertyValue() : null;
					value = GMEUtil.transformIfSet(value, propertyName, entityType);
					
					if (value instanceof EnhancedCollection)
						((EnhancedCollection) value).setIncomplete(result.getHasMore());
					
					entityType.getProperty(propertyName).set(entity,
							GMEUtil.sortIfSet(value, propertyQuery, gmSession, useCase, codecRegistry));
					addManipulationListener(value);
				} catch (GmSessionException e) {
					exception = e;
				} finally {
					gmSession.resumeHistory();
					if (exception != null)
						handlePropertyQueryError(propertyName, showError, future, exception);
					else
						future.onSuccess(null);
				}
			});
		}, e -> handlePropertyQueryError(propertyName, showError, future, e)));
		
		return future;
	}
	
	private void handlePropertyQueryError(String propertyName, boolean showError, Future<Void> future, Throwable e) {
		e.printStackTrace();
		if (showError)
			ErrorDialog.show(LocalizedText.INSTANCE.errorLoadingAbsentProperty(propertyName), e);
		future.onFailure(e);
	}
	
	private TraversingCriterion getSpecialTraversingCriterion(Class<?> clazz) {
		if (specialEntityTraversingCriterion != null)
			return specialEntityTraversingCriterion.get(clazz);
		
		return null;
	}
	
	private void markNodeAsLoading(AbstractGenericTreeModel model) {
		TreeNode<AbstractGenericTreeModel> node = AssemblyUtil.findNode(editorTreeGrid, model);
		if (node != null)
			editorTreeGrid.getTreeView().onLoading(node);
	}
	
	protected void ensureAllChildrenAreVisible(AbstractGenericTreeModel model) {
		int childCount = model.getChildCount();
		if (childCount > 0) {
			GridView<AbstractGenericTreeModel> view = editorTreeGrid.getView();
			view.ensureVisible(editorTreeGrid.getStore().indexOf(model.getChild(childCount - 1)), 0, false);
			view.ensureVisible(editorTreeGrid.getStore().indexOf(model), 0, false);
		}
	}
	
	private Set<AbstractGenericTreeModel> getParentModels(AbstractGenericTreeModel rootModel, Object object) {
		Set<AbstractGenericTreeModel> parentModels = new HashSet<>();
		boolean rootIncluded = false;
		if (rootModel.refersTo(object)) {
			parentModels.add(rootModel);
			rootIncluded = true;
		}
		
		List<AbstractGenericTreeModel> children = null;
		AbstractGenericTreeModel delegateModel = rootModel.getDelegate();
		if (!(rootModel instanceof CondensedEntityTreeModel) && !(delegateModel instanceof CondensedEntityTreeModel))
			children = rootModel.getChildren();
		else if (!rootIncluded) {
			children = rootModel instanceof CondensedEntityTreeModel ? ((CondensedEntityTreeModel) rootModel).getEntityTreeModel().getChildren()
					: ((CondensedEntityTreeModel) delegateModel).getEntityTreeModel().getChildren();
		}
		
		if (children == null)
			return parentModels;
		
		for (AbstractGenericTreeModel treeModel : children) {
			rootIncluded = false;
			if (treeModel.refersTo(object)) {
				parentModels.add(treeModel);
				rootIncluded = true;
			}
			boolean hasChildren = false;
			if (!(treeModel instanceof CondensedEntityTreeModel) && !(treeModel.getDelegate() instanceof CondensedEntityTreeModel))
				hasChildren = treeModel.getChildCount() > 0;
			else {
				if (rootIncluded)
					hasChildren = false;
				else if (treeModel instanceof CondensedEntityTreeModel)
					hasChildren = ((CondensedEntityTreeModel) treeModel).getEntityTreeModel().getChildCount() > 0;
				else
					hasChildren = ((CondensedEntityTreeModel) treeModel.getDelegate()).getEntityTreeModel().getChildCount() > 0;
			}
				
			if (hasChildren)
				parentModels.addAll(getParentModels(treeModel, object));
		}
		
		return parentModels;
	}
	
	@Override
	protected void prepareDefaultActions() {
		if (useCopyEntityToClipboardAction) {
			copyEntityToClipboardAction = new CopyEntityToClipboardAction();
			copyEntityToClipboardAction.setAssemblyPanel(this);
		}
		
		copyTextAction = new CopyTextAction();
		copyTextAction.setCodecRegistry(codecRegistry);
		copyTextAction.configureGmContentView(this);
	}

	private void configureTreeGridDragAndDrop() {
		treeGridDragSource = new AssemblyPanelTreeGridDragSource(this);
		treeGridDropTarget = new AssemblyPanelTreeGridDropTarget(this);
	}
	
	/**
	 * Configures the Copy and Paste operations for copying references/cloning entities.
	 * Ctrl + C copies to the "clipboard".
	 * Ctrl + V pastes the reference/clone.
	 */
	@SuppressWarnings("unused")
	private void configureCopyAndPaste() {
		new KeyNav(editorTreeGrid) {
			@Override
			public void onKeyPress(NativeEvent evt) {
				if (evt.getCtrlKey() || (GXT.isMac() && evt.getMetaKey())) {
					if (evt.getKeyCode() == KeyCodes.KEY_C)
						addSelectedModelsToClipBoard();
					else if (evt.getKeyCode() == KeyCodes.KEY_V)
						handlePastedOrDroppedModels(currentClipboardModels, editorTreeGrid.getSelectionModel().getSelectedItem());
				} else if (!evt.getShiftKey() && evt.getKeyCode() == KeyCodes.KEY_ENTER
						&& (editorTreeGrid.getView().findRowIndex(Element.as(evt.getEventTarget())) != -1)) { //-1 when editing
					fireClickOrDoubleClick(false,
							new AssemblyPanelMouseInteractionEvent(evt, editorTreeGrid.getSelectionModel().getSelectedItem(), AssemblyPanel.this));
				}
			}
		};
	}
	
	public void addSelectedModelsToClipBoard() {
		List<AbstractGenericTreeModel> selectedModels = editorTreeGrid.getSelectionModel().getSelection();
		if (selectedModels.isEmpty())
			return;
		
		AbstractGenericTreeModel delegateModel = selectedModels.get(0).getDelegate();
		
		if (selectedModels.size() > 1) {
			for (AbstractGenericTreeModel model : selectedModels) {
				if (model.getDelegate().getClass() != delegateModel.getClass()) {
					GlobalState.showWarning(LocalizedText.INSTANCE.onlySameEntityTypeCanBeCopied());
					return;
				}
			}
		}
		
		EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
		if (entityTreeModel == null && !isMapCopiable(delegateModel)) {
			GlobalState.showWarning(LocalizedText.INSTANCE.onlyEntitiesCanBeCopied());
			return;
		}
		
		List<AbstractGenericTreeModel> modelsToClipboard = new ArrayList<>();
		for (AbstractGenericTreeModel model : selectedModels) {
			if (model instanceof PropertyEntryModelInterface) {
				PropertyEntry propertyEntry = ((PropertyEntryModelInterface) model).getPropertyEntry();
				GenericEntity entity = propertyEntry.getEntity();
				ModelMdResolver modelMdResolver = getMetaData(entity).useCase(useCase);
				PropertyMdResolver propertyMdResolver = modelMdResolver.entity(entity).property(propertyEntry.getPropertyName());
				if (!GMEMetadataUtil.isReferenceable(propertyMdResolver, modelMdResolver))
					continue;
			}
			modelsToClipboard.add(model);
		}
		
		configureClipboardModels(modelsToClipboard);
	}
	
	private boolean isMapCopiable(AbstractGenericTreeModel delegateModel) {
		if (delegateModel instanceof MapKeyAndValueTreeModel) {
			//MapKeyAndValueTreeModel keyAndValueModel = (MapKeyAndValueTreeModel) delegateModel;
			//if (keyAndValueModel.getKeyElementType().isEntity() && keyAndValueModel.getElementType().isEntity())
			return true;
		}
		
		return false;
	}
	
	private void configureClipboardModels(List<AbstractGenericTreeModel> models) {
		currentClipboardModels.clear();
		currentClipboardModels.addAll(models);
		fireModelsAddedToClipboard(models);
		if (models.size() == 1) {
			ValueDescriptionBean bean = assemblyUtil.prepareLabel(models.get(0), isTopLevelMap(), showNodeTextAsTooltip, false, false);
			GlobalState.showSuccess(LocalizedText.INSTANCE.entityCopiedClipboard(bean.getValue()));
		} else
			GlobalState.showSuccess(LocalizedText.INSTANCE.entitiesCopiedClipboard(models.size()));
	}
	
	private void handlePastedOrDroppedModels(Collection<AbstractGenericTreeModel> sourceModels, AbstractGenericTreeModel targetModel) {
		if (readOnly || sourceModels.isEmpty())
			return;
		
		AbstractGenericTreeModel firstSourceModel = sourceModels.stream().findFirst().get();
		if (targetModel == null || targetModel == firstSourceModel) {
			handleNewInstance(firstSourceModel);
			return;
		}
		
		AbstractGenericTreeModel targetDelegate = targetModel.getDelegate();
		if (targetDelegate instanceof ListTreeModelInterface || targetDelegate instanceof SetTreeModel || isTargetCondensed(targetDelegate)) {
			if (isTargetCondensed(targetDelegate))
				targetDelegate = ((CondensedEntityTreeModel) targetDelegate).getPropertyDelegate();
			//TODO: need to prepare for the case where I am dragging a list/set and not an entity
			List<ModelPath> modelPaths = new ArrayList<>();
			sourceModels.forEach(m -> modelPaths.add(AssemblyUtil.getModelPath(m, rootModelPath)));
			
			if (targetDelegate.isCollectionTreeModel()) {
				TypeCondition typeCondition = GMEUtil.prepareTypeCondition(((CollectionTreeModel) targetDelegate).<GenericModelType>getElementType());
				for (ModelPath modelPath : modelPaths) {
					if (!typeCondition.matches(modelPath.last().<GenericModelType>getType())) {
						GlobalState.showWarning(LocalizedText.INSTANCE.invalidPaste());
						return;
					}
				}
			}
			
			String dialogTitle = targetDelegate instanceof ListTreeModelInterface ? LocalizedText.INSTANCE.addToList() :
					LocalizedText.INSTANCE.addToSet();
			handleDropOrPaste(modelPaths, dialogTitle, targetModel, targetDelegate);
		} else if (targetDelegate instanceof EntityTreeModel && (!(targetModel instanceof DelegatingTreeModel) ||
				targetModel instanceof PropertyEntryTreeModel)) {
			TypeCondition typeCondition = GMEUtil.prepareTypeCondition(((EntityTreeModel) targetDelegate).getElementType());
			
			boolean performed = false;
			for (AbstractGenericTreeModel model : sourceModels) {
				ModelPath modelPath = AssemblyUtil.getModelPath(model, rootModelPath);
				if (typeCondition.matches(modelPath.last().<GenericModelType>getType())) {
					handleDropOrPaste(Arrays.asList(modelPath), LocalizedText.INSTANCE.changeInstance(), targetModel, targetDelegate);
					performed = true;
					break;
				}
			}
			
			if (!performed)
				GlobalState.showWarning(LocalizedText.INSTANCE.invalidPaste());
		} else if (targetDelegate instanceof MapTreeModel) {
			//TODO: need to prepare for the case where I am dragging a list/set and not an entity
			//Map<GMTypeInstanceBean, GMTypeInstanceBean> sourceInstanceBeans = new LinkedHashMap<GMTypeInstanceBean, GMTypeInstanceBean>();
			
			List<ModelPath> modelPaths = new ArrayList<ModelPath>();
			for (AbstractGenericTreeModel sourceModel : sourceModels) {
				modelPaths.add(AssemblyUtil.getModelPath(sourceModel, rootModelPath));
				/*if (sourceModel instanceof MapEntryTreeModel) {
					MapEntryTreeModel mapEntryModel = (MapEntryTreeModel)sourceModel;
					MapEntry mapEntry = mapEntryModel.getMapEntry();
					if (mapEntry.getRepresentsKey()) {
						sourceInstanceBeans.put(new GMTypeInstanceBean(mapEntry.getKeyElementType(), mapEntry.getKey()), 
						new GMTypeInstanceBean(sourceModel.getElementType(), sourceModel.getModelObject()));
					} else {
						sourceInstanceBeans.put(new GMTypeInstanceBean(sourceModel.getElementType(), sourceModel.getModelObject()),
						new GMTypeInstanceBean(mapEntry.getKeyElementType(), mapEntry.getKey()));
					}
				} else if (sourceModel instanceof MapKeyAndValueTreeModel) {
					MapKeyAndValueTreeModel mapEntryModel = (MapKeyAndValueTreeModel)sourceModel;
					sourceInstanceBeans.put(new GMTypeInstanceBean(mapEntryModel.getMapKeyEntryTreeModel().getElementType(),
							mapEntryModel.getMapKeyEntryTreeModel().getModelObject()), new GMTypeInstanceBean(
							mapEntryModel.getMapValueEntryTreeModel().getElementType(), mapEntryModel.getMapValueEntryTreeModel().getModelObject()));
				}*/
			}
			
			/*List<GMTypeInstanceBean> gmTypeInstanceBeans = new ArrayList<GMTypeInstanceBean>();
			for (Map.Entry<GMTypeInstanceBean, GMTypeInstanceBean> entry : sourceInstanceBeans.entrySet()) {
				gmTypeInstanceBeans.add(entry.getKey());
			}*/
			handleDropOrPaste(modelPaths, LocalizedText.INSTANCE.addToMap(), targetModel, targetDelegate);
		} else {
			handleNewInstance(firstSourceModel);
		}
	}
	
	private boolean isTargetCondensed(AbstractGenericTreeModel delegateModel) {
		return delegateModel instanceof CondensedEntityTreeModel
				&& (((CondensedEntityTreeModel) delegateModel).getPropertyDelegate() instanceof ListTreeModelInterface
						|| ((CondensedEntityTreeModel) delegateModel).getPropertyDelegate() instanceof SetTreeModel);
	}
	
	private void handleDropOrPaste(List<ModelPath> modelPaths, String dialogTitle, AbstractGenericTreeModel targetModel,
			AbstractGenericTreeModel targetDelegate) {
		NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
		
		if (modelPaths.get(0).last() instanceof MapValuePathElement) {
			MapValuePathElement mapElement = (MapValuePathElement) modelPaths.get(0).last();
			if (!mapElement.getKeyType().isEntity() && !mapElement.getMapValueType().isEntity()) {
				handleNewInstances(Collections.emptyList(), modelPaths, nestedTransaction, targetModel, targetDelegate);
				return;
			}
		}
		
		InstanceTypeSelectorDialog dialog = InstanceTypeSelectorDialog.INSTANCE;
		dialog.setHeading(dialogTitle);
		dialog.apply(new InstanceTypeSelectorDialogParameters(modelPaths, gmSession, useCase))
				.andThen(newInstances -> handleNewInstances(newInstances, modelPaths, nestedTransaction, targetModel, targetDelegate)) //
				.onError(e -> {
					ErrorDialog.show(LocalizedText.INSTANCE.errorPreparingNewInstance(), e);
					e.printStackTrace();
					try {
						nestedTransaction.rollback();
					} catch (TransactionException t) {
						ErrorDialog.show(LocalizedText.INSTANCE.errorRollingEditionBack(), t);
						t.printStackTrace();
					}
				});
	}
	
	private void handleNewInstances(List<GMTypeInstanceBean> newInstances, List<ModelPath> modelPaths, NestedTransaction nestedTransaction,
			AbstractGenericTreeModel targetModel, AbstractGenericTreeModel targetDelegate) {
		if (newInstances == null)
			return;
		
		EntityTreeModel entityTreeModel = targetDelegate.getEntityTreeModel();
		if (entityTreeModel != null) {
			handleDropOrPasteEntityTreeModel(newInstances, targetModel, targetDelegate);
		} else if (targetDelegate instanceof ListTreeModelInterface || targetDelegate instanceof SetTreeModel) {
			GMEUtil.insertToListOrSet(AssemblyUtil.prepareCollectionPathElement((CollectionTreeModel) targetDelegate, rootModelPath), newInstances,
					-1);
		} else if (targetDelegate instanceof MapTreeModel) {
			handleDropOrPasteMap(newInstances, modelPaths, AssemblyUtil.prepareCollectionPathElement((CollectionTreeModel) targetDelegate, rootModelPath));
		}
		nestedTransaction.commit();
	}
	
	private void handleDropOrPasteEntityTreeModel(List<GMTypeInstanceBean> newInstances, AbstractGenericTreeModel targetModel,
			AbstractGenericTreeModel targetDelegate) {
		final EntityTreeModel parentEntityTreeModel;
		final AbstractGenericTreeModel delegateModel;
		EntityTreeModel entityTreeModel = targetModel.getEntityTreeModel();
		if (entityTreeModel != null) {
			parentEntityTreeModel = entityTreeModel;
			delegateModel = parentEntityTreeModel.getDelegate();
		} else {
			parentEntityTreeModel = AssemblyUtil.getParentEntityTreeModel(targetModel);
			delegateModel = targetDelegate;
		}
		
		String propertyName = AssemblyUtil.getPropertyName(delegateModel, rootModelPath);
		if (propertyName == null)
			propertyName = parentEntityTreeModel.getPropertyName();
		
		GMEUtil.changeEntityPropertyValue((GenericEntity) parentEntityTreeModel.getModelObject(),
				parentEntityTreeModel.getElementType().getProperty(propertyName), newInstances.get(0).getInstance());
	}
	
	/**
	 * Handles the drop or paste operations for maps.
	 */
	private void handleDropOrPasteMap(List<GMTypeInstanceBean> newInstances, List<ModelPath> modelPaths, PropertyRelatedModelPathElement collectionElement) {
		MapType mapType = collectionElement.getType();
		
		List<KeyAndValueGMTypeInstanceBean> keysAndValues = new ArrayList<>();
		int lastIndex = 0;
		for (ModelPath modelPath : modelPaths) {
			MapValuePathElement mapEntry = (MapValuePathElement) modelPath.last();
			
			GMTypeInstanceBean mapKey;
			if (mapType.getKeyType().isEntity()) {
				mapKey = newInstances.get(lastIndex);
				lastIndex++;
			} else
				mapKey = new GMTypeInstanceBean(mapEntry.getKeyType(), mapEntry.getKey());
			
			GMTypeInstanceBean mapValue;
			if (mapType.getValueType().isEntity()) {
				mapValue = newInstances.get(lastIndex);
				lastIndex++;
			} else
				mapValue = new GMTypeInstanceBean(mapEntry.getMapValueType(), mapEntry.getMapValue());
			
			keysAndValues.add(new KeyAndValueGMTypeInstanceBean(mapKey, mapValue));
		}
		
		GMEUtil.insertOrRemoveToCollection(collectionElement, keysAndValues, true);
	}
	
	public void expandEntries() {
		if (editorTreeGrid.isViewReady()) {
			if (autoExpand != null) {
				try {
					autoExpandLevel = Integer.parseInt(autoExpand.getDepth());
				} catch (NumberFormatException ex) {
					autoExpandLevel = Integer.MAX_VALUE;
				}
			}
			
			List<AbstractGenericTreeModel> models = editorTreeGrid.getTreeStore().getRootItems();
			for (int i = 0; i < autoExpandLevel; i++) {
				List<AbstractGenericTreeModel> childModels = new ArrayList<>();
				for (AbstractGenericTreeModel model : models) {
					editorTreeGrid.setExpanded(model, true);
					if (model.getChildren() != null)
						childModels.addAll(model.getChildren());
				}
				models = childModels;
			}
		} else {
			viewReadyRegistration = editorTreeGrid.addViewReadyHandler(event -> {
				if (viewReadyRegistration != null)
					viewReadyRegistration.removeHandler();
				expandEntries();
			});
		}
	}
	
	private void getExpandedEntries(List<AbstractGenericTreeModel> models) {
		if (models == null)
			return;
		
		models.stream().filter(m -> editorTreeGrid.isExpanded(m)).forEach(model -> {
			currentExpandedItems.add(model.getModelObject());
			getExpandedEntries(model.getChildren());
		});
	}
	
	private void expandCurrentExpandedItems(List<AbstractGenericTreeModel> models) {
		if (currentExpandedItems.isEmpty() || models == null || models.isEmpty())
			return;
		
		for (AbstractGenericTreeModel model : models) {
			if (currentExpandedItems.remove(model.getModelObject())) {
				editorTreeGrid.setExpanded(model, true);
				expandCurrentExpandedItems(model.getChildren());
			}
			
			if (currentExpandedItems.isEmpty())
				return;
		}
	}
	
	private void selectCurrentSelectedItems(List<AbstractGenericTreeModel> models) {
		if (currentSelectedItems.isEmpty() || models == null || models.isEmpty())
			return;
		
		boolean singleEntry = currentSelectedItems.size() == 1;
		AbstractGenericTreeModel modelToFocus = null;
		
		for (AbstractGenericTreeModel model : models) {
			if (currentSelectedItems.remove(model.getModelObject())) {
				editorTreeGrid.getSelectionModel().select(model, true);
				modelToFocus = model;
			}
			
			if (editorTreeGrid.isExpanded(model))
				selectCurrentSelectedItems(model.getChildren());
			
			if (currentSelectedItems.isEmpty())
				break;
		}
		
		if (singleEntry && modelToFocus != null) {
			//Do not focus the grid in case we have some active window
			if (WindowManager.get().getActive() == null) {
				AbstractGenericTreeModel model = modelToFocus;
				Scheduler.get().scheduleDeferred(() -> {
					editorTreeGrid.getTreeView().focusRow(editorTreeGrid.getStore().indexOf(model));
				});
			}
		}
	}
	
	@Override
	protected void exchangeCenterWidget(Widget widget) {
		if (currentWidget == widget)
			return;
		
		boolean doLayout = false;
		if (currentWidget != null) {
			this.remove(currentWidget);
			doLayout = true;
		}
		currentWidget = widget;
		this.setCenterWidget(widget);
		if (doLayout) {
			this.doLayout();
			Scheduler.get().scheduleDeferred(() -> {
				if (autoExpandLevel != 0 || autoExpand != null)
					expandEntries();
			});
		}
	}
	
	@Override
	protected HTML getEmptyPanel() {
		if (emptyPanel == null) {
			emptyPanel = new HTML(getEmptyPanelHtml());
			
			if (gmeDragAndDropSupport != null)
				prepareDropTargetWidget(emptyPanel, -1);
		} else if (contentSet)
			updateEmptyPanel();
		
		return emptyPanel;
	}
	
	private String getEmptyPanelHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
		html.append("<div style='display: table-cell; vertical-align: middle'>").append(contentSet ? emptyTextMessage : "").append("</div></div>");
		
		return html.toString();
	}
	
	private void updateEmptyPanel() {
		emptyPanel.setHTML(getEmptyPanelHtml());
	}
	
	protected Menu getHelperMenu() {
		if (helperMenu == null) {
			helperMenu = new Menu();
			
			setNullItem = new MenuItem(LocalizedText.INSTANCE.setNull(), AssemblyPanelResources.INSTANCE.nullAction());
			setNullItem.setToolTip(LocalizedText.INSTANCE.setPropertyToNull());
			helperMenu.add(setNullItem);
			
			clearStringItem = new MenuItem(LocalizedText.INSTANCE.clearText(), AssemblyPanelResources.INSTANCE.clearString());
			clearStringItem.setToolTip(LocalizedText.INSTANCE.clearTextDescription());
			helperMenu.add(clearStringItem);
			
			if (navigationEnabled) {
				workWithItem = new MenuItem(LocalizedText.INSTANCE.workWith(), AssemblyPanelResources.INSTANCE.open());
				workWithItem.setToolTip(LocalizedText.INSTANCE.workWithDescription());
				helperMenu.add(workWithItem);
			}
			
			changeInstanceItem = new MenuItem(LocalizedText.INSTANCE.change(), AssemblyPanelResources.INSTANCE.changeExisting());
			
			changeInstanceAction = new ChangeInstanceAction();
			changeInstanceAction.setInstanceSelectionFutureProvider(selectionFutureProviderProvider);
			changeInstanceAction.configureGmContentView(this);
			
			helperMenu.add(changeInstanceItem);
			
			if (copyTextAction != null) {
				MenuItem copyTextItem = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem(copyTextAction, copyTextItem);
				helperMenu.add(copyTextItem);
			}
			
			SelectionHandler<Item> selectionHandler = event -> {
				GenericEntity parentEntity = helperMenuPropertyModel.getParentEntity();
				if (event.getSelectedItem() == setNullItem) {
					parentEntity.entityType().getProperty(helperMenuPropertyModel.getPropertyName()).set(parentEntity, null);
				} else if (event.getSelectedItem() == clearStringItem) {
					parentEntity.entityType().getProperty(helperMenuPropertyModel.getPropertyName()).set(parentEntity, "");
				} else if (event.getSelectedItem() == workWithItem) {
					ModelAction action = actionManager.getWorkWithEntityAction(this);
					if (action != null) {
						action.updateState(Collections.singletonList(Collections.singletonList(getPropertyModelPath(helperMenuPropertyModel))));
						action.perform(null);
					}
				} else if (event.getSelectedItem() == changeInstanceItem)
					changeInstanceAction.perform(null);
				/* else if (ce.getItem() == gimaDialogOpenerItem) {
					try {
						TriggerInfo triggerInfo = new TriggerInfo();
						ObjectAndType objectAndType = new ObjectAndType();
						objectAndType.setObject(helperMenuPropertyModel.getValue());
						objectAndType.setType(helperMenuPropertyModel.getValueElementType());
						EntityTreeModel entityTreeModel = new EntityTreeModel(objectAndType, genericModelEditorPanel.getModelFactory());
						triggerInfo.put(GIMADialogOpenerAction.ENTITY_TRIGGER, entityTreeModel);
						genericModelEditorPanel.getGimaDialogOpenerAction().perform(triggerInfo);
					} catch (Exception ex) {
						logger.error("Error while creating entityTreeModel.", ex);
						ex.printStackTrace();
					}
				}*/
			};
			
			setNullItem.addSelectionHandler(selectionHandler);
			clearStringItem.addSelectionHandler(selectionHandler);
			if (workWithItem != null)
				workWithItem.addSelectionHandler(selectionHandler);
			changeInstanceItem.addSelectionHandler(selectionHandler);
		}
		
		boolean allowEmptyString = false;
		EntityMdResolver entityContext = getMetaData(helperMenuPropertyModel.getParentEntity()).entity(helperMenuPropertyModel.getParentEntity()).useCase(useCase);
		PropertyMdResolver propertyMetaDataContextBuilder = entityContext.property(helperMenuPropertyModel.getPropertyName());
		
		MinLength minLength = propertyMetaDataContextBuilder.meta(MinLength.T).exclusive();
		if (minLength != null)
			allowEmptyString = minLength.getLength() == 0L;
		
		boolean mandatory = propertyMetaDataContextBuilder.is(Mandatory.T);
		
		Object propertyValue = helperMenuPropertyModel.getValue();
		GenericModelType propertyElementType = helperMenuPropertyModel.getElementType();
		setNullItem.setEnabled(propertyValue != null && !mandatory && helperMenuPropertyModel.isNullable() && !helperMenuPropertyModel.isReadOnly()
				&& LocalizedString.class != propertyElementType.getJavaType());
		clearStringItem.setVisible(allowEmptyString && !helperMenuPropertyModel.isReadOnly() && propertyElementType.getJavaType() == String.class);
		
		if (workWithItem != null) {
			workWithItem.setVisible((propertyValue != null && propertyElementType.isEntity() && LocalizedString.class != propertyElementType.getJavaType())
					|| propertyElementType.isCollection());
		}
		
		List<List<ModelPath>> propertyModelPaths = transformSelection(Collections.singletonList(getPropertyModelPath(helperMenuPropertyModel)));
		
		if (propertyElementType.isEntity() && LocalizedString.class != propertyElementType.getJavaType())
			changeInstanceAction.updateState(propertyModelPaths);
		else
			changeInstanceAction.setHidden(true);
		changeInstanceItem.setVisible(!changeInstanceAction.getHidden());
		
		if (propertyTriggerFieldActionMap != null) {
			String normalizedPropertyName = helperMenuPropertyModel.getNormalizedPropertyName();
			TriggerFieldAction triggerFieldAction = propertyTriggerFieldActionMap.get(normalizedPropertyName);
			if (triggerFieldAction != null && !helperMenuPropertyModel.isReadOnly()) {
				if (triggerFieldActionItemMap == null || !triggerFieldActionItemMap.containsKey(triggerFieldAction)) {
					MenuItem menuItem = new MenuItem();
					MenuItemActionAdapter.linkActionToMenuItem(triggerFieldAction.getTriggerFieldAction(), menuItem);
					if (triggerFieldActionItemMap == null)
						triggerFieldActionItemMap = new HashMap<>();
					triggerFieldActionItemMap.put(triggerFieldAction, menuItem);
					helperMenu.add(menuItem);
					
				}
				triggerFieldAction.setGridInfo(editorTreeGrid.gridEditing, helperMenuGridCell);
				
				triggerFieldAction.getTriggerFieldAction().setHidden(false);
				changeInstanceItem.setVisible(false);
			}
			
			propertyTriggerFieldActionMap.entrySet().stream().filter(e -> !normalizedPropertyName.equals(e.getKey()))
					.forEach(entry -> entry.getValue().getTriggerFieldAction().setHidden(true));
		}
		
		if (copyTextAction != null)
			copyTextAction.updateState(propertyModelPaths);
		
		return helperMenu;
	}
	
	protected ModelPath getPropertyModelPath(TreePropertyModel propertyModel)  {
		if (propertyModel == null)
			return null;
		
		ModelPath modelPath = new ModelPath();
		GenericEntity parentEntity = propertyModel.getParentEntity();
		EntityType<?> parentEntityType = parentEntity.entityType();
		RootPathElement rootPathElement = new RootPathElement(parentEntityType, parentEntity);
		modelPath.add(rootPathElement);
		
		PropertyPathElement propertyPathElement = new PropertyPathElement(parentEntity, parentEntityType.getProperty(propertyModel.getPropertyName()),
				propertyModel.getValue());
		modelPath.add(propertyPathElement);
		return modelPath;
	}
	
	@Override
	protected void updateEmptyMenuItem() {
		boolean emptyMenu = true;
		for (int i = 0; i < actionsContextMenu.getWidgetCount(); i++) {
			Widget menuItem = actionsContextMenu.getWidget(i);
			if (menuItem != emptyMenuItem && menuItem instanceof Component && !isComponentHidden((Component) menuItem)) {
				emptyMenu = false;
				break;
			}
		}
		
		emptyMenuItem.setVisible(emptyMenu);
	}
	
	@Override
	protected ManipulationListener getRootEnhancedSetManipulationListener() {
		if (rootEnhancedSetManipulationListener != null)
			return rootEnhancedSetManipulationListener;
		
		rootEnhancedSetManipulationListener = manipulation -> {
			if (rootModelPath.last() instanceof PropertyRelatedModelPathElement) {
				GenericEntity parentEntity = ((PropertyRelatedModelPathElement) rootModelPath.last()).getEntity();
				Property property = ((PropertyRelatedModelPathElement) rootModelPath.last()).getProperty();
				rootEnhancedSet.removeManipulationListener(rootEnhancedSetManipulationListener);
				switch (manipulation.manipulationType()) {
				case CLEAR_COLLECTION:
					((Set<?>) property.get(parentEntity)).clear();
					break;
				case ADD:
					Map<Object, Object> itemsToAdd = ((AddManipulation) manipulation).getItemsToAdd();
					((Set<Object>) property.get(parentEntity)).addAll(itemsToAdd.values());
					break;
				case REMOVE:
					Map<Object, Object> itemsToRemove = ((RemoveManipulation) manipulation).getItemsToRemove();
					((Set<Object>) property.get(parentEntity)).removeAll(itemsToRemove.values());
					break;
				default:
					break;
				}
				rootEnhancedSet.addManipulationListener(rootEnhancedSetManipulationListener);
			}
		};
		
		return rootEnhancedSetManipulationListener;
	}
	
	/**
	 * Uncondenses the given entity.
	 * @param expandOthers - true to expand all current expanded entries.
	 * @param expandMe - true to expand after uncondensing.
	 * @return the uncondensed model, which takes place of the condensed one.
	 */
	@Override
	protected AbstractGenericTreeModel uncondenseSingleEntity(AbstractGenericTreeModel model, boolean expandOthers, boolean expandMe) {
		if (!(model instanceof CondensedEntityTreeModel) && !(model.getDelegate() instanceof CondensedEntityTreeModel))
			return null;
		
		AbstractGenericTreeModel uncondensedModel = null;
		int indexInListStore = editorTreeGrid.getStore().indexOf(model);
		List<AbstractGenericTreeModel> expandedModels = null;
		Point scrollState = null;
		if (expandOthers) {
			expandedModels = getExpandedModels();
			scrollState = editorTreeGrid.getView().getScrollState();
		}
		
		CondensedEntityTreeModel condensedModel = model instanceof CondensedEntityTreeModel ? (CondensedEntityTreeModel) model
				: (CondensedEntityTreeModel) model.getDelegate();
		TreeStore<AbstractGenericTreeModel> treeStore = editorTreeGrid.getTreeStore();
		EntityTreeModel entityTreeModel = condensedModel.getEntityTreeModel();
		if (model instanceof DelegatingTreeModel) {
			AbstractGenericTreeModel parentInStore = treeStore.getParent(model);
			int index = treeStore.indexOf(model);
			treeStore.remove(model);
			
			entityTreeModel.restoreNotCompleteDueToCondensation();
			((DelegatingTreeModel) model).setDelegate(entityTreeModel);
			if (entityTreeModel.getPropertyName() == null)
				entityTreeModel.setProperty(condensedModel.getDefaultProperty());
			if (condensedModel.getPropertyDelegate() != null)
				condensedModel.getPropertyDelegate().setParent(entityTreeModel);
			
			entityTreeModel.setParent(parentInStore);
			
			AssemblyUtil.insertToTreeStore(this, parentInStore, model, index, false, true, false);
			uncondensedModel = model;
		} else {
			AbstractGenericTreeModel parentModel = model.getParent();
			if (parentModel != null) {
				int index = parentModel.getChildren().indexOf(model);
				parentModel.remove(index);
				parentModel.insert(entityTreeModel, index);
				AbstractGenericTreeModel parentInStore = treeStore.getParent(model);
				treeStore.remove(model);
				AssemblyUtil.insertToTreeStore(this, parentInStore, entityTreeModel, index, false, false, false);
			} else {
				int index = treeStore.getRootItems().indexOf(model);
				treeStore.remove(model);
				AssemblyUtil.insertToTreeStore(this, null, entityTreeModel, index, false, true, false);
			}
			if (condensedModel.getPropertyDelegate() != null)
				condensedModel.getPropertyDelegate().setParent(entityTreeModel);
			uncondensedModel = entityTreeModel;
		}
		
		if (expandOthers)
			expandedModels.stream().filter(m -> m != model).forEach(m -> editorTreeGrid.setExpanded(m, true));
		if (expandMe)
			editorTreeGrid.setExpanded(editorTreeGrid.getStore().get(indexInListStore), true);
		if (expandOthers) {
			XElement scroller = editorTreeGrid.getView().getScroller();
			scroller.setScrollLeft(scrollState.getX());
			scroller.setScrollTop(scrollState.getY());
		}
		
		return uncondensedModel;
	}
	
	private List<AbstractGenericTreeModel> getExpandedModels() {
		List<AbstractGenericTreeModel> expandedModels = new ArrayList<>();
		editorTreeGrid.getStore().getAll().stream().filter(m -> editorTreeGrid.isExpanded(m)).forEach(m -> expandedModels.add(m));
		return expandedModels;
	}
	
	/**
	 * Condenses a single entity.
	 */
	@Override
	protected AbstractGenericTreeModel condenseSingleEntity(AbstractGenericTreeModel model, String collectionPropertyName) {
		EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
		if (entityTreeModel == null)
			return null;
		
		AbstractGenericTreeModel replacementModel = null;
		CondensedEntityTreeModel condensedModel = new CondensedEntityTreeModel(model instanceof PropertyEntryTreeModel ? (PropertyEntryTreeModel) model : null,
				entityTreeModel, entityTreeModel.getElementType().getProperty(collectionPropertyName), useCase, 0);
		
		TreeStore<AbstractGenericTreeModel> treeStore = editorTreeGrid.getTreeStore();
		if (model instanceof DelegatingTreeModel) {
			AbstractGenericTreeModel parentInStore = treeStore.getParent(model);
			int index = treeStore.indexOf(model);
			treeStore.remove(model);
			
			((DelegatingTreeModel) model).setDelegate(condensedModel);
			condensedModel.setParent(parentInStore);
			
			AssemblyUtil.insertToTreeStore(this, parentInStore, model, index, false, true, false);
			replacementModel = model;
		} else {
			AbstractGenericTreeModel parentModel = model.getParent();
			if (parentModel != null) {
				int index = parentModel.getChildren().indexOf(model);
				parentModel.remove(index);
				parentModel.insert(condensedModel, index);
				AbstractGenericTreeModel parentInStore = treeStore.getParent(model);
				treeStore.remove(model);
				AssemblyUtil.insertToTreeStore(this, parentInStore, condensedModel, index, false, false, false);
			} else {
				int index = treeStore.getRootItems().indexOf(model);
				treeStore.remove(model);
				AssemblyUtil.insertToTreeStore(this, null, condensedModel, index, false, true, false);
			}
			replacementModel = condensedModel;
		}
		
		return replacementModel;
	}
	
	@Override
	protected void condenseEntity(EntityType<?> entityType, String collectionPropertyName, CondensationMode condensationMode) {
		List<AbstractGenericTreeModel> expandedModels = getExpandedModels();
		//TODO: The section bellow was commented out previously... check why
		if (!expandedModels.isEmpty()) {
			List<AbstractGenericTreeModel> children = editorTreeGrid.getTreeStore().getAllChildren(editorTreeGrid.getSelectionModel().getSelectedItem());
			if (children != null)
				children.forEach(child -> expandedModels.remove(child));
		}
		Point scrollState = editorTreeGrid.getView().getScrollState();
		
		Map<AbstractGenericTreeModel, AbstractGenericTreeModel> modelMap = new HashMap<>();
		//int count = editorTreeGrid.getTreeStore().getRootCount();
		if (collectionPropertyName != null) {
			Property property = entityType.getProperty(collectionPropertyName);
			if (property.getType().isCollection()) {
				modelFactory.markEntityTypeAsCondensed(entityType, collectionPropertyName, condensationMode);
				editorTreeGrid.getTreeStore().getRootItems().forEach(m -> modelMap.putAll(condenseEntity(m, entityType, collectionPropertyName)));
			}
		} else {
			modelFactory.unmarkEntityTypeAsCondensed(entityType);
			editorTreeGrid.getTreeStore().getRootItems().forEach(m -> modelMap.putAll(uncondenseEntity(m, entityType)));
		}
		
		for (AbstractGenericTreeModel expandedModel : expandedModels) {
			AbstractGenericTreeModel modelToExpand = modelMap.get(expandedModel);
			if (modelToExpand == null)
				modelToExpand = expandedModel;
			if (modelToExpand != null && editorTreeGrid.getTreeStore().findModel(modelToExpand) != null)
				editorTreeGrid.setExpanded(modelToExpand, true);
		}
		
		AbstractGenericTreeModel modelToSelect = modelMap.get(editorTreeGrid.getSelectionModel().getSelectedItem());
		if (modelToSelect != null) {
			expandUntilModel(modelToSelect);
			editorTreeGrid.getSelectionModel().select(modelToSelect, false);
		}
		
		XElement scroller = editorTreeGrid.getView().getScroller();
		scroller.setScrollLeft(scrollState.getX());
		scroller.setScrollTop(scrollState.getY());
	}
	
	private Map<AbstractGenericTreeModel, AbstractGenericTreeModel> condenseEntity(AbstractGenericTreeModel model, EntityType<?> entityType,
			String collectionPropertyName) {
		Map<AbstractGenericTreeModel, AbstractGenericTreeModel> modelsMap = new HashMap<>();
		int counter = model.getChildCount();
		for (int i = 0; i < counter; i++)
			modelsMap.putAll(condenseEntity(model.getChild(i), entityType, collectionPropertyName));
		
		EntityTreeModel entityTreeModel = model.getDelegate().getEntityTreeModel();
		if (entityTreeModel != null) {
			if (entityType.isAssignableFrom(entityTreeModel.getElementType())) {
				AbstractGenericTreeModel condensedModel = condenseSingleEntity(model, collectionPropertyName);
				modelsMap.put(model, condensedModel);
			}
		}
		
		return modelsMap;
	}
	
	private Map<AbstractGenericTreeModel, AbstractGenericTreeModel> uncondenseEntity(AbstractGenericTreeModel model, EntityType<?> entityType) {
		Map<AbstractGenericTreeModel, AbstractGenericTreeModel> modelsMap = new HashMap<>();
		/*List<AbstractGenericTreeModel> expandedModels = getExpandedModels(treeGrid);
		Point scrollState = treeGrid.getView().getScrollState();*/
		
		int counter = model.getChildCount();
		for (int i = 0; i < counter; i++)
			modelsMap.putAll(uncondenseEntity(model.getChild(i), entityType));
		
		EntityType<?> modelEntityType = null;
		EntityTreeModel entityTreeModel = model.getEntityTreeModel();
		if (entityTreeModel != null)
			modelEntityType = entityTreeModel.getElementType();
		else if (model.getElementType().isEntity())
			modelEntityType = (EntityType<?>) model.getElementType();
		
		if (modelEntityType == entityType) {
			AbstractGenericTreeModel uncondensedModel = uncondenseSingleEntity(model, false, false);
			modelsMap.put(model, uncondensedModel);
		}
		
		/*for (AbstractGenericTreeModel expandedModel : expandedModels) {
			treeGrid.setExpanded(expandedModel, true);
		}
		treeGrid.setExpanded(model, true);
		treeGrid.getView().getScroller().setScrollLeft(scrollState.x);
		treeGrid.getView().getScroller().setScrollTop(scrollState.y);*/
		
		return modelsMap;
	}
	
	private void expandUntilModel(AbstractGenericTreeModel model) {
		AbstractGenericTreeModel parentModel = editorTreeGrid.getTreeStore().getParent(model);
		if (parentModel != null) {
			if (!editorTreeGrid.isExpanded(parentModel)) {
				expandUntilModel(parentModel);
				editorTreeGrid.setExpanded(parentModel, true);
			}
		}
	}
	
	private boolean isEntitySimplified(GenericEntity entity) {
		if (simplifiedEntityTypes != null && entity != null) {
			return simplifiedEntityTypes.contains(entity.entityType().getJavaType());
		}
		
		return false;
	}
	
	private void handleNewInstance(AbstractGenericTreeModel model) {
		final NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
		ModelPath modelPath = AssemblyUtil.getModelPath(model, rootModelPath);
		
		if (modelPath.last().getValue() instanceof GenericEntity) {
			GenericEntity entity = modelPath.last().getValue();
			if (!getMetaData(entity).entity(entity).useCase(useCase).is(Instantiable.T)) {
				GlobalState.showWarning(LocalizedText.INSTANCE.pastedEntityNotInstantiable());
				return;
			}
		}
		
		GMTypeInstanceBean clone = InstanceTypeSelectorDialog.INSTANCE
				.getClone(new InstanceTypeSelectorDialogParameters(Collections.singletonList(modelPath), gmSession, useCase));
		
		nestedTransaction.commit();
		InstantiatedEntityListener instantiatedEntityListener = GMEUtil.getInstantiatedEntityListener(this);
		if (instantiatedEntityListener != null) {
			modelPath.add(new RootPathElement(clone.getGenericModelType(), clone.getInstance()));
			instantiatedEntityListener.onEntityInstantiated(new RootPathElement(clone.getGenericModelType(), clone.getInstance()), true, true,
					LocalizedText.INSTANCE.copying());
		}
	}
	
	/***************************** GmeDragAndDropView ***********************************/
	protected GmeDragAndDropSupport gmeDragAndDropSupport;
	private WorkbenchActionContext<TemplateBasedAction> workbenchActionContext;
	private Set<AbstractGenericTreeModel> waitingDropListenerModels;
	private HandlerRegistration ddViewReadyRegistration;
	
	/**
	 * This is required when we want to support file drag and drop on this instance of the ap.
	 */
	@Configurable
	public void setGmeDragAndDropSupport(GmeDragAndDropSupport gmeDragAndDropSupport) {
		this.gmeDragAndDropSupport = gmeDragAndDropSupport;
	}
	
	@Override
	public int getMaxAmountOfFilesToUpload() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public void handleDropFileList(FileList fileList) {
		if (gmeDragAndDropSupport != null)
			gmeDragAndDropSupport.handleDropFileList(fileList, this);
	}
	
	protected void addModelToWaitingDropListener(AbstractGenericTreeModel model) {
		if (waitingDropListenerModels == null)
			waitingDropListenerModels = new HashSet<>();
		
		waitingDropListenerModels.add(model);
		
		if (ddViewReadyRegistration == null) {
			ddViewReadyRegistration = editorTreeGrid.addViewReadyHandler(event -> {
				if (ddViewReadyRegistration != null) {
					ddViewReadyRegistration.removeHandler();
					ddViewReadyRegistration = null;
				}
				
				if (waitingDropListenerModels == null)
					return;
				
				waitingDropListenerModels.forEach(m -> AssemblyUtil.addDragAndDropListenerToElementModel(m, AssemblyPanel.this));
			});
		}
	}
	
	@Override
	public WorkbenchActionContext<TemplateBasedAction> getDragAndDropWorkbenchActionContext() {
		if (workbenchActionContext != null)
			return workbenchActionContext;
		
		workbenchActionContext = prepareWorkbenchActionContext();
		return workbenchActionContext;
	}
	
}
