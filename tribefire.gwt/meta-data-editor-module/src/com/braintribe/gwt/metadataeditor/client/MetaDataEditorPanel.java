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
package com.braintribe.gwt.metadataeditor.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmResetableActionsContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.HasAddtionalWidgets;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.TabbedWidgetContext;
import com.braintribe.gwt.gmview.metadata.client.MetaDataEditorPanelHandler;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.orangeflattab.client.OrangeFlatTabPanelAppearance;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.metadataeditor.client.action.MetaDataEditorHistory;
import com.braintribe.gwt.metadataeditor.client.listeners.ChangeListeners;
import com.braintribe.gwt.metadataeditor.client.listeners.SelectionListeners;
import com.braintribe.gwt.metadataeditor.client.resources.MetaDataEditorResources;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorEntityInfoProvider;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorFilterType;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorProvider;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorResolutionView;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataEditorSearchDialog;
import com.braintribe.gwt.metadataeditor.client.view.MetaDataResolverProvider;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.info.GmCustomTypeInfo;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.PlainTabPanel;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.ShowContextMenuEvent;
import com.sencha.gxt.widget.core.client.event.ShowContextMenuEvent.ShowContextMenuHandler;
import com.sencha.gxt.widget.core.client.event.ShowEvent;
import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
import com.sencha.gxt.widget.core.client.menu.Menu;

