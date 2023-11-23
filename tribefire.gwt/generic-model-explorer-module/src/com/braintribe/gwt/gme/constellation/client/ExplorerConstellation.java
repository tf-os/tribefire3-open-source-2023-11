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
package com.braintribe.gwt.gme.constellation.client;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellationDialog.ValueDescriptionBean;
import com.braintribe.gwt.gme.constellation.client.action.GlobalActionPanel;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.notification.client.NotificationConstellation;
import com.braintribe.gwt.gme.notification.client.resources.NotificationResources;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.servicerequestpanel.client.ServiceRequestAutoPagingView;
import com.braintribe.gwt.gme.servicerequestpanel.client.ServiceRequestConstellationScope;
import com.braintribe.gwt.gme.servicerequestpanel.client.ServiceRequestExecutionConstellation;
import com.braintribe.gwt.gme.servicerequestpanel.client.TemplateQueryOpenerView;
import com.braintribe.gwt.gme.templateevaluation.client.TemplateEvaluationPreparer;
import com.braintribe.gwt.gme.templateevaluation.client.TemplateGIMADialog;
import com.braintribe.gwt.gme.templateevaluation.client.expert.TemplateQueryActionHandler.TemplateQueryOpener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar.TetherBarListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTab;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.gme.verticaltabpanel.client.action.VerticalTabConfig;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.gwt.gme.workbench.client.WorkbenchListenerAdapter;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.action.client.ActionPerformanceContext;
import com.braintribe.gwt.gmview.action.client.ActionPerformanceListener;
import com.braintribe.gwt.gmview.action.client.FieldDialogOpenerAction;
import com.braintribe.gwt.gmview.action.client.InstantiationActionHandler;
import com.braintribe.gwt.gmview.action.client.WorkWithEntityExpert;
import com.braintribe.gwt.gmview.action.client.WorkbenchActionSelectionHandler;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.ClipboardListener;
import com.braintribe.gwt.gmview.client.EditEntityActionListener;
import com.braintribe.gwt.gmview.client.EditEntityContext;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmAmbiguousSelectionSupport;
import com.braintribe.gwt.gmview.client.GmContentSupplier;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmInteractionListenerAdapter;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmTreeView;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.InstantiationData;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelEnvironmentSetListener;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.client.ParentModelPathSupplier;
import com.braintribe.gwt.gmview.client.ReloadableGmView;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.gmview.client.js.GmExternalViewInitializationListener;
import com.braintribe.gwt.gmview.client.js.GmExternalViewInitializationSupport;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMETraversingCriterionUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.validationui.client.ValidationConstellation;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.EntryPointPathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.validation.ValidationKind;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.meta.data.prompt.AutoCommit;
import com.braintribe.model.meta.data.prompt.DetailsViewMode;
import com.braintribe.model.meta.data.prompt.DetailsViewMode.DetailsViewModeOption;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.template.Template;
import com.braintribe.model.uicommand.RunWorkbenchAction;
import com.braintribe.model.workbench.InstantiationAction;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.TemplateServiceRequestBasedAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

import tribefire.extension.js.model.deployment.ViewWithJsUxComponent;

