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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationCss;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.templateevaluation.client.TemplateEvaluationPreparer;
import com.braintribe.gwt.gme.templateevaluation.client.TemplateGIMADialog;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar.TetherBarListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.action.client.SpotlightPanelListenerAdapter;
import com.braintribe.gwt.gmview.client.ExpertUI;
import com.braintribe.gwt.gmview.client.ExpertUIContext;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.GmTreeView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.client.SelectionTabConfig;
import com.braintribe.gwt.gmview.client.parse.ParserWithPossibleValues;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMTypeInstanceBean;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedColumnHeader;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedTextButton;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.NoBorderSplitButton;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesResources;
import com.braintribe.gwt.gxt.gxtresources.gridwithoutlines.client.GridWithoutLinesAppearance.GridWithoutLinesStyle;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.QueryAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.LayoutRegion;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.SplitButton;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class SelectionConstellation extends BorderLayoutContainer implements InitializableBean, TemplateEvaluationPreparer, DisposableBean {
	
	private static final float EAST_PANEL_SIZE = 400;
	private static final double NORTH_PANEL_SIZE = 30;
	
	static {
		ConstellationResources.INSTANCE.css().ensureInjected();
	}
	
	//private Workbench workbench;
	private VerticalTabPanel verticalTabPanel;
	private final BorderLayoutData northData = new BorderLayoutData(NORTH_PANEL_SIZE);
	private Supplier<HomeConstellation> homeConstellationSupplier;
	private HomeConstellation homeConstellation;
	private Supplier<ChangesConstellation> changesConstellationSupplier;
	private ChangesConstellation changesConstellation;
	private Function<Supplier<MasterDetailConstellation>, Supplier<ClipboardConstellation>> clipboardConstellationProvider;
	private ClipboardConstellation clipboardConstellation;
	private Widget currentWidget;
	private final BorderLayoutData centerData = new BorderLayoutData();
	private List<VerticalTabElement> initialQueryTabElements;
	private List<BrowsingConstellation> initialSelectionBrowsingConstellations;
	private List<QueryConstellation> initialSelectionQueryConstellations;
	private List<VerticalTabElement> queryTabElements;
	private List<BrowsingConstellation> selectionBrowsingConstellations;
	private List<QueryConstellation> selectionQueryConstellations;
	private List<SelectionConstellationListener> selectionConstellationListeners;
	private ContentPanel eastRegionPanel;
	private BorderLayoutContainer eastRegionPanelContainer;
	private ContentPanel selectionPanel;
	private String useCase;
	private Grid<GMTypeInstanceBean> selectionGrid;
	private TextButton moveUpButton;
	private TextButton moveDownButton;
	private TextButton deleteItemButton;
	private Supplier<BrowsingConstellation> browsingConstellationProvider;
	private Supplier<QueryConstellation> queryConstellationProvider;
	private Supplier<MasterDetailConstellation> masterDetailConstellationProvider;
	private Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	private SpotlightPanel quickAccessPanel;
	private Supplier<? extends TemplateGIMADialog> templateEvaluationDialogProvider;
	private SelectionConfig selectionConfig;
	private CodecRegistry<String> codecRegistry;
	private boolean isSelectionShown = false;
	private boolean isEastExpanded = true;
	private Supplier<? extends GmEntityView> detailPanelProvider;
	private GmEntityView detailPanel;
	private GmSelectionListener gmSelectionListener;
	private boolean refreshQueryConstellation = false;
	protected boolean requeryNeededAfterHide = false;
	private final Map<ExpertUI<?>, VerticalTabElement> openExpertUIs = new HashMap<>();
	private PersistenceGmSession dataSession;
	private TransientGmSession transientGmSession;
	private VerticalTabElement homeElement;
	private VerticalTabElement changesElement;
	private VerticalTabElement clipboardElement;
	//private VerticalTabElement workbenchElement;
	private boolean configuredAsNonReferenceable;
	private PersistenceGmSession theSession;
	private Supplier<ParserWithPossibleValues> parserWithPossibleValuesSupplier;
	private SplitButton createNewButton;
	private BorderLayoutContainer northRegionPanelContainer;
	private ModelPath currentDetailModelPath;
	private boolean detailPanelVisible;
	private BorderLayoutContainer centerRegionPanelContainer;
	private Map<String, Supplier<? extends ExpertUI<?>>> expertUISupplierMap;
	private Map<String, ExpertUI<?>> expertUIMap;
	private boolean hasMultipleCreateNewOptions;
	private VerticalTabElement quickAccessElement;
	private Set<MasterDetailConstellationProvidedListener> changesMasterDetailListeners;
	private Set<MasterDetailConstellationProvidedListener> clipboardMasterDetailListeners;
	private boolean homeHidden;
	
	public SelectionConstellation() {
		setBorders(false);
		getElement().addClassName(ConstellationResources.INSTANCE.css().explorerConstellationCenterBackground());
		centerData.setMargins(new Margins(8, 0, 0, 4));
	}
	
	/*
	 * Configures the required Workbench.
	 *
	@Required
	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}*/
	
	/**
	 * Configures the required {@link VerticalTabPanel}.
	 */
	@Required
	public void setVerticalTabPanel(VerticalTabPanel verticalTabPanel) {
		this.verticalTabPanel = verticalTabPanel;
		verticalTabPanel.setParentUseCase("$selection");
		verticalTabPanel.addVerticalTabListener(new VerticalTabListener() {
			@Override
			public void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement, VerticalTabElement verticalTabElement) {
				if (verticalTabElement != null)
					configureCurrentWidget(verticalTabElement.getWidget());
			}
			
			@Override
			public void onVerticalTabElementAddedOrRemoved(int elements, boolean added, List<VerticalTabElement> verticalTabElements) {
				//NOP
			}
			
			@Override
			public void onHeightChanged(int newHeight) {
				//NOP
			}
		});
	}
	
	/**
	 * Configures the required {@link HomeConstellation}, which is the default center widget,
	 * and it is used as an static element in the {@link VerticalTabPanel}.
	 */
	@Required
	public void setHomeConstellation(Supplier<HomeConstellation> homeConstellationSupplier) {
		this.homeConstellationSupplier = homeConstellationSupplier;
	}
	
	/**
	 * Configures the required {@link ChangesConstellation}, which is used as an static element in the {@link VerticalTabPanel}.
	 */
	@Required
	public void setChangesConstellation(Supplier<ChangesConstellation> changesConstellationSupplier) {
		this.changesConstellationSupplier = changesConstellationSupplier;
	}
	
	/**
	 * Configures the required useCase where this panel is being used on.
	 */
	@Required
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	/**
	 * Configures the required {@link Supplier} for {@link BrowsingConstellation}.
	 */
	@Required
	public void setBrowsingConstellationProvider(Supplier<BrowsingConstellation> browsingConstellationProvider) {
		this.browsingConstellationProvider = browsingConstellationProvider;
	}
	
	@Required
	public void setQueryConstellationProvider(Supplier<QueryConstellation> queryConstellationProvider) {
		this.queryConstellationProvider = queryConstellationProvider;
	}
	
	/**
	 * Configures the required provider for {@link MasterDetailConstellation}s.
	 * Those are used in the {@link ClipboardConstellation}. It should be read only and without actions...
	 */
	@Required
	public void setMasterDetailConstellationProvider(final Supplier<MasterDetailConstellation> masterDetailConstellationProvider) {
		this.masterDetailConstellationProvider = masterDetailConstellationProvider;
	}
	
	/**
	 * Configures the required {@link ClipboardConstellation} provider.
	 */
	@Required
	public void setClipboardConstellationProvider(Function<Supplier<MasterDetailConstellation>, Supplier<ClipboardConstellation>> clipboardConstellationProvider) {
		this.clipboardConstellationProvider = clipboardConstellationProvider;
	}
	
	/**
	 * Configures the required {@link SpotlightPanel}.
	 */
	@Required
	public void setSpotlightPanel(SpotlightPanel quickAccessPanel) {
		this.quickAccessPanel = quickAccessPanel;
		quickAccessPanel.setMinCharsForFilter(0);
		quickAccessPanel.configureTextFieldMaxWidth(400);
//		quickAccessPanel.setAutoExpand(false);
		
		quickAccessPanel.addSpotlightPanelListener(new SpotlightPanelListenerAdapter() {
			@Override
			public void onValueOrTypeSelected(ObjectAndType objectAndType) {
				handleObjectAndType(objectAndType, true);
			}
		});
		
		quickAccessPanel.addQuickAccessValueSelectionListener((value, type) -> {
			if (value instanceof GenericEntity) {
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(GMF.getTypeReflection().getType(value), value));
				currentDetailModelPath = modelPath;
				if (detailPanel != null)
					detailPanel.setContent(currentDetailModelPath);
				return;
			}
			
			if (type instanceof GmEntityType) {
				EntityType<?> entityType = ((GmEntityType) type).reflectionType();
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(entityType, entityType.createRaw()));
				currentDetailModelPath = modelPath;
			} else
				currentDetailModelPath = null;
			
			if (detailPanel != null)
				detailPanel.setContent(currentDetailModelPath);
		});
	}
	
	/**
	 * Configures the registry for {@link WorkbenchAction}s handlers.
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}
	
	/**
	 * Configures the required provider for the detail panel. It is shown in the east region, with the selection panel if that is available.
	 */
	@Required
	public void setDetailPanelProvider(Supplier<? extends GmEntityView> detailPanelProvider) {
		this.detailPanelProvider = detailPanelProvider;
	}
	
	/**
	 * Data session to be used for querying.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.dataSession = gmSession;
	}
	
	public PersistenceGmSession getGmSession() {
		return this.dataSession;
	}
	
	/**
	 * Configures the required {@link TransientGmSession}.
	 */
	@Required
	public void setTransientGmSession(TransientGmSession transientGmSession) {
		this.transientGmSession = transientGmSession;
	}
	
	/**
	 * Configures the required {@link ParserWithPossibleValues}.
	 */
	@Required
	public void setParserWithPossibleValuesSupplier(Supplier<ParserWithPossibleValues> parserWithPossibleValuesSupplier) {
		this.parserWithPossibleValuesSupplier = parserWithPossibleValuesSupplier;
	}
	
	@Configurable
	public void setTemplateEvaluationDialogProvider(Supplier<? extends TemplateGIMADialog> templateEvaluationDialogProvider) {
		this.templateEvaluationDialogProvider = templateEvaluationDialogProvider;
	}
	
	/**
	 * Configures the {@link CodecRegistry} used as renderers.
	 */
	@Configurable
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	/**
	 * Configures a map with experts responsible for returning new instances for a given type.
	 */
	@Configurable
	public void setExpertUIMap(Map<String, Supplier<? extends ExpertUI<?>>> expertUISupplierMap) {
		this.expertUISupplierMap = expertUISupplierMap;
	}
	
	public void addSelectionConstellationListener(SelectionConstellationListener listener) {
		if (selectionConstellationListeners == null)
			selectionConstellationListeners = new ArrayList<SelectionConstellationListener>();
		selectionConstellationListeners.add(listener);
	}
	
	public void removeSelectionConstellationListener(SelectionConstellationListener listener) {
		if (selectionConstellationListeners != null) {
			selectionConstellationListeners.remove(listener);
			if (selectionConstellationListeners.isEmpty())
				selectionConstellationListeners = null;
		}
	}
	
	public void configureRequeryNeededAfterHide(boolean requeryNeededAfterHide) {
		this.requeryNeededAfterHide = requeryNeededAfterHide;
	}

	public void configureSelectionConfig(SelectionConfig selectionConfiguration) {
		Scheduler.get().scheduleDeferred(() -> {
			VerticalTabElement selectedElement = verticalTabPanel.getSelectedElement();
			if (selectedElement != null)
				verticalTabPanel.setSelectedVerticalTabElement(selectedElement);
			else
				verticalTabPanel.setSelectedVerticalTabElement(verticalTabPanel.getTabElements().get(0));
		});
		
		if (isSelectionShown)
			hideSelectionPanel();
			
		selectionConfig = selectionConfiguration;
		selectionGrid.getStore().clear();
		
		theSession = getGmSession(selectionConfiguration);
		
		GenericModelType selectionType = selectionConfiguration.getGmType();
		boolean isBase = selectionType.isBase();
		boolean referenceable = isReferenceable(selectionConfiguration.isReferenceable(), selectionType);

		if (clipboardConstellation != null) {
			clipboardConstellation.configureGmSession(theSession);
			clipboardConstellation.configureMaxSelection(selectionConfiguration.getMaxSelection());
		}
		
		if (changesConstellation != null) {
			changesConstellation.configureSessions(theSession, transientGmSession);
			changesConstellation.configureMaxSelection(selectionConfiguration.getMaxSelection());
		}
		
		if (!referenceable && !configuredAsNonReferenceable) {
			verticalTabPanel.removeVerticalTabElement(homeElement, false);
			verticalTabPanel.removeVerticalTabElement(changesElement, false);
			verticalTabPanel.removeVerticalTabElement(clipboardElement, false);
			//verticalTabPanel.removeVerticalTabElement(workbenchElement, false);
			configuredAsNonReferenceable = true;
		} else if (referenceable && configuredAsNonReferenceable) {
			int index = 2;
			if (!homeHidden)
				verticalTabPanel.insertVerticalTabElement(homeElement, 0);
			else
				index--;
			verticalTabPanel.insertVerticalTabElement(changesElement, index++);
			verticalTabPanel.insertVerticalTabElement(clipboardElement, index++);
			//verticalTabPanel.insertVerticalTabElement(workbenchElement, index);
			configuredAsNonReferenceable = false;
		}
		
		if (homeConstellation != null && homeConstellation.getWorkbenchSession() != selectionConfiguration.getWorkbenchSession()
				&& selectionConfiguration.getWorkbenchSession() instanceof ModelEnvironmentDrivenGmSession) {
			homeConstellation.setWorkbenchSession((ModelEnvironmentDrivenGmSession) selectionConfiguration.getWorkbenchSession());
			homeConstellation.onModelEnvironmentSet();
		}
		// TODO: I must add the default query to the home (so it can be easily selected once again)

		clearVerticalTabElements(true);
		boolean queryable = false;

		if (referenceable && !selectionType.isEnum()) {
			EntityType<?> entityType = isBase ? GenericEntity.T : (EntityType<?>) selectionType;

			int elementNr = 1;
			List<SelectionTabConfig> tabConfigList = selectionConfiguration.getEntityQueries() != null ? selectionConfiguration.getEntityQueries()
					: prepareEntityQuery(entityType);
			boolean singleQueryTab = tabConfigList.size() == 1;
			for (SelectionTabConfig tabConfig : tabConfigList) {
				String name;
				if (tabConfig.getTabName() != null && !tabConfig.getTabName().isEmpty())
					name = tabConfig.getTabName();
				else {
					name = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, theSession.getModelAccessory().getMetaData(), this.useCase)
							+ (elementNr == 1 ? "" : elementNr);
				}

				maybeCreateVerticalTabElement(null, singleQueryTab ? "query" : name, singleQueryTab ? "" : name, name,
						singleQueryTab ? ConstellationResources.INSTANCE.search() : null,
						provideSelectionBrowsingConstellation(name, tabConfig.getEntityQuery(), true), true,
						(elementNr == 1 ? selectionConfiguration.isUseQueryTabAsDefault() : false), singleQueryTab);

				elementNr++;
			}
			
			queryable = true;
		}

		if (quickAccessPanel.getGmSession() != theSession) {
			quickAccessPanel.setGmSession(theSession);
			quickAccessPanel.onModelEnvironmentChanged();
		}
		
		quickAccessPanel.setLoadExistingValues(selectionConfiguration.isReferenceable());
		//quickAccessPanel.configureUseQueryActions(selectionConfiguration.isReferenceable());
		//quickAccessPanel.configureEnableInstantiation(selectionConfiguration.isInstantiable());
		
		if (selectionConfiguration.isSimplified()) {
			quickAccessPanel.configureUseQueryActions(false);
			quickAccessPanel.configureEnableInstantiation(false);
			quickAccessPanel.setMaximumNumberOfExistingValuesToShow(20);
			quickAccessPanel.setLoadTypes(selectionConfiguration.isInstantiable());
			quickAccessPanel.configureSimplifiedAssignment(true);
			configurePossibleValues(selectionConfiguration.getPossibleValues());
		}
		
		TypeCondition typeCondition = selectionConfiguration.getTypeCondition() != null ? selectionConfiguration.getTypeCondition()
				: quickAccessPanel.prepareTypeCondition(selectionType);
		quickAccessPanel.configureTypeCondition(typeCondition);
		if (!selectionConfiguration.isUseQueryTabAsDefault() || !queryable) {
			if (verticalTabPanel.getSelectedElement() == quickAccessElement)
				verticalTabPanel.setSelectedVerticalTabElement(null);
			verticalTabPanel.setSelectedVerticalTabElement(quickAccessElement);
		}
		
		boolean createNewVisible = selectionConfiguration.isInstantiable() && (selectionType.isBase() || selectionType.isEntity());
		if (createNewVisible) {
			BorderLayoutData northLayoutData = new BorderLayoutData(120);
			northLayoutData.setMargins(new Margins(0, 4, 0, 0));
			northRegionPanelContainer.setEastWidget(getCreateNewButton(), northLayoutData);
			
			prepareExpertUiEntry(typeCondition);
		} else if (createNewButton != null)
			createNewButton.removeFromParent();
		
		if (selectionConfiguration.isSimplified()) {
			northData.setSize(0);
			northRegionPanelContainer.removeFromParent();
		} else {
			northData.setSize(NORTH_PANEL_SIZE);
			setNorthWidget(northRegionPanelContainer, northData);
		}
		
		Scheduler.get().scheduleDeferred(() -> {
			forceLayout();
			quickAccessPanel.getGrid().getView().refresh(false);
		});
		//workbench.prepareFolders(false);
		
		if (!hasMultipleCreateNewOptions && !referenceable && selectionConfig.isInstantiable())
			Scheduler.get().scheduleDeferred(this::handleCreateNew);
	}
	
	private void prepareExpertUiEntry(TypeCondition typeCondition) {
		createNewButton.setMenu(null);
		hasMultipleCreateNewOptions = false;
		
		if (expertUISupplierMap == null)
			return;
		
		IsAssignableTo entityTypeCondition = SpotlightPanel.getFirstEntityTypeCondition(typeCondition);
		if (entityTypeCondition == null)
			return;
		
		String typeSignature = entityTypeCondition.getTypeSignature();
		Supplier<? extends ExpertUI<?>> expertUISupplier = expertUISupplierMap.get(typeSignature);
		if (expertUISupplier == null)
			return;
		
		if (expertUIMap == null)
			expertUIMap = new FastMap<>();
		
		ExpertUI<?> expertUI = expertUIMap.computeIfAbsent(typeSignature, v -> expertUISupplier.get());
		if (!expertUI.isValid())
			return;
		
		String i18nName = expertUI.getName() != null ? I18nTools.getLocalized(expertUI.getName()) : null;
		String displayName = i18nName != null ? i18nName : expertUI.getTechnicalName();
		
		MenuItem menuItem = new MenuItem(displayName, expertUI.getImageResource());
		menuItem.addSelectionHandler(event -> handleExpertUI(expertUI));
		Menu menu = new Menu();
		menu.add(menuItem);
		
		createNewButton.setMenu(menu);
		hasMultipleCreateNewOptions = true;
	}
	
	private boolean isReferenceable(boolean referenceable, GenericModelType modelType) {
		if (!referenceable)
			return false;
		
		return modelType.isEntity() || modelType.isBase() || modelType.isEnum();
	}
	
	private void configurePossibleValues(List<Object> possibleValues) {
		if (possibleValues == null) {
			quickAccessPanel.configureOriginalSimpleTypesProvider();
			return;
		}
		
		ParserWithPossibleValues parserWithPossibleValues = parserWithPossibleValuesSupplier.get();
		parserWithPossibleValues.configurePossibleValues(possibleValues, theSession);
		quickAccessPanel.setSimpleTypesValuesProvider(() -> parserWithPossibleValues);
	}
	
	public void hideHome() {
		verticalTabPanel.removeVerticalTabElement(homeElement, false);
		homeHidden = true;
	}
	
	private void updateChangesTabElementVisibility(VerticalTabElement tabElement, boolean added) {
		tabElement.setVisible(added);
		verticalTabPanel.refresh();
		if (!added && verticalTabPanel.getSelectedElement() == tabElement)
			verticalTabPanel.selectFirstTabElement();
	}

	private PersistenceGmSession getGmSession(SelectionConfig selectionConfiguration) {
		if (dataSession.getModelAccessory().getOracle().findGmType(selectionConfiguration.getGmType()) == null)
			return selectionConfiguration.getGmSession();
		
		return dataSession;
	}

	private void hideSelectionPanel() {
		isSelectionShown = false;
		selectionPanel.removeFromParent();
		centerRegionPanelContainer.forceLayout();
	}

	@Override
	public void intializeBean() throws Exception {
		//workbench.configureUseCase(useCase);
		
		quickAccessPanel.setUseCase(useCase);

		verticalTabPanel.setClosableItems(false);
		verticalTabPanel.setUseHorizontalTabs(true);
		prepareSelectionPanel();		
		
		//workbenchElement = new VerticalTabElement("$workbenchConstellation" ,"", LocalizedText.INSTANCE.workbench(), workbench,
			//	ConstellationResources.INSTANCE.workbench(), null, true, null);
		//verticalTabPanel.insertVerticalTabElement(workbenchElement, true);
		clipboardElement = new VerticalTabElement("$clipboardConstellation" ,"", LocalizedText.INSTANCE.clipboard(), getClipboardConstellationSupplier(),
				ConstellationResources.INSTANCE.clipboard(), null, true, null);
		clipboardElement.setSystemConfigurable(true);
		verticalTabPanel.insertVerticalTabElement(clipboardElement, true);
		changesElement = new VerticalTabElement("$changesConstellation","", LocalizedText.INSTANCE.changes(), getChangesConstellationSupplier(),
				ConstellationResources.INSTANCE.changes(), null, true, null);
		changesElement.setSystemConfigurable(true);
		changesElement.setVisible(false);
		verticalTabPanel.insertVerticalTabElement(changesElement, true);
		quickAccessElement = new VerticalTabElement("$quickAccessConstellation","", LocalizedText.INSTANCE.quickAccess(), () -> quickAccessPanel,
				ConstellationResources.INSTANCE.quickAccess(), null, true, null);
		quickAccessElement.setSystemConfigurable(true);
		verticalTabPanel.insertVerticalTabElement(quickAccessElement, true);
		homeElement = new VerticalTabElement("$homeConstellation", "", LocalizedText.INSTANCE.home(), getHomeConstellationSupplier(),
				ConstellationResources.INSTANCE.home(), null, true, null);
		homeElement.setSystemConfigurable(true);
		verticalTabPanel.insertVerticalTabElement(homeElement, true);
		
		northRegionPanelContainer = new ExtendedBorderLayoutContainer();
		northRegionPanelContainer.setCenterWidget(verticalTabPanel);

 		setNorthWidget(northRegionPanelContainer, northData);
 		
 		centerRegionPanelContainer = new ExtendedBorderLayoutContainer();
		centerRegionPanelContainer.setBorders(false);
		add(centerRegionPanelContainer, centerData);
		
		eastRegionPanel = new ContentPanel();
		eastRegionPanel.setBorders(false);
		eastRegionPanel.setBodyBorder(false);
		eastRegionPanel.setHeaderVisible(false);
		eastRegionPanelContainer = new ExtendedBorderLayoutContainer();
		eastRegionPanel.add(eastRegionPanelContainer);
		
		BorderLayoutData eastLayoutData = new BorderLayoutData(EAST_PANEL_SIZE);
		eastLayoutData.setSplit(true);
		eastLayoutData.setCollapsible(false);
		eastLayoutData.setFloatable(false);
		eastLayoutData.setCollapseHidden(true);
		setEastWidget(eastRegionPanel, eastLayoutData);
		
		/*getWorkbench().addWorkbenchListener(new WorkbenchListenerAdapter() {
			@Override
			public void onFolderSelected(Folder folder) {
				if (folder != null && folder.getContent() instanceof WorkbenchAction) {
					try {
						ModelAction action = workbenchActionHandlerRegistry.provide(prepareWorkbenchActionContext(folder));
						if (action != null)
							action.perform(null);
					} catch (RuntimeException e) {
						ErrorDialog.show("Error while providing workbench action handler.", e);
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onModelEnvironmentChanged(ModelEnvironment modelEnvironment) {
				quickAccessPanel.onModelEnvironmentChanged();
			}
		});*/
	}
	
	/*
	 * Returns the {@link Workbench} used within this {@link SelectionConstellation}.
	 *
	public Workbench getWorkbench() {
		return workbench;
	}*/
	
	public void addClipboardConstellationMasterDetailProvidedListener(MasterDetailConstellationProvidedListener listener) {
		if (clipboardConstellation != null)
			clipboardConstellation.addMasterDetailConstellationProvidedListener(listener);
		else {
			if (clipboardMasterDetailListeners == null)
				clipboardMasterDetailListeners = new HashSet<>();
			
			clipboardMasterDetailListeners.add(listener);
		}
	}
	
	public void addChangesConstellationMasterDetailProvidedListener(MasterDetailConstellationProvidedListener listener) {
		if (changesConstellation != null)
			changesConstellation.addMasterDetailConstellationProvidedListener(listener);
		else {
			if (changesMasterDetailListeners == null)
				changesMasterDetailListeners = new HashSet<>();
			
			changesMasterDetailListeners.add(listener);
		}
	}
	
	public SpotlightPanel getSpotlightPanel() {
		return quickAccessPanel;
	}
	
	public Widget getCurrentWidget() {
		return currentWidget;
	}
	
	public ModelMdResolver getMetaDataResolver() {
		return theSession.getModelAccessory().getMetaData();
	}
	
	public String getUseCase() {
		return useCase;
	}
	
	private WorkbenchActionContext<WorkbenchAction> prepareWorkbenchActionContext(final WorkbenchAction workbenchAction) {
		return new WorkbenchActionContext<WorkbenchAction>() {
			@Override
			public GmSession getGmSession() {
				return theSession;
			}

			@Override
			public List<ModelPath> getModelPaths() {
				return Collections.emptyList();
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public WorkbenchAction getWorkbenchAction() {
				return workbenchAction;
			}

			@Override
			public Object getPanel() {
				return SelectionConstellation.this;
			}
			
			@Override
			@SuppressWarnings("unusable-by-js")
			public Folder getFolder() {
				return null;
			}
		};
	}
	
	/**
	 * Creates a new {@link VerticalTabElement} with the current {@link Widget}, if it doesn't exist already.
	 */
	public VerticalTabElement maybeCreateVerticalTabElement(WorkbenchActionContext<?> workbenchActionContext, String id, String name, String description,
			ImageResource icon, Supplier<BrowsingConstellation> browsingConstellationSupplier, boolean initial, boolean select, boolean createAsStatic) {
		VerticalTabElement tabElement = verticalTabPanel.getVerticalTabElementByWorkbenchActionContext(workbenchActionContext);
		if (tabElement != null) {
			if (select)
				verticalTabPanel.setSelectedVerticalTabElement(tabElement);
			return tabElement;
		}
		
		tabElement = new VerticalTabElement("$" + id, name, description, browsingConstellationSupplier, icon, null, createAsStatic,
				workbenchActionContext);
		verticalTabPanel.insertVerticalTabElement(tabElement, -1);

		if (select)
			verticalTabPanel.setSelectedVerticalTabElement(tabElement);

		if (initial) {
			if (initialQueryTabElements == null)
				initialQueryTabElements = new ArrayList<>();

			initialQueryTabElements.add(tabElement);
		} else {
			if (queryTabElements == null)
				queryTabElements = new ArrayList<>();

			queryTabElements.add(tabElement);
		}

		return tabElement;
	}

	public void clearVerticalTabElements(boolean initial) {
		if (initial) {
			if (initialQueryTabElements != null) {
				for (VerticalTabElement queryTabElement : initialQueryTabElements) {
					if (verticalTabPanel.containsVerticalTabElement(queryTabElement))
						verticalTabPanel.removeVerticalTabElement(queryTabElement);
				}

				initialQueryTabElements.clear();
				initialQueryTabElements = null;
			}
			
			if (initialSelectionBrowsingConstellations != null) {
				initialSelectionBrowsingConstellations.clear();
				initialSelectionBrowsingConstellations = null;
			}
			if (initialSelectionQueryConstellations != null) {
				initialSelectionQueryConstellations.clear();
				initialSelectionQueryConstellations = null;
			}
			
			return;
		}
		
		if (queryTabElements != null) {
			for (VerticalTabElement queryTabElement : queryTabElements) {
				if (verticalTabPanel.containsVerticalTabElement(queryTabElement))
					verticalTabPanel.removeVerticalTabElement(queryTabElement);
			}

			queryTabElements.clear();
			queryTabElements = null;
		}
		
		if (selectionBrowsingConstellations != null) {
			selectionBrowsingConstellations.clear();
			selectionBrowsingConstellations = null;
		}
		if (selectionQueryConstellations != null) {
			selectionQueryConstellations.clear();
			selectionQueryConstellations = null;
		}
	}

	public Grid<GMTypeInstanceBean> getSelectionGrid() {
		return selectionGrid;
	}
	
	public void addObject(Object object) {
		if (!isSelectionShown) {
			centerRegionPanelContainer.setSouthWidget(selectionPanel, new BorderLayoutData(100));
			isSelectionShown = true;
			Scheduler.get().scheduleDeferred(centerRegionPanelContainer::forceLayout);
		}
		
		selectionGrid.getStore().add(new GMTypeInstanceBean(GMF.getTypeReflection().getType(object), object));
	}
	
	public VerticalTabPanel getVerticalTabPanel() {
		return verticalTabPanel;
	}
	
	public void setBrowsingConstellation(BrowsingConstellation browsingConstellation) {
		fireBrowsingConstellationSet(browsingConstellation);
	}
	
	public List<GMTypeInstanceBean> getSelections() {
		return selectionGrid.getStore().getAll();
	}
	
	public ImageResource getIcon(Resource rasterImageResource) {
		if (rasterImageResource != null)
			return new GmImageResource(rasterImageResource, theSession.resources().url(rasterImageResource).asString());
		
		return null;
	}
	
	private void configureCurrentWidget(Widget currentWidget) {
		if (this.currentWidget == currentWidget)
			return;
		
		if (this.currentWidget != null)
			centerRegionPanelContainer.remove(this.currentWidget);
		
		if (this.currentWidget instanceof GmContentView)
			((GmContentView) this.currentWidget).removeSelectionListener(getSelectionListener());
		
		this.currentWidget = currentWidget;
		
		if (currentWidget == null)
			return;
		
		if (createNewButton != null)
			createNewButton.setVisible(!(currentWidget instanceof ExpertUI));
		
		centerRegionPanelContainer.add(currentWidget);
		centerRegionPanelContainer.forceLayout();
		
		if (currentWidget instanceof Workbench) {
			new Timer() {
				@Override
				public void run() {
					((Workbench) SelectionConstellation.this.currentWidget).expandEntries();
				}
			}.schedule(200);
		} else if (currentWidget instanceof BrowsingConstellation) {
			GmContentView contentView = ((BrowsingConstellation) currentWidget).getCurrentContentView();
			if (contentView instanceof QueryConstellation) {
				if (requeryNeededAfterHide && refreshQueryConstellation) {
					((QueryConstellation) contentView).setMaxSelectCount(selectionConfig.getMaxSelection());
					((QueryConstellation) contentView).performSearch();
					requeryNeededAfterHide = false;
				}
				refreshQueryConstellation = true;
				
				GmContentView view = ((QueryConstellation) contentView).getView();
				if (view instanceof GmTreeView) {
					if (detailPanelVisible)
						((GmTreeView) view).showOnlyNodeColumn();
					else
						((GmTreeView) view).showAllColumns();
				}
			}
		}
		
		if (currentWidget instanceof GmSelectionSupport)
			((GmSelectionSupport) currentWidget).addSelectionListener(getSelectionListener());
	}
	
	private void handleTreeViewColumns(boolean hide) {
		if (!(currentWidget instanceof BrowsingConstellation))
			return;

 		GmContentView contentView = ((BrowsingConstellation) currentWidget).getCurrentContentView();
		if (!(contentView instanceof QueryConstellation))
			return;

 		GmContentView view = ((QueryConstellation) contentView).getView();
		if (!(view instanceof GmTreeView))
			return;

 		if (hide)
			((GmTreeView) view).showOnlyNodeColumn();
		else
			((GmTreeView) view).showAllColumns();
	}
	
	public void clearDetailPanel() {
		currentDetailModelPath = null;
		if (detailPanel != null)
			detailPanel.setContent(currentDetailModelPath);
	}
	
	private GmSelectionListener getSelectionListener() {
		if (gmSelectionListener != null)
			return gmSelectionListener;
		
		gmSelectionListener = gmSelectionSupport -> {
			ModelPath modelPath = gmSelectionSupport.getFirstSelectedItem();
			if (modelPath == null || modelPath.last().getValue() instanceof GenericEntity)
				currentDetailModelPath = gmSelectionSupport.getFirstSelectedItem();
			else
				currentDetailModelPath = null;
			
			if (detailPanel != null)
				detailPanel.setContent(currentDetailModelPath);
		};
		
		return gmSelectionListener;
	}
	
	private void fireEntityTypeChanged(EntityType<?> entityType) {
		if (selectionConstellationListeners != null)
			selectionConstellationListeners.forEach(listener -> listener.onEntityTypeChanged(entityType));
	}
	
	private void fireBrowsingConstellationSet(BrowsingConstellation browsingConstellation) {
		if (selectionConstellationListeners != null)
			selectionConstellationListeners.forEach(listener -> listener.onBrowsingConstellationSet(browsingConstellation));
	}
	
	private void fireObjectSelected(List<Object> objects, boolean finish, boolean handleInstantiation) {
		if (selectionConstellationListeners != null)
			selectionConstellationListeners.forEach(listener -> listener.onObjectSelected(objects, finish, handleInstantiation));
	}
	
	public void handleDetailPanelVisibility(boolean visible) throws RuntimeException {
		detailPanelVisible = visible;
		if (visible) {
			if (detailPanel == null) {
				detailPanel = detailPanelProvider.get();
				detailPanel.configureGmSession(theSession);
				
				if (currentDetailModelPath != null)
					detailPanel.setContent(currentDetailModelPath);
			}
			
			if (detailPanel instanceof Widget) {
				eastRegionPanelContainer.setCenterWidget((Widget) detailPanel);
				eastRegionPanelContainer.forceLayout();
			}
			
			handleTreeViewColumns(true);
			
			if (!isEastExpanded) {
				this.expand(LayoutRegion.EAST);
				isEastExpanded = true;
				doLayout();
			}
			
			return;
		}
		
		if (!isEastExpanded)
			return;
		
		collapse(LayoutRegion.EAST);
		isEastExpanded = false;
		
		handleTreeViewColumns(false);
		doLayout();
	}
	
	protected GmEntityView getDetailPanel() {
		return detailPanel;
	}
	
	private void prepareSelectionPanel() {
		selectionPanel = new ContentPanel();
		selectionPanel.setHeaderVisible(false);
		selectionPanel.setBorders(false);
		selectionPanel.setBodyBorder(false);
		
		ColumnConfig<GMTypeInstanceBean, GMTypeInstanceBean> column = new ColumnConfig<>(new IdentityValueProvider<>());
		column.setCell(new AbstractCell<GMTypeInstanceBean>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, GMTypeInstanceBean bean, SafeHtmlBuilder sb) {
				Object value = bean.getInstance();
				if (value instanceof GenericEntity) {
					GenericEntity entity = (GenericEntity) value;
					String selectiveInfo = SelectiveInformationResolver.resolve((EntityType<?>) bean.getGenericModelType(), entity, getMetaData(entity),
							useCase/* , null */);
					sb.appendHtmlConstant(selectiveInfo.isEmpty() ? "&nbsp;" : selectiveInfo);
					return;
				}
				
				if (value != null && codecRegistry != null) {
					Codec<Object, String> codec = codecRegistry.getCodec(value.getClass());
					if (codec != null) {
						try {
							sb.appendEscaped(codec.encode(value));
							return;
						} catch (CodecException e) {
							e.printStackTrace();
						}
					} else {
						sb.appendEscaped(value.toString());
						return;
					}
				}
				
				sb.appendHtmlConstant("&nbsp;");
			}
		});
		
		List<ColumnConfig<GMTypeInstanceBean, ?>> columns = new ArrayList<>();
		columns.add(column);
		
		selectionGrid = new Grid<>(new ListStore<>(getModelKeyProvider()), new ColumnModel<>(columns));
		selectionGrid.setHideHeaders(true);
		selectionGrid.getSelectionModel().addSelectionChangedHandler(event -> {
			boolean itemSelected = event.getSelection() != null && !event.getSelection().isEmpty();
			moveDownButton.setEnabled(itemSelected);
			moveUpButton.setEnabled(itemSelected);
			deleteItemButton.setEnabled(itemSelected);
		});
		
		selectionGrid.setView(new GridView<GMTypeInstanceBean>() {
			@Override
			protected void onRowSelect(int rowIndex) {
				super.onRowSelect(rowIndex);
				Element row = getRow(rowIndex);
			    if (row != null)
			    	row.addClassName("x-grid3-row-selected");
			}
			
			@Override
			protected void onRowDeselect(int rowIndex) {
				super.onRowDeselect(rowIndex);
				Element row = getRow(rowIndex);
			    if (row != null)
			    	row.removeClassName("x-grid3-row-selected");
			}
		});
		selectionGrid.getView().setColumnHeader(new ExtendedColumnHeader<>(selectionGrid, selectionGrid.getColumnModel()));
		
		selectionGrid.getView().setTrackMouseOver(false);
		selectionGrid.getView().setAutoFill(true);
		GridWithoutLinesStyle style = GWT.<GridWithoutLinesResources>create(GridWithoutLinesResources.class).css();
		style.ensureInjected();
		selectionGrid.addStyleName(style.gridWithoutLines());
		
		selectionGrid.getView().setViewConfig(new GridViewConfig<GMTypeInstanceBean>() {
			@Override
			public String getRowStyle(GMTypeInstanceBean model, int rowIndex) {
				return "";
			}
			
			@Override
			public String getColStyle(GMTypeInstanceBean model, ValueProvider<? super GMTypeInstanceBean, ?> valueProvider,
					int rowIndex, int colIndex) {
				return "gmeGridColumn";
			}
		});
		
		BorderLayoutContainer container = new ExtendedBorderLayoutContainer();
		container.setCenterWidget(selectionGrid);
		container.setNorthWidget(prepareToolBar(selectionGrid), new BorderLayoutData(30));
		container.setBorders(false);
		
		selectionPanel.add(container);
	}
	
	private ModelKeyProvider<GMTypeInstanceBean> getModelKeyProvider() {
		return item -> item.getId().toString();
	}
	
	protected ToolBar prepareToolBar(Grid<GMTypeInstanceBean> selectionGrid) {
		ToolBar toolBar = new ToolBar();
		toolBar.getElement().getStyle().setPaddingTop(6, Unit.PX);
		toolBar.setBorders(false);
		prepareMoveItemsButtons(selectionGrid);
		
		Label label = new Label(LocalizedText.INSTANCE.selectedValues());
		label.addStyleName(ConstellationResources.INSTANCE.css().graySmallText());
		
		toolBar.add(label);
		toolBar.add(new FillToolItem());
		toolBar.add(moveDownButton);
		toolBar.add(moveUpButton);
		toolBar.add(deleteItemButton);
		
		return toolBar;
	}
	
	protected void prepareMoveItemsButtons(Grid<GMTypeInstanceBean> selectionGrid) {
		moveDownButton = new FixedTextButton();
		moveDownButton.setToolTip(LocalizedText.INSTANCE.moveEntityDown());
		moveDownButton.setIcon(ConstellationResources.INSTANCE.moveDown());
		moveDownButton.setEnabled(false);
		moveUpButton = new FixedTextButton();
		moveUpButton.setToolTip(LocalizedText.INSTANCE.moveEntityUp());
		moveUpButton.setIcon(ConstellationResources.INSTANCE.moveUp());
		moveUpButton.setEnabled(false);
		
		SelectHandler selectedHandler = event -> {
			if (event.getSource() == deleteItemButton) {
				List<GMTypeInstanceBean> selectedModels = selectionGrid.getSelectionModel().getSelectedItems();
				if (selectedModels != null && !selectedModels.isEmpty()) {
					int index = -1;
					if (selectedModels.size() == 1)
						index = selectionGrid.getStore().indexOf(selectedModels.get(0));
					
					selectedModels.forEach(model -> selectionGrid.getStore().remove(model));
					
					if (index != -1 && selectionGrid.getStore().size() > 0)
						selectionGrid.getSelectionModel().select(index, false);
				}
				
				return;
			}
			
			int offset;
			if (event.getSource() == moveDownButton)
				offset = 1;
			else
				offset = -1;
			
			ListStore<GMTypeInstanceBean> store = selectionGrid.getStore();
			List<GMTypeInstanceBean> models = selectionGrid.getSelectionModel().getSelectedItems();
			
			int firstIndex = Integer.MAX_VALUE;

			for (GMTypeInstanceBean model2: models)
				firstIndex = Math.min(firstIndex, store.indexOf(model2));
			
			int insertIndex = firstIndex + offset;
			// only act within limits
			if (insertIndex <= store.size() - models.size() && insertIndex >= 0) {
				models.forEach(m -> store.remove(m));
				store.addAll(insertIndex, models);
				selectionGrid.getSelectionModel().select(models, true);
			}
		};

		moveDownButton.addSelectHandler(selectedHandler);
		moveUpButton.addSelectHandler(selectedHandler);
		
		deleteItemButton = new FixedTextButton();
		deleteItemButton.setToolTip(LocalizedText.INSTANCE.removeEntity());
		deleteItemButton.setIcon(ConstellationResources.INSTANCE.remove());
		deleteItemButton.setEnabled(false);
		deleteItemButton.addSelectHandler(selectedHandler);
	}
	
	public void handleObjectAndType(ObjectAndType objectAndType, boolean finish) {
		if (objectAndType.getObject() instanceof QueryAction) {
			ModelAction action = workbenchActionHandlerRegistry.apply(prepareWorkbenchActionContext((QueryAction) objectAndType.getObject()));
			if (action != null)
				action.perform(null);
		} else if (objectAndType.getObject() instanceof GenericEntity)
			fireObjectSelected(Collections.singletonList(objectAndType.getObject()), finish, false);
		else if (objectAndType.getObject() instanceof ExpertUI)
			handleExpertUI((ExpertUI<?>) objectAndType.getObject());
		else if (objectAndType.getType() instanceof GmEntityType) {
			EntityType<?> entityType = GMF.getTypeReflection().getEntityType(objectAndType.getType().getTypeSignature());
			/*if (!entityType.isAbstract() && isInstantiable(entityType)) {
				PersistenceGmSession session = selectionConfig.getGmSession();
				NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
				GenericEntity entity = session.create(entityType);
				nestedTransaction.commit();
				fireObjectSelected(Collections.singletonList(entity), finish, true);
			} else*/
			fireEntityTypeChanged(entityType);
		} else if (objectAndType.getObject() != null)
			fireObjectSelected(Collections.singletonList(objectAndType.getObject()), finish, false);
	}
	
	public void singleAssign(EntityType<?> entityType, boolean finish) {
//		EntityType<?> entityType = GMF.getTypeReflection().getEntityType(objectAndType.getType().getTypeSignature());
		if (entityType.isAbstract() || !isInstantiable(entityType))
			fireEntityTypeChanged(entityType);
		else {
			PersistenceGmSession session = selectionConfig.getGmSession();
			NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
			GenericEntity entity = session.create(entityType);
			nestedTransaction.commit();
			fireObjectSelected(Collections.singletonList(entity), finish, true);
		}
	}
	
	private boolean isInstantiable(EntityType<?> entityType) {
		ModelMdResolver modelMdResolver = theSession.getModelAccessory().getMetaData();
		if (!modelMdResolver.entityType(entityType).useCase(useCase).is(Instantiable.T))
			return false;
		
		EntityProperty entityProperty = selectionConfig.getQueryEntityProperty();
		if (entityProperty != null && entityProperty.getPropertyName() != null) {
			return modelMdResolver.lenient(true).entityTypeSignature(entityProperty.getReference().getTypeSignature())
					.property(entityProperty.getPropertyName()).useCase(useCase).is(Instantiable.T);
		}
		
		return true;
	}
	
	private void handleExpertUI(ExpertUI<?> expertUI) {
		VerticalTabElement tabElement = openExpertUIs.get(expertUI);
		if (tabElement != null) {
			verticalTabPanel.setSelectedVerticalTabElement(tabElement);
			return;
		}
			
		ExpertUIContext context = new ExpertUIContext();
		context.setMaxElementsToReturn(selectionConfig.getMaxSelection());
		context.setModelPath(null);
		
		LocalizedString name = expertUI.getName();
		String display = name != null ? I18nTools.getLocalized(name) : expertUI.getTechnicalName();
		
		tabElement = new VerticalTabElement("$expertUI", display, null, () -> expertUI.getComponent(), expertUI.getImageResource(), null, false, null);
		verticalTabPanel.insertVerticalTabElement(tabElement, -1);
		verticalTabPanel.setSelectedVerticalTabElement(tabElement);
		
		expertUI.getFuture(context) //
				.andThen(result -> {
					if (result != null)
						fireObjectSelected((List<Object>) result, true, false);
				}).onError(Throwable::printStackTrace);
		openExpertUIs.put(expertUI, tabElement);
	}
	
	public void closeExpertUIs() {
		openExpertUIs.values().forEach(el -> verticalTabPanel.removeVerticalTabElement(el));
		openExpertUIs.clear();
	}
	
	private BrowsingConstellation initializeBrowsingConstellation() throws RuntimeException {
		final BrowsingConstellation browsingConstellation = browsingConstellationProvider.get();
		browsingConstellation.configureTopPanelVisibility(false);
		browsingConstellation.configureGmSession(theSession);
		
		TetherBar tetherBar = browsingConstellation.getTetherBar();
		tetherBar.addTetherBarListener(new TetherBarListener() {
			@Override
			public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
				if (tetherBarElement.getContentViewIfProvided() != null)
					tetherBarElement.getContentViewIfProvided().configureUseCase(useCase);
				browsingConstellation.configureCurrentContentView(tetherBarElement.getContentView());
			}

			@Override
			public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
				//NOP
			}

			@Override
			public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
				//NOP
			}
		});
		
		setBrowsingConstellation(browsingConstellation);
		return browsingConstellation;
	}

	public Supplier<BrowsingConstellation> provideSelectionBrowsingConstellation(String name, GenericEntity queryOrTemplate, boolean initial) throws RuntimeException {
		return () -> {
			BrowsingConstellation browsingConstellation = initializeBrowsingConstellation();
			if (initial) {
				if (initialSelectionBrowsingConstellations == null)
					initialSelectionBrowsingConstellations = new ArrayList<>();

				initialSelectionBrowsingConstellations.add(browsingConstellation);
			} else {
				if (selectionBrowsingConstellations == null)
					selectionBrowsingConstellations = new ArrayList<>();

				selectionBrowsingConstellations.add(browsingConstellation);
			}

			TetherBar tetherBar = browsingConstellation.getTetherBar();
			QueryConstellation queryConstellation = null;

			TetherBarElement tetherBarElement = tetherBar.getElementAt(0);
			if (tetherBarElement != null) {
				queryConstellation = (QueryConstellation) tetherBarElement.getContentView();
				if (queryConstellation != null) {
					if (initial) {
						if (initialSelectionQueryConstellations == null)
							initialSelectionQueryConstellations = new ArrayList<>();

						initialSelectionQueryConstellations.add(queryConstellation);
					} else {
						if (selectionQueryConstellations == null)
							selectionQueryConstellations = new ArrayList<>();

						selectionQueryConstellations.add(queryConstellation);
					}
				}
			}

			tetherBar.clearTetherBarElements();

			Query query = (Query) (queryOrTemplate instanceof Template ? ((Template) queryOrTemplate).getPrototype() : queryOrTemplate);
			/*if (query.getTraversingCriterion() == null) { We disabled the preparation of specific TC for condensed properties
				if (query instanceof EntityQuery) {
					TraversingCriterion tc = prepareCondensedTraversingCriterion(GMF.getTypeReflection().getEntityType(((EntityQuery) query).getEntityTypeSignature()));
					if (tc != null)
						query.setTraversingCriterion(tc);
				} else if (query instanceof SelectQuery) {
					String entityTypeSignature = GMEUtil.getSingleEntityTypeSignatureFromSelectQuery((SelectQuery) query);
					if (entityTypeSignature != null) {
						TraversingCriterion tc = prepareCondensedTraversingCriterion(GMF.getTypeReflection().getEntityType(entityTypeSignature));
						if (tc != null)
							query.setTraversingCriterion(tc);
					}
				}
			}*/
			EntityType<?> queryEntityType = query.entityType();

			TetherBarElement newBarElement;
			if (queryConstellation == null)
				newBarElement = new TetherBarElement(null, name, name, getQueryConstellationProvider(query));
			else {
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(queryEntityType, query));
				queryConstellation.configureGmSession(theSession);
				queryConstellation.setMaxSelectCount(selectionConfig.getMaxSelection());
				queryConstellation.setContent(modelPath);
				queryConstellation.performSearch();
				newBarElement = new TetherBarElement(null, name, name, queryConstellation);
			}

			tetherBar.insertTetherBarElement(0, newBarElement);
			tetherBar.setSelectedThetherBarElement(newBarElement);

			return browsingConstellation;
		};
	}

	@Override
	public Future<Object> prepareTemplateEvaluation(TemplateEvaluationContext templateEvaluationContext) throws TemplateEvaluationException {
		if (!templateEvaluationContext.getUseFormular())
			return templateEvaluationContext.evaluateTemplate();
		
		TemplateGIMADialog templateEvaluationDialog = templateEvaluationDialogProvider.get();
		
		boolean showDialog = templateEvaluationDialog.setTemplateEvaluationContext(templateEvaluationContext, null);
		if (showDialog) {
			templateEvaluationDialog.show();
			templateEvaluationDialog.center();
		}
		
		return templateEvaluationDialog.getEvaluatedPrototype();
	}
	
	private Supplier<QueryConstellation> getQueryConstellationProvider(final Query query) {
		return () -> {
			final QueryConstellation queryConstellation = queryConstellationProvider.get();
			queryConstellation.configureGmSession(theSession);
			queryConstellation.configureUseCase(useCase);
			ModelPath modelPath = new ModelPath();
			modelPath.add(new RootPathElement(query.entityType(), query));
			queryConstellation.setMaxSelectCount(selectionConfig.getMaxSelection());			
			queryConstellation.setContent(modelPath);
			if (selectionConfig.getGmType().isEntity())
				queryConstellation.configureEntityType((EntityType<?>) selectionConfig.getGmType(), true, false);
			queryConstellation.performSearch();
			return queryConstellation;
		};
	}
	
	private List<SelectionTabConfig> prepareEntityQuery(EntityType<?> entityType) {
		List<SelectionTabConfig> tabsConfig = new ArrayList<>();
		
		EntityQuery entityQuery = EntityQuery.T.create();
		entityQuery.setEntityTypeSignature(entityType.getTypeSignature());
		tabsConfig.add(new SelectionTabConfig(entityQuery));
		
		return tabsConfig;
	}
	
	/*private TraversingCriterion prepareCondensedTraversingCriterion(EntityType<?> queriedEntityType) {
		ModelMdResolver modelMdResolver = theSession.getModelAccessory().getMetaData();
		GMEMetadataUtil.CondensationBean bean = GMEMetadataUtil.getEntityCondensationProperty(
				GMEMetadataUtil.getEntityCondensations(null, queriedEntityType, modelMdResolver, useCase), false);
		if (bean == null || (!bean.getMode().equals(CondensationMode.auto) && !bean.getMode().equals(CondensationMode.forced)))
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
	
	private SplitButton getCreateNewButton() {
		if (createNewButton != null)
			return createNewButton;

 		createNewButton = new NoBorderSplitButton(LocalizedText.INSTANCE.createNew());
		createNewButton.addSelectHandler(event -> handleCreateNew());
		createNewButton.addStyleName(ConstellationCss.EXTERNAL_HIGHLIGHT_BUTTON);

 		return createNewButton;
	}
	
	private void handleCreateNew() {
		GenericModelType modelType = selectionConfig.getGmType();
		if (modelType.isBase())
			modelType = GenericEntity.T;
		
		GmType typeToCreate = dataSession.getModelAccessory().getOracle().findGmType(modelType);
		if(typeToCreate == null)
			typeToCreate = transientGmSession.getModelAccessory().getOracle().findGmType(selectionConfig.getGmType());

 		handleObjectAndType(new ObjectAndType(null, typeToCreate, null), selectionConfig.getMaxSelection() == 1);
	}
	
	private Supplier<HomeConstellation> getHomeConstellationSupplier() {
		return () -> {
			homeConstellation = homeConstellationSupplier.get();
			
			homeConstellation.configureUseCase(useCase);
			if (homeConstellation.getWorkbenchSession() != selectionConfig.getWorkbenchSession()
					&& selectionConfig.getWorkbenchSession() instanceof ModelEnvironmentDrivenGmSession) {
				homeConstellation.setWorkbenchSession((ModelEnvironmentDrivenGmSession) selectionConfig.getWorkbenchSession());
				homeConstellation.onModelEnvironmentSet();
			}
			
			return homeConstellation;
		};
	}
	
	private Supplier<ChangesConstellation> getChangesConstellationSupplier() {
		return () -> {
			changesConstellation = changesConstellationSupplier.get();
			
			changesConstellation.addChangesConstellationListener((boolean added) -> updateChangesTabElementVisibility(changesElement, added));
			changesConstellation.configureSessions(theSession, transientGmSession);
			changesConstellation.addMasterDetailConstellationProvidedListener(
					masterDetailConstellation -> masterDetailConstellation.configureTypeForCheck(selectionConfig.getGmType()));
			changesConstellation.configureMaxSelection(selectionConfig.getMaxSelection());
			
			if (changesMasterDetailListeners != null)
				changesMasterDetailListeners.forEach(l -> changesConstellation.addMasterDetailConstellationProvidedListener(l));
			
			return changesConstellation;
		};
	}
	
	private Supplier<ClipboardConstellation> getClipboardConstellationSupplier() {
		return () -> {
			clipboardConstellation = clipboardConstellationProvider.apply(masterDetailConstellationProvider).get();
			
			clipboardConstellation.configureGmSession(theSession);
			clipboardConstellation.addMasterDetailConstellationProvidedListener(
					masterDetailConstellation -> masterDetailConstellation.configureTypeForCheck(selectionConfig.getGmType()));
			clipboardConstellation.configureMaxSelection(selectionConfig.getMaxSelection());
			
			if (clipboardMasterDetailListeners != null)
				clipboardMasterDetailListeners.forEach(l -> clipboardConstellation.addMasterDetailConstellationProvidedListener(l));
			
			return clipboardConstellation;
		};
	}

	public interface SelectionConstellationListener {
		public void onEntityTypeChanged(EntityType<?> entityType);
		public void onBrowsingConstellationSet(BrowsingConstellation browsingConstellation);
		public void onObjectSelected(List<Object> objects, boolean finish, boolean handleInstantiation);
	}
	
	@Override
	public void disposeBean() throws Exception {
		configureCurrentWidget(null);
	}

}