public class MetaDataEditorPanel extends BorderLayoutContainer implements MetaDataEditorPanelHandler, GmCheckSupport, InitializableBean,
		DisposableBean, HasAddtionalWidgets, GmResetableActionsContentView, GmViewActionProvider, GmActionSupport {

	private static final void log(String message) {
		GWT.log("calling MetaDataEditorPanel." + message);
	}

	private static int infoPanelHeight = 55;
	private static int filterPanelHeight = 85;	
	
	private GmContentViewActionManager actionManager;
	private List<Pair<ActionTypeAndName, ModelAction>> externalActions;
	private PlainTabPanel tabPanel;
	//private Widget actionBar;
	private MetaDataEditorResolutionView resolutionView;
	private Widget filterView = null;
	private Widget entityInfoView = null;
	private PersistenceGmSession gmSession;
	private String useCase;
	private HandlerRegistration attachHandlerRegistration;
	private List<Supplier<MetaDataEditorProvider>> tabsProvider;
	private List<GmCheckListener> gmCheckListeners;
	private GmCheckListener filterCheckListener = null;
    private Set<String> lastUseCaseFilter;
    private Set<String> lastRolesFilter;
    private Set<String> lastAccessFilter;
    private ModelPath lastModelPath;
	private Menu actionsContextMenu;
	private boolean showContextMenu = true;
	private ActionProviderConfiguration actionProviderConfiguration;
	//private SelectionConstellation selectionConstellation;
	//private ModelMetaDataContextBuilder modelMetaDataContextBuilder = null;
	//private ModelMetaDataContextBuilder sessionModelMetaDataContextBuilder = null;
	//private CascadingMetaDataResolver metaDataResolver = null;
	private Boolean useSessionResolver = false;
	private Boolean useFilterForResolver = true;
	private MetaDataResolverProvider metaDataResolverProvider;
	private CodecRegistry<String> codecRegistry;
	private Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion;
	
	private final SelectionListeners gmSelectionListeners = new SelectionListeners(this);
	private final ChangeListeners gmChangeListeners = new ChangeListeners(this);
	private boolean filterExternalActions = true;
	private final Set<GenericEntity> entitiesLoading = new HashSet<GenericEntity>();
	private HTML htmlSeachPanel;
	private MetaDataEditorSearchDialog searchDialog = null;
	private Boolean isSearchMode = false;
	private List<TabbedWidgetContext> tabbedWidgetContext = null;
	private MetaDataEditorHistory history = null; 
	private MetaDataEditorMaster master = null;
	private boolean firstLoad = true;
	
	//private StringBuilder logMessage = new StringBuilder();
	public MetaDataEditorPanel() {
		setBorders(false);
		//forceLayout();
		doLayout();
	}

	// ----- InitializableBean Members ----- //

	@Override
	public void intializeBean() throws Exception {
		if (this.master != null)
			this.master.addPanel(this);
		
		this.tabPanel = new PlainTabPanel(GWT.<OrangeFlatTabPanelAppearance> create(OrangeFlatTabPanelAppearance.class));
		this.tabPanel.setTabScroll(true);
		this.tabPanel.setBorders(false);
		this.tabPanel.setBodyBorder(false);
		this.tabPanel.addSelectionHandler(this.gmSelectionListeners);
		setCenterWidget(this.tabPanel);
		BorderLayoutData southData = new BorderLayoutData(60);
		southData.setMargins(new Margins(4, 0, 0, 0));
		
		this.tabPanel.addShowHandler(new ShowHandler() {			
			@Override
			public void onShow(ShowEvent event) {
				if (tabPanel.getActiveWidget() instanceof MetaDataEditorProvider) 
					history.add(getContentPath() , tabPanel.getActiveWidget(), ((MetaDataEditorProvider) tabPanel.getActiveWidget()).getCaption());
			}
		});
		
		//setSouthWidget(actionBar, southData);
		if (this.tabsProvider != null) {
			for (Supplier<MetaDataEditorProvider> provider : this.tabsProvider) {
				MetaDataEditorProvider view = provider.get();
				view.setMetaDataEditorPanel(this);
				view.setMetaDataResolverProvider(this.metaDataResolverProvider);
				view.addRowClickHandler(this.gmSelectionListeners);
				//tabPanel.add(view.asWidget(), view.getCaption());
				this.tabPanel.add(view.asWidget(), new TabItemConfig(view.getCaption(), false));
			}
			
		}
				
		this.tabPanel.addSelectionHandler(new SelectionHandler<Widget>() {
			@Override
			public void onSelection(SelectionEvent<Widget> event) {	
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {				
					@Override
					public void execute() {
						if (MetaDataEditorPanel.this.filterView != null)
							if (MetaDataEditorPanel.this.filterView instanceof MetaDataEditorFilter)
								((MetaDataEditorFilter) MetaDataEditorPanel.this.filterView).forceLayout();
						
						if (event.getSelectedItem() instanceof MetaDataEditorProvider) {
							history.add(getContentPath() , event.getSelectedItem(), ((MetaDataEditorProvider) event.getSelectedItem()).getCaption());
							//((MetaDataEditorProvider) event.getSelectedItem()).doRefresh();
						}						
						MetaDataEditorPanel.this.gmSelectionListeners.fireListeners();
						updateSearchUI();
					}
				});			
			}
		});
				
		if (this.filterView != null || this.entityInfoView != null) {
			//BorderLayoutContainer source = (BorderLayoutContainer) event.getSource();
			if (getNorthWidget() == null) {
				if (this.filterView != null && this.entityInfoView != null) {
					VerticalLayoutContainer verticalLayout = new VerticalLayoutContainer();
					verticalLayout.add(this.entityInfoView, new VerticalLayoutData(-1, infoPanelHeight));
					verticalLayout.add(this.filterView, new VerticalLayoutData(-1, filterPanelHeight));
					setNorthWidget(verticalLayout, new BorderLayoutData(infoPanelHeight + filterPanelHeight));
					
				} else if (this.entityInfoView == null) {
					setNorthWidget(this.filterView);
				} else {
					setNorthWidget(this.entityInfoView);
				}
			}
		}
		
		if (this.actionManager != null) {
			this.actionManager.connect(this);
			setActions(this.actionManager);
		}
		/*
		if (effectiveActionManager != null) {
			effectiveActionManager.connect(this);
			setActions(effectiveActionManager);
		}
		*/
			
		if (this.showContextMenu) {
			addShowContextMenuHandler(new ShowContextMenuHandler() {
				@Override
				public void onShowContextMenu(ShowContextMenuEvent event) { //checking items visibility each time the context menu is shown
						//actionsContextMenu.showAt(event.getMenu().getAbsoluteLeft(), event.getMenu().getAbsoluteTop());
						event.setCancelled(true);
						return;
				}
			});
		}
		
		this.filterCheckListener = new GmCheckListener() {
			@Override
			public void onCheckChanged(GmCheckSupport gmSelectionSupport) {
				if ((MetaDataEditorPanel.this.filterView != null)) {
					if (MetaDataEditorPanel.this.filterView instanceof MetaDataEditorFilter) {
						//MetaDataEditorPanel.this.useSessionResolver = ((MetaDataEditorFilter) MetaDataEditorPanel.this.filterView).getUseSessionResolver();
						MetaDataEditorPanel.this.useSessionResolver = false;
						doLoad(MetaDataEditorPanel.this.lastModelPath);
					}
				}
			}
		};
		this.gmChangeListeners.add(this.filterCheckListener);
		
		StringBuilder htmlString = new StringBuilder();
		//htmlString.append("<a id='anchor").append("' class='' style='background-image: url('" + MetaDataEditorResources.INSTANCE.search24() +"')'></a>"); //"searchIcon"
		ImageResource icon = MetaDataEditorResources.INSTANCE.search24();
		if (icon != null) {
			String iconUrl = icon.getSafeUri().asString();
			htmlString.append("<a id='metaDataSearchIcon").append("' class='" + MetaDataEditorResources.INSTANCE.constellationCss().searchHorizontalIcon() + "' style=\"background-image: url('" + iconUrl + "');\">"); //"searchIcon"
			htmlString.append("<a id='metaDataSearchClear").append("' class='" + MetaDataEditorResources.INSTANCE.constellationCss().searchClear() + "'>").append(LocalizedText.INSTANCE.clear()).append("</a>"); //"clearText"
		}

		this.htmlSeachPanel = new HTML();
		this.htmlSeachPanel.setHTML(htmlString.toString());
		this.htmlSeachPanel.setStyleName("MetaDataSearch");
				 
		this.setEastWidget(this.htmlSeachPanel, new BorderLayoutData(32));
		 
		this.htmlSeachPanel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EventTarget target = event.getNativeEvent().getEventTarget();
				if (Element.is(target)) {
					Element targetElement = Element.as(target);
					String idName = targetElement.getId();
					if (idName.contains("metaDataSearchIcon"))  {
						searchDialog = getSearchDialog();						
						searchDialog.show();
						searchDialog.alignTo(targetElement, new AnchorAlignment(Anchor.TOP_RIGHT, Anchor.BOTTOM_RIGHT), 0, 3); 
						Scheduler.get().scheduleDeferred(searchDialog::focusTextField);
					} 
					if (idName.contains("metaDataSearchClear")) {
						searchDialog = getSearchDialog();
						searchDialog.setSearchText("");
						searchDialog.setDeclaredOnly(false);
						doSearch("", false);
					} 
				}				
			}
		});
		
	}

	private void doSearch(String searchText, Boolean declaredTypesOnly) {
		doMaskExplorer(LocalizedText.INSTANCE.searching());
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {				
			@Override
			public void execute() {
				try {
					getActiveEditor().applySearchFilter(searchText, declaredTypesOnly);
				} finally {
					doUnmaskExplorer();
					updateSearchUI();
				}				
			}
		});			
	}

	private void updateSearchUI() {
		isSearchMode = getActiveEditor().isSearchMode();
		Element element = Document.get().getElementById("metaDataSearchClear");
		if (isSearchMode) {
			element.getStyle().setDisplay(Display.BLOCK);
		} else {
			element.getStyle().setDisplay(Display.NONE);
		}
	}	
	
	private MetaDataEditorSearchDialog getSearchDialog() {
		if (this.searchDialog != null) {
			this.searchDialog.setShowCheckBox(getActiveEditor().getSearchDeclaredText() != null);
			return this.searchDialog;
		}
		
		this.searchDialog = new MetaDataEditorSearchDialog(getActiveEditor().getSearchDeclaredText() != null, getActiveEditor().getSearchDeclaredText());
		this.searchDialog.addHideHandler(new HideHandler() {							
			@Override
			public void onHide(HideEvent event) {
				if (searchDialog.isValueSet() && searchDialog.getSearchText() != null) {
					doSearch(searchDialog.getSearchText(), searchDialog.getDeclaredOnly());
				}								
			}

		});	
		this.searchDialog.addHandler(event -> {
			doSearch(searchDialog.getSearchText(), searchDialog.getDeclaredOnly());		
		}, ValueChangeEvent.getType());		
		
		return this.searchDialog;
	}
	
	public void setMetaDataEditorHistory(MetaDataEditorHistory history) {
		this.history  = history;
	}
	
	public void setMetaDataEditorMaster(MetaDataEditorMaster master) {
		this.master = master;
	}
	
	// ----- DisposableBean Members ---- //

	@Override
	public void disposeBean() throws Exception {
		if (this.actionManager != null)
			this.actionManager.notifyDisposedView(this);
		
		if (this.master != null)
			this.master.removePanel(this);
	}

	// ----- GmContentView.GmSelectionSupport Members ----- //

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		this.gmSelectionListeners.add(sl);
		for (Widget widget : this.tabPanel.getContainer())
			if (widget instanceof GmSelectionSupport)
				((GmSelectionSupport) widget).addSelectionListener(sl);
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		this.gmSelectionListeners.remove(sl);
		for (Widget widget : this.tabPanel.getContainer())
			if (widget instanceof GmSelectionSupport)
				((GmSelectionSupport) widget).removeSelectionListener(sl);
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		ModelPath modelPath = getActiveEditor().getFirstSelectedItem();
		
		if (this.resolutionView != null) {
			ModelPath extendedModelPath = getActiveEditor().getExtendedSelectedItem();
			this.resolutionView.setContent(extendedModelPath);
		}
		
		if (modelPath == null && !getActiveEditor().isSelectionActive()) {
			return this.lastModelPath;
		}
		
		return modelPath;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		//have effect on External Actions
		
		Boolean updateContextBuilder = false;
        if (this.filterView != null)
			if (this.filterView instanceof MetaDataEditorFilter) {
				((MetaDataEditorFilter) this.filterView).refresh();
				
				//RVE - list of useCases, Roles, Acces can be chnged on another tab, so than we need refresh content in view
			    Set<String> useCaseFilter = ((MetaDataEditorFilter) this.filterView).getFilterList(MetaDataEditorFilterType.UseCase);
			    Set<String> rolesFilter = ((MetaDataEditorFilter) this.filterView).getFilterList(MetaDataEditorFilterType.Role);
			    Set<String> accessFilter = ((MetaDataEditorFilter) this.filterView).getFilterList(MetaDataEditorFilterType.Access);
								
				Boolean useCaseChanged = ((this.lastUseCaseFilter == null) || (this.lastUseCaseFilter.size() != useCaseFilter.size()) || (!this.lastUseCaseFilter.containsAll(useCaseFilter)));
				Boolean rolesChanged = ((this.lastRolesFilter == null) || (this.lastRolesFilter.size() != rolesFilter.size()) || (!this.lastRolesFilter.containsAll(rolesFilter)));
				Boolean accessChanged = ((this.lastAccessFilter == null) || (this.lastAccessFilter.size() != accessFilter.size()) || (!this.lastAccessFilter.containsAll(accessFilter)));
				
				updateContextBuilder = useCaseChanged || rolesChanged || accessChanged;
				if (firstLoad)   //RVE - do not allow setContent if was not been already inicialized
					updateContextBuilder = false;
			
			    if (updateContextBuilder) {
					this.lastUseCaseFilter = useCaseFilter;
					this.lastRolesFilter = rolesFilter;
					this.lastAccessFilter = accessFilter;			
			    	updateModelMetaDataContextBuilder(this.lastModelPath);
			    	getActiveEditor().setContent(this.lastModelPath);
			    }				
			}
        	 
        ModelPath modelPath;
		if (getActiveEditor() != null  && !getActiveEditor().isActionManipulationAllowed()) {
			//Effective tabs
			List<ModelPath> listModelpath2 = null;
			return listModelpath2;
		}
			 

		List<ModelPath> listModelpath = new ArrayList<ModelPath>();		
		modelPath = getActiveEditor().getFirstSelectedItem();
		if (modelPath != null) 
			listModelpath.add(modelPath);
		
		if (listModelpath.isEmpty()) {
			return null;
		}
		
		return listModelpath;
	}

	@Override
	public boolean isSelected(Object element) {
		log("isSelected");
		return getActiveEditor().isSelected(element);
	}

	@Override
	public void select(int index, boolean keepExisting) {
		log("select");
		// NOP
	}

	@Override
	public GmContentView getView() {
		log("getView");
		
		//resetActions();
		return this;
	}

	@Override
	public void resetActions() {
		/*
		if (getActiveEditor() != null && !getActiveEditor().isActionManipulationAllowed()) {
			setActions(this.effectiveActionManager);
		} else {
			setActions(this.actionManager);
		}
		*/
		setActions(this.actionManager);
	}

	@SuppressWarnings("rawtypes")
	private void setActions(GmContentViewActionManager viewActionManager) {
		if (viewActionManager == null || !this.showContextMenu) 
			return;
		
		//viewActionManager.resetActions(this);		
		Widget actionMenu = viewActionManager.getActionMenu(this, (List) GMEUtil.prepareExternalMenuItems(this.externalActions), this.filterExternalActions);
		if (actionMenu instanceof Menu) {
			this.actionsContextMenu = (Menu) actionMenu;
			for (Widget widget : this.tabPanel.getContainer())
				if (widget instanceof MetaDataEditorProvider)
					((MetaDataEditorProvider) widget).setContextMenu(this.actionsContextMenu);
		}
	}
	
	/**
	 * Configures whether to show the context menu.
	 * Defaults to true.
	 */
	@Configurable
	public void setShowContextMenu(boolean showContextMenu) {
		this.showContextMenu = showContextMenu;
	}
	
	/**
	 * Configures whether to filter the external actions based on the actions defined in the root folder.
	 * Defaults to true.
	 */
	@Configurable
	public void setFilterExternalActions(boolean filterExternalActions) {
		this.filterExternalActions = filterExternalActions;
	}
	
	// ----- GmContentView.GmCheckSupport ----- //

	@Override
	public void addCheckListener(GmCheckListener cl) {
		if (cl == null) 
			return;
		
		if (this.gmCheckListeners == null)
			this.gmCheckListeners = new ArrayList<GmCheckListener>();
		this.gmCheckListeners.add(cl);
	}

	@Override
	public void removeCheckListener(GmCheckListener cl) {
		if (this.gmCheckListeners == null) 
			return;

		this.gmCheckListeners.remove(cl);
		if (this.gmCheckListeners.isEmpty())
			this.gmCheckListeners = null;
	}

	@Override
	public ModelPath getFirstCheckedItem() {
		log("getFirstCheckedItem");
		return null;
	}

	@Override
	public List<ModelPath> getCurrentCheckedItems() {
		log("getCurrentCheckedItems");
		return null;
	}

	@Override
	public boolean isChecked(Object element) {
		log("isChecked");
		return false;
	}

	@Override
	public boolean uncheckAll() {
		log("uncheckAll");
		return false;
	}

	// ----- GmContentView.GmSessionHandler Members ---- //

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		/*
		if (this.resolutionView != null) {
			this.resolutionView.configureGmSession(gmSession);
		}
		if (this.entityInfoView != null) {
			if (this.widget instanceof MetaDataEditorEntityInfoProvider)
			  ((MetaDataEditorEntityInfoProvider) this.entityInfoView).configureGmSession(gmSession);
		}
		*/
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return this.gmSession;
	}

	// ----- GmContentView.UseCaseHandler Members ---- //

	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
		for (Widget widget : this.tabPanel.getContainer())
			if (widget instanceof MetaDataEditorProvider)
				((MetaDataEditorProvider) widget).configureUseCase(useCase);
		if (this.resolutionView != null) 
			this.resolutionView.configureUseCase(useCase);
		if (this.entityInfoView == null) 
			return;
		
		if (this.widget instanceof MetaDataEditorEntityInfoProvider)
		  ((MetaDataEditorEntityInfoProvider) this.entityInfoView).configureUseCase(useCase);
	}

	@Override
	public String getUseCase() {
		return this.useCase;
	}

	// ----- GmContentView Members ---- //

	@Override
	public ModelPath getContentPath() {
		log("getContentPath");
		//have effect on View Action bar
		return this.lastModelPath;
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		/*List<Pair<String, TextButton>> buttonsList = new ArrayList<>();
		List<Pair<String, TextButton>> externalButtons = GMEUtil.prepareExternalActionButtons(externalActions);
		if (externalButtons != null)
			buttonsList.addAll(externalButtons);
		actionBar = actionManager.getActionMenu(this, buttonsList);*/
	}

	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		this.externalActions = externalActions;		
		if (externalActions == null)
			return;
		
		if (this.actionProviderConfiguration != null)
			this.actionProviderConfiguration.addExternalActions(externalActions);
		//if (actionsContextMenu != null && actionManager != null) //Already initialized
		if (this.actionManager != null) //Already initialized
			this.actionManager.addExternalActions(this, externalActions);				
	}

	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return this.externalActions;
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
	public void setContent(final ModelPath modelPath) {
		if (modelPath == null) 
			return;
		
		firstLoad = true;		
		
		doLoad(modelPath);			
	}

	private void doLoad(final ModelPath modelPath) {
		doMaskExplorer(LocalizedText.INSTANCE.loading());
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {				
			@Override
			public void execute() {
				Boolean unmask = true;
				try {
					unmask = loaderExecute(modelPath);
				} catch (Exception e) {
					unmask = true;
					ErrorDialog.show("ModelPathEditor failed to load data.", e);
				} finally {
					if (unmask)
						doUnmaskExplorer();
				}
			}
		});
	}

	public void doRefresh() {
		doLoad(this.lastModelPath);
	}
	
	// ----- HasAddtionalWidgets Members -----

	
	@Override
	public void configureAdditionalWidgets(List<TabbedWidgetContext> additionalWidgets) {
		log("configureAdditionalWidgets");
		// NOP
	}

	@Override
	public List<TabbedWidgetContext> getTabbedWidgetContexts() {
		if (this.tabbedWidgetContext == null) {
			this.tabbedWidgetContext = new ArrayList<TabbedWidgetContext>();
			TabbedWidgetContext typesContext = new TabbedWidgetContext(null, LocalizedText.INSTANCE.resolutionView(), () -> this.resolutionView);
			tabbedWidgetContext.add(typesContext);
		}
		
     	return this.tabbedWidgetContext;		
	}
	
	// ----- Public Members ----- //

	public void setResolutionView(MetaDataEditorResolutionView resolutionView) {
		this.resolutionView = resolutionView;
		if (this.resolutionView != null)
			this.resolutionView.setMetaDataResolverProvider(this.metaDataResolverProvider);
	}

	public void setFilterView(IsWidget filterView) {
		this.filterView = filterView.asWidget();
		this.filterView.addHandler(this.gmChangeListeners, ChangeEvent.getType());
	}

	public void setEntityInfoView(IsWidget entityInfoView) {
		this.entityInfoView = entityInfoView.asWidget();
	}
	
	public void setTabs(List<Supplier<MetaDataEditorProvider>> tabsProvider) {
		this.tabsProvider = tabsProvider;
	}
	
	/**
	 * Configures the {@link CodecRegistry} used as renderers.
	 */
	@Configurable
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	/**
	 * Configures a map containing special traversing criterion for the given entities.
	 * This is used when loading an absent property. Special entities (such as {@link LocalizedString}) require some properties to be loaded.
	 */
	@Configurable
	public void setSpecialEntityTraversingCriterion(Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion) {
		this.specialEntityTraversingCriterion = specialEntityTraversingCriterion;
	}		
	
	
	// ----- Internal Members ----- //

	@Override
	protected void onAfterFirstAttach() {
		super.onAfterFirstAttach();
		this.attachHandlerRegistration = getParent().addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (!event.isAttached() || MetaDataEditorPanel.this.attachHandlerRegistration == null)
					return;				
				
				try {
					/*
					if (event.getSource() instanceof BorderLayoutContainer && (filterView != null || entityInfoView != null)) {
						BorderLayoutContainer source = (BorderLayoutContainer) event.getSource();
						if (source.getNorthWidget() == null) {
							if (filterView != null && entityInfoView != null) {
								VerticalLayoutContainer verticalLayout = new VerticalLayoutContainer();
								verticalLayout.add(entityInfoView, new VerticalLayoutData(-1, 60));
								verticalLayout.add(filterView, new VerticalLayoutData(-1, 100));
								//verticalLayout.add(filterView, new VerticalLayoutData(-1, 100));
								//source.setNorthWidget(verticalLayout);
								source.setNorthWidget(verticalLayout, new BorderLayoutData(160));
								
							} else if (entityInfoView == null) {
								source.setNorthWidget(filterView);
							} else {
								source.setNorthWidget(entityInfoView);
							}
						}
					}
					*/
					/*  RVE - disabled ResolutionView as a DefaultTab (on Gregor demand)
					if (MetaDataEditorPanel.this.resolutionView != null && MetaDataEditorPanel.this.resolutionView.getParent() instanceof CardLayoutContainer) {
						TabPanel tabs = (TabPanel) MetaDataEditorPanel.this.resolutionView.getParent().getParent();
						tabs.setActiveWidget(MetaDataEditorPanel.this.resolutionView);
					}
					*/
					resetActions();
					MetaDataEditorPanel.this.attachHandlerRegistration.removeHandler();
				} finally {
					MetaDataEditorPanel.this.attachHandlerRegistration = null;
				}
			}
		});
	}

	protected void fireCheckListeners(GmCheckSupport gmSelectionSupport) {
		if (this.gmCheckListeners != null)
			for (GmCheckListener cl : this.gmCheckListeners)
				cl.onCheckChanged(gmSelectionSupport);
	}
	
	public boolean loaderExecute(ModelPath modelPath) {
		this.lastModelPath = modelPath;
		if (this.filterView == null) 
			return updateModelMetaDataContextBuilder(modelPath);
		
		if (!((MetaDataEditorFilter) this.filterView).isLoadingReady()) {
			new Timer() {
				@Override
				public void run() {
					loaderExecute(MetaDataEditorPanel.this.lastModelPath);
				}
			}.schedule(100);
			doMaskExplorer(LocalizedText.INSTANCE.loading());
			return false;
		}

		((MetaDataEditorFilter) this.filterView).refresh();
		this.lastUseCaseFilter = ((MetaDataEditorFilter) this.filterView).getFilterList(MetaDataEditorFilterType.UseCase);
		this.lastRolesFilter = ((MetaDataEditorFilter) this.filterView).getFilterList(MetaDataEditorFilterType.Role);
		this.lastAccessFilter = ((MetaDataEditorFilter) this.filterView).getFilterList(MetaDataEditorFilterType.Access);			
		
		return updateModelMetaDataContextBuilder(modelPath);
	}

	private void prepareCotent(ModelPath modelPath) {		
		//Disable Overview Tab Visibility
		List<Widget> listRemove = new ArrayList<Widget>();
		for (Widget widget : this.tabPanel.getContainer()) 
			if (widget instanceof MetaDataEditorProvider && (!((MetaDataEditorProvider) widget).getEditorVisible((modelPath != null) ? modelPath.last() : null))) 
				listRemove.add(widget);
		for (Widget widget : listRemove) 
			this.tabPanel.remove(widget);
				
		//Fill Tabs with MetaData
		for (Widget widget : this.tabPanel.getContainer()) {
			if (widget instanceof MetaDataEditorProvider) {
				//((MetaDataEditorProvider) widget).setContextBuilder(this.getModelMetaDataContextBuilder(this.useSessionResolver));
				((MetaDataEditorProvider) widget).setNeedUpdate();
				((MetaDataEditorProvider) widget).setContent(modelPath);
				//set Caption
				TabItemConfig tabItemConfig = this.tabPanel.getConfig(widget);
				if (tabItemConfig != null && (tabItemConfig.getText() == null || !tabItemConfig.getText().equals(((MetaDataEditorProvider) widget).getCaption())))
					tabItemConfig.setText(((MetaDataEditorProvider) widget).getCaption());
				this.tabPanel.update(widget, tabItemConfig);
			}
		}
		// Show Entity Info
		if (this.entityInfoView != null ) {
			if (this.entityInfoView instanceof MetaDataEditorEntityInfoProvider)
				((MetaDataEditorEntityInfoProvider) this.entityInfoView).setContent(modelPath);
		}
		this.gmSelectionListeners.fireListeners();
		
		//RVE - need 1st History entry
		if (firstLoad) {
			firstLoad = false;
			if (this.tabPanel.getActiveWidget() instanceof MetaDataEditorProvider) 
				history.add(getContentPath() , this.tabPanel.getActiveWidget(), ((MetaDataEditorProvider) this.tabPanel.getActiveWidget()).getCaption());
		}		
	}

	public void setFilter(Set<String> set) {
		for (Widget widget : this.tabPanel.getContainer())
			if (widget instanceof MetaDataEditorProvider)
				((MetaDataEditorProvider) widget).setFilter(set);
	}
	
    public void setMetaDataResolverProvider(MetaDataResolverProvider metaDataResolverProvider) {
    	this.metaDataResolverProvider = metaDataResolverProvider;
    }
	
    public MetaDataEditorProvider getEditorProvider() {
    	return getActiveEditor();
    }
    
	// ----- Internal Members ----- //

	protected MetaDataEditorProvider getActiveEditor() {
		Widget widget = this.tabPanel.getActiveWidget();
		//if (widget instanceof MetaDataEditorProvider)
		//	metaDataResolverProvider.setUseSessionResolver(((MetaDataEditorProvider) widget).getUseSessionResolver());
		assert widget instanceof MetaDataEditorProvider : "invalid active widget";
		return (MetaDataEditorProvider) widget;
	}

	// ----- Internal Classes ----- //

	/*
	private void doUpdateLogMessage(String message, Boolean show) {
		
		long delayTime = System.currentTimeMillis() - lasttime;
		logMessage.append("\n").append(" Time ").append(message).append(" : ").append(delayTime);
		lasttime = System.currentTimeMillis(); 
		
		
		//if (show)
		//	ErrorDialog.showMessage(logMessage.toString());
			
		
	} */
	
	private void doUnmaskExplorer() {
		GlobalState.unmask();
	}
	
	private void doMaskExplorer(String message) {
		GlobalState.mask(message);
	}
	
	/*
	protected class SimpleLoader implements Scheduler.RepeatingCommand {

		private final ModelPath modelPath;

		public SimpleLoader(ModelPath modelPath) {
			this.modelPath = modelPath;
		}

		@Override
		public boolean execute() {
			Boolean unmask = true;
			try {
				unmask = loaderExecute(modelPath);
			} catch (Exception e) {
				unmask = true;
				ErrorDialog.show("ModelPathEditor failed to load data.", e);
			} finally {
				if (unmask)
					doUnmaskExplorer();
			}
			return false; // one shot
		}
	}
	*/
	
	protected class LoaderObject {
       private GenericEntity entity;
       private EntityType<GenericEntity> entityType;
       private List<Property> absentProperties;
	}

	private Future<Void> loadAbsentProperties(List<LoaderObject> absentObjectProperties) {
		final Future<Void> future = new Future<>();
		
		MultiLoader multiLoader = new MultiLoader();
		multiLoader.setParallel(false);
		int i = 0;
		for (LoaderObject object : absentObjectProperties) {
			for (Property property : object.absentProperties) {
				multiLoader.add(Integer.toString(i++), GMEUtil.loadAbsentProperty(object.entity, object.entityType, property, gmSession, useCase,
						codecRegistry, specialEntityTraversingCriterion));
			}
			//entitiesLoading.add(entity);
		}
		
		multiLoader.load().andThen(result -> future.onSuccess(null)).onError(future::onFailure);
		return future;
	}
	
	
	@Override
	public ActionProviderConfiguration getActions() {
		if (!(this.actionProviderConfiguration == null)) 
			return this.actionProviderConfiguration;

		this.actionProviderConfiguration = new ActionProviderConfiguration();
		this.actionProviderConfiguration.setGmContentView(this);
		
		List<Pair<ActionTypeAndName, ModelAction>> knownActions = null;
		if (this.actionManager != null)
			knownActions = this.actionManager.getKnownActionsList(this);
		if (knownActions != null || this.externalActions != null) {
			List<Pair<ActionTypeAndName, ModelAction>> allActions = new ArrayList<>();
			if (knownActions != null)
				allActions.addAll(knownActions);
			if (this.externalActions != null)
				allActions.addAll(this.externalActions);
			
			this.actionProviderConfiguration.addExternalActions(allActions);
		}		
		return this.actionProviderConfiguration;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return false;
	}

	private Boolean updateModelMetaDataContextBuilder( final ModelPath modelPath) {
		ModelPathElement modelPathElement = (modelPath != null) ? modelPath.last() : null;
		if (modelPathElement == null || !(modelPathElement.getValue() instanceof GenericEntity))
			return true;
		
		//load all properties		
		GenericEntity source = (GenericEntity) modelPathElement.getValue();				
		List<LoaderObject> listLoaderObject = new ArrayList<MetaDataEditorPanel.LoaderObject>();
		List<Property> completeAbsentPropertiesList = getPropertiesForEntity(source);
		
		if (!completeAbsentPropertiesList.isEmpty()) {
			EntityType<GenericEntity> entityType = source.entityType();
			LoaderObject loaderObject = new LoaderObject();
			loaderObject.absentProperties = completeAbsentPropertiesList;
			loaderObject.entity = source;
			loaderObject.entityType = entityType;
			listLoaderObject.add(loaderObject);
		}
		
		Boolean unmask = true;
		if (!listLoaderObject.isEmpty()) {
			unmask = false;
			loadAbsentProperties(listLoaderObject).andThen(result -> getGmModelProperties(modelPath)).onError(e -> {
				//entitiesLoading.remove(md);
			    doUnmaskExplorer();
				ErrorDialog.show("Error load MetaData Editor Properties", e);
				e.printStackTrace();
			});
		} else {
			unmask = false;
			getGmModelProperties(modelPath);
		}
		return unmask;
	}

	private static List<Property> getPropertiesForEntity(GenericEntity source) {
		List<Property> absentProperties = new ArrayList<Property>();
		//TODO - create diff classes for panel
		for (Property prop : source.entityType().getProperties()) {
				String propertyName = prop.getName();
				if (source instanceof GmEntityType) {
					if ((!propertyName.equals("superTypes")) && (!propertyName.equals("properties")) && (!propertyName.equals("metaData")) && (!propertyName.equals("declaringModel")) && (!propertyName.equals("propertyMetaData")))  
						break;
				} else if (source instanceof GmProperty) {
					if ((!propertyName.equals("declaringType")) && (!propertyName.equals("metaData"))) 
						break;
				} else if (source instanceof GmEnumType) {
					if ((!propertyName.equals("metaData")) && (!propertyName.equals("declaringModel"))) 
						break;
				} else if (source instanceof GmEnumConstant) {
					if ((!propertyName.equals("metaData")) && (!propertyName.equals("declaringType"))) 
						break;					
				} else if (source instanceof GmEntityTypeOverride) {
					if ((!propertyName.equals("entityType")) && (!propertyName.equals("propertyOverrides")) && (!propertyName.equals("metaData")) && (!propertyName.equals("declaringModel")) && (!propertyName.equals("propertyMetaData")))  
						break;
				} else if (source instanceof GmPropertyOverride) {
					if ((!propertyName.equals("property")) && (!propertyName.equals("metaData")) && (!propertyName.equals("Initializer")))
						break;
				} else if (source instanceof GmEnumTypeOverride) {
					if ((!propertyName.equals("enumType")) && (!propertyName.equals("metaData")) && (!propertyName.equals("declaringModel"))) 
						break;
				} else if (source instanceof GmEnumConstantOverride) {
					if ((!propertyName.equals("enumConstant")) && (!propertyName.equals("metaData")) && (!propertyName.equals("declaringType"))) 
						break;
				} else {
					break;
				}
				
				if (GMEUtil.isPropertyAbsent(source, prop))
					absentProperties.add(prop);									
		}
		return absentProperties;
	}

	private void getGmModelProperties(final ModelPath modelPath) {
		if (modelPath == null)
			return;
		
		ModelPathElement modelPathElement = modelPath.last();
		if (modelPathElement == null)
			return;
		
		Object value = modelPathElement.getValue();
		if (! (value instanceof GenericEntity)) {
			doUnmaskExplorer();
			return;
		}
		
		GenericEntity source = (GenericEntity) value;
		
		GmMetaModel gmMetaModel = null;
		GmMetaModel pathGmMetaModel = null;
		GmEntityType gmEditingEntityType = null;
		//RVE - first we take MetaModel from Path, if not in Path then take declaredMetaModel
		for (ModelPathElement element : modelPath) {
			if (element.getValue() instanceof GmMetaModel) {
				pathGmMetaModel = element.getValue();
			} else if (element.getValue() instanceof GmEntityType) {
				gmEditingEntityType = element.getValue();
				Property properties = gmEditingEntityType.entityType().getProperty("declaringModel");
				if (GMEUtil.isPropertyAbsent(gmEditingEntityType, properties)) {
					getAbsentProperties(modelPath, gmEditingEntityType, properties);				
					return;
				}					
				gmMetaModel = gmEditingEntityType.getDeclaringModel();
			}
		}
		if (pathGmMetaModel != null)
			gmMetaModel = pathGmMetaModel;
				
		if (source instanceof GmEntityType || source instanceof GmEnumType) {
			//RVE  - in case of selecting BasicModel at Assembly Panel, and subselect Types from this Model, than click on SuperTypes
			Property properties = source.entityType().getProperty("declaringModel");
			if (GMEUtil.isPropertyAbsent(source, properties)) {
				getAbsentProperties(modelPath, source, properties);				
				return;
			}	
			if (gmMetaModel == null)
				gmMetaModel = ((GmCustomTypeInfo) source).getDeclaringModel();
		} else if (source instanceof GmProperty) {
			Property properties = source.entityType().getProperty("declaringType");
			if (GMEUtil.isPropertyAbsent(source, properties)) {
				getAbsentProperties(modelPath, source, properties);				
				return;
			}
			GmEntityType gmEntityType = ((GmProperty) source).getDeclaringType();
			properties = gmEntityType.entityType().getProperty("declaringModel");
			if (GMEUtil.isPropertyAbsent(gmEntityType, properties)) {
				getAbsentProperties(modelPath, gmEntityType, properties);
				return;
			}
			if (gmMetaModel == null)
				gmMetaModel = ((GmProperty) source).getDeclaringType().getDeclaringModel();
		} else if (source instanceof GmEnumConstant) {
			Property properties = source.entityType().getProperty("declaringType");
			if (GMEUtil.isPropertyAbsent(source, properties)) {
				getAbsentProperties(modelPath, source, properties);				
				return;
			}
			GmEnumType gmEnumType = ((GmEnumConstant) source).getDeclaringType();
			properties = gmEnumType.entityType().getProperty("declaringModel");
			if (GMEUtil.isPropertyAbsent(gmEnumType, properties)) {
				getAbsentProperties(modelPath, gmEnumType, properties);
				return;
			}
			if (gmMetaModel == null)
				gmMetaModel = ((GmEnumConstant) source).getDeclaringType().getDeclaringModel();		
		}
				
		//before create Resolver...we need to load all Property for Model (EntityTypes, Enumtypes,...superClass)		
		if (gmMetaModel != null) {
			final GmMetaModel loadGmMetaModel = gmMetaModel;
			
			loadGmMetaModelProperties(gmMetaModel/*, entityTypeSource*/).andThen(result -> {
				new Timer() {
					@Override
					public void run() {
						if (metaDataResolverProvider != null) {
							metaDataResolverProvider.setUseSessionResolver(useSessionResolver);
							metaDataResolverProvider.setUseFilter(useFilterForResolver);
							metaDataResolverProvider.setFilter(lastUseCaseFilter, lastRolesFilter, lastAccessFilter);
							metaDataResolverProvider.setGmMetaModel(loadGmMetaModel);
							metaDataResolverProvider.rebuildResolver();
						}
						prepareCotent(modelPath);
						doUnmaskExplorer();
					}
				}.schedule(100);
			}).onError(e -> {
				doUnmaskExplorer();
				ErrorDialog.show("Error load MetaData Editor Model Properties", e);
				e.printStackTrace();
			});
		} else {
			if (this.metaDataResolverProvider != null) {
				this.metaDataResolverProvider.setUseSessionResolver(true);
				this.metaDataResolverProvider.setUseFilter(this.useFilterForResolver);
				this.metaDataResolverProvider.setFilter(this.lastUseCaseFilter, this.lastRolesFilter, this.lastAccessFilter);
				this.metaDataResolverProvider.setGmMetaModel(gmMetaModel);
				this.metaDataResolverProvider.rebuildResolver();
			}
			prepareCotent(modelPath);
			doUnmaskExplorer();
		}
	}

	private void getAbsentProperties(final ModelPath modelPath, GenericEntity source, Property properties) {
		loadAbsentProperties(source, source.entityType() ,Arrays.asList(properties)).andThen(result -> {
			entitiesLoading.remove(source);
			new Timer() {
				@Override
				public void run() {
					getGmModelProperties(modelPath);
				}
			}.schedule(100);
		}).onError(e -> {
			entitiesLoading.remove(source);
			ErrorDialog.show("Error load MetaData Editor Properties", e);
			e.printStackTrace();
		});
	}
	
	private Future<Void> loadAbsentProperties(final GenericEntity entity, EntityType<GenericEntity> entityType, List<Property> absentProperties) {
		final Future<Void> future = new Future<>();
		
		MultiLoader multiLoader = new MultiLoader();
		multiLoader.setParallel(false);
		int i = 0;
		for (Property property : absentProperties) {
			multiLoader.add(Integer.toString(i++),
					GMEUtil.loadAbsentProperty(entity, entityType, property, gmSession, useCase, codecRegistry, specialEntityTraversingCriterion));
		}
		
		this.entitiesLoading.add(entity);
		
		multiLoader.load().andThen(result -> future.onSuccess(null)).onError(future::onFailure);
		return future;
	}
	
	private Future<Void> loadGmMetaModelProperties(final GmMetaModel gmMetaModel /*, final GenericEntity entity*/) {
		final Future<Void> future = new Future<>();
		
		MultiLoader multiLoader = new MultiLoader();
		multiLoader.setParallel(false);
		int i = 0;
				
		multiLoader.add(Integer.toString(i++), loadGmMetaModelProperty(gmMetaModel));
		//multiLoader.add(Integer.toString(i++), loadGmMetaModelProperty(gmMetaModel, "entityTypes"));
		/*
		multiLoader.add(Integer.toString(i++), loadGmMetaModelProperty(gmMetaModel, "entityTypes"));
		multiLoader.add(Integer.toString(i++), loadGmMetaModelProperty(gmMetaModel, "enumTypes"));
		multiLoader.add(Integer.toString(i++), loadGmMetaModelProperty(gmMetaModel, "entityTypeOverrides"));
		multiLoader.add(Integer.toString(i++), loadGmMetaModelProperty(gmMetaModel, "enumTypeOverrides"));
		multiLoader.add(Integer.toString(i++), loadGmMetaModelProperty(gmMetaModel, "dependencies"));
		multiLoader.add(Integer.toString(i++), loadGmMetaModelProperty(gmMetaModel, "metaData"));
		*/
		
		/*
		if (entity != null) {
			if (entity instanceof GmEntityType) {
				multiLoader.add(Integer.toString(i++), loadGmEntityTypeProperty((GmEntityType) entity, "properties"));
				multiLoader.add(Integer.toString(i++), loadGmEntityTypeProperty((GmEntityType) entity, "propertyMetaData"));
				multiLoader.add(Integer.toString(i++), loadGmEntityTypeProperty((GmEntityType) entity, "propertyOverrides"));		
				multiLoader.add(Integer.toString(i++), loadGmEntityTypeProperty((GmEntityType) entity, "metaData"));
			} 
			
		}
		*/
		
		multiLoader.load().andThen(result -> future.onSuccess(null)).onError(future::onFailure);
		return future;
	}		
	
	/*private Timestamp GetCurrentTimeStamp() 
	{
		 java.util.Date date= new java.util.Date();
		 return new Timestamp(date.getTime());
	}*/	
	
	private Loader<Void> loadGmMetaModelProperty(final GmMetaModel gmMetaModel/*, final String propertyName*/) {	
		return new Loader<Void>() {
			@Override
			public void load(final AsyncCallback<Void> asyncCallback) {
				TraversingCriterion allPropertiesTc = TC.create().negation().joker().done();
				allPropertiesTc = TC.create()
					     .pattern()
					      .typeCondition(TypeConditions.isAssignableTo(IncrementalAccess.T))
					      .negation()
					       .typeCondition(TypeConditions.isKind(TypeKind.simpleType))
					     .close()
					    .done();												
																		
				//SelectQuery query = new SelectQueryBuilder().from(GmMetaModel.class, "m").select("m", propertyName).where().property("m", "id").eq(gmMetaModel.getId()).tc(allPropertiesTc ).done();
				SelectQuery query = new SelectQueryBuilder().from(GmMetaModel.class, "m").where().property("m", "id").eq(gmMetaModel.getId()).tc(allPropertiesTc ).done();

				/*
				Timestamp startTimestamp = GetCurrentTimeStamp();
				SelectQueryResult future = null;
				try {
					future = gmSession.query().select(query).result();
				} catch (GmSessionException e) {
					e.printStackTrace();
				}
				Timestamp endTimestamp = GetCurrentTimeStamp();
				
				Window.alert("Start: " + startTimestamp + " End: " + endTimestamp);
				*/
								
				gmSession.query().select(query).result(com.braintribe.processing.async.api.AsyncCallback.of(future -> {
					/*
					try {
						
						Set<GmEntityType> listEntityTypes = new HashSet<GmEntityType>();
						Set<GmEnumType> listEnumTypes = new HashSet<GmEnumType>();
						Set<GmEntityTypeOverride> listEntityTypeOverride = new HashSet<GmEntityTypeOverride>();
						Set<GmEnumTypeOverride> listEnumTypeOverride = new HashSet<GmEnumTypeOverride>();
						for (Object object : future.result().getResults()) {
							if (object instanceof GmEntityType) {
								listEntityTypes.add((GmEntityType) object);
							}
							if (object instanceof GmEnumType) {
								listEnumTypes.add((GmEnumType) object);
							}
							if (object instanceof GmEntityTypeOverride) {
								listEntityTypeOverride.add((GmEntityTypeOverride) object);
							}
							if (object instanceof GmEnumTypeOverride) {
								listEnumTypeOverride.add((GmEnumTypeOverride) object);
							}
						}
						
						
						gmSession.suspendHistory();
						if (!listEntityTypes.isEmpty())
							gmQueryMetaModel.setEntityTypes(listEntityTypes);
						if (!listEnumTypes.isEmpty())
							gmQueryMetaModel.setEnumTypes(listEnumTypes);
						if (!listEntityTypeOverride.isEmpty())
							gmQueryMetaModel.setEntityTypeOverrides(listEntityTypeOverride);
						if (!listEnumTypeOverride.isEmpty())
							gmQueryMetaModel.setEnumTypeOverrides(listEnumTypeOverride);
						gmSession.resumeHistory();
						
						
						//ph2.stop();
					} catch (GmSessionException e) {
					}
					*/
					asyncCallback.onSuccess(null);
				}, asyncCallback::onFailure));
			}
		};
	}

	public void showTab(String tabType) {
		for (Widget widget : this.tabPanel.getContainer())
			if (widget instanceof MetaDataEditorProvider)
				if (((MetaDataEditorProvider) widget).getCaption().equals(tabType)) {
					this.tabPanel.setActiveWidget(widget);
					return;
				}
	}
		
	/*
	private Loader<Void> loadGmEntityTypeProperty(final GmEntityType gmEntityType, final String propertyName) {
		return new Loader<Void>() {
			@Override
			public void load(final AsyncCallback<Void> asyncCallback) {
				//TraversingCriterion allPropertiesTc = TC.create().negation().joker().done();
								
				TraversingCriterion allPropertiesTc = null;
					allPropertiesTc = TC.create()
						     .pattern()
						      .typeCondition().entityType(Access.T, EntityTypeStrategy.assignable)
						      .negation()
						       .typeCondition().simpleType()
						     .close()
						    .done();												
					
				SelectQuery query = new SelectQueryBuilder().from(GmEntityType.class, "e").select("e", propertyName).where().property("e", "id").eq(gmEntityType.getId()).tc(allPropertiesTc ).done();
				
				MetaDataEditorPanel.this.gmSession.query().select(query).result(new com.braintribe.processing.async.api.AsyncCallback<SelectQueryResultConvenience>() {					
					public void onSuccess(SelectQueryResultConvenience future) {
						asyncCallback.onSuccess(null);
					}
		
					public void onFailure(Throwable t) {
						asyncCallback.onFailure(t);
					}
				
				});
				
			}
		};
	}
	*/

	/*
	private Loader<Void> loadGmEntityTypeOverrideProperty(final GmEntityTypeOverride gmEntityTypeOverride, final String propertyName) {
		return new Loader<Void>() {
			@Override
			public void load(final AsyncCallback<Void> asyncCallback) {
				//TraversingCriterion allPropertiesTc = TC.create().negation().joker().done();
							
				TraversingCriterion allPropertiesTc = null;
				if (propertyName.equals("propertyMetaData"))
					allPropertiesTc = TC.create()
							.negation()
								.disjunction()
							    	.typeCondition()
							       		.simpleType()
									.property("metaData")
									.property("entityType")
									.property("typeSignature")
								.close()
							.done();
				else if (propertyName.equals("propertyOverrides"))
					allPropertiesTc = TC.create()
							.negation()
								.disjunction()
							    	.typeCondition()
							       		.simpleType()
									.property("metaData")
									.property("entityType")
									.property("property")
									.property("typeSignature")
								.close()
							.done();
				else
					allPropertiesTc = TC.create().negation().joker().done();
																				
				SelectQuery query = new SelectQueryBuilder().from(GmEntityTypeOverride.class, "e").select("e", propertyName).where().property("e", "id").eq(gmEntityTypeOverride.getId()).tc(allPropertiesTc ).done();
				
				MetaDataEditorPanel.this.gmSession.query().select(query).result(new com.braintribe.processing.async.api.AsyncCallback<SelectQueryResultConvenience>() {					
					public void onSuccess(SelectQueryResultConvenience future) {
						asyncCallback.onSuccess(null);
					}
		
					public void onFailure(Throwable t) {
						asyncCallback.onFailure(t);
					}
				
				});
				
			}
		};
	}
	*/
	
	/*
	public ModelMetaDataContextBuilder getModelMetaDataContextBuilder (Boolean useSessionResolver) {
		if (useSessionResolver) {
			return this.sessionModelMetaDataContextBuilder;
		} else {
			return this.modelMetaDataContextBuilder;
		}
	}
	*/
}