public class ExplorerConstellation extends ExtendedBorderLayoutContainer implements InitializableBean, InstantiatedEntityListener,
		EditEntityActionListener, TemplateEvaluationPreparer, ActionPerformanceListener, ModelPathNavigationListener, ModelEnvironmentSetListener,
		InstantiationActionHandler, WorkbenchActionSelectionHandler, ManipulationListener, Supplier<TransientGmSession>, GmContentSupplier {
	private static final int WEST_SIZE = 250;
	private static final int MARGIN_SIZE = 1;
	private static final int VERTICAL_TAB_PANEL_HORIZONTAL_HEIGHT = 34;
	private static final int VERTICAL_TAB_PANEL_WITH_GLOBAL_ACTIONS_HORIZONTAL_HEIGHT = 95;
	
	static {
		ConstellationResources.INSTANCE.css().ensureInjected();
	}
	
	private Workbench workbench;
	private VerticalTabPanel verticalTabPanel;
	private HomeConstellation homeConstellation;
	private NotificationConstellation notificationConstellation;
	private Supplier<? extends AbstractChangesConstellation> changesConstellationSupplier;
	private Function<Supplier<MasterDetailConstellation>, Supplier<ClipboardConstellation>> clipboardConstellationProvider;
	private List<ClipboardConstellation> clipboardConstellationsList = new ArrayList<>();
	private List<ChangesConstellation> changesConstellationsList = new ArrayList<>();
	private Widget currentWidget;
	private final BorderLayoutData centerData = new BorderLayoutData();
	private BorderLayoutData westData;
	private String useCase;
	private Supplier<BrowsingConstellation> browsingConstellationProvider;
	private Supplier<MasterDetailConstellation> readOnlyMasterDetailConstellationProvider;
	private Supplier<? extends TemplateGIMADialog> templateEvaluationDialogProvider;
	private Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier;
	private Map<GenericEntity, VerticalTabElement> instantiatedEntityVerticalTabElements;
	private Supplier<GIMADialog> gimaDialogProvider;
	private Supplier<GIMADialog> transientGimaDialogProvider;
	private Supplier<TemplateGIMADialog> templateGimaDialogSupplier;
	private Map<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>> fieldDialogOpenerActions;
	private Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	private ModelEnvironmentDrivenGmSession gmSession;
	private TransientGmSession transientGmSession;
	private Supplier<? extends TransientGmSession> transientSessionSupplier;
	private ExtendedBorderLayoutContainer centerPanel;
	private BorderLayoutContainer westPanel;
	private BorderLayoutContainer globalActionPanelContainer;	
	private boolean useClipboardConstellation = false;
	private boolean useHorizontalTabs = true;
	private boolean useToolBar = true;
	private final int southDataSize = 86;
	private final BorderLayoutData southData = new BorderLayoutData(southDataSize);
	private BorderLayoutData verticalTabPanelLayoutData;
	private boolean useWorkbenchWithinTab = false;
	private Supplier<GmViewActionBar> gmViewActionBarProvider;
	private GmViewActionBar gmViewActionBar;
	private BrowsingConstellation currentBrowsingConstellation;
	private TetherBarListener actionRefreshTetherBarListener;
	private String freeInstantiationUseCase;
	private Supplier<? extends GmEntityView> serviceRequestConstellationProvider;
	private boolean changesMarkerChanged;
	private VerticalTabElement changesElement;
	private VerticalTabElement notificationElement;
	private VerticalTabElement validationElement;
	private boolean clipboardMarkerChanged;
	private VerticalTabElement clipboardElement;
	private Supplier<? extends Action> commitActionSupplier;
	private double lastWestDataSize = WEST_SIZE;
	private double lastVerticalTabPanelLayoutData = VERTICAL_TAB_PANEL_HORIZONTAL_HEIGHT;
	private BorderLayoutData globalActionPanelWestData = new BorderLayoutData(VERTICAL_TAB_PANEL_HORIZONTAL_HEIGHT + 1);
	private boolean autoCommit = false;
	private final List<GenericEntity> instantiatedEntitiesWithTab = new ArrayList<>();
	private WorkWithEntityExpert workWithEntityExpert;
	private boolean actionToolBarVisible = true;
	private boolean verticalTabActionToolbarVisible = false;
	private boolean topGlobalActionToolbarVisible = false;
	private Integer maxNumberOfOpenedTabs = 15;
	private GlobalActionPanel globalActionPanel;
	private List<Action> constellationDefaultModelActionList;
	private ValidationConstellation validationConstellation;
	
	public ExplorerConstellation() {
		setBorders(false);
		getElement().addClassName(ConstellationResources.INSTANCE.css().explorerConstellationCenterBackground());
		centerData.setMargins(new Margins(MARGIN_SIZE, 0, 0, MARGIN_SIZE));
		addStyleName("gmExplorerConstellation");
	}
	
	/**
	 * Configures the required Workbench.
	 */
	@Required
	public void setWorkbench(Workbench workbench) {
		workbench.setId("gmEntryPointsPanel");
		workbench.addStyleName("gmExplorerConstellationWorkbench");
		this.workbench = workbench;
	}
	
	@Required
	public void setGlobalActionPanel(GlobalActionPanel globalActionPanel) {
		globalActionPanel.setId("gmGlobalActionPanel");
		globalActionPanel.addStyleDependentName("explorerConstellationGlobalActionPanel");
		this.globalActionPanel = globalActionPanel;
	}		
	
	/**
	 * Configures the required {@link VerticalTabPanel}.
	 */
	@Required
	public void setVerticalTabPanel(VerticalTabPanel verticalTabPanel) {
		verticalTabPanel.getElement().setId("gmVerticalTabPanel");
		verticalTabPanel.setParentUseCase("$explorer");
		this.verticalTabPanel = verticalTabPanel;
		verticalTabPanel.addVerticalTabListener(new VerticalTabListener() {
			@Override
			public void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement, VerticalTabElement verticalTabElement) {
				Widget widget = verticalTabElement.getWidget();
				
				prepareActionsForView(widget, false);
				
				configureCurrentWidget(widget);
				
				if (changesMarkerChanged && verticalTabElement == changesElement) {
					verticalTabPanel.updateTabElementName("", changesElement);
					changesMarkerChanged = false;
				} else if (clipboardMarkerChanged && verticalTabElement == clipboardElement) {
					verticalTabPanel.updateTabElementName("", clipboardElement);
					clipboardMarkerChanged = false;
				}
				
				if (widget instanceof ReloadableGmView && ((ReloadableGmView) widget).isReloadPending())
					((ReloadableGmView) widget).reloadGmView();
			}
			
			@Override
			public void onVerticalTabElementAddedOrRemoved(int elements, boolean added, List<VerticalTabElement> verticalTabElements) {
				if (!added && instantiatedEntityVerticalTabElements != null) {
					for (VerticalTabElement verticalTabElement : verticalTabElements) {
						List<GenericEntity> keysToRemove = new ArrayList<>();
						instantiatedEntityVerticalTabElements.entrySet().stream().filter(entry -> entry.getValue() == verticalTabElement)
								.forEach(entry -> keysToRemove.add(entry.getKey()));
						
						keysToRemove.forEach(keyToRemove -> instantiatedEntityVerticalTabElements.remove(keyToRemove));
					}
				}
					
				if (!useHorizontalTabs && verticalTabPanelLayoutData != null) {
					verticalTabPanelLayoutData.setSize((elements * VerticalTabPanel.ELEMENT_HEIGHT) + MARGIN_SIZE);
					doLayout();
				}
			}
			
			@Override
			public void onHeightChanged(int newHeight) {
				if (useHorizontalTabs && verticalTabPanelLayoutData != null && verticalTabPanelLayoutData.getSize() != newHeight) {
					verticalTabPanelLayoutData.setSize(newHeight);
					centerPanel.doLayout();
				}
			}
		});
	}
	
	/**
	 * Configures the required {@link HomeConstellation}, which is the default center widget,
	 * and it is used as an static element in the {@link VerticalTabPanel}.
	 */
	@Required
	public void setHomeConstellation(HomeConstellation homeConstellation) {
		this.homeConstellation = homeConstellation;
	}
	
	/**
	 * Configures the required {@link NotificationConstellation},
	 * and it is used as an static element in the {@link VerticalTabPanel}.
	 */
	@Configurable
	public void setNotificationConstellation(NotificationConstellation notificationConstellation) {
		this.notificationConstellation = notificationConstellation;
	}	
	
	/**
	 * Configures the required {@link ValidationConstellation},
	 * and it is used as an static element in the {@link VerticalTabPanel}.
	 */
	@Configurable
	public void setValidationConstellation(ValidationConstellation validationConstellation) {
		this.validationConstellation = validationConstellation;
	}		
	
	/**
	 * Configures the required {@link ChangesConstellation}, which is used as an static element in the {@link VerticalTabPanel}.
	 */
	@Required
	public void setChangesConstellation(Supplier<? extends AbstractChangesConstellation> changesConstellationSupplier) {
		this.changesConstellationSupplier = changesConstellationSupplier;
	}

	/**
	 * Configures the required {@link ModelEnvironmentDrivenGmSession}.
	 */
	@Required
	public void setGmSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the required {@link TransientGmSession}.
	 */
	@Required
	public void setTransientGmSession(TransientGmSession transientGmSession) {
		this.transientGmSession = transientGmSession;
	}
	
	/**
	 * Configures the required supplier for prototype {@link TransientGmSession}.
	 */
	@Required
	public void setTransientSessionSupplier(Supplier<? extends TransientGmSession> transientSessionSupplier) {
		this.transientSessionSupplier = transientSessionSupplier;
	}
	
	/**
	 * Configures the useCase where this panel is being used on.
	 */
	@Required
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	/**
	 * Configures the required provider for {@link BrowsingConstellation}s.
	 */
	@Required
	public void setBrowsingConstellationProvider(Supplier<BrowsingConstellation> browsingConstellationProvider) {
		this.browsingConstellationProvider = browsingConstellationProvider;
	}
	
	/**
	 * Configures the required {@link ViewSituationResolver}.
	 */
	@Required
	public void setViewSituationResolver(Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier) {
		this.viewSituationResolverSupplier = viewSituationResolverSupplier;
	}
	
	/**
	 * Configures the required provider for {@link MasterDetailConstellation}s.
	 * Those are used in the {@link ClipboardConstellation}. It should be read only...
	 */
	@Required
	public void setReadOnlyMasterDetailConstellationProvider(final Supplier<MasterDetailConstellation> readOnlyMasterDetailConstellationProvider) {
		this.readOnlyMasterDetailConstellationProvider = readOnlyMasterDetailConstellationProvider;
	}
	
	/**
	 * Configures the required provider for {@link GIMADialog}.
	 */
	@Required
	public void setGIMADialogProvider(Supplier<GIMADialog> gimaDialogProvider) {
		this.gimaDialogProvider = gimaDialogProvider;
	}
	
	/**
	 * Configures the required provider for transient {@link GIMADialog}.
	 */
	@Required
	public void setTransientGimaDialogProvider(Supplier<GIMADialog> transientGimaDialogProvider) {
		this.transientGimaDialogProvider = transientGimaDialogProvider;
	}
	
	/**
	 * Configures the required supplier for {@link TemplateGIMADialog}, used when there are {@link InstantiationAction}s.
	 */
	@Required
	public void setTemplateGimaDialogSupplier(Supplier<TemplateGIMADialog> templateGimaDialogSupplier) {
		this.templateGimaDialogSupplier = templateGimaDialogSupplier;
	}
	
	/**
	 * Configures the registry for {@link WorkbenchAction}s handlers.
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}
	
	/**
	 * Configures the required GmViewActionBar used for holding all actions needed by the currentView.
	 * If {@link #setUseToolBar(boolean)} is true (default), then this is required.
	 */
	@Configurable
	public void setGmViewActionBarProvider(Supplier<GmViewActionBar> gmViewActionBarProvider) {
		this.gmViewActionBarProvider = gmViewActionBarProvider;
	}
	
	/**
	 * Configures the useCase of the view used for displaying a new tab when instantiating a new entity.
	 * If none is set, then we do not force any useCase.
	 */
	@Configurable
	public void setFreeInstantiationUseCase(String freeInstantiationUseCase) {
		this.freeInstantiationUseCase = freeInstantiationUseCase;
	}
	
	/**
	 * Configures the provider which provides constellation for displaying service requests.
	 */
	@Required
	public void setServiceRequestConstellationProvider(Supplier<? extends GmEntityView> serviceRequestConstellationProvider) {
		this.serviceRequestConstellationProvider = serviceRequestConstellationProvider;
	}
	
	/**
	 * Configures a map containing actions that are used instead of GIMA for the given entities.
	 */
	@Configurable
	public void setFieldDialogOpenerActions(Map<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>> fieldDialogOpenerActions) {
		this.fieldDialogOpenerActions = fieldDialogOpenerActions;
	}
	
	/**
	 * Configures whether the {@link ClipboardConstellation} should be used. Defaults to false.
	 */
	@Configurable
	public void setUseClipboardConstellation(boolean useClipboardConstellation) {
		this.useClipboardConstellation = useClipboardConstellation;
	}
	
	/**
	 * Configures the required provider for {@link ClipboardConstellation}.
	 * If {@link #setUseClipboardConstellation(boolean)} was set to true, then this is required.
	 */
	@Configurable
	public void setClipboardConstellationProvider(Function<Supplier<MasterDetailConstellation>, Supplier<ClipboardConstellation>> clipboardConstellationProvider) {
		this.clipboardConstellationProvider = clipboardConstellationProvider;
	}
	
	/**
	 * Configures whether horizontal tabs should be used.
	 * Defaults to true. If false, vertical tabs are used instead.
	 */
	@Configurable
	public void setUseHorizontalTabs(boolean useHorizontalTabs) {
		this.useHorizontalTabs = useHorizontalTabs;
	}
	
	/**
	 * Configures whether we should use the toolBar. Defaults to true.
	 */
	@Configurable
	public void setUseToolBar(boolean useToolBar) {
		this.useToolBar = useToolBar;
	}
	
	@Required
	public void setTemplateEvaluationDialogProvider(Supplier<? extends TemplateGIMADialog> templateEvaluationDialogProvider) {
		this.templateEvaluationDialogProvider = templateEvaluationDialogProvider;
	}
	
	/**
	 * Configures whether to use the {@link Workbench} within a tab.
	 * Defaults to false (use it in the west region).
	 */
	@Configurable
	public void setUseWorkbenchWithinTab(boolean useWorkbenchWithinTab) {
		this.useWorkbenchWithinTab = useWorkbenchWithinTab;
	}
	
	/**
	 * Configures the {@link Action} used for committing when {@link AutoCommit} is available.
	 */
	@Configurable
	public void setCommitAction(Supplier<? extends Action> commitActionSupplier) {
		this.commitActionSupplier = commitActionSupplier;
	}
	
	/**
	 * Configures the provider for {@link WorkWithEntityExpert}s.
	 */
	@Configurable
	public void setWorkWithEntityExpert(WorkWithEntityExpert workWithEntityExpert) {
		this.workWithEntityExpert = workWithEntityExpert;
	}		
	
	/**
	 * Returns the {@link Workbench} used within this {@link ExplorerConstellation}.
	 */
	public Workbench getWorkbench() {
		return workbench;
	}
	
	/**
	 * Returns the {@link HomeConstellation} used within this {@link ExplorerConstellation}.
	 */
	public HomeConstellation getHomeConstellation() {
		return homeConstellation;
	}
	
	/**
	 * Returns the {@link NotificationConstellation} used within this {@link ExplorerConstellation}.
	 */
	public VerticalTabElement getNotificationElement() {
		return notificationElement;
	}	
	
	/**
	 * Returns the {@link VerticalTabPanel} used within this {@link ExplorerConstellation}.
	 */
	public VerticalTabPanel getVerticalTabPanel() {
		return verticalTabPanel;
	}	
	
	public void setConstellationDefaultModelActionList(List<Action> constellationDefaultModelActionList) {
		this.constellationDefaultModelActionList = constellationDefaultModelActionList;
	}
	
	@Override
	public void intializeBean() throws Exception {
		verticalTabPanel.setUseHorizontalTabs(useHorizontalTabs);
							
		int staticElements = 0;
		
		//RVE - validation Constellation - need to set validationConstellation directly, not via supplier, because need receive vent about AdditionalInformation over the icon
		//validationElement = new VerticalTabElement("$validationConstellation", "", VerticalTabConfig.ACTION_VALIDATION.getDisplayName(),
		//		() -> validationConstellation, ConstellationResources.INSTANCE.validationSVG(), null, true, null);
		if (validationConstellation != null) {
			validationElement = new VerticalTabElement("$validationConstellation", "", VerticalTabConfig.ACTION_VALIDATION.getDisplayName(),
					 ConstellationResources.INSTANCE.validationSVG(), null, true, null, validationConstellation);
			validationElement.setSystemConfigurable(true);
			verticalTabPanel.insertVerticalTabElement(validationElement, true);
			staticElements++;
		}

		//RVE - notification Constellation
		if (notificationConstellation != null) {
			notificationElement = new VerticalTabElement("$notificationsConstellation", "", VerticalTabConfig.ACTION_NOTIFICATIONS.getDisplayName(),
					() -> notificationConstellation, NotificationResources.INSTANCE.notificationsOrange(), null, true, null);
			notificationElement.setSystemConfigurable(true);
			verticalTabPanel.insertVerticalTabElement(notificationElement, true);
			staticElements++;
		}
		
		if (useClipboardConstellation) {
			clipboardElement = new VerticalTabElement("$clipboardConstellation", "", LocalizedText.INSTANCE.clipboard(),
					getClipboardConstellationSupplier(), ConstellationResources.INSTANCE.clipboard(), null, true, null);
			clipboardElement.setSystemConfigurable(true);
			verticalTabPanel.insertVerticalTabElement(clipboardElement, true);
			staticElements++;
		}
		
		changesElement = new VerticalTabElement("$changesConstellation", "", LocalizedText.INSTANCE.changes(), getChangesConstellationSupplier(),
				ConstellationResources.INSTANCE.changes(), null, true, null);
		changesElement.setSystemConfigurable(true);
		verticalTabPanel.insertVerticalTabElement(changesElement, true);
		staticElements++;
		
		if (useWorkbenchWithinTab) {
			VerticalTabElement workbenchElement = new VerticalTabElement("$workbenchConstellation", "", LocalizedText.INSTANCE.workbench(),
					() -> workbench, ConstellationResources.INSTANCE.workbench(), null, true, null);
			workbenchElement.setSystemConfigurable(true);
			verticalTabPanel.insertVerticalTabElement(workbenchElement, true);
			staticElements++;
		} 
		
		VerticalTabElement homeElement = new VerticalTabElement("$homeConstellation", "", LocalizedText.INSTANCE.home(), () -> homeConstellation,
				ConstellationResources.INSTANCE.home(), null, true, null);
		homeElement.setSystemConfigurable(true);
		verticalTabPanel.insertVerticalTabElement(homeElement, true);
		staticElements++;
		
		southData.setMargins(new Margins(-MARGIN_SIZE, 0, 0, MARGIN_SIZE));
		
		if (!useHorizontalTabs) {
			BorderLayoutContainer westPanel = new ExtendedBorderLayoutContainer();
			westPanel.setBorders(false);
			westPanel.addStyleName("gmExplorerConstellationWestPanel");
			
			if (!useWorkbenchWithinTab) {
				westPanel.setCenterWidget(workbench);
				
				verticalTabPanelLayoutData = new BorderLayoutData((VerticalTabPanel.ELEMENT_HEIGHT * staticElements) + MARGIN_SIZE);
				verticalTabPanelLayoutData.setMargins(new Margins(0, 0, MARGIN_SIZE, 0));
				westPanel.setNorthWidget(verticalTabPanel, verticalTabPanelLayoutData);
			} else
				westPanel.setCenterWidget(verticalTabPanel);
			
			westData = new BorderLayoutData(WEST_SIZE);
			westData.setMinSize(WEST_SIZE);
			westData.setMaxSize(800);
			westData.setSplit(true);
			westData.setCollapsible(true);
			setWestWidget(westPanel, westData);
			if (useToolBar)
				setSouthWidget(getGmViewActionBar().getView(), southData);
		} else {
			centerPanel = new ExtendedBorderLayoutContainer();
			centerPanel.getElement().getStyle().setBackgroundColor("#dedede");
			centerPanel.setBorders(false);
			centerPanel.addStyleName("gmExplorerConstellationCenterPanel");
			
			verticalTabPanelLayoutData = new BorderLayoutData(VERTICAL_TAB_PANEL_HORIZONTAL_HEIGHT);
			centerPanel.setNorthWidget(verticalTabPanel, verticalTabPanelLayoutData);
			setCenterWidget(centerPanel);
			if (useToolBar)
				centerPanel.setSouthWidget(getGmViewActionBar().getView(), southData);
			
			westPanel = new BorderLayoutContainer();
			westPanel.setStyleName("gmExplorerConstellationWestPanel");
			westPanel.setBorders(false);
			
			if (!useWorkbenchWithinTab) {
				globalActionPanelContainer = new BorderLayoutContainer();
				globalActionPanelContainer.setStyleName("gmWorkbenchTopPanel");
				globalActionPanelContainer.setBorders(false);
				
				westPanel.setNorthWidget(globalActionPanelContainer, globalActionPanelWestData);
				westData = new BorderLayoutData(WEST_SIZE);
				westData.setMinSize(WEST_SIZE);
				westData.setMaxSize(800);
				westData.setSplit(true);
			
				westPanel.setCenterWidget(workbench);
				setWestWidget(westPanel, westData);
			}
		}
		
		verticalTabPanel.setSelectedVerticalTabElement(homeElement);
		
		if (workbench != null) {
			workbench.addWorkbenchListener(new WorkbenchListenerAdapter() {
				@Override
				public void onFolderSelected(Folder folder, Widget callerWidget) {
					performWorkbenchAction(folder, callerWidget, null);
				}
			});
		}		
	}
	
	/**
	 * Handles a {@link RunWorkbenchAction} command.
	 */
	public void runWorkbenchAction(Folder folder, RunWorkbenchAction runWorkbenchAction) {
		performWorkbenchAction(folder, null, runWorkbenchAction.getVariables());
	}
	
	private void performWorkbenchAction(Folder folder, Widget callerWidget, Map<String, Object> variableValues) {
		if (folder != null && folder.getContent() instanceof WorkbenchAction) {
			ModelAction action = workbenchActionHandlerRegistry.apply(prepareWorkbenchActionContext(folder, callerWidget, variableValues));
			if (action != null)
				action.perform(null);
		}
	}

	private void handleWorkbenchConfiguration() {
		WorkbenchConfiguration workbenchConfiguration = null;
		if (gmSession != null)
			workbenchConfiguration = gmSession.getModelEnvironment().getWorkbenchConfiguration();
		
		if (workbenchConfiguration == null) {
			verticalTabActionToolbarVisible = false;
			actionToolBarVisible = true;
			topGlobalActionToolbarVisible = false;
			if (verticalTabPanel != null)
				verticalTabPanel.setMaxNumberOfNonStaticElements(maxNumberOfOpenedTabs);
			return;
		}
		
		verticalTabActionToolbarVisible = workbenchConfiguration.getUseTopContextActionBar();
		actionToolBarVisible = !verticalTabActionToolbarVisible;
		topGlobalActionToolbarVisible = workbenchConfiguration.getUseTopGlobalActionBar();
		
		Integer maxTabCount = workbenchConfiguration.getMaxNumberOfOpenedTabs();
		if (maxTabCount != null)
			maxNumberOfOpenedTabs = maxTabCount;
		if (verticalTabPanel != null)
			verticalTabPanel.setMaxNumberOfNonStaticElements(maxNumberOfOpenedTabs);
		
		Integer workbenchWidth = workbenchConfiguration.getWorkbenchWidth();
		if (workbenchWidth != null && (!useHorizontalTabs || !useWorkbenchWithinTab)) {
			lastWestDataSize = workbenchWidth;
			westData.setSize(workbenchWidth);
			westData.setMinSize(workbenchWidth);
			
			ExtendedBorderLayoutContainer panel = useHorizontalTabs ? centerPanel : this;
			panel.doLayout();
		}
	}
	
	private void getActionBarConfig() {				
		if (gmViewActionBar != null)
			gmViewActionBar.setToolBarVisible(actionToolBarVisible);
		
		if (topGlobalActionToolbarVisible && !this.globalActionPanel.isEmpty()) {					
			globalActionPanelContainer.setCenterWidget(this.globalActionPanel);
			globalActionPanelWestData.setSize(VERTICAL_TAB_PANEL_WITH_GLOBAL_ACTIONS_HORIZONTAL_HEIGHT+1);
		} else {
			globalActionPanelContainer.setCenterWidget(null);
			globalActionPanelWestData.setSize(VERTICAL_TAB_PANEL_HORIZONTAL_HEIGHT + 1);
		}
		
		if (useToolBar && verticalTabActionToolbarVisible && topGlobalActionToolbarVisible) {
			if (centerPanel != null) 
				centerPanel.setSouthWidget(null, new BorderLayoutData(0));				
			 else 
				setSouthWidget(null, new BorderLayoutData(0));
		}
		
		if (notificationConstellation != null)
			notificationConstellation.setVisibleVerticalTabActionBar(verticalTabActionToolbarVisible);
		for (ClipboardConstellation clipboardConstellation : clipboardConstellationsList) 
			clipboardConstellation.setVisibleVerticalTabActionBar(verticalTabActionToolbarVisible);		
		for (ChangesConstellation changesConstellation : changesConstellationsList) 
			changesConstellation.setVisibleVerticalTabActionBar(verticalTabActionToolbarVisible);
		
		forceLayout();
	}
	
	/**
	 * Hides the Workbench and the VerticalTabPanel.
	 */
	public void hideWorkbenchAndVerticalTabPanel() {
		lastWestDataSize = westData.getSize();
		westData.setSize(0);
		lastVerticalTabPanelLayoutData = verticalTabPanelLayoutData.getSize(); 
		verticalTabPanelLayoutData.setSize(0);
		verticalTabPanel.getElement().getStyle().setVisibility(Visibility.HIDDEN);
	}
	
	/**
	 * Restores the visibility of the Workbench and the VerticalTabPanel
	 */
	public void restoreWorkbenchAndVerticalTabPanel() {
		westData.setSize(lastWestDataSize);
		verticalTabPanelLayoutData.setSize(lastVerticalTabPanelLayoutData);
		verticalTabPanel.getElement().getStyle().clearVisibility();
	}
	
	@Override
	public void onModelEnvironmentSet() {
		ClipboardConstellation clipboardConstellation = (ClipboardConstellation) clipboardElement.getWidgetIfSupplied();
		if (clipboardConstellation != null) {
			clipboardConstellation.configureGmSession(gmSession);
			clipboardConstellation.addMasterDetailConstellationProvidedListener(masterDetailConstellation -> {
				GmContentView masterView = masterDetailConstellation.getCurrentMasterView();
				if (masterView instanceof GmInteractionSupport)
					((GmInteractionSupport) masterView).addInteractionListener(getInteractionListener());
				if (masterDetailConstellation.getDetailView() != null)
					masterDetailConstellation.getDetailView().addSelectionListener(getSelectionListener());
			});
		}
		
		AbstractChangesConstellation changesConstellation = (AbstractChangesConstellation) changesElement.getWidgetIfSupplied();
		if (changesConstellation != null) {
			changesConstellation.configureSessions(gmSession, transientGmSession);
			changesConstellation.addMasterDetailConstellationProvidedListener(masterDetailConstellation -> {
				if (masterDetailConstellation != null && masterDetailConstellation.getCurrentMasterView() != null) {
					GmContentView masterView = masterDetailConstellation.getCurrentMasterView();
					if (masterView instanceof GmInteractionSupport)
						((GmInteractionSupport) masterView).addInteractionListener(getInteractionListener());
					if (masterDetailConstellation.getDetailView() != null)
						masterDetailConstellation.getDetailView().addSelectionListener(getSelectionListener());
				}
			});
		}
		
		handleWorkbenchConfiguration();
		getActionBarConfig();		

		ModelMdResolver mdResolver = gmSession.getModelAccessory().getMetaData();
		autoCommit = mdResolver == null ? false : mdResolver.is(AutoCommit.T);
	}
	
	private GmInteractionListenerAdapter getInteractionListener() { //TODO: can this be a singleton?
		return new GmInteractionListenerAdapter() {
			@Override
			public boolean onBeforeExpand(GmMouseInteractionEvent event) {
				ModelPath modelPath = event.getElement();
				if (modelPath != null && modelPath.last().getType().isEntity())
					showEntityVerticalTabElement(modelPath, false, false, false);
				
				return true;
			}
		};
	}
	
	private GmSelectionListener getSelectionListener() { //TODO: can this be a singleton?
		return gmSelectionSupport -> {
			ModelPath modelPath = null;
			if (!(gmSelectionSupport instanceof GmAmbiguousSelectionSupport))
				modelPath = gmSelectionSupport.getFirstSelectedItem();
			else {
				List<List<ModelPath>> modelPaths = ((GmAmbiguousSelectionSupport) gmSelectionSupport).getAmbiguousSelection();
				if (modelPaths != null && !modelPaths.isEmpty()) {
					List<ModelPath> modelPathList = modelPaths.get(0);
					for (ModelPath path : modelPathList) {
						if (path.last().getValue() instanceof GenericEntity) {
							modelPath = path;
							break;
						}
					}
				}
			}
			
			if (modelPath != null && modelPath.last().getValue() instanceof GenericEntity)
				showEntityVerticalTabElement(modelPath, false, false, false);
		};
	}
	
	private Supplier<? extends AbstractChangesConstellation> getChangesConstellationSupplier() {
		return () -> {
			AbstractChangesConstellation changesConstellation = changesConstellationSupplier.get();
			changesConstellationsList.add((ChangesConstellation) changesConstellation);
			changesConstellation.addChangesConstellationListener((boolean added) -> handleChangesMarker(changesElement, added));
			//changesConstellation.addChangesConstellationListener((boolean added) -> updateChangesTabElementVisibility(changesElement, added));
			
			changesConstellation.configureSessions(gmSession, transientGmSession);
			changesConstellation.addMasterDetailConstellationProvidedListener(masterDetailConstellation -> {
				if (masterDetailConstellation != null && masterDetailConstellation.getCurrentMasterView() != null) {
					GmContentView masterView = masterDetailConstellation.getCurrentMasterView();
					if (masterView instanceof GmInteractionSupport)
						((GmInteractionSupport) masterView).addInteractionListener(getInteractionListener());
					if (masterDetailConstellation.getDetailView() != null)
						masterDetailConstellation.getDetailView().addSelectionListener(getSelectionListener());
				}
			});
			
			changesConstellation.setConstellationDefaultModelActions(constellationDefaultModelActionList);
			changesConstellation.setVisibleVerticalTabActionBar(verticalTabActionToolbarVisible);
			changesConstellation.prepareLayout(); 			
			
			return changesConstellation;
		};
	}
	
	private Supplier<ClipboardConstellation> getClipboardConstellationSupplier() {
		return () -> {
			Supplier<ClipboardConstellation> clipboardConstellationSupplier = clipboardConstellationProvider.apply(readOnlyMasterDetailConstellationProvider);
			ClipboardConstellation clipboardConstellation = clipboardConstellationSupplier.get();
			clipboardConstellationsList.add(clipboardConstellation);
			clipboardConstellation.addClipboardListener(new ClipboardListener() {
				@Override
				public void onModelsRemovedFromClipboard(List<ModelPath> models) {
					//NOP
				}
				
				@Override
				public void onModelsInClipoboardCleared() {
					//NOP
				}
				
				@Override
				public void onModelsAddedToClipboard(List<ModelPath> models) {
					if (verticalTabPanel.getSelectedElement() != clipboardElement) {
						verticalTabPanel.updateTabElementName("*", clipboardElement);
						clipboardMarkerChanged = true;
					}
				}
			});
			
			clipboardConstellation.configureGmSession(gmSession);
			clipboardConstellation.addMasterDetailConstellationProvidedListener(masterDetailConstellation -> {
				GmContentView masterView = masterDetailConstellation.getCurrentMasterView();
				if (masterView instanceof GmInteractionSupport)
					((GmInteractionSupport) masterView).addInteractionListener(getInteractionListener());
				if (masterDetailConstellation.getDetailView() != null)
					masterDetailConstellation.getDetailView().addSelectionListener(getSelectionListener());
			});
			clipboardConstellation.setConstellationDefaultModelActions(constellationDefaultModelActionList);
			clipboardConstellation.setVisibleVerticalTabActionBar(verticalTabActionToolbarVisible);
			clipboardConstellation.prepareLayout(); 
			return clipboardConstellation;
		};
	}
	
	private GmViewActionBar getGmViewActionBar() {
		if (gmViewActionBar == null) {
			gmViewActionBar = gmViewActionBarProvider.get();
			gmViewActionBar.getView().getElement().setId("gmViewActionBar");
			gmViewActionBar.setToolBarVisible(this.actionToolBarVisible);
		}
		
		return gmViewActionBar;
	}
	
	private void handleChangesMarker(VerticalTabElement tabElement, boolean added) {
		if (verticalTabPanel.getSelectedElement() != tabElement) {
			verticalTabPanel.updateTabElementName(added ? "*" : "", tabElement);
			if (added)
				changesMarkerChanged = true;
		}
	}
	
	private WorkbenchActionContext<WorkbenchAction> prepareWorkbenchActionContext(Folder folder, Widget callerView, Map<String, Object> values) {
		return new WorkbenchActionContext<WorkbenchAction>() {
			@Override
			public GmSession getGmSession() {
				return gmSession;
			}

			@Override
			public List<ModelPath> getModelPaths() {
				return Collections.emptyList();
			}
			
			@Override
			public ModelPath getRootModelPath() {
				return ExplorerConstellation.this.getRootModelPath();
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public WorkbenchAction getWorkbenchAction() {
				return (WorkbenchAction) folder.getContent();
			}

			@Override
			public Object getPanel() {
				return callerView == null ? ExplorerConstellation.this : callerView;
			}
			
			@Override
			@SuppressWarnings("unusable-by-js")
			public Folder getFolder() {
				return folder;
			}
			
			@Override
			public Map<String, Object> getValues() {
				return values;
			}
		};
	}
	
	private ModelPath getRootModelPath() {
		return currentWidget instanceof ParentModelPathSupplier ? ((ParentModelPathSupplier) currentWidget).apply(null) : null;
	}
	
	/**
	 * Creates a new {@link VerticalTab} related to the given {@link WorkbenchActionContext}. If there is already a tab created for that {@link WorkbenchActionContext},
	 * then the existing tab is selected. Otherwise still a new tab is created.
	 */
	public Future<VerticalTabElement> maybeCreateVerticalTabElement(WorkbenchActionContext<?> workbenchActionContext, String name, String description,
			Supplier<? extends Widget> widgetSupplier, ImageResource icon, Object modelObject, boolean staticElement) {
		VerticalTabElement element = verticalTabPanel.getVerticalTabElementByWorkbenchActionContext(workbenchActionContext);
		if (element == null)
			element = verticalTabPanel.getVerticalTabElementByWidgetSupplier(widgetSupplier);
		if (element == null) {
			Future<VerticalTabElement> verticalElement = verticalTabPanel.createAndInsertVerticalTabElement(workbenchActionContext, name,
					useHorizontalTabs ? verticalTabPanel.getElementsSize() : 0, description, widgetSupplier, icon, modelObject, staticElement);
			return verticalElement;
		}
		
		Future<VerticalTabElement> future = new Future<>(element);
		verticalTabPanel.setSelectedVerticalTabElement(element);
		return future;
	}
	
	public Future<VerticalTabElement> maybeCreateVerticalTabElement(WorkbenchActionContext<?> workbenchActionContext, String name, String description,
			Supplier<? extends Widget> widgetSupplier, ImageResource icon, Object modelObject, boolean staticElement, boolean showToolBar) {
		southData.setSize(showToolBar ? southDataSize : 0);
		forceLayout();
		return maybeCreateVerticalTabElement(workbenchActionContext, name, description, widgetSupplier, icon, modelObject, staticElement);
	}
	
	public Future<VerticalTabElement> prepareValidationLog(ValidationLog validationLog, TriggerInfo triggerInfo) {
		return prepareValidationLog(validationLog, gmSession, triggerInfo);
	}

	public Future<VerticalTabElement> prepareValidationLog(ValidationLog validationLog, PersistenceGmSession gmSession) {
		return prepareValidationLog(validationLog, gmSession, null);
	}

	private Future<VerticalTabElement> prepareValidationLog(ValidationLog validationLog, PersistenceGmSession gmSession, TriggerInfo triggerInfo) {
		if (validationConstellation == null)
			return new Future<>(null);
		
		boolean useAutoCommit = false;
		if (triggerInfo != null && triggerInfo.getWidget() != null) {
			Boolean autoCommit = triggerInfo.get("AutoCommit");
			useAutoCommit = autoCommit == null ? false : autoCommit;
			
			if (useAutoCommit && triggerInfo.getWidget() instanceof PropertyPanel)
				((PropertyPanel) triggerInfo.getWidget()).handleValidationLog(validationLog, ValidationKind.fail);
		}
				
		validationConstellation.configureGmSession(gmSession);
 		ModelPath modelPath = new ModelPath();
 		modelPath.add(new RootPathElement(GMF.getTypeReflection().getType(validationLog), validationLog));
		validationConstellation.addContent(modelPath);
		return new Future<>(null);
	}
	
	
	@Override
	public Future<Object> prepareTemplateEvaluation(TemplateEvaluationContext templateEvaluationContext) throws TemplateEvaluationException {
		if (!templateEvaluationContext.getUseFormular())
			return templateEvaluationContext.evaluateTemplate();
		
		TemplateGIMADialog templateEvaluationDialog;
		templateEvaluationDialog = templateEvaluationDialogProvider.get();
		
		boolean showDialog = templateEvaluationDialog.setTemplateEvaluationContext(templateEvaluationContext, null);
		if (showDialog) {
			templateEvaluationDialog.show();
			templateEvaluationDialog.center();
		}
		
		return templateEvaluationDialog.getEvaluatedPrototype();
	}
	
	public String getUseCase() {
		return useCase;
	}
	
	public boolean isUseWorkbenchWithinTab() {
		return useWorkbenchWithinTab;
	}
	
	@Override
	public void handleInstantiationAction(InstantiationAction action, Widget callerView) {
		workbench.handleInstantiationAction(action, callerView);
	}
	
	private void removeEntityVerticalTabElement(ModelPathElement pathElement) {
		if (instantiatedEntityVerticalTabElements != null) {
			VerticalTabElement element = instantiatedEntityVerticalTabElements.get(pathElement.getValue());
			if (element != null) {
				verticalTabPanel.removeVerticalTabElement(element);
				verticalTabPanel.setSelectedVerticalTabElement(verticalTabPanel.getFirstNotStaticVerticalTabElementIfAny());
			}
		}
	}
	
	public void removeVerticalTabElement(VerticalTabElement elementToRemove){
		if (elementToRemove != null) {
			int removedIndex = verticalTabPanel.removeVerticalTabElement(elementToRemove);
			if (removedIndex != -1)
				verticalTabPanel.setSelectedVerticalTabElement(verticalTabPanel.getFirstNotStaticVerticalTabElementIfAny());
		}
	}
	
	/**
	 * Returns the west position of the BorderLayout, which contains both the {@link VerticalTabPanel} and the {@link Workbench}.
	 */
	public BorderLayoutData getWestBorderLayoutData() {
		return westData;
	}
	
	/**
	 * Removes all non static elements from the {@link VerticalTabPanel}, clears elements from the {@link ClipboardConstellation},
	 * and sets the {@link HomeConstellation} as selected.
	 */
	public void cleanup() {
		verticalTabPanel.removeAllNotStaticVerticalTabElements();
		verticalTabPanel.setSelectedVerticalTabElement(verticalTabPanel.getVerticalTabElementByWidget(homeConstellation));
		workbench.configureModelEnvironment(gmSession.getModelEnvironment());
		
		ClipboardConstellation clipboardConstellation = (ClipboardConstellation) clipboardElement.getWidgetIfSupplied();
		if (clipboardConstellation != null)
			clipboardConstellation.cleanup();
	}
	
	@Override
	public void onOpenModelPath(ModelPath modelPath) {
		onOpenModelPath(modelPath, null, null, false, false);
	}
	
	public void onOpenModelPath(ModelPath modelPath, List<ModelPathElement> workWithModelPath, ModelPathElement selectedModelPathElement,
			boolean insertAllPathElements, boolean addToCurrentView) {
		if (modelPath == null)
			return;
		
		if (!insertAllPathElements) {
			ModelPathElement modelPathElement = modelPath.last() == selectedModelPathElement ? selectedModelPathElement : null;			
			fireOpenModelPath(modelPath, workWithModelPath, modelPathElement, addToCurrentView);
			return;
		}
			
		ModelPath addModelPath = new ModelPath();
		int selectedIndex = (selectedModelPathElement != null) ? modelPath.indexOf(selectedModelPathElement) : modelPath.size() - 1;
		int i = 0;
		boolean modelPathSelected = false;
		for (ModelPathElement modelPathElement : modelPath) {
			addModelPath.add(modelPathElement.copy());				
			ModelPath newModelPath = addModelPath.copy();
			ModelPathElement newSelectedModelPathElement = selectedModelPathElement;
			if (selectedIndex >= 0 && selectedIndex < newModelPath.size()) {
				newSelectedModelPathElement = newModelPath.get(selectedIndex);
				modelPathSelected = true;
			}
			if (!modelPathSelected && modelPath.last().equals(modelPathElement))
				newSelectedModelPathElement = newModelPath.last();
			//RVE if show on new Tab and the 1st Element is not defaulty selected, we need to load him, to set BrowsingConstellation correctly
			if (!addToCurrentView && modelPath.first().equals(modelPathElement))
				newSelectedModelPathElement = newModelPath.first();					
			fireOpenModelPath(newModelPath, workWithModelPath, newSelectedModelPathElement, (addToCurrentView || i > 0));
			i++;
		}
	}

	private void fireOpenModelPath(ModelPath modelPath, List<ModelPathElement> workWithPath, ModelPathElement selectedPathElement,
			boolean addToCurrentView) {
		boolean isPrepared = false;
		ModelPathElement modelPathElement = modelPath.last();
		if (workWithPath != null && !workWithPath.isEmpty()) {
			WorkbenchAction actionToPerform = null;
			if (workWithEntityExpert != null /*&& workWithEntityExpert.checkWorkWithAvailable(modelPath, (Widget) currentContentView)*/)
				actionToPerform = workWithEntityExpert.getActionToPerform(modelPath);
			
			if (actionToPerform != null) {
				workWithEntityExpert.configureGmSession(gmSession);
				if (modelPathElement.getValue() != null && selectedPathElement != null)
					workWithEntityExpert.setSelectModelPath(modelPathElement.getValue().equals(selectedPathElement.getValue()));
				workWithEntityExpert.performAction(modelPath, actionToPerform, null, !addToCurrentView);
				isPrepared = true;
			}			   
		}
		
		if (isPrepared)
			return;
		
		if (modelPathElement.getType().isEntity()) {
			showEntityVerticalTabElement(modelPath, workWithPath, selectedPathElement, browsingConstellationProvider, false, false, addToCurrentView);
			return;
		}
		
		if (modelPathElement instanceof PropertyRelatedModelPathElement) {
			PropertyRelatedModelPathElement propertyPathElement = (PropertyRelatedModelPathElement) modelPathElement;
			GenericEntity entity = propertyPathElement.getEntity();
			String propertyName = propertyPathElement.getProperty().getName();
			Pair<String, String> nameAndDescription = GMEMetadataUtil
					.getPropertyDisplayAndDescription(propertyName, getMetaData(entity).entity(entity).property(propertyName).useCase(useCase));
			ValueDescriptionBean valueDescriptionBean = new ValueDescriptionBean(nameAndDescription.getFirst(), nameAndDescription.getSecond());
			
			if (!addToCurrentView || verticalTabPanel.getSelectedElement() == null
					|| !maybeAddTocurrentView(modelPath, workWithPath, selectedPathElement, false)) {
				maybeCreateVerticalTabElement(null, valueDescriptionBean.getValue(), valueDescriptionBean.getDescription(),
						provideBrowsingConstellation(valueDescriptionBean, modelPath, browsingConstellationProvider), null,
						modelPathElement.getValue(), false);
			}
			
			return;
		}
		
		if (addToCurrentView && verticalTabPanel.getSelectedElement() != null
				&& maybeAddTocurrentView(modelPath, workWithPath, selectedPathElement, false))
			return;
		
		onOpenModelPath(modelPath, new TabInformation() {
			@Override
			public String getTabName() {
				return GMEUtil.getTabName(modelPathElement, LocalizedText.INSTANCE.data());
			}
			
			@Override
			public ImageResource getTabIcon() {
				return null;
			}
			
			@Override
			public String getTabDescription() {
				return null;
			}
		});
	}

	private boolean maybeAddTocurrentView(ModelPath modelPath, List<ModelPathElement> workWithModelPath, ModelPathElement selectedModelPathElement,
			boolean insertAllPathElements) {
		VerticalTabElement element = verticalTabPanel.getSelectedElement();
		if (element == null || !(element.getWidget() instanceof BrowsingConstellation)) 
			return false;
		
		BrowsingConstellation browsingConstellation = (BrowsingConstellation) element.getWidget();
		browsingConstellation.setInsertOnlyLastPathElementToTether(!insertAllPathElements);
		browsingConstellation.setVisibleBrowsingConstellationActionBar(this.verticalTabActionToolbarVisible);
		browsingConstellation.onWorkWithEntity(modelPath, workWithModelPath, selectedModelPathElement, freeInstantiationUseCase, false);
		return true;
	}

	@Override
	public void onOpenModelPath(ModelPath modelPath, TabInformation tabInformation) {
		ModelPathElement last = modelPath.last();
		if (last.getType().isEntity() || last instanceof PropertyRelatedModelPathElement) {
			onOpenModelPath(modelPath);
			return;
		}
		
		ValueDescriptionBean valueDescriptionBean = new ValueDescriptionBean(tabInformation.getTabName(), tabInformation.getTabDescription());
		
		maybeCreateVerticalTabElement(null, tabInformation.getTabName(), tabInformation.getTabDescription(),
				provideBrowsingConstellation(valueDescriptionBean, modelPath, browsingConstellationProvider), tabInformation.getTabIcon(),
				last.getValue(), false);
	}
	
	/**
	 * Opens a new tab for the ServiceRequest by type within the {@link QuickAccessResult}.
	 */
	public void handleServiceRequestPanel(QuickAccessResult result) {
		EntityType<?> entityType = GMF.getTypeReflection().getEntityType(result.getObjectAndType().getType().getTypeSignature());
		ServiceRequestConstellationScope.scopeManager.pushScope(new ServiceRequestConstellationScope());
		TransientGmSession transientGmSession = transientSessionSupplier.get();
		transientGmSession.configureGmMetaModel(transientGmSession.getTransientGmMetaModel());
		handleServiceRequestPanel((ServiceRequest) transientGmSession.create(entityType), false, false, true, false);
	}
	
	/**
	 * Opens a new tab for the given {@link ServiceRequest}, and creates (or not) a new scope.
	 * Also, if configured to, it auto executes the request.
	 * autoPaging - true for using the auto paging feature
	 * pagingEditable - true for editing the paging related properties
	 */
	public void handleServiceRequestPanel(ServiceRequest request, boolean createScope, boolean autoExecute, boolean autoPaging, boolean pagingEditable) {
		String name = request.entityType().getShortName();
		if (createScope)
			ServiceRequestConstellationScope.scopeManager.pushScope(new ServiceRequestConstellationScope());
		maybeCreateVerticalTabElement(null, name, name,
				provideServiceRequestBrowsingConstellation(name, request, null, autoExecute, autoPaging, pagingEditable), null, request, false);
	}
	
	/**
	 * Opens a new tab for the ServiceRequest, based on the received templateEvaluation.
	 */
	public void handleServiceRequestPanel(TemplateEvaluationContext templateEvaluationContext, TemplateServiceRequestBasedAction action, String name,
			TemplateQueryOpener opener) {
		ServiceRequestConstellationScope.scopeManager.pushScope(new ServiceRequestConstellationScope());
		maybeCreateVerticalTabElement(null, name, name, provideServiceRequestBrowsingConstellation(templateEvaluationContext, action, name, opener), null,
				templateEvaluationContext.getTemplate(), false);
	}
	
	@Override
	public void onEntityInstantiated(InstantiationData instantiationData) {
		if (instantiationData.isShowGima()) {
			displayGIMA(instantiationData);
			return;
		}

 		ModelPathElement pathElement = instantiationData.getPathElement();
 		ModelPath modelPath = new ModelPath();
 		modelPath.add(new RootPathElement(pathElement.getType(), pathElement.getValue()));
 		showEntityVerticalTabElement(modelPath, instantiationData.isFreeInstantiation(), false, false);
	}
	
	@Override
	public void onEntityInstantiated(ModelPathElement pathElement, boolean showGima, boolean isFreeInstantiation, String namePrefix) {
		onEntityInstantiated(new InstantiationData(pathElement, showGima, isFreeInstantiation, namePrefix, false, false));
	}
	
	@Override
	public void onEntityUninstantiated(ModelPathElement pathElement) {
		removeEntityVerticalTabElement(pathElement);
	}
	
	@Override
	public void handleActionSelection(List<TemplateBasedAction> actions, Future<TemplateBasedAction> future) {
		TemplateBasedAction priorityAction = getPriorityAction(actions);
		
		List<TemplateBasedAction> actionsCopy = new ArrayList<>(actions);
		if (priorityAction != null)
			actionsCopy.retainAll(Arrays.asList(priorityAction));
		
		if (actionsCopy.size() == 1) {
			TemplateBasedAction action = actionsCopy.get(0);
			Scheduler.get().scheduleDeferred(() -> future.onSuccess(action));
			return;
		}
		
		GIMADialog gimaDialog = getGIMADialog(null, false);
		gimaDialog.setHeading(LocalizedText.INSTANCE.execute());
		gimaDialog.showForActionSelection(actionsCopy, future);
	}
	
	@Override
	public void onEditEntity(ModelPath modelPath) {
		displayGIMA(modelPath, false, null, null, null);
	}
	
	@Override
	public void onEditEntity(ModelPath modelPath, EditEntityContext editEntityContext) {
		ValidationKind validationKind = ValidationKind.info;
		if (editEntityContext != null)
			validationKind = editEntityContext.getValidationKind();
			
		displayGIMA(modelPath, false, null, null, null, validationKind);
	}	
	
	public void onEditEntity(GenericEntity entity, boolean isFreeInstantiation) {
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(entity));
		displayGIMA(modelPath, isFreeInstantiation, null, null, null);
	}
	
	public void onEditEntity(ModelPath modelPath, boolean checkPropertyPanelProperties, String useCase,
			Supplier<? extends GmEntityView> propertyPanelProvider, Supplier<GIMADialog> gimaDialogProvider) {
		displayGIMA(modelPath, false, checkPropertyPanelProperties, useCase, propertyPanelProvider, gimaDialogProvider, ValidationKind.info);
	}
	
	public void onEditEntity(ModelPath modelPath, boolean checkPropertyPanelProperties, String useCase,
			Supplier<? extends GmEntityView> propertyPanelProvider, Supplier<GIMADialog> gimaDialogProvider, ValidationKind validationKind) {
		displayGIMA(modelPath, false, checkPropertyPanelProperties, useCase, propertyPanelProvider, gimaDialogProvider, validationKind);
	}
	
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	public TransientGmSession getTransientGmSession() {
		return transientGmSession;
	}
	
	@Override
	public TransientGmSession get() {
		return getTransientGmSession();
	}
	
	@Override
	public ModelPath getContent() {
		if (currentBrowsingConstellation != null)
			return currentBrowsingConstellation.getFirstSelectedItem();
		
		return null;
	}
	
	@Override
	protected void onResize(int width, int height) {
		if (currentWidget instanceof GmContentView) {
			GmTreeView gmTreeView = getGmTreeView((GmContentView) currentWidget);
			if (gmTreeView != null) {
				gmTreeView.disableTreeViewResizeHandling();
				gmTreeView.saveScrollState();
				gmTreeView.limitAmountOfDataToRender();
			}
		}
		
		super.onResize(width, height);
	}
	
	private void configureCurrentWidget(Widget currentWidget) {
		if (this.currentWidget == currentWidget)
			return;
		
		ExtendedBorderLayoutContainer panel = useHorizontalTabs ? centerPanel : this;
		if (this.currentWidget != null) {
			if (this.currentWidget instanceof GmContentView) {
				GmTreeView gmTreeView = getGmTreeView((GmContentView) this.currentWidget);
				if (gmTreeView != null)
					gmTreeView.saveScrollState();
			}
			panel.remove(this.currentWidget);
		}
		this.currentWidget = currentWidget;
		
		if (currentWidget instanceof GmContentView) {
			GmTreeView gmTreeView = getGmTreeView((GmContentView) currentWidget);
			if (gmTreeView != null)
				gmTreeView.limitAmountOfDataToRender();
		}
		
		panel.setCenterWidget(currentWidget, centerData);
		
		if (currentBrowsingConstellation != null) {
			currentBrowsingConstellation.getTetherBar().removeTetherBarListener(getActionRefreshTetherBarListener());
			currentBrowsingConstellation = null;
		}
		
		if (currentWidget instanceof Workbench) {
			new Timer() {
				@Override
				public void run() {
					((Workbench) ExplorerConstellation.this.currentWidget).expandEntries();
				}
			}.schedule(200);
		} else if (currentWidget instanceof ClipboardConstellation) {
			((ClipboardConstellation) currentWidget).updateVerticalTabActionBar(null);
		} else if (currentWidget instanceof ChangesConstellation) {
			((ChangesConstellation) currentWidget).updateVerticalTabActionBar(null);
		} else if (currentWidget instanceof GmContentView) {
			currentBrowsingConstellation = getChildBrowsingConstellation((GmContentView) currentWidget);
			if (currentBrowsingConstellation != null) {
				currentBrowsingConstellation.getTetherBar().addTetherBarListener(getActionRefreshTetherBarListener());
				currentBrowsingConstellation.updateVerticalTabActionBar(null);
			}
						
			southData.setSize(southDataSize);
		} else if (notificationConstellation != null && notificationConstellation.equals(currentWidget)) {
			notificationConstellation.updateVerticalTabActionBar(null);
		} 
		
		panel.doLayout();
	}
	
	public BrowsingConstellation getCurrentBrowsingConstellation() {
		return currentBrowsingConstellation;
	}
	
	public Supplier<BrowsingConstellation> provideBrowsingConstellation(String name, GenericEntity queryOrTemplate) throws RuntimeException {
		return provideBrowsingConstellation(name, queryOrTemplate, null);
	}
	
	public Supplier<BrowsingConstellation> provideBrowsingConstellation(String name, GenericEntity queryOrTemplate,
			WorkbenchActionContext<TemplateQueryAction> context) {
		return () -> {
			BrowsingConstellation browsingConstellation = browsingConstellationProvider.get();
			browsingConstellation.configureGmSession(gmSession);
			browsingConstellation.setVisibleBrowsingConstellationActionBar(this.verticalTabActionToolbarVisible);

			browsingConstellation.getTetherBar().addTetherBarListener(getTetherBarListener(browsingConstellation));
			maybeCreateQueryTetherBarElement(name, queryOrTemplate, context, browsingConstellation, true);
			
			return browsingConstellation;
		};
	}
	
	public void maybeCreateQueryTetherBarElement(String name, GenericEntity queryOrTemplate, WorkbenchActionContext<TemplateQueryAction> context,
			BrowsingConstellation browsingConstellation) {
		maybeCreateQueryTetherBarElement(name, queryOrTemplate, context, browsingConstellation, false);
	}
	
	private void maybeCreateQueryTetherBarElement(String name, GenericEntity queryOrTemplate, WorkbenchActionContext<TemplateQueryAction> context,
			BrowsingConstellation browsingConstellation, boolean handlingNewTab) {
		TetherBarElement tetherBarElement = null;
		Boolean select = canSelectTetherElement(context);
			
		ModelPath modelPath = context == null || context.getModelPaths() == null || context.getModelPaths().isEmpty() ? null
				: context.getModelPaths().get(0);
		TetherBar tetherBar = browsingConstellation.getTetherBar();
		if (modelPath != null)
			tetherBarElement = tetherBar.getTetherBarElementByModelPathElement(modelPath.last());
		if (tetherBarElement != null) {			
			if (select)
				tetherBar.setSelectedThetherBarElement(tetherBarElement);
			return;
		}
		
		int selectedIndex = tetherBar.getSelectedElementIndex();
		if (tetherBar.getElementsSize() > selectedIndex + 1) {
			List<TetherBarElement> elementsToRemove = new ArrayList<>();
			for (int i = tetherBar.getSelectedElementIndex() + 1; i < tetherBar.getElementsSize(); i++)
				elementsToRemove.add(tetherBar.getElementAt(i));
			tetherBar.removeTetherBarElements(elementsToRemove);
		}
		
		Query query = (Query) (queryOrTemplate instanceof Template ? ((Template) queryOrTemplate).getPrototype() : queryOrTemplate);
		String entityTypeSignature = null;
		if (query instanceof EntityQuery)
			entityTypeSignature = ((EntityQuery) query).getEntityTypeSignature();
		else if (query instanceof SelectQuery)
			entityTypeSignature = GMEUtil.getSingleEntityTypeSignatureFromSelectQuery((SelectQuery) query);
		
		String preferredUseCase = null; //if we are opening a new tab, then the context view is not taken into consideration
		if (!handlingNewTab && context != null && context.getPanel() instanceof GmContentView)
			preferredUseCase = ((GmContentView) context.getPanel()).getUseCase();
		
		preferredUseCase = GMEMetadataUtil.getSpecialDefaultView(preferredUseCase, queryOrTemplate, entityTypeSignature, gmSession);
		
		if ((query instanceof EntityQuery || query instanceof SelectQuery) && query.getTraversingCriterion() == null && entityTypeSignature != null) {
			//It seems having a TC for condensed properties is not required. We disabled this for now.
			//TraversingCriterion tc = prepareCondensedTraversingCriterion(GMF.getTypeReflection().getEntityType(entityTypeSignature));
			TraversingCriterion tc = GMETraversingCriterionUtil.prepareForDepthTC(null, queryOrTemplate, entityTypeSignature, gmSession, preferredUseCase);
			if (tc != null)
				query.setTraversingCriterion(tc);
		}
		
		EntityType<?> queryEntityType = (query != null ? query.entityType() : Query.T);
		
		List<GmContentViewContext> possibleContentViews = viewSituationResolverSupplier.get()
				.getPossibleContentViews(new EntryPointPathElement(queryEntityType, query));
		
		Supplier<GmEntityView> entityViewProvider = null;
		if (!possibleContentViews.isEmpty()) {
			GmContentViewContext providerAndName = possibleContentViews.get(0);
			entityViewProvider = (Supplier<GmEntityView>) providerAndName.getContentViewProvider();
		}
		
		DetailsViewMode detailsViewMode = GMEMetadataUtil.getDetailsViewMode(queryOrTemplate, entityTypeSignature, gmSession);
		
		tetherBarElement = new TetherBarElement(modelPath, name, name, getEntityViewProvider(entityViewProvider, queryEntityType, queryOrTemplate,
				false, context, preferredUseCase, detailsViewMode, queryOrTemplate instanceof Template, false, null, false, false));
		
		browsingConstellation.getTetherBar().insertTetherBarElement(browsingConstellation.getTetherBar().getElementsSize(), tetherBarElement);
		if (select)
			browsingConstellation.getTetherBar().setSelectedThetherBarElement(tetherBarElement);
	}

	private boolean canSelectTetherElement(WorkbenchActionContext<?> context) {
		boolean select = true;
		if (workWithEntityExpert != null && workWithEntityExpert.getCurrentWorkbenchContext() != null
				&& workWithEntityExpert.getCurrentWorkbenchContext().equals(context)) {
			select = workWithEntityExpert.getSelectModelPath();
		}
		
		return select;
	}
	
	private Supplier<BrowsingConstellation> provideServiceRequestBrowsingConstellation(String name, GenericEntity serviceRequest,
			TemplateQueryOpener opener, boolean autoExecute, boolean autoPaging, boolean pagingEditable) {
		return () -> {
			BrowsingConstellation browsingConstellation = browsingConstellationProvider.get();
			browsingConstellation.configureGmSession(transientGmSession);
			browsingConstellation.setVisibleBrowsingConstellationActionBar(this.verticalTabActionToolbarVisible);
			
			TetherBarElement element = new TetherBarElement(null, name, name, getEntityViewProvider(serviceRequestConstellationProvider,
					serviceRequest.entityType(), serviceRequest, true, null, null, null, false, autoExecute, opener, autoPaging, pagingEditable));
			TetherBar tetherBar = browsingConstellation.getTetherBar();
			tetherBar.insertTetherBarElement(0, element);
			tetherBar.addTetherBarListener(getTetherBarListener(browsingConstellation));
			tetherBar.setSelectedThetherBarElement(element);
			
			return browsingConstellation;
		};
	}
	
	private Supplier<BrowsingConstellation> provideServiceRequestBrowsingConstellation(TemplateEvaluationContext templateEvaluationContext,
			TemplateServiceRequestBasedAction action, String name, TemplateQueryOpener opener) {
		return () -> {
			BrowsingConstellation browsingConstellation = browsingConstellationProvider.get();
			browsingConstellation.configureGmSession(transientGmSession);
			browsingConstellation.setVisibleBrowsingConstellationActionBar(this.verticalTabActionToolbarVisible);
			
			TetherBarElement element = new TetherBarElement(null, name, name,
					getServiceRequestConstellationProvider(templateEvaluationContext, action, opener));
			TetherBar tetherBar = browsingConstellation.getTetherBar();
			tetherBar.insertTetherBarElement(0, element);
			tetherBar.addTetherBarListener(getTetherBarListener(browsingConstellation));
			tetherBar.setSelectedThetherBarElement(element);
			
			return browsingConstellation;
		};
	}
	
	protected Supplier<BrowsingConstellation> provideBrowsingConstellation(ValueDescriptionBean valueDescriptionBean, ModelPath modelPath,
			Supplier<BrowsingConstellation> browsingConstellationProvider) {
		return provideBrowsingConstellation(valueDescriptionBean, modelPath, null, null, browsingConstellationProvider, false, false, false);
	}
	
	protected Supplier<BrowsingConstellation> provideBrowsingConstellation(ValueDescriptionBean valueDescriptionBean, ModelPath modelPath,
			List<ModelPathElement> workWithModelPath, ModelPathElement selectedModelPathElement,
			Supplier<BrowsingConstellation> browsingConstellationProvider, boolean useTransientSession, boolean isFreeInstantiation,
			boolean insertAllPathElements) {
		return () -> {
			Supplier<BrowsingConstellation> finalBrowsingSupplier;
			if (browsingConstellationProvider == null)
				finalBrowsingSupplier = this.browsingConstellationProvider;
			else
				finalBrowsingSupplier = browsingConstellationProvider;
			
			BrowsingConstellation browsingConstellation = finalBrowsingSupplier.get();
			browsingConstellation.configureGmSession(useTransientSession ? transientGmSession : gmSession);
			browsingConstellation.setInsertOnlyLastPathElementToTether(!insertAllPathElements);
			
			browsingConstellation.setVisibleBrowsingConstellationActionBar(this.verticalTabActionToolbarVisible);
			
			String elementName = valueDescriptionBean.getValue();
			if (isFreeInstantiation)
				elementName = LocalizedText.INSTANCE.newEntry() + " <" + elementName +  " >";
			
			browsingConstellation.onWorkWithEntity(modelPath, workWithModelPath, selectedModelPathElement, freeInstantiationUseCase, false);
			
			return browsingConstellation;
		};
	}
	
	private TetherBarListener getTetherBarListener(final BrowsingConstellation browsingConstellation) { //TODO: can this be a singleton?
		return new TetherBarListener() {
			@Override
			public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
				VerticalTabElement element = verticalTabPanel.getVerticalTabElementByWidget(browsingConstellation);
				if (element != null) {
					element.setName(tetherBarElement.getName());
					verticalTabPanel.refresh();
					verticalTabPanel.setSelectedVerticalTabElement(element);
					prepareActionsForView(element.getWidget(), true);
				}
			}
			
			@Override
			public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
				//NOP
			}
			
			@Override
			public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
				//NOP
			}
		};
	}
	
	private Supplier<GmEntityView> getEntityViewProvider(final Supplier<? extends GmEntityView> originalProvider, final EntityType<?> entityType,
			final GenericEntity entity, boolean useTransient, WorkbenchActionContext<TemplateQueryAction> context, String preferredUseCase,
			DetailsViewMode detailsViewMode, boolean useSpecialTC, boolean autoExecute, TemplateQueryOpener opener, boolean autoPaging, boolean pagingEditable) {
		return new Supplier<GmEntityView>() {
			@Override
			public GmEntityView get() throws RuntimeException {
				GmEntityView entityView = originalProvider.get();
				if (preferredUseCase != null)
					entityView.configureUseCase(preferredUseCase);
				entityView.configureGmSession(useTransient ? transientGmSession : gmSession);
				
				if (entityView instanceof ServiceRequestAutoPagingView)
					((ServiceRequestAutoPagingView) entityView).configureAutoPaging(autoPaging, pagingEditable);
				
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(entityType, entity));
				entityView.setContent(modelPath);
				
				if (entityView instanceof ReloadableGmView && autoExecute)
					((ReloadableGmView) entityView).reloadGmView();
				
				if (entityView instanceof TemplateQueryOpenerView)
					((TemplateQueryOpenerView) entityView).configureTemplateQueryOpener(opener);
				
				if (!(entityView instanceof QueryConstellation))
					return entityView;
				
				QueryConstellation queryConstellation = (QueryConstellation) entityView;
				if (!useSpecialTC)
					queryConstellation.setSpecialEntityTraversingCriterionMap(null);
				
				if (detailsViewMode != null) {
					GmContentView view = queryConstellation.getView();
					if (view instanceof MasterDetailConstellation) {
						MasterDetailConstellation mdc = (MasterDetailConstellation) view;
						DetailsViewModeOption viewModeOption = detailsViewMode.getDetailsViewModeOption();
						mdc.setShowDetailViewCollapsed(!DetailsViewModeOption.visible.equals(viewModeOption) && viewModeOption != null);
						if (DetailsViewModeOption.forcedHidden.equals(viewModeOption))
							mdc.setShowDetailView(false);
					}
				}
				
				if (queryConstellation.isFormAvailable() && context != null && context.getWorkbenchAction().getForceFormular())
					Scheduler.get().scheduleDeferred(queryConstellation::showForm);
				else
					queryConstellation.performSearch();
				
				return entityView;
			}
		};
	}
	
	private Supplier<GmEntityView> getServiceRequestConstellationProvider(TemplateEvaluationContext templateEvaluationContext,
			TemplateServiceRequestBasedAction action, TemplateQueryOpener opener) {
		return new Supplier<GmEntityView>() {
			@Override
			public GmEntityView get() {
				ServiceRequestExecutionConstellation serviceRequestExecutionConstellation = (ServiceRequestExecutionConstellation) serviceRequestConstellationProvider
						.get();
				serviceRequestExecutionConstellation.configureGmSession(transientGmSession);
				serviceRequestExecutionConstellation.setTemplateEvaluationContext(templateEvaluationContext, action);
				serviceRequestExecutionConstellation.configureTemplateQueryOpener(opener);
				return serviceRequestExecutionConstellation;
			}
		};
	}
	
	protected void showEntityVerticalTabElement(ModelPath modelPath, boolean isFreeInstantiation, boolean insertAllPathElements,
			boolean addToCurrentView) {
		showEntityVerticalTabElement(modelPath, null, null, browsingConstellationProvider, isFreeInstantiation, insertAllPathElements,
				addToCurrentView);
	}

	public void showEntityVerticalTabElement(ModelPath modelPath, Supplier<BrowsingConstellation> browsingConstellationProvider,
			boolean isFreeInstantiation, boolean insertAllPathElements) {
		showEntityVerticalTabElement(modelPath, null, null, browsingConstellationProvider, isFreeInstantiation, insertAllPathElements, false);
	}	
	
	/**
	 * Creates (or selects) a vertical tab element with the value from the modelPath.
	 * It uses the given browsingConstellationProvider (or the default if null).
	 */
	public void showEntityVerticalTabElement(ModelPath modelPath, List<ModelPathElement> workWithModelPath, ModelPathElement selectedModelPathElement,
			Supplier<BrowsingConstellation> browsingConstellationProvider, boolean isFreeInstantiation, boolean insertAllPathElements,
			boolean addToCurrentView) {
		VerticalTabElement tabElement = null;
		final ModelPathElement lastElement = modelPath.last();
		GenericEntity entity = lastElement.getValue();
		if (instantiatedEntityVerticalTabElements != null)
			tabElement = instantiatedEntityVerticalTabElements.get(entity);
		
		if (tabElement != null) {
			if (!verticalTabPanel.containsVerticalTabElement(tabElement))
				verticalTabPanel.insertVerticalTabElement(tabElement, useHorizontalTabs ? verticalTabPanel.getElementsSize() : 0);
			verticalTabPanel.setSelectedVerticalTabElement(tabElement);
			return;
		}
		
		boolean useTransientSession = false;
		if (entity != null && entity.session() instanceof TransientPersistenceGmSession)
			useTransientSession = true;
		
		PersistenceGmSession theSession = useTransientSession ? transientGmSession : gmSession;
		ValueDescriptionBean valueDescriptionBean = getValueDescriptionBean(entity, (EntityType<?>) lastElement.getType());
		EntityMdResolver entityContextBuilder;
		if (entity != null)
			entityContextBuilder = getMetaData(entity).entity(entity);
		else
			entityContextBuilder = theSession.getModelAccessory().getMetaData().entityType((EntityType<?>) lastElement.getType());
		
		Icon entityIcon = entityContextBuilder.useCase(useCase).meta(Icon.T).exclusive();
		ImageResource icon = null;
		if (entityIcon == null || entityIcon.getIcon() == null)
			icon = ConstellationResources.INSTANCE.entity();
		else {
			Resource resource = GMEIconUtil.getSmallImageFromIcon(entityIcon.getIcon());
			icon = new GmImageResource(resource, theSession.getModelAccessory().getModelSession().resources().url(resource).asString());
		}
		
		// In case we are showing a JsUxComponent, then we must use the normal session, and not the transient one. If in
		// the future those external views need to decide whether to use the transient or not, then some metadata must
		// be created for this decision
		ViewWithJsUxComponent viewWithJsUxComponent = entityContextBuilder.useCase(useCase).meta(ViewWithJsUxComponent.T).exclusive();
		if (viewWithJsUxComponent != null)
			useTransientSession = false;
			
		if (addToCurrentView && verticalTabPanel.getSelectedElement() != null
				&& maybeAddTocurrentView(modelPath, workWithModelPath, selectedModelPathElement, insertAllPathElements)) {
			return;
		}
		
		Supplier<BrowsingConstellation> browsingConstellationSupplier = provideBrowsingConstellation(valueDescriptionBean, modelPath,
				workWithModelPath, selectedModelPathElement, browsingConstellationProvider, useTransientSession, isFreeInstantiation,
				insertAllPathElements);
		
		maybeCreateVerticalTabElement(null, valueDescriptionBean.getValue(), valueDescriptionBean.getDescription(), browsingConstellationSupplier,
				icon, entity, false).onError(e -> {
					if (e != null)
						return;
					
					GenericEntity theEntity = lastElement.getValue();
					if (theEntity instanceof EnhancedEntity) {
						GmSession session = ((EnhancedEntity) theEntity).session();
						if (session instanceof PersistenceGmSession)
							UndoAction.undoManipulation((PersistenceGmSession) session);
					}
				}).andThen(verticalTabElement -> {
					if (verticalTabElement != null) {
						if (instantiatedEntityVerticalTabElements == null)
							instantiatedEntityVerticalTabElements = new HashMap<>();
						instantiatedEntityVerticalTabElements.put(entity, verticalTabElement);
					}
				});
	}
	
	private ValueDescriptionBean getValueDescriptionBean(GenericEntity entity, EntityType<?> entityType) {
		ModelMdResolver modelMdResolver;
		if (entity != null)
			modelMdResolver = getMetaData(entity);
		else
			modelMdResolver = gmSession.getModelAccessory().getMetaData();
		
		String selectiveInformation = SelectiveInformationResolver.resolve(entityType, entity, modelMdResolver, useCase/*, null*/);
		String displayInfo = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, modelMdResolver, useCase);
		String value;
		if (selectiveInformation != null && !selectiveInformation.trim().isEmpty())
			value = selectiveInformation;
		else
			value = displayInfo;
		
		return new ValueDescriptionBean(value, displayInfo);
	}
	
	private void displayGIMA(InstantiationData instantiationData) {
		GIMADialog gimaDialog;
		if (instantiationData.getInstantiationActions() != null && !instantiationData.getInstantiationActions().isEmpty())
			gimaDialog = getTemplateGIMADialog();
		else
			gimaDialog = getGIMADialog(instantiationData.getParentWidget(), instantiationData.isTransient());
		
		Future<GenericEntity> future = new Future<>();
		gimaDialog.showForInstantiation(instantiationData, future);
		future.onError(Throwable::printStackTrace).andThen(entity -> {
			boolean isFreeInstantiation = instantiationData.isFreeInstantiation();
			if (isFreeInstantiation && entity != null) {
				gmSession.listeners().entity(entity).add(ExplorerConstellation.this);
				instantiatedEntitiesWithTab.add(entity);
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(entity.entityType(), entity));
				showEntityVerticalTabElement(modelPath, isFreeInstantiation, false, false);
			}
		});
	}
	
	private GIMADialog getGIMADialog(Widget view, boolean isTransient) {
		if (view == null) {
			if (isTransient)
				return transientGimaDialogProvider.get();
			return  gimaDialogProvider.get();
		}

 		if (view instanceof GIMADialog)
			return (GIMADialog) view;

 		return getGIMADialog(view.getParent(), isTransient);
	}
	
	private TemplateGIMADialog getTemplateGIMADialog() {
		return templateGimaDialogSupplier.get();
	}
	
	private void displayGIMA(ModelPath modelPath, boolean isFreeInstantiation, String useCase, Supplier<? extends GmEntityView> propertyPanelProvider,
			Supplier<GIMADialog> gimaDialogProvider) {
		displayGIMA(modelPath, isFreeInstantiation, useCase, propertyPanelProvider, gimaDialogProvider, ValidationKind.info);
	}

	private void displayGIMA(ModelPath modelPath, boolean isFreeInstantiation, String useCase, Supplier<? extends GmEntityView> propertyPanelProvider,
			Supplier<GIMADialog> gimaDialogProvider, ValidationKind validationKind) {
		displayGIMA(modelPath, isFreeInstantiation, isFreeInstantiation, useCase, propertyPanelProvider, gimaDialogProvider, validationKind);
	}
	
	private void displayGIMA(ModelPath modelPath, boolean isFreeInstantiation, boolean checkPanelProperties, String useCase,
			Supplier<? extends GmEntityView> panelProvider, Supplier<GIMADialog> gimaDialogProvider, ValidationKind validationKind) {
		EntityType<?> entityType = (EntityType<?>) modelPath.last().getType();
		FieldDialogOpenerAction<GenericEntity> fieldDialogOpenerAction = getFieldDialogOpenerAction(entityType);
		
		final boolean transientInstantiation;
		Object value = modelPath.last().getValue();
		TransientGmSession theTransientSession;
		PersistenceGmSession mdSession = null;
		if (value instanceof GenericEntity) {
			GmSession theSession = ((GenericEntity) value).session();
			transientInstantiation = theSession instanceof TransientPersistenceGmSession;
			if (transientInstantiation && transientGmSession != theSession)
				theTransientSession = (TransientGmSession) theSession;
			else
				theTransientSession = transientGmSession;
			if (theSession instanceof PersistenceGmSession)
				mdSession = (PersistenceGmSession) theSession;
		} else {
			transientInstantiation = false;
			theTransientSession = transientGmSession;
		}
		
		if (mdSession == null)
			mdSession = gmSession;
		
		if (fieldDialogOpenerAction != null) {
			fieldDialogOpenerAction.configureGmSession(transientInstantiation ? theTransientSession : gmSession);
			fieldDialogOpenerAction.configureEntityValue((GenericEntity) value);
			fieldDialogOpenerAction.setIsFreeInstantiation(isFreeInstantiation);
			fieldDialogOpenerAction.perform(null);
			if (isFreeInstantiation)
				showEntityVerticalTabElement(modelPath, isFreeInstantiation, false, false);
			return;
		}
			
		GIMADialog gimaDialog = gimaDialogProvider != null ? gimaDialogProvider.get() : this.gimaDialogProvider.get();
		if (useCase != null)
			gimaDialog.setUseCase(useCase);
		if (transientInstantiation)
			gimaDialog.setGmSession(theTransientSession);
		gimaDialog.setHeading(LocalizedText.INSTANCE.viewAndEdit(
				GMEMetadataUtil.getEntityNameMDOrShortName(entityType, mdSession.getModelAccessory().getMetaData().lenient(true), useCase)));
		
		gimaDialog.showForModelPathElement(modelPath, checkPanelProperties, panelProvider).onError(Throwable::printStackTrace).andThen(result -> {
			if (isFreeInstantiation && result) {
				handleAutoCommit(transientInstantiation);
				showEntityVerticalTabElement(modelPath, isFreeInstantiation, false, false);
			} else if (isFreeInstantiation && !result) {
				try {
					PersistenceGmSession theSession = transientInstantiation ? theTransientSession : gmSession;
					theSession.getTransaction().undo(1);
				} catch (TransactionException e) {
					ErrorDialog.show(LocalizedText.INSTANCE.errorUndoingInstantiation(), e);
					e.printStackTrace();
				}
			} else if (result)
				handleAutoCommit(transientInstantiation);
		});
		
		if (!validationKind.equals(ValidationKind.none))
			gimaDialog.performValidation(validationKind);
		else
			gimaDialog.setIgnoreValidation(true);
	}
	
	private void handleAutoCommit(boolean transientInstantiation) {
		if (autoCommit && commitActionSupplier != null && !transientInstantiation) {
			Action commitAction = commitActionSupplier.get();
			if (commitAction.getEnabled()) {
				TriggerInfo triggerInfo = new TriggerInfo();
				triggerInfo.put("AutoCommit", true);
				commitAction.perform(triggerInfo);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private FieldDialogOpenerAction<GenericEntity> getFieldDialogOpenerAction(EntityType<?> entityType) {
		if (fieldDialogOpenerActions == null)
			return null;
		
		for (Map.Entry<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>> entry : fieldDialogOpenerActions.entrySet()) {
			if (entry.getKey().isAssignableFrom(entityType)) {
				Supplier<? extends FieldDialogOpenerAction<?>> supplier = entry.getValue();
				return supplier == null ? null : (FieldDialogOpenerAction) supplier.get();
			}
		}
		
		return null;
	}

	@Override
	public void onBeforePerformAction(ActionPerformanceContext actionPerformanceContext) {
		GlobalState.mask(actionPerformanceContext.getMessage());
	}

	@Override
	public void onAfterPerformAction(ActionPerformanceContext actionPerformanceContext) {
		if (actionPerformanceContext != null && actionPerformanceContext.getParentWidget() instanceof Widget) {
			verticalTabPanel.markVerticalTabElementAsReloadPending((Widget) actionPerformanceContext.getParentWidget());
			return;
		}
		
		GlobalState.unmask();
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (instantiatedEntitiesWithTab.isEmpty()
				|| (!(manipulation instanceof DeleteManipulation) && !(manipulation instanceof InstantiationManipulation)))
			return;

 		GenericEntity entity = manipulation instanceof DeleteManipulation ? ((DeleteManipulation) manipulation).getEntity()
				: ((InstantiationManipulation) manipulation).getEntity();

 		if (instantiatedEntitiesWithTab.contains(entity)) {
			RootPathElement rootPathElement = new RootPathElement(entity.entityType(), entity);
			if (manipulation instanceof DeleteManipulation)
				onEntityUninstantiated(rootPathElement);
			else
				onEntityInstantiated(new InstantiationData(rootPathElement, false, false, null, false, false));
		}
	}
	
	private GmTreeView getGmTreeView(GmContentView gmContentView) {
		if (gmContentView instanceof GmTreeView)
			return (GmTreeView) gmContentView;
		
		if (gmContentView instanceof BrowsingConstellation)
			return getGmTreeView(((BrowsingConstellation) gmContentView).getCurrentContentView());
		
		if (gmContentView instanceof QueryConstellation)
			return getGmTreeView(((QueryConstellation) gmContentView).getView());
		
		if (gmContentView instanceof MasterDetailConstellation)
			return getGmTreeView(((MasterDetailConstellation) gmContentView).getCurrentMasterView());
		
		return null;
	}
	
	private BrowsingConstellation getChildBrowsingConstellation(GmContentView gmContentView) {
		if (gmContentView instanceof BrowsingConstellation)
			return (BrowsingConstellation) gmContentView;
		
		return null;
	}
	
	private void prepareActionsForView(Object view, boolean doLayout) {
		if (useToolBar) {
			if (!isViewAnActionProvider(view))
				getGmViewActionBar().prepareActionsForView(null);
			else {
				if (!(view instanceof GmExternalViewInitializationSupport) || (view instanceof GmContentView && ((GmContentView) view).isViewReady()))
					getGmViewActionBar().prepareActionsForView((GmViewActionProvider) view);
				else {
					getGmViewActionBar().prepareActionsForView(null);
					GmExternalViewInitializationListener listener = new GmExternalViewInitializationListener() {
						@Override
						public void onExternalViewInitialized(GmExternalViewInitializationSupport instantiatedSupport) {
							((GmExternalViewInitializationSupport) view).removeInitializationListener(this);
							getGmViewActionBar().prepareActionsForView((GmViewActionProvider) view);
						}
					};
					
					((GmExternalViewInitializationSupport) view).addInitializationListener(listener);
				}
			}
		}
		
		if (view instanceof GmContentView && doLayout) {
			southData.setSize(southDataSize);
			forceLayout();
		}
	}
	
	private boolean isViewAnActionProvider(Object view) {
		if (!(view instanceof GmViewActionProvider))
			return false;
		
		if (view instanceof MasterDetailConstellation)
			return ((MasterDetailConstellation) view).getCurrentMasterView() instanceof GmViewActionProvider;
		
		return true;
	}
	
	private TetherBarListener getActionRefreshTetherBarListener() {
		if (actionRefreshTetherBarListener != null)
			return actionRefreshTetherBarListener;
		
		actionRefreshTetherBarListener = new TetherBarListener() {
			@Override
			public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
				GmContentView view = tetherBarElement.getContentViewIfProvided();
				if (view instanceof Widget)
					prepareActionsForView(view, true);
				else
					prepareActionsForView(null, true);
				
				verticalTabPanel.updateTabElementName(tetherBarElement.getName(), verticalTabPanel.getSelectedElement());
			}
			
			@Override
			public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
				//NOP
			}
			
			@Override
			public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
				//NOP
			}
		};
		
		return actionRefreshTetherBarListener;
	}
	
	/*private TraversingCriterion prepareCondensedTraversingCriterion(EntityType<?> queriedEntityType) {
		GMEMetadataUtil.CondensationBean bean = GMEMetadataUtil.getEntityCondensationProperty(
				GMEMetadataUtil.getEntityCondensations(null, queriedEntityType, gmSession.getModelAccessory().getMetaData(), useCase), false);
		if (bean == null || (!(bean.getMode().equals(CondensationMode.auto)) && !(bean.getMode().equals(CondensationMode.forced))))
			return null;
		
		TraversingCriterion tc = TC.create()
			    .conjunction()
			     .property()
			     .typeCondition(or(isKind(TypeKind.collectionType), isKind(TypeKind.entityType)))
			     .negation()
			      .disjunction()
			       .pattern()
			        .entity(queriedEntityType)
			        .property(bean.getProperty())
			       .close()
			       .propertyType(LocalizedString.class)
			       .pattern()
			        .entity(LocalizedString.class)
			        .property("localizedValues")
			       .close()
			      .close()
			    .close()
			   .done();
		
		return GMEUtil.expandTc(tc);
	}*/
	
	@Override
	public void mask() {
		super.mask();
		Widget parent = getParent();
		if (parent instanceof CustomizationConstellation)
			((CustomizationConstellation) parent).disableUI();
	}
	
	@Override
	public void unmask() {
		super.unmask();
		Widget parent = getParent();
		if (parent instanceof CustomizationConstellation)
			((CustomizationConstellation) parent).enableUI();
	}

	public void showValidations() {
		verticalTabPanel.setSelectedVerticalTabElement(validationElement);
	}

	public void clearValidationLog() {
		if (validationConstellation != null)
			validationConstellation.clearValidationLog();
	}	
}
