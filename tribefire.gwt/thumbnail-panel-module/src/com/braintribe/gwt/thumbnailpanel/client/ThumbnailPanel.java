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
package com.braintribe.gwt.thumbnailpanel.client;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.entityProperty;
import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.ExchangeContentViewActionFolderContent;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.browserfeatures.client.Console;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.KnownActions;
import com.braintribe.gwt.gmview.action.client.WorkbenchActionSelectionHandler;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.braintribe.gwt.gmview.client.GmCondensationView;
import com.braintribe.gwt.gmview.client.GmContentContext;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewActionManagerHandler;
import com.braintribe.gwt.gmview.client.GmContentViewListener;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmViewportListener;
import com.braintribe.gwt.gmview.client.GmeDragAndDropSupport;
import com.braintribe.gwt.gmview.client.GmeDragAndDropView;
import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.PreviewRefreshHandler;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GmPreviewUtil;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedListViewDefaultResources;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.thumbnailpanel.client.expert.ImageLoader;
import com.braintribe.gwt.thumbnailpanel.client.expert.ImageLoaderListener;
import com.braintribe.gwt.thumbnailpanel.client.expert.ImageLoadingChain;
import com.braintribe.gwt.thumbnailpanel.client.resources.ThumbnailPanelClientBundle;
import com.braintribe.model.extensiondeployment.meta.Preview;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.LifecycleManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.path.SetItemPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.display.GroupBy;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.meta.data.prompt.CondensationMode;
import com.braintribe.model.meta.data.prompt.Inline;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Outline;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.PixelDimensionSpecification;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.model.resourceapi.request.PreviewType;
import com.braintribe.model.uicommand.RefreshPreview;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.theme.base.client.listview.ListViewCustomAppearance;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.ListViewSelectionModel;
import com.sencha.gxt.widget.core.client.Slider;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class ThumbnailPanel extends BorderLayoutContainer implements InitializableBean, GmListView, GmViewActionProvider, GmActionSupport, GmViewport,
		ImageLoaderListener, GmContentViewActionManagerHandler, GmCondensationView, ManipulationListener, GmeDragAndDropView, GmCheckSupport,
		GmInteractionSupport, GmContentSupport, DisposableBean, PreviewRefreshHandler {
	
	static {
		ThumbnailPanelClientBundle.INSTANCE.css().ensureInjected();
	}
	
	private static final Logger logger = new Logger(ThumbnailPanel.class);
	
	public static String EMPTYIMAGE_SRC = ThumbnailPanelClientBundle.INSTANCE.emptyIcon().getSafeUri().asString();
	
	//private final double thumbnailFactor = 0.70;
	private final double thumbnailMargin = 6;
	private int thumbnailSize = 200; //50-200
	private int maxThumbnailSize = 250;
	private int minThumbnailSize = 50;
	private double coverWidthHeightCoefficient = 1.5;
	//private int thumbnailIconSize = 64;
	//private boolean resizable = true;
	
	private ListView<ImageResourceModelData, ImageResourceModelData> imagesListView;
	protected ListStore<ImageResourceModelData> imagesListStore;
	private List<ImageResourceModelData> imageResourceModelPreparationList = new ArrayList<>();
	private Slider sizeSlider;
	
	private CodecRegistry<String> codecRegistry;
	private Function<GenericEntity, Future<Resource>> rasterImageResourceProvider;
	private Function<Resource, String> urlProvider;	
	protected PersistenceGmSession gmSession;
	private String useCase;
	private final ImageLoadingChain imageLoadingChain = new ImageLoadingChain();
	private List<Object> currentListContent;
	private List<GmSelectionListener> gmSelectionListeners;
	private List<GmContentViewListener> listViewListeners;
	protected List<GmInteractionListener> gmInteractionListeners;
	protected List<GmViewportListener> gmViewportListeners;
	private List<GmCheckListener> gmCheckListeners;
	private ModelPath rootModelPath;
	private List<ModelPath> addedModelPaths;
	private GmContentContext gmContentContext;
	protected GmContentViewActionManager actionManager;
	protected ImageResourceModelData selectedModel;
	private boolean prepareToolBarActions = true;
	private HTML emptyPanel;
	private Widget currentWidget;
	private List<Pair<ActionTypeAndName, ModelAction>> externalActions;
	private boolean useGroups;
	private boolean useGroupTogether;
	
	private boolean showContextMenu = true;
	private Menu actionsContextMenu;
	private MenuItem emptyMenuItem;
	
	protected boolean unselectAfterClick;
	private ActionProviderConfiguration actionProviderConfiguration;
	private List<Pair<String, ? extends Widget>> menuItemsList;
	private Set<Class<?>> simplifiedEntityTypes;
	private boolean useCondensationActions = true;
	private Supplier<? extends GmViewActionBar> gmViewActionBarProvider;
	private GmViewActionBar gmViewActionBar;
	private Map<Class<?>, TraversingCriterion> specialEntityTraversingCriterion;
	private final Map<EntityType<?>, String> condensedTypes = new HashMap<>();
	private final Map<EntityType<?>, CondensationMode> condensedModes = new HashMap<>();
	private final Set<EntityType<?>> uncondensedTypes = new HashSet<>();
	private EntityType<GenericEntity> entityTypeForProperties;
	private IconProvider iconProvider;
	private List<GenericEntity> absentPropertyModelList = new ArrayList<>();
	
	protected int clickX = 0;
	protected int clickY = 0;	
	private boolean filterExternalActions = true;
	private boolean useModelSession;
	private GmPreviewUtil previewUtil;
	private Supplier<? extends Action> defaultContextMenuActionSupplier;
	private Action defaultContextMenuAction;
	private String defaultContextMenuActionName;
	private Map<GenericEntity, ImageResourceModelData> deletedEntities;
	private List<Object> currentSelectedItems;
	protected ThumbnailPanelDragSource thumbnailPanelDragSource;
	protected Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	protected ThumbnailPanelDropTarget thumbnailPanelDropTarget;
	boolean modelsAtStorePrepared = false;
	protected WorkbenchActionSelectionHandler workbenchActionSelectionHandler;
	
	public void setRasterImageResourceProvider(Function<GenericEntity, Future<Resource>> rasterImageResourceProvider) {
		this.rasterImageResourceProvider = rasterImageResourceProvider;
	}
	
	public Function<GenericEntity, Future<Resource>> getRasterImageResourceProvider() {
		if (rasterImageResourceProvider != null)
			return rasterImageResourceProvider;
		
		rasterImageResourceProvider = index -> {
			if (index instanceof Resource)
				return new Future<>((Resource) index);
			
			if (index instanceof GmMetaModel) { //TODO: This kind of code should be assigned to an expert. We should not have any direct check on the type here
				GmMetaModel gmMetaModel = (GmMetaModel) index;
				
				if (gmMetaModel.getMetaData() != null && !gmMetaModel.getMetaData().isEmpty())
					return handleMetaData(gmMetaModel.getMetaData());
				
				if (gmMetaModel instanceof EnhancedEntity) {
					final Future<Resource> result = new Future<>(); //TODO: nothing is being done with this result!! shouldn't we return it??
					final EntityType<GmMetaModel> entityType = gmMetaModel.type().cast();
					if (!GMEUtil.isPropertyAbsent(gmMetaModel, entityType.getProperty("metaData")))
						result.onSuccess(null);
					else {
						EntityProperty entityProperty = entityProperty(gmMetaModel.reference(), "metaData");
						PropertyQuery propertyQuery = PropertyQueryBuilder.forProperty(entityProperty).done();
						gmSession.query().property(propertyQuery).result(com.braintribe.processing.async.api.AsyncCallback.of( //
								future -> {
									try {
										Object value = future.result() != null ? future.result().getPropertyValue() : null;
										value = GMEUtil.transformIfSet(value, "metaData", entityType);
										handleMetaData((Set<MetaData>) value) //
												.andThen(result::onSuccess) //
												.onError(e -> result.onSuccess(null));
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}, e -> {
									e.printStackTrace();
									result.onSuccess(null);
								}));
					}
				}
			}
			
			Icon entityIcon = getMetaData(index).entity(index).useCase(useCase).meta(Icon.T).exclusive();
			if (entityIcon != null && entityIcon.getIcon() != null) {
				Resource resource = GMEIconUtil.getLargestImageFromIcon(entityIcon.getIcon());
				return new Future<>(resource);
			}
			
			return new Future<>(null);
		};
		
		return rasterImageResourceProvider;
	}
	
	private Future<Resource> handleMetaData(Set<MetaData> metaData){
		if (metaData == null || metaData.isEmpty())
			return new Future<>(null);
		
		for (MetaData modelMetaData : metaData) {
			if (modelMetaData.entityType() != (Object) Icon.T)
				continue;
			
			Icon modelIcon = (Icon) modelMetaData;
			if (modelIcon.getIcon() != null)
				return new Future<Resource>(getResourceFromIcon(modelIcon));
			
			final EntityType<Icon> entityType = modelIcon.entityType();
			if (!GMEUtil.isPropertyAbsent(modelIcon, entityType.getProperty("icon")))
				continue;
			
			Future<Resource> result = new Future<Resource>();
			
			EntityProperty entityProperty = entityProperty(modelIcon.reference(), "icon");
			PropertyQuery propertyQuery = PropertyQueryBuilder.forProperty(entityProperty).tc(TC.create().negation().joker().done()).done();
			gmSession.query().property(propertyQuery).result(com.braintribe.processing.async.api.AsyncCallback.of( //
					future -> {
						try {
							result.onSuccess(getResourceFromIcon((Icon) future.result().getPropertyValue()));
						} catch (Exception ex) {
							result.onSuccess(null);
							ex.printStackTrace();
						}
					}, e -> {
						e.printStackTrace();
						result.onSuccess(null);
					}));
			
			return result;
		}
		
		return new Future<>(null);
	}

	private Resource getResourceFromIcon(Icon icon) {
		Resource resource = null;
		if (icon != null) {
			com.braintribe.model.resource.Icon iconResource = icon.getIcon();
			resource = GMEIconUtil.getLargestImageFromIcon(iconResource);
		}
		return resource;
	}
	
	/**
	  * Configures the required provider for GmViewActionBar used for action navigation.
	  */
	 @Required
	 public void setGmViewActionBarProvider(Supplier<? extends GmViewActionBar> gmViewActionBarProvider) {
		 this.gmViewActionBarProvider = gmViewActionBarProvider;
	 }
	 
	 /**
	  * Configures the required provider which will provide icons.
	  */
	 @Configurable
	 public void setIconProvider(IconProvider iconProvider) {
		 this.iconProvider = iconProvider;
	 }

	/**
	 * If the session is unable to deal with resources, then this is required.
	 */
	@Configurable
	public void setUrlProvider(Function<Resource, String> urlProvider) {
		this.urlProvider = urlProvider;
	}
	
	/**
	 * Configures the {@link WorkbenchActionSelectionHandler} for selecting between actions to execute.
	 */
	@Configurable
	public void setWorkbenchActionSelectionHandler(WorkbenchActionSelectionHandler workbenchActionSelectionHandler) {
		this.workbenchActionSelectionHandler = workbenchActionSelectionHandler;
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		
		if (iconProvider != null)
			iconProvider.configureGmSession(gmSession);
		
		if (previewUtil != null)
			previewUtil.setSession(gmSession);
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
		
		if (iconProvider != null)
			iconProvider.configureUseCase(useCase);
	}
		
	public void setPreviewUtil(GmPreviewUtil previewUtil) {
		this.previewUtil = previewUtil;
	}
	
	public void setMaxThumbnailSize(int maxThumbnailSize) {
		this.maxThumbnailSize = maxThumbnailSize;
	}
	
	public int getMaxThumbnailWidth() {
		return this.maxThumbnailSize;
	}
	
	public int getMaxThumbnailHeight() {
		return this.maxThumbnailSize;
	}
	
	public void setMinThumbnailSize(int minThumbnailSize) {
		this.minThumbnailSize = minThumbnailSize;
	}
	
	public void setThumbnailSize(int thumbnailSize) {
		this.thumbnailSize = thumbnailSize;
	}
	
	public void setPrepareToolBarActions(boolean prepareToolBarActions) {
		this.prepareToolBarActions = prepareToolBarActions;
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
	
	/**
	 * Configures whether to show the context menu.
	 * Defaults to true.
	 */
	@Configurable
	public void setShowContextMenu(boolean showContextMenu) {
		this.showContextMenu = showContextMenu;
	}
	
	/**
	 * Configures whether to unselect the clicked entry after the selection event is fired.
	 * Defaults to false.
	 */
	@Configurable
	public void setUnselectAfterClick(boolean unselectAfterClick) {
		this.unselectAfterClick = unselectAfterClick;
	}
	
	/**
	 * Configures a set of Entity Type classes that act as simplified by default.
	 */
	@Configurable
	public void setSimplifiedEntityTypes(Set<Class<?>> simplifiedEntityTypes) {
		this.simplifiedEntityTypes = simplifiedEntityTypes;
	}
	
	/**
	 * Configures whether to use the condensation related actions.
	 * Defaults to true.
	 */
	@Configurable
	public void setUseCondensationActions(boolean useCondensationActions) {
		this.useCondensationActions = useCondensationActions;
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
	 * Configures whether we should use the modelSession for resolving the resources. Defaults to false.
	 */
	@Configurable
	public void setUseModelSession(boolean useModelSession) {
		this.useModelSession = useModelSession;
	}
	
	/**
	 * Configures the action supplier which should be showed when no other action is available and there is no selection.
	 */
	@Configurable
	public void setDefaultContextMenuActionSupplier(Supplier<? extends Action> actionSupplier, String actionName) {
		this.defaultContextMenuActionSupplier = actionSupplier;
		this.defaultContextMenuActionName = actionName;
	}
	
	/**
	 * Configures the required workbench action registry.
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}
		
	public ThumbnailPanel() {
		//centerData = new BorderLayoutData();
		exchangeCenterWidget(getEmptyPanel());
		setBorders(false);
		//ScriptInjector.fromUrl("BtClientCustomization/bt-resources/thumbnailPanel/thumbnailPanel.js").inject();
	}
	
	@Override
	public void intializeBean() throws Exception {
		if (actionManager != null && (prepareToolBarActions || showContextMenu))
			actionManager.connect(this);
		
		if (iconProvider != null)
			iconProvider.configureUseCase(useCase);
		//initialized = true;
		currentSelectedItems = new ArrayList<>();
		
		if (gmeDragAndDropSupport != null)
			prepareDropTargetWidget(emptyPanel, -1);
				
		if (showContextMenu) {
			//show Context set for parent, not just for imagesListView, than can have ContextMenu also on Empty Panel
			setContextMenu(actionsContextMenu);
			addBeforeShowContextMenuHandler(event -> {
				boolean clickedElement = false;
				for (Element e : imagesListView.getElements()) {
					Rect eRect = new Rect(e.getAbsoluteLeft(), e.getAbsoluteTop(), e.getOffsetWidth(), e.getOffsetHeight());
					Rect tRect = new Rect(clickX, clickY, 1, 1);
					if (eRect.intersect(tRect) == null)
						continue;
					
					int index = imagesListView.findElementIndex(e);
					ImageResourceModelData modelData = imagesListView.getStore().get(index);
					if (modelData != null && modelData.isBreakModel())
						continue;
					
					if (modelData == null || !imagesListView.getSelectionModel().isSelected(modelData))
						imagesListView.getSelectionModel().select(index, false);
					clickedElement = true;
					break;
				}
				
				if (!clickedElement)
					imagesListView.getSelectionModel().deselectAll();		
			});
			
			addShowContextMenuHandler(event -> updateEmptyMenuItem());
		}		
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (currentSelectedItems != null)
			currentSelectedItems.clear();
		
		if (thumbnailPanelDragSource != null)
			thumbnailPanelDragSource.release();
		
		if (thumbnailPanelDropTarget != null)
			thumbnailPanelDropTarget.release();
	}	
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	@Override
	public String getUseCase() {
		return useCase;
	}
	
	public ListView<ImageResourceModelData, ImageResourceModelData> getImagesListView() {
		if (imagesListView != null)
			return imagesListView;
		
		ListViewCustomAppearance<ImageResourceModelData> appearance = new ListViewCustomAppearance<ImageResourceModelData>(
				"." + "thumbnailPanelSelectionContainer", "thumbnailPanelSelectionContainerOver", "thumbnailPanelSelectionContainerSelected") {
			@Override
			public void renderEnd(SafeHtmlBuilder builder) {
				// String markup = new StringBuilder("<div
				// class=\"").append(CommonStyles.get().clear()).append("\"></div>").toString();
				// builder.appendHtmlConstant(markup);
			}

			@Override
			public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
				// builder.appendHtmlConstant("<div class='thumbnailPanelSelectionContainer'>");
				builder.append(content);
				// builder.appendHtmlConstant("</div>");
			}

			@Override
			public void onSelect(XElement item, boolean select) {
				super.onSelect(item, select);
				item.setClassName(ExtendedListViewDefaultResources.GME_LIST_VIEW_SEL, select);
			}
		};
		
		imagesListView = new ThumbnailListView(this, getImagesListStore(), new IdentityValueProvider<>(), appearance);
	          
		if (gmeDragAndDropSupport != null) {
			Scheduler.get().scheduleDeferred(() -> prepareDropTargetWidget(imagesListView, -1));
			
			thumbnailPanelDragSource = new ThumbnailPanelDragSource(this);
			thumbnailPanelDropTarget = new ThumbnailPanelDropTarget(this);
		}
		
		return imagesListView;
	}
	
	private void updateEmptyMenuItem() {
		boolean emptyMenu = true;
		Widget widget;
		Component menuItem;
		for ( int i = 0; i < actionsContextMenu.getWidgetCount(); i++) {
			widget = actionsContextMenu.getWidget(i);
			if (widget instanceof Component) {				
				menuItem = (Component) widget;
				if (menuItem != emptyMenuItem && menuItem.isVisible(false)) {
					emptyMenu = false;
					break;
				}
			}
		}
		
		emptyMenuItem.setVisible(emptyMenu);
	}
	
	public ListStore<ImageResourceModelData> getImagesListStore() {
		if (imagesListStore != null)
			return imagesListStore;
		
		imagesListStore = new ListStore<>(item -> item != null ? item.getUniqueId() : null);
		imagesListStore.addStoreAddHandler(event -> exchangeCenterWidget(getImagesListView()));
		imagesListStore.addStoreClearHandler(event -> exchangeCenterWidget(getEmptyPanel()));
		imagesListStore.addStoreRemoveHandler(event -> {
			if (imagesListStore.size() == 0)
				exchangeCenterWidget(getEmptyPanel());
		});
		
		return imagesListStore;
	}
	
	public Slider getSizeSlider() {
		if (sizeSlider != null)
			return sizeSlider;
		
		sizeSlider = new Slider();
		sizeSlider.setMaxValue(maxThumbnailSize);
		sizeSlider.setIncrement(1);
		sizeSlider.setMinValue(minThumbnailSize);
		sizeSlider.setWidth("150px");
		sizeSlider.setValue(thumbnailSize);
		sizeSlider.setMessage("{0} pixel");
		sizeSlider.addValueChangeHandler(event -> updateUI(event.getValue()));
		
		return sizeSlider;
	}
	
	private void updateUI(int thumbnailSize){
		this.thumbnailSize = thumbnailSize;
		ListStore<ImageResourceModelData> store = getImagesListStore();
		if (store != null) {
			for(ImageResourceModelData data : store.getAll())
				data.setContainerSize(thumbnailSize);
		}
		ListView<ImageResourceModelData, ImageResourceModelData> listView = getImagesListView();
		if (listView != null)
			listView.refresh();
		//updateModelsVisibility();
	}
	
	private void prepareAbsentProperties(List<Object> listContent) {
		if (listContent == null)
			return;
		
		for (Object object : listContent) {
			if (!(object instanceof GenericEntity))
				continue;
			
			ModelMdResolver modelResolver = getMetaData((GenericEntity) object);
			if (modelResolver == null)
				continue;
			
			EntityMdResolver entityResolver = modelResolver.entity((GenericEntity) object).useCase(this.useCase);
			if (entityResolver == null)
				continue;
			
			GroupBy groupBy = entityResolver.meta(GroupBy.T).exclusive(); 
			if (groupBy == null || groupBy.getGroupName().isEmpty())
				continue;
			
			for (Property property : ((GenericEntity) object).entityType().getProperties()) {
				if (!property.getName().equals(groupBy.getGroupName()))
					continue;
				
				if (!GMEUtil.isPropertyAbsent((GenericEntity) object, property))
					break;
				
				absentPropertyModelList.add((GenericEntity) object);
				loadAbsentProperties((GenericEntity) object, Arrays.asList(property)) //
						.andThen(v -> {
							absentPropertyModelList.remove(object);
							if (absentPropertyModelList.isEmpty()) {
								prepareGroupModels(useGroupTogether);
								// updateUI(thumbnailSize);
							}
						}).onError(e -> {
							ErrorDialog.show("Error load ThumbnailPanel Properties", e);
							e.printStackTrace();
						});
				break;
			}
		}
	}
	
	int contentCount = 0;
	private void loadThumbnails(List<Object> listContent) {
		if (listContent == null)
			return;
			
		prepareAbsentProperties(listContent);
		
		imagesListView.getStore().clearSortInfo();
		imagesListStore.clearSortInfo();
		
		for (Object listObject : listContent) {
			Object theValue = listObject;
			if (listObject instanceof EntityPropertyBean && ((EntityPropertyBean) listObject).getValue() instanceof GenericEntity)
				theValue = ((EntityPropertyBean) listObject).getValue();
			
			if (theValue instanceof GenericEntity) {
				loadThumbnailFromEntity((GenericEntity) theValue, listObject, null);
				continue;
			}
			
			if (!(listObject instanceof EntityPropertyBean))
				continue;
			
			EntityPropertyBean entityPropertyBean = (EntityPropertyBean) listObject;
			ImageResourceModelData data = new ImageResourceModelData();
			String uniqueId = DOM.createUniqueId();
			data.setUniqueId(uniqueId);
			data.setImageHeight(32);
			data.setImageWidth(32);
			data.setContainerSize(thumbnailSize);
			data.setCoverImage(data.getImageWidth() > data.getImageHeight() * coverWidthHeightCoefficient);					

			updateModelForEntityPropertyBean(entityPropertyBean, data, true);

			data.setClassName("thumbnailWatermark");
			
			ImageLoader imageLoader = new ImageLoader();
			//imageLoader.setSrc(ImageResourceModelData.WATERMARK_SRC);
			imageLoader.setDefaultSrc(ImageResourceModelData.WATERMARK_SRC);				
			imageLoader.setImageElementId("thumbnailImage-" + uniqueId);
			imageLoader.addImageLoaderListener(data);
			imageLoader.addImageLoaderListener(imageLoadingChain);
			imageLoader.addImageLoaderListener(ThumbnailPanel.this);
			data.setImageLoader(imageLoader);
			imagesListStore.add(data);
			addEntityListener(entityPropertyBean.getParentEntity());
		}
		
		prepareGroupModels(useGroupTogether);		
	}

	private void prepareModelsAtStore() {
		if (modelsAtStorePrepared)
			return;
		
		//imagesListView.getStore().addAll(imageResourceModelPreparationList);
		updateUI(thumbnailSize);
		doSortByPriority();
		modelsAtStorePrepared = true;
	}

	private void updateModelForGenericEntity(GenericEntity entity, Object object, ImageResourceModelData data) {
		String propertyDisplayName;
		if (!(object instanceof EntityPropertyBean))
			propertyDisplayName = null;
		else {
			propertyDisplayName = ((EntityPropertyBean) object).getPropertyDisplayName();
			data.setEntityPropertyBean((EntityPropertyBean) object);
		}		
		
		data.setCoverImage(data.getImageWidth() > data.getImageHeight() * coverWidthHeightCoefficient);
		
		/*if (entity instanceof com.braintribe.model.document.Document) {
			com.braintribe.model.document.Document ducumentEntity = (com.braintribe.model.document.Document) entity;
			data.setCoverImage(ducumentEntity.getWidthInCm() > ducumentEntity.getHeightInCm() * coverWidthHeightCoefficient);
		}*/
		
		//data.setFileName(result.getInfo().getName());
		//data.setFileSize(result.getFileSize());
		data.setContainerSize(thumbnailSize);
		String selectiveInfo = SelectiveInformationResolver.resolve(entity.entityType(), entity, (ModelMdResolver) null, useCase);
		if (propertyDisplayName != null)
			//selectiveInfo = propertyDisplayName + selectiveInfo;
			data.setBeforeSelectiveInfo(propertyDisplayName);
		data.setSelectiveInfo(selectiveInfo, false);
		
		updateModelMetaData(data, object);
	}
	
	private void updateModelForEntityPropertyBean(EntityPropertyBean entityPropertyBean, ImageResourceModelData data, boolean preparePreviewUrl) {
		String propertyDisplayName = entityPropertyBean.getPropertyDisplayName();
		data.setEntityPropertyBean(entityPropertyBean);
		if (propertyDisplayName != null)
			data.setBeforeSelectiveInfo(propertyDisplayName);
		
		if (entityPropertyBean.isAbsent())
			data.setSelectiveInfo("(absent)", false);
		else if (entityPropertyBean.getValue() instanceof Collection) 
			data.setSelectiveInfo(String.valueOf(((Collection<?>) entityPropertyBean.getValue()).size()), false);
		else
			data.setSelectiveInfo("", false);

		if (preparePreviewUrl)
			getPreviewUrl(data, entityPropertyBean.parentEntity/*, PreviewType.STANDARD, null*/);
						
		//Resolve Property order priority
		updateModelMetaData(data, entityPropertyBean);
	}

	private void updateModelMetaData(ImageResourceModelData data, Object object) {
		if (object instanceof GenericEntity) {
			ModelMdResolver modelResolver = getMetaData((GenericEntity) object);
			if (modelResolver != null) {
				EntityMdResolver entityResolver = modelResolver.entity((GenericEntity) object).useCase(this.useCase);
				if (entityResolver != null) {
					GroupBy groupBy = entityResolver.meta(GroupBy.T).exclusive(); 
					if (groupBy != null)
						data.setGroupName(displayGroupName(/*data,*/ (GenericEntity) object, groupBy.getGroupName()));
				}
			}
		}
				
		if (!(object instanceof EntityPropertyBean))
			return;
		
		if (((EntityPropertyBean) object).getParentEntity() == null)
			return;
		
		ModelMdResolver modelResolver = getMetaData(((EntityPropertyBean) object).getParentEntity());
		EntityMdResolver entityResolver = null;
		if (modelResolver != null) 
			entityResolver = modelResolver.entity(((EntityPropertyBean) object).getParentEntity());
		PropertyMdResolver propertyMdResolver = null;
		if (entityResolver != null)
			propertyMdResolver = entityResolver.property(((EntityPropertyBean) object).getProperty()).useCase(this.useCase);
		Double priority = GMEMetadataUtil.getPropertyPriority(propertyMdResolver);
		if (priority != null)
			data.setPriority(priority);

		if (propertyMdResolver != null) {
			Icon icon = propertyMdResolver.meta(Icon.T).exclusive();
			if (icon != null)
				data.setTypeIcon(icon);
			
			GroupBy groupBy = propertyMdResolver.meta(GroupBy.T).exclusive(); 
            if (groupBy != null)
            	data.setGroupName(displayGroupName(/*data,*/ ((EntityPropertyBean) object).getParentEntity(), groupBy.getGroupName()));			
		}										
	}

	private String displayGroupName(/*ImageResourceModelData model,*/ GenericEntity object, String groupName) {
		if (object == null || groupName == null || groupName.isEmpty())
			return null;
		
		String resString = null;
		if (groupName.toLowerCase().equals("$type")) {
			//group by EntityType
			resString = getShortName(object.type().getTypeName());			
		} else {
		    //group by Property value
			for (Property property : object.entityType().getProperties())					
				if (property.getName().equals(groupName)) {
					resString = prepareStringValue(property.get(object), property.getType(), codecRegistry);
					break;
				}
		}				
		return resString;
	}
	
	private Future<Void> loadAbsentProperties(GenericEntity entity, List<Property> absentProperties) {
		Future<Void> future = new Future<Void>();
		
		MultiLoader multiLoader = new MultiLoader();
		multiLoader.setParallel(false);
		int i = 0;
		for (Property property : absentProperties) {
			multiLoader.add(Integer.toString(i++), GMEUtil.loadAbsentProperty(entity, entity.entityType(), property, gmSession, useCase,
					codecRegistry, specialEntityTraversingCriterion));
		}
		
		multiLoader.load().andThen(result -> future.onSuccess(null)).onError(future::onFailure);
		return future;
	}	
	
	private String getShortName(String fullName) {
		int index = fullName.lastIndexOf(".");
		if (index > 0) {
			index ++;
			return fullName.substring(index);			
		} 
		
		return fullName;
	}	
	
	public String prepareStringValue(Object propertyValue, GenericModelType valueType, CodecRegistry<String> codecRegistry) {
		if (propertyValue == null)
			return "";
		
	    String stringValue = null;				
		if (valueType == null)
			valueType = GMF.getTypeReflection().getType(propertyValue);
		
		if (codecRegistry != null) {
			Codec<Object, String> codec = codecRegistry.getCodec(valueType.getJavaType());
			if (codec != null) {
				try {
					stringValue = codec.encode(propertyValue);
				} catch (CodecException e) {
					e.printStackTrace();
				}
			}
		}			
								
		if (stringValue == null && (valueType instanceof EntityType || valueType instanceof EnumType) && propertyValue instanceof GenericEntity) {
			//RVE - must be used this version, because this get own resolver - for showing value we need original Resolver from where the MetaData are
			String selectiveInformation = SelectiveInformationResolver.resolve((EntityType<?>) valueType, (GenericEntity) propertyValue,
					getMetaData((GenericEntity) propertyValue), useCase);
			if (selectiveInformation != null && !selectiveInformation.trim().isEmpty())
				stringValue =  selectiveInformation;
		}
		
		return stringValue != null ? stringValue : propertyValue.toString();		
	}
	
	private void loadThumbnailFromEntity(GenericEntity entity, Object object, ImageResourceModelData imageResourceModelData) {
		Future<Resource> future = getRasterImageResourceProvider().apply(entity);
		
		if (future == null) {
			--contentCount;
			return;
		}
		
		future.andThen(result -> {
			String resourceSource = null;
			if (result == null && iconProvider != null) {
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(entity.entityType(), entity));
				IconAndType iconAndType = iconProvider.apply(modelPath);
				if (iconAndType != null && iconAndType.getIcon() != null)
					resourceSource = iconAndType.getIcon().getSafeUri().asString();
			}
			
			ImageResourceModelData data = imageResourceModelData;
			if (data == null) {
				data = new ImageResourceModelData();
				String uniqueId = DOM.createUniqueId();
				data.setUniqueId(uniqueId);
			}
			
			data.setEntity(entity);
			data.setOwnerEntity(object);
			//data.setSrc(urlProvider.provide(result));
			
			///need take parameters to be able to Cover Images
			PixelDimensionSpecification pixelDimensionSpecification = null;
			if (result != null) {
				ResourceSpecification specification = result.getSpecification();
				if (specification instanceof PixelDimensionSpecification)
					pixelDimensionSpecification = (PixelDimensionSpecification) specification;
			}
			data.setImageHeight(pixelDimensionSpecification != null ? pixelDimensionSpecification.getPixelHeight() : 32);
			data.setImageWidth(pixelDimensionSpecification != null ? pixelDimensionSpecification.getPixelWidth() : 32);
							
			updateModelForGenericEntity(entity, object, data);
			
			getPreviewUrl(data, entity/*, PreviewType.STANDARD, null*/);
			ImageLoader imageLoader = new ImageLoader();
			if (result == null) {
				if (resourceSource != null) {
					imageLoader.setSrc(resourceSource);
					data.setClassName("");
				} else {
					if (entity instanceof GmMetaModel) {
						imageLoader.setSrc(ImageResourceModelData.MODEL_ICON_SRC);
						imageLoader.setDefaultSrc(ImageResourceModelData.MODEL_ICON_SRC);
						data.setClassName("");
					} else {
						//imageLoader.setSrc(ImageResourceModelData.WATERMARK_SRC);
						imageLoader.setDefaultSrc(ImageResourceModelData.WATERMARK_SRC);
						data.setClassName("thumbnailWatermark");	
					}
				}
			} else {
				if (urlProvider != null)
					imageLoader.setSrc(urlProvider.apply(result));
				else {
					ManagedGmSession theSession = gmSession;
					if (useModelSession && theSession.getModelAccessory().getModelSession() != null)
						theSession = theSession.getModelAccessory().getModelSession();
					imageLoader.setSrc(theSession.resources().url(result).asString());
				}
				data.setClassName("");
			}
			
			imageLoader.setImageElementId("thumbnailImage-" + data.getUniqueId());
			
			data.setImageLoader(imageLoader);
			
			imageLoader.addImageLoaderListener(data);
			imageLoader.addImageLoaderListener(imageLoadingChain);
			imageLoader.addImageLoaderListener(ThumbnailPanel.this);
			/*
			ListStore<ImageResourceModelData> store = getImagesListStore();
			if (store != null)
				store.add(data);
			*/
			if (imageResourceModelData != null)
				imagesListView.getStore().update(data);
			else {
				imagesListView.getStore().add(data);
				//imageResourceModelPreparationList.add(data);
				addEntityListener(data.getEntity());
			}
			
			if (--contentCount == 0)
				prepareModelsAtStore();
		}).onError(e -> {
			e.printStackTrace();
			if (--contentCount == 0)
				updateUI(thumbnailSize);
		});
	}
	
	/**
	private void loadModels(int modelCount){
		for(int i = 0;i<modelCount;i++){			
			ImageResourceModelData data = new ImageResourceModelData();
			//int index = getRandom(0,7);			
			//Image image = new Image(testUrls[index]);			
			//data.setImage(image);
			data.setContainerSize(thumbnailSize);
			getImagesListStore().add(data);
//			ThumbnailElement thumbnailElement = new ThumbnailElement(testUrls[index], thumbnailSize);
//			getThumbnailElementsPanel().addThumbnailElement(thumbnailElement);
		}
		getImagesListView().refresh();
	}
	*/
	
	/*private double getStartupModelCount(){
		double column = 0, row = 0;
		
		column = (int)(getOffsetWidth() /  (thumbnailSize));
		row = (int)(getOffsetHeight() / ((2 * thumbnailMargin) + (thumbnailSize)));
		
		return column * row;
	}
	
	private int getRandom(int min, int max) {
		 if(min > max) {
		  return -1;
		 }
		 
		 if(min == max) {
		  return min;
		 }
		 
		 double r;
		 
		 do {
		  r = Math.random();
		 }
		 while(r == 1.0);
		 
		 return min + (int)(r * (max-min+1));
	}*/
	
	public boolean reachEnd(){
		int horizontalScrollPosition = getAbsoluteLeft();
		int verticalScrollPosition = getAbsoluteTop();
		
		int scrollWidth = getOffsetWidth();
		int scrollHeight = getOffsetHeight();
		
		Rect window = new Rect(horizontalScrollPosition, verticalScrollPosition, scrollWidth, scrollHeight);		
		Element lastWidget = getImagesListView().getElement(getImagesListView().getItemCount()-1);
		
		if (lastWidget == null)
			return false;		
		
		int x =	lastWidget.getAbsoluteLeft();
		int y = lastWidget.getAbsoluteTop();
		int w = lastWidget.getOffsetWidth();
		int h = lastWidget.getOffsetHeight();
		Rect pageRect = new Rect(x, y, w, h);
		//int scrollPos = getVerticalScrollPosition();
		Rect intersection = window.intersect(pageRect);
		return intersection != null;
	}
	
	/*
	private void updateModelsVisibility(){
		if(imagesListView.isRendered()){
			Rect window = new Rect(imagesListView.getAbsoluteLeft(), imagesListView.getAbsoluteTop(), imagesListView.getOffsetWidth(), imagesListView.getOffsetHeight());
			for(int x = 0; x < getImagesListView().getElements().size(); x++) {	
				Element element = getImagesListView().getElement(x);
								
				if (element != null) {					
					int size = getImagesListStore().size();
					if(x < size){
						ImageResourceModelData data = getImagesListStore().get(x);
						if (data != null) {
							Rect elementRect = new Rect(element.getAbsoluteLeft(), element.getAbsoluteTop(), element.getClientWidth(), element.getClientHeight());
							if(elementRect.intersect(window) != null){
								data.setVisible(true);
								//System.err.println(element.getId() + " got visible");
							}
							else
								data.setVisible(false);
						}
					}
				}
			}
		}
	}
	*/
	
	public List<Element> getVisibleElements() {
		
		Rect window = new Rect(imagesListView.getAbsoluteLeft(), imagesListView.getAbsoluteTop(), imagesListView.getOffsetWidth(),
				imagesListView.getOffsetHeight());	
		if (getImagesListStore().getAll().size() == 0)
			return Collections.<Element>emptyList();
		
		int containerWidth = imagesListView.getOffsetWidth();
		
		int cols = imagesListView.getOffsetWidth() / (thumbnailSize);
		int rows = (int) (imagesListView.getOffsetHeight() / ((2 * thumbnailMargin) + (thumbnailSize)));
		int colAdvance = thumbnailSize;
		int rowAdvance = (int) (thumbnailSize + thumbnailMargin * 2);
		int coreWidth = colAdvance * cols;
		int xoffset = containerWidth - coreWidth;
		double sx = window.getX();
		double ex = window.getMaxX();
		double sy = window.getY();
		double ey = window.getMaxY();
		
		int xOffset = (int) (thumbnailMargin * 2 + xoffset);
		int yOffset = (int) (thumbnailMargin * 2);
				
		int scol = (int)(sx - xOffset) / (colAdvance==0 ? 1 : colAdvance);
		int ecol = (int)(ex - xOffset) / (colAdvance==0 ? 1 : colAdvance);
		
		if (sx - scol * colAdvance > thumbnailSize)
			scol++;
		
		int srow = (int)(sy - yOffset) / rowAdvance;
		int erow = (int)(ey - yOffset) / rowAdvance;
		
		if (sy - srow * rowAdvance > thumbnailSize)
			srow++;
		
		srow = Math.max(0, srow);
		erow = Math.min(rows - 1, erow);
		scol = Math.max(0, scol);
		ecol = Math.min(cols -1, ecol);
		
		int rowCount = erow - srow + 1;
		int colCount = ecol - scol + 1;
		
		List<Element> elements = new ArrayList<Element>(Math.max(1,rowCount * colCount));
		
		for (int r = srow; r <= erow; r++) {
			int rowPageIndex = r * cols;
			for (int c = scol; c <= ecol && (rowPageIndex + c) < getImagesListStore().size(); c++) {
				int pageIndex = rowPageIndex + c;
				Element cellElement = getLayoutCell(pageIndex);
				elements.add(cellElement);
			}
		}
		
		/*List<Element> elements = new ArrayList<Element>();
		
		getImagesListView().getStore().getModels();
		
		for(Element element : getImagesListView().getElements()){
			Rect elementRect = new Rect(element.getAbsoluteLeft(), element.getAbsoluteTop(), element.getClientWidth(), element.getClientHeight());
			if(elementRect.intersect(window) != null)
				elements.add(element);
		} */
		
		return elements;
	}
	
	public Element getLayoutCell(int pageIndex) {
		String id =  "thumbnailPanelSelectionContainer" + '-' + pageIndex;
		Element cellElement = Document.get().getElementById(id);
		return cellElement;
	}

	public ModelPath getRootModelPath() {
		return rootModelPath;
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;
	}
	
	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		return actionManager;
	}
	
	@Override
	public void addGmViewportListener(GmViewportListener vl) {
		if (vl != null) {
			if (gmViewportListeners == null)
				gmViewportListeners = new ArrayList<GmViewportListener>();
			gmViewportListeners.add(vl);
		}
	}

	@Override
	public void removeGmViewportListener(GmViewportListener vl) {
		if (gmViewportListeners != null) {
			gmViewportListeners.remove(vl);
			if (gmViewportListeners.isEmpty())
				gmViewportListeners = null;
		}
	}

	@Override
	public boolean isWindowOverlappingFillingSensorArea() {
//		//Check if the last page is visible.
//		El viewEl = imagesListView.el();
//		
//		if (viewEl == null)
//			return false;
//		
//		if (!viewEl.isScrollableY())
//			return true;
//		
//		int visibleViewHeight = viewEl.dom.getClientHeight();
//		//Whole View - current scroll - visible view
//		return viewEl.dom.getScrollHeight() /*- POINT */ - visibleViewHeight < visibleViewHeight;
		
		return reachEnd() ? true : false;
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		if (sl != null) {
			if (gmSelectionListeners == null) {
				gmSelectionListeners = new ArrayList<>();
			}
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
		selectedModel = getImagesListView().getSelectionModel().getSelectedItem();
		return getModelPath(selectedModel);
	}
	
	@Override
	public int getFirstSelectedIndex() {
		ImageResourceModelData selectedItem = getImagesListView().getSelectionModel().getSelectedItem();
		if (selectedItem == null)
			return -1;
		
		int index = -1;
		for (ImageResourceModelData modelItem : imagesListStore.getAll()) {
			if (!modelItem.isGroupModel())
				index++;

			if (modelItem == selectedItem)
				return index;
		}

		return -1;
	}
	
	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		List<ModelPath> modelPaths = null;
		List<ImageResourceModelData> selectedModels = getImagesListView().getSelectionModel().getSelectedItems();
		if (selectedModels != null && !selectedModels.isEmpty()) {
			modelPaths = new ArrayList<>();
			for (ImageResourceModelData itemSelectedModel : selectedModels) {
				if (itemSelectedModel != null && !itemSelectedModel.isGroupModel())
					modelPaths.add(getModelPath(itemSelectedModel));
			}
		} 
		return modelPaths;
	}

	@Override
	public boolean isSelected(Object element) {
		List<ImageResourceModelData> selectedModels = getImagesListView().getSelectionModel().getSelectedItems();
		if (selectedModels != null && !selectedModels.isEmpty()) {
			for (ImageResourceModelData selectedModel : selectedModels) {
				if (selectedModel.refersTo(element) && !selectedModel.isGroupModel())
					return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean selectVertical(Boolean next, boolean keepExisting) {
		ListView<ImageResourceModelData, ImageResourceModelData>  listView = getImagesListView();
		if (listView instanceof ThumbnailListView) {
			Console.log("TP selectVertical");
			return ((ThumbnailListView) listView).selectNextVerticalItem(next, keepExisting, false);
		}
		Console.log("TP selectVertical ret false");
	    return false;
	}
	
	@Override
	public boolean selectHorizontal(Boolean next, boolean keepExisting) {
		ListView<ImageResourceModelData, ImageResourceModelData>  listView = getImagesListView();
		if (listView instanceof ThumbnailListView)
			return ((ThumbnailListView) listView).selectNextHorizontalItem(next, keepExisting, false);
		
		return false;
	}
	
	@Override
	public void select(int index, boolean keepExisting) {
		// RVE - changed to skip groups indexes
		int i = -1;
		ImageResourceModelData selectedModel = null;
		for (ImageResourceModelData modelItem : imagesListStore.getAll()) {
			if (!modelItem.isGroupModel())
				i++;

			if (i == index) {
				selectedModel = modelItem;
				getImagesListView().getSelectionModel().select(modelItem, keepExisting);
				break;
			}
		}
		
		fireGmSelectionListeners(); //This is needed due to a bug in the selectionModel. It expects selections to be clicks.
		
		if (selectedModel != null) {
			Scheduler.get().scheduleDeferred(() -> {
				ListViewSelectionModel<ImageResourceModelData> selectionModel = getImagesListView().getSelectionModel();
				if (selectionModel instanceof ThumbnailListViewSelectionModel)
					((ThumbnailListViewSelectionModel<?>) selectionModel).focusItem(index);
			});
		}
		
		/*
		while (index < getImagesListView().getStore().size()) {
			ImageResourceModelData model = getImagesListView().getStore().get(index);
			if (model == null)
				return;
		
			if (!model.isGroupModel()) {
				getImagesListView().getSelectionModel().select(index, keepExisting);
				return;
			}
			index++;
		}
		*/
	}
	
	@Override
	public void deselectAll() {
		if (imagesListView != currentWidget || imagesListView == null)
			return;
		
		getImagesListView().getSelectionModel().deselectAll();
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
	public void setGmContentContext(GmContentContext context) {
		this.gmContentContext = context;
	}
	
	@Override
	public GmContentContext getGmContentContext() {
		return this.gmContentContext;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		setContent(modelPath, true);
	}
	
	@Override
	public void addContent(ModelPath modelPath) {
		setContent(modelPath, false);
	}
	
	@Override
	public void onPreviewRefresh(RefreshPreview refreshPreview) {
		ImageResourceModelData updateData = null;
		for (ImageResourceModelData data : imagesListView.getStore().getAll()) {
			if (data == null || data.getEntity() == null)
				continue;

			//RVE - actually remove check for TypeSignature, as Custom types returns Parent type
			//if (data.getEntity().type().getTypeSignature().equals(refreshPreview.getTypeSignature()) && 
			//		data.getEntity().getId().equals(refreshPreview.getId())	) {
			
			if (data.getEntity().getId().equals(refreshPreview.getId())) {
				updateData = data;
				break;
			}							
		}
		
		if (updateData != null) {
			loadThumbnailFromEntity(updateData.getEntity(), updateData.getEntity(), updateData);
		}
	}	
	
	private void setContent(ModelPath modelPath, boolean initialData) {
		boolean addingInitialElements = initialData ? initialData : rootModelPath == null && (addedModelPaths == null || addedModelPaths.isEmpty());
		
		if (initialData) {
			rootModelPath = modelPath;
			if (addedModelPaths != null)
				addedModelPaths.clear();
			if (deletedEntities != null) {
				deletedEntities.clear();
				deletedEntities = null;
			}		
			currentSelectedItems.clear();
			absentPropertyModelList.clear();
		} else {
			if (addedModelPaths == null)
				addedModelPaths = new ArrayList<>();
			addedModelPaths.add(modelPath);
		}
		
		List<Object> newValue = null;
		entityTypeForProperties = null;
		GenericEntity parentEntity = null;
		
		if (modelPath != null) {
			Object value = modelPath.last().getValue();
			if (value instanceof GenericEntity) {
				newValue = handleEntitySet((GenericEntity) value, (EntityType<GenericEntity>) modelPath.last().getType());
				parentEntity = ((GenericEntity) value);
			} else if (value instanceof List) {
				newValue = (List<Object>) modelPath.last().getValue();
				if (newValue.size() == 1 && ((CollectionType) modelPath.last().getType()).getCollectionElementType() instanceof EntityType<?>) {
					entityTypeForProperties = (EntityType<GenericEntity>) ((CollectionType) modelPath.last().getType()).getCollectionElementType();
					parentEntity = (GenericEntity) newValue.get(0);
				}
			} else if (value instanceof Set) {
				newValue = new ArrayList<>();
				for (Object element : ((Set<Object>) value))
					newValue.add(element);
				
				if (newValue.size() == 1 && ((CollectionType) modelPath.last().getType()).getCollectionElementType() instanceof EntityType<?>) {
					entityTypeForProperties = (EntityType<GenericEntity>) ((CollectionType) modelPath.last().getType()).getCollectionElementType();
					parentEntity = (GenericEntity) newValue.get(0);
				}
			} else
				newValue = Arrays.asList(modelPath.last().getValue());
		}
		
		List<Object> listContent = null;
		
		if (initialData) {
			if (imagesListView == null)
				currentSelectedItems.clear();
			else { 
				List<ImageResourceModelData> selectedItems = imagesListView.getSelectionModel().getSelectedItems();
				for (ImageResourceModelData selectedItem : selectedItems)
					if (selectedItem.getEntity() != null)
						currentSelectedItems.add(selectedItem.getEntity());
					else if (selectedItem.getEntityPropertyBean() != null)
						currentSelectedItems.add(selectedItem.getEntityPropertyBean().getProperty());						
					//currentSelectedItems.add(imagesListView.getStore().indexOf(selectedItem));
			}
			
			currentListContent = new ArrayList<>();
			if (newValue != null)
				listContent = new ArrayList<>(newValue);
			else
				listContent = new ArrayList<>();				
			imageLoadingChain.clear();
			ListStore<ImageResourceModelData> store = getImagesListStore();
			if (store != null)
				store.clear();
			imageResourceModelPreparationList.clear();
			modelsAtStorePrepared = false;
		} else {
			listContent = new ArrayList<>();
			//RVE - add only new objects not all (problem in lazy loading)				
			for (Object object: newValue) {
				if (!currentListContent.contains(object))
					listContent.add(object);
			}			
		}
		
		currentListContent.addAll(listContent);
		
		contentCount = currentListContent.size();
		if (getImagesListView().isRendered()) {
			if (contentCount == 0)
				imagesListView.mask("No Elements Found");
			else
				imagesListView.unmask();
		}
		
		boolean loadModels = true;
		if (entityTypeForProperties != null) {
			String condensedPropertyName = condensedTypes.get(entityTypeForProperties);
			if (condensedPropertyName == null)
				condensedPropertyName = getCondensedPropertyFromMetaModel(parentEntity, entityTypeForProperties);
			
			if (condensedPropertyName != null) {
				loadModels = false;
				condenseEntity(parentEntity, entityTypeForProperties, condensedPropertyName);
			}
		}
		
		if (modelPath == null)
			return;
		
		if (loadModels) 
			loadThumbnails(listContent);
		
		if (addingInitialElements)
			fireListContentSet();

		/*
		if (currentSelectedItems != null && !currentSelectedItems.isEmpty()) {
			for (Integer index : currentSelectedItems)
				select(index, true);
			currentSelectedItems.clear();
		}
		*/
		if (currentSelectedItems == null || currentSelectedItems.isEmpty()) {
			doSortByPriority();
			return;
		}
		
		ImageResourceModelData modelToSelect = null;
		for (Object object : currentSelectedItems) {
			if (object == null)
				continue;
			for (ImageResourceModelData item : imagesListView.getStore().getAll()) {
				Object compareObject = null;
				if (item.getEntity() != null)
					compareObject = item.getEntity();
				else if (item.getEntityPropertyBean() != null)
					compareObject = item.getEntityPropertyBean().getProperty();
				if (object.equals(compareObject)) {
					modelToSelect = item;
					getImagesListView().getSelectionModel().select(item, true);
					break;
				}
			}
		}
		
		if (currentSelectedItems.size() == 1 && modelToSelect != null) {
			ImageResourceModelData model = modelToSelect;
			new Timer() {
				@Override
				public void run() {
					ListViewSelectionModel<ImageResourceModelData> selectionModel = getImagesListView().getSelectionModel();
					if (selectionModel instanceof ThumbnailListViewSelectionModel) {
						int index = getImagesListStore().indexOf(model);
						((ThumbnailListViewSelectionModel<?>) selectionModel).focusItem(index);
					}
				}
			}.schedule(100);
		}
		currentSelectedItems.clear();
		
		doSortByPriority();
	}
	
	@Override
	public List<ModelPath> getAddedModelPaths() {
		return addedModelPaths;
	}
	
	@Override
	public ModelPath getContentPath() {
		return rootModelPath;
	}
	
	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		this.externalActions = externalActions;
		
		if (externalActions != null) {
			if (actionProviderConfiguration != null)
				actionProviderConfiguration.addExternalActions(externalActions);
			if (actionsContextMenu != null  && actionManager != null) //Already initialized
				actionManager.addExternalActions(this, externalActions);
		}
	}
	
	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return externalActions;
	}
	
	@Override
	public ActionProviderConfiguration getActions() {
		if (actionProviderConfiguration != null)
			return actionProviderConfiguration;
		
		actionProviderConfiguration = new ActionProviderConfiguration();
		actionProviderConfiguration.setGmContentView(this);
		
		List<Pair<ActionTypeAndName, ModelAction>> knownActions = null;
		if (actionManager != null)
			knownActions = actionManager.getKnownActionsList(this);
		if (knownActions != null || externalActions != null) {
			List<Pair<ActionTypeAndName, ModelAction>> allActions = new ArrayList<>();
			if (knownActions != null)
				allActions.addAll(knownActions);
			if (externalActions != null)
				allActions.addAll(externalActions);
			
			actionProviderConfiguration.addExternalActions(allActions);
		}
		
		return actionProviderConfiguration;
		
		//if (buttonsMap != null)
			//actionProviderConfiguration.setExternalButtons(new LinkedHashMap<String, TextButton>(buttonsMap));
	
		/*if (actionProviderConfiguration == null) {
			actionProviderConfiguration = new ActionProviderConfiguration();
			actionProviderConfiguration.addExternalActions(externalActions);
			
			if (prepareToolBarActions) {
				buttonsMap = new LinkedHashMap<String, TextButton>();
				
				Map<String, TextButton> externalButtons = GMEUtil.prepareExternalActionButtons(externalActions);
				if (externalButtons != null)
					buttonsMap.putAll(externalButtons);
			}
			
			if (buttonsMap != null)
				actionProviderConfiguration.setExternalButtons(new LinkedHashMap<String, TextButton>(buttonsMap));
		}*/
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return filterExternalActions;
	}
	
	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		if (!showContextMenu)
			return;
		
		if (emptyMenuItem == null) {
			if (defaultContextMenuActionSupplier != null) {
				defaultContextMenuAction = defaultContextMenuActionSupplier.get();
				emptyMenuItem = new MenuItem(defaultContextMenuActionName);
				//emptyMenuItem.setIcon(defaultContextMenuAction.getHoverIcon());
				emptyMenuItem.addSelectionHandler(event -> defaultContextMenuAction.perform(null));
			} else {
				emptyMenuItem = new MenuItem(LocalizedText.INSTANCE.noItemsToDisplay());
				emptyMenuItem.setEnabled(false);
			}
			emptyMenuItem.setVisible(false);
		}
		
		menuItemsList = new ArrayList<>();
		menuItemsList.add(new Pair<>("Not Known", emptyMenuItem));
		
		List<Pair<String, MenuItem>> externalMenuItems = GMEUtil.prepareExternalMenuItems(externalActions);
		if (externalMenuItems != null)
			menuItemsList.addAll(externalMenuItems);
				
		if (actionManager != null) {
			//actionManager.addExternalComponents(this, menuItemsList);
			Widget actionMenu = actionManager.getActionMenu(this, menuItemsList, filterExternalActions);
			if (actionMenu instanceof Menu)
				actionsContextMenu = (Menu) actionMenu;
		}
	}

	@Override
	public void addGmContentViewListener(GmContentViewListener listener) {
		if (listener != null) {
			if (listViewListeners == null)
				listViewListeners = new ArrayList<GmContentViewListener>();
			listViewListeners.add(listener);
		}
	}
	
	@Override
	public void removeGmContentViewListener(GmContentViewListener listener) {
		if (listener != null && listViewListeners != null) {
			listViewListeners.remove(listener);
			if (listViewListeners.isEmpty())
				listViewListeners = null;
		}
	}

	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		//this.typeForCheck = typeForCheck;
	}
	
	private void fireListContentSet() {
		if (listViewListeners != null) {
			for (GmContentViewListener listener : listViewListeners)
				listener.onContentSet(this);
		}
	}
	
	protected void fireGmSelectionListeners() {
		if (gmSelectionListeners != null) {
			for (GmSelectionListener listener : gmSelectionListeners)
				listener.onSelectionChanged(this);
		}
	}
	
	protected ModelPath getModelPath(ImageResourceModelData model) {
		ModelPath modelPath = null;
		
		if (rootModelPath != null) {
			modelPath = new ModelPath();
			for (ModelPathElement element : rootModelPath)
				modelPath.add(element.copy());
		}
		
		if (model == null || model.isGroupModel())
			return modelPath;		
		
		if (modelPath == null)
			modelPath = new ModelPath();
		
		if (model.getEntityPropertyBean() != null) {
			EntityPropertyBean bean = model.getEntityPropertyBean();
			modelPath.add(new PropertyPathElement(bean.getParentEntity(), bean.getProperty(), bean.getValue()));
			return modelPath;
		}
		
		if (modelPath.isEmpty() || !(modelPath.last() instanceof PropertyRelatedModelPathElement)) {
			modelPath.clear();
			EntityType<?> entityType = model.getEntity().entityType();
			modelPath.add(0, new RootPathElement(entityType, model.getEntity()));
			return modelPath;
		}
			
		if (modelPath.last() instanceof PropertyRelatedModelPathElement && modelPath.last().getType() instanceof CollectionType) {
			PropertyRelatedModelPathElement propertyRelatedModelPathElement = (PropertyRelatedModelPathElement) modelPath.last();
			CollectionType collectionType = modelPath.last().getType();
			GenericEntity entity = model.getEntity();
			EntityType<?> entityType = entity.entityType();
			if (collectionType.getCollectionKind().equals(CollectionKind.list)) {
				List<Object> list = modelPath.last().getValue();
				ListItemPathElement listItemPathElement = new ListItemPathElement(
						propertyRelatedModelPathElement.getEntity(),
						propertyRelatedModelPathElement.getProperty(),
						list.indexOf(entity),
						entityType,
						entity);
				modelPath.add(listItemPathElement);
			} else if (collectionType.getCollectionKind().equals(CollectionKind.set)) {
				SetItemPathElement setItemPathElement = new SetItemPathElement(
						propertyRelatedModelPathElement.getEntity(),
						propertyRelatedModelPathElement.getProperty(),
						entityType,
						entity);
				modelPath.add(setItemPathElement);
			}
		}
		
		return modelPath;
	}

	@Override
	public void addCheckListener(GmCheckListener listener) {
		if (listener != null) {
			if (gmCheckListeners == null)
				gmCheckListeners = new ArrayList<GmCheckListener>();
			gmCheckListeners.add(listener);
		}
	}

	@Override
	public void removeCheckListener(GmCheckListener listener) {
		if (listener != null && gmCheckListeners != null) {
			gmCheckListeners.remove(listener);
			if (gmCheckListeners.isEmpty())
				gmCheckListeners = null;
		}
	}

	@Override
	public ModelPath getFirstCheckedItem() {
		if (selectedModel == null)
			return rootModelPath != null ? rootModelPath : null;
		
		Resource imageResource = selectedModel.getRasterImageResource();
		ModelPath modelPath = new ModelPath();
		RootPathElement rootPathElement = new RootPathElement(Resource.T, imageResource);
		modelPath.add(rootPathElement);
		return modelPath;
	}

	@Override
	public List<ModelPath> getCurrentCheckedItems() {
		return null;
	}

	@Override
	public boolean isChecked(Object element) {
		return false;
	}
	
	@Override
	public boolean uncheckAll() {
		return false;
	}
	
	@Override
	public void onLoadingStatusChanged(ImageLoader imageLoader) {
		/*if(imageLoader.getParentLoadingChaing() != null && imageLoader.getParentLoadingChaing().isLoadingChainEmpty() && imageLoader.getParentLoadingChaing().isWaitingChainEmpty()){
			switch(imageLoader.getImageLoadingStatus()){
			case LOADED:
				getImagesListView().refresh();
				break;
			default:
				break;
			}
		}*/
	}
	
	@Override
	public void onVisibiltyChanged(ImageResourceModelData imageResourceModelData) {
		//Nothing to do
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		ListView<ImageResourceModelData, ImageResourceModelData> listView = getImagesListView();
		if (listView != null && listView.isRendered())
			updateUI(thumbnailSize);
	}
	
	private List<Object> handleEntitySet(GenericEntity parentEntity, EntityType<GenericEntity> entityType) {
		List<Object> list = new ArrayList<>();
		entityTypeForProperties = entityType;
		
		if (entityType.getProperties() == null)
			return list;
		
		for (Property property : entityType.getProperties()) {
			GenericModelType propertyType = property.getType();
			if (!(propertyType instanceof EntityType) && !(propertyType instanceof CollectionType) && !(propertyType instanceof BaseType)) {
				Outline outline = getMetaData(parentEntity).entity(parentEntity).useCase(useCase).property(property).meta(Outline.T).exclusive();
				if (outline == null)
					continue;
			}
			
			Object propertyValue = null;
			if (!(propertyType instanceof BaseType))
				propertyValue = property.get(parentEntity);
			else if (!GMEUtil.isPropertyAbsent(parentEntity, property)) {
				propertyValue = property.get(parentEntity);
				if (!(propertyValue instanceof GenericEntity))
					continue;
				propertyType = ((BaseType) propertyType).getActualType(parentEntity);
			}
			
			if (simplifiedEntityTypes != null && simplifiedEntityTypes.contains(propertyType.getJavaType()))
				continue;
			
			if (propertyType instanceof EntityType) {
				//RVE - reworking to get correct MetaDataResolver
				//EntityMdResolver entityContextBuilder;
				ModelMdResolver metaDataResolver;
				if (propertyValue instanceof GenericEntity) {
					//entityContextBuilder = getMetaData((GenericEntity) propertyValue).entity((GenericEntity) propertyValue);
					metaDataResolver = getMetaData(parentEntity).useCase(useCase);
				} else {
					//entityContextBuilder = gmSession.getModelAccessory().getMetaData().entityType((EntityType<?>) propertyType);
					metaDataResolver = gmSession.getModelAccessory().getMetaData().useCase(useCase);
				}
				
				Boolean isSimpleOrSimplified = metaDataResolver.entityType((EntityType<?>) propertyType).is(Inline.T);
				if (isSimpleOrSimplified)
					continue;

				/*
				Inline entitySimplification = entityContextBuilder
						.useCase(useCase)
						.meta(Inline.T)
						.exclusive();
				if (entitySimplification != null && entitySimplification.isTrue())
					continue;
				*/	
			}
			
			PropertyMdResolver propertyMdResolver = getMetaData(parentEntity).entity(parentEntity).useCase(useCase).property(property);
			
			if (!propertyMdResolver.is(Visible.T))
				continue;
			
			boolean absent = GMEUtil.isPropertyAbsent(parentEntity, property);
			
			Name name = propertyMdResolver.meta(Name.T).exclusive();
			String propertyDisplayname = (name != null && name.getName() != null //
					? I18nTools.getLocalized(name.getName()) : property.getName()) //
					+ ":";
			
			if (absent || propertyValue == null)
				list.add(new EntityPropertyBean(property, propertyDisplayname, null, parentEntity, absent));
			else
				list.add(new EntityPropertyBean(property, propertyDisplayname, propertyValue, parentEntity, absent));
		}
		
		return list;
	}
	
	@Override
	public EntityType<GenericEntity> getEntityTypeForProperties() {
		return entityTypeForProperties;
	}
	
	@Override
	public boolean isLocalCondensationEnabled() {
		return false;
	}
	
	@Override
	public boolean checkUncondenseLocalEnablement() {
		return false;
	}
	
	@Override
	public String getCondensendProperty() {
		List<ImageResourceModelData> models = imagesListView.getSelectionModel().getSelectedItems();
		ImageResourceModelData model = null;
		if (models != null && !models.isEmpty())
			model = models.get(models.size() - 1);
		
		if (model == null)
			return null;
		
		String condensedProperty = null;
		GenericEntity entity = null;
		EntityType<?> entityType = null;
		if (model.getEntity() != null)
			entity = model.getEntity();
		else if (model.getEntityPropertyBean() != null)
			entity = model.getEntityPropertyBean().getParentEntity();
		
		if (entity != null)
			entityType = entity.entityType();
		
		if (entityType != null)
			condensedProperty = getCondensedProperty(entity, entityType);
		
		return condensedProperty;
	}
	
	@Override
	public String getCurrentCondensedProperty(EntityType<?> entityType) {
		return getCondensedProperty(null, entityType);
	}
	
	@Override
	public void condenseLocal() {
		//Nothing to do
	}
	
	@Override
	public void condense(String propertyName, CondensationMode condensationMode, EntityType<?> entityType) {
		condenseEntity(entityType, propertyName, condensationMode);
		
		Scheduler.get().scheduleDeferred(() -> getGmViewActionBar()
				.navigateToAction(new ActionTypeAndName(ExchangeContentViewActionFolderContent.T, KnownActions.EXCHANGE_CONTENT_VIEW.getName())));
	}
	
	@Override
	public void uncondenseLocal() {
		//Nothing to do
	}
	
	@Override
	public GmViewActionBar getGmViewActionBar() {
		if (gmViewActionBar == null)
			gmViewActionBar = gmViewActionBarProvider.get();
		
		return gmViewActionBar;
	}
	
	@Override
	public boolean isUseCondensationActions() {
		return useCondensationActions;
	}
	
	private void exchangeCenterWidget(Widget widget) {
		if (currentWidget == widget)
			return;
		
		boolean doLayout = false;
		if (currentWidget != null) {
			this.remove(currentWidget);
			doLayout = true;
		}
		currentWidget = widget;
		setCenterWidget(widget);
		if (doLayout)
			this.forceLayout();
	}
	
	private HTML getEmptyPanel() {
		if (emptyPanel == null) {
			emptyPanel = new HTML();
			
			StringBuilder html = new StringBuilder();
			html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
			html.append("<div style='display: table-cell; vertical-align: middle'>").append(LocalizedText.INSTANCE.noItemsToDisplay())
					.append("</div></div>");
			emptyPanel.setHTML(html.toString());
		}
		
		return emptyPanel;
	}
	
	private void condenseEntity(EntityType<?> entityType, String collectionPropertyName, CondensationMode condensationMode) {
		ImageResourceModelData selectedItem = imagesListView.getSelectionModel().getSelectedItem();
		imagesListStore.clear();
		imageResourceModelPreparationList.clear();
		modelsAtStorePrepared = false;
		ImageResourceModelData modelToSelect = null;
		if (collectionPropertyName != null) {
			Property property = entityType.getProperty(collectionPropertyName);
			if (property.getType() instanceof CollectionType) {
				markEntityTypeAsCondensed(entityType, collectionPropertyName, condensationMode);
				
				List<ImageResourceModelData> condensedModels = condenseEntity(selectedItem, entityType, collectionPropertyName);
				if (condensedModels != null && !condensedModels.isEmpty())
					modelToSelect = condensedModels.get(0);
			}
		} else {
			uncondensedTypes.add(entityType);
			List<ImageResourceModelData> uncondensedModels = uncondenseEntity((EntityType<GenericEntity>) entityType);
			if (uncondensedModels != null && !uncondensedModels.isEmpty())
				modelToSelect = uncondensedModels.get(0);
		}
		
		if (modelToSelect != null)
			imagesListView.getSelectionModel().select(modelToSelect, false);
	}
	
	private List<ImageResourceModelData> condenseEntity(ImageResourceModelData model, EntityType<?> entityType, String collectionPropertyName) {
		GenericEntity entity;
		if (model.getEntityPropertyBean() != null)
			entity = model.getEntityPropertyBean().getParentEntity();
		else
			entity = model.getEntity();
		
		return condenseEntity(entity, entityType, collectionPropertyName);
	}
	
	private List<ImageResourceModelData> condenseEntity(GenericEntity entity, EntityType<?> entityType, String collectionPropertyName) {
		Object collection = ((EntityType<GenericEntity>) entityType).getProperty(collectionPropertyName).get(entity);
		List<Object> list;
		if (collection instanceof List)
			list = (List<Object>) collection;
		else {
			list = new ArrayList<>();
			for (Object element : ((Set<Object>) collection))
				list.add(element);
		}
		
		loadThumbnails(list);
		
		List<ImageResourceModelData> models = imagesListStore.getAll();
		for (ImageResourceModelData newModel : models)
			newModel.setCondensed(true);
		
		return models;
	}
	
	private List<ImageResourceModelData> uncondenseEntity(EntityType<GenericEntity> entityType) {
		GenericEntity entity = rootModelPath.last().getValue();
		List<Object> list = handleEntitySet(entity, entityType);
		loadThumbnails(list);
		
		return imagesListStore.getAll();
	}
	
	private String getCondensedProperty(GenericEntity entity, EntityType<?> entityType) {
		String condensedProperty = null;
		if (!uncondensedTypes.contains(entityType)) {
			condensedProperty = condensedTypes.get(entityType);
			if (condensedProperty == null)
				condensedProperty = getCondensedPropertyFromMetaModel(entity, entityType);
		}
		return condensedProperty;
	}
	
	private String getCondensedPropertyFromMetaModel(GenericEntity entity, EntityType<?> entityType) {
		GMEMetadataUtil.CondensationBean bean = GMEMetadataUtil.getEntityCondensationProperty(
				GMEMetadataUtil.getEntityCondensations(entity, entityType, gmSession.getModelAccessory().getMetaData(), useCase), false);
		
		if (bean != null) {
			markEntityTypeAsCondensed(entityType, bean.getProperty(), bean.getMode());
			return bean.getProperty();
		}
		
		return null;
	}
	
	private void markEntityTypeAsCondensed(EntityType<?> entityType, String collectionPropertyName, CondensationMode condensationMode) {
		condensedTypes.put(entityType, collectionPropertyName);
		condensedModes.put(entityType, condensationMode);
		uncondensedTypes.remove(entityType);
	}
	
	public static class EntityPropertyBean {
		private final Property property;
		private final String propertyDisplayName;
		private Object value;
		private final GenericEntity parentEntity;
		private boolean absent;
		
		public EntityPropertyBean(Property property, String propertyDisplayName, Object value, GenericEntity parentEntity, boolean absent) {
			this.property = property;
			this.propertyDisplayName = propertyDisplayName;
			this.value = value;
			this.parentEntity = parentEntity;
			this.absent = absent;
		}

		public Property getProperty() {
			return property;
		}
		
		public String getPropertyDisplayName() {
			return propertyDisplayName;
		}

		public Object getValue() {
			return value;
		}
		
		public GenericEntity getParentEntity() {
			return parentEntity;
		}

		public boolean isAbsent() {
			return absent;
		}
	}
	
	
	protected void getPreviewUrl(ImageResourceModelData model, GenericEntity entity/*, PreviewType previewType, AsyncCallback<Void> callback*/) {
		if (entity == null || model == null)
			return;
				
		int previewWidth = model.getPreviewWidth();
		int previewHeight = model.getPreviewHeight();
		
		logger.debug("Getting Preview MD with usecase: "+getUseCase());
		Preview preview = previewUtil.getPreview(entity,getUseCase());
		
		if (preview != null) {
			if (preview.getWidth() != null)
				previewWidth = preview.getWidth();
			if (preview.getHeight() != null)
				previewHeight = preview.getHeight();
			
		}
		
		final int pWidth = previewWidth;
		final int pHeight = previewHeight;		
		
		if (pHeight < 0) {			
			logger.debug("Using empty src: "+EMPTYIMAGE_SRC+" for preview image.");
			model.setPreviewUrl(EMPTYIMAGE_SRC);
			model.setActivePreviewUrl(EMPTYIMAGE_SRC);
			model.setPreviewWidth(pWidth);
			model.setPreviewHeight(Math.abs(pHeight));
			
			/*
			// Special case that a negative height is configured. This means that preview is disabled but still the width is respected for the info box.
			new Future<>(EMPTYIMAGE_SRC).get(new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					caught.printStackTrace();
					model.setPreviewUrl(null);
					if(callback != null) callback.onFailure(caught);
				}
				@Override
				public void onSuccess(String url) {
					model.setPreviewUrl(url);
					model.setPreviewWidth(pWidth);
					model.setPreviewHeight(Math.abs(pHeight)); // convert negative height to absolute value for rendering purposes
					if(callback != null) callback.onSuccess(null);
				}
			});
			*/
			
		} else {
			model.setPreviewUrl(previewUtil.previewUrl(entity,getUseCase(), PreviewType.STANDARD));
			model.setActivePreviewUrl(previewUtil.previewUrl(entity,getUseCase(), PreviewType.ACTIVE));
			model.setPreviewWidth(pWidth);
			model.setPreviewHeight(pHeight);
			
			/*
			previewUtil.previewUrl(entity,getUseCase(), previewType).get(new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					caught.printStackTrace();
					model.setPreviewUrl(null);
					if(callback != null) callback.onFailure(caught);
				}
				
				@Override
				public void onSuccess(String url) {
					Console.log(url);
					model.setPreviewUrl(url);
					model.setPreviewWidth(pWidth);
					model.setPreviewHeight(pHeight);
					if(callback != null) callback.onSuccess(null);
				}
			});
			*/
		}				
	}
	
	private void doSortByPriority() {
		imagesListView.getStore().clearSortInfo();
		imagesListView.getStore().addSortInfo(new StoreSortInfo<>((arg1, arg2) -> {
			Double priority1 = arg1.getPriority();
			Double priority2 = arg2.getPriority();	
			
			return priority2.compareTo(priority1);			
		}
		, SortDir.ASC));
	}

	private void addEntityListener(GenericEntity entity) {
		if (entity == null) 
			return;
		
		this.gmSession.listeners().entity(entity).remove(this);
    	this.gmSession.listeners().entity(entity).add(this);
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//if (!(manipulation instanceof PropertyManipulation)) 
		//	return;
		
		new Timer() {
			@Override
			public void run() {
				GenericEntity entity = null;
				Property property = null;
				Object manipulationOwner = null;
				if (manipulation instanceof PropertyManipulation) {
					manipulationOwner =	((PropertyManipulation) manipulation).getOwner();

					if (manipulationOwner instanceof LocalEntityProperty) {
						entity = ((LocalEntityProperty) manipulationOwner).getEntity();
						property = ((LocalEntityProperty) manipulationOwner).property();
					} 
				} else if (manipulation instanceof LifecycleManipulation) {
					entity = ((LifecycleManipulation) manipulation).getEntity();
				}
				
				if (entity == null && property == null)
					return;

				ImageResourceModelData foundModel = null;
				for (ImageResourceModelData model : imagesListView.getStore().getAll()) {
					if (entity != null) {
						if (entity.equals(model.getEntity())) {	
							foundModel = model;
							break;
						} else if (property == null && model.getEntityPropertyBean() != null && entity.equals(model.getEntityPropertyBean().getParentEntity())) {
							foundModel = model;
							break;
						}
					}
					
					if (property != null && model.getEntityPropertyBean() != null && property.equals(model.getEntityPropertyBean().getProperty())) {	
						foundModel = model;
						break;
					}
				}
				//check deleted model and Undo delete it
				if (foundModel == null && entity != null && deletedEntities != null)
					foundModel = deletedEntities.get(entity);
				
				if (handleAbsentPropertyBeanLoaded(manipulation, entity, property))
					return;
				
				if (foundModel == null)
					return;
				
				GenericEntity deletedEntity;
				switch (manipulation.manipulationType()) {
					case CHANGE_VALUE:
					case ADD:
					case REMOVE:
					case CLEAR_COLLECTION:
                        if (foundModel.getEntity() != null)
                       	    updateModelForGenericEntity(foundModel.getEntity(), foundModel.getOwnerEntity(), foundModel);
                        else if (foundModel.getEntityPropertyBean() != null)
							updateModelForEntityPropertyBean(foundModel.getEntityPropertyBean(), foundModel, true);
                        
                   	    getImagesListStore().update(foundModel);
						break;
					case DELETE:
						deletedEntity = entity;
						if (deletedEntities == null)
							deletedEntities = new HashMap<>();
						deletedEntities.put(deletedEntity, foundModel);						
						getImagesListStore().remove(foundModel);
						break;
					case INSTANTIATION:
					case MANIFESTATION:
						deletedEntity = entity;
						if (deletedEntities != null) {
							ImageResourceModelData model = deletedEntities.remove(deletedEntity);
							if (model != null) {
								imagesListStore.clearSortInfo();
								getImagesListStore().add(model);	
								doSortByPriority();
							}
						}						
						break;
					default:
						break;
				}				
			}
		}.schedule(10); //needed, so the value in the entity was the correct one
	}

	protected void setSelectedModelPath(ModelPath selectedModelPath) {
		for (ImageResourceModelData model : imagesListView.getStore().getAll()) {
			ModelPath modelPath = getModelPath(model);
			if (modelPath.last().equals(selectedModelPath.last())) {
				getImagesListView().getSelectionModel().select(model, false);
				return;
			}
		}
	}
	
	private boolean handleAbsentPropertyBeanLoaded(Manipulation manipulation, GenericEntity entity, Property property) {
		if (entity == null || property == null)
			return false;
		
		for (ImageResourceModelData model : imagesListView.getStore().getAll()) {
			EntityPropertyBean entityPropertyBean = model.getEntityPropertyBean();
			if (entityPropertyBean == null)
				continue;
			
			if (entityPropertyBean.getParentEntity() != entity || property != entityPropertyBean.getProperty())
				continue;
			
			if (manipulation instanceof ChangeValueManipulation) {
				entityPropertyBean.value = ((ChangeValueManipulation) manipulation).getNewValue();
				entityPropertyBean.absent = false;
			
				if (entityPropertyBean.value instanceof GenericEntity)
					loadThumbnailFromEntity((GenericEntity) entityPropertyBean.value, entityPropertyBean, model);
				else if (entityPropertyBean.value == null) {
					updateModelForEntityPropertyBean(entityPropertyBean, model, false);
					imagesListStore.update(model);
				}
				
				if (selectedModel == model)
					fireGmSelectionListeners();
				
				return true;
			}
		}
		
		return false;
	}
	
	private void prepareGroupModels(boolean groupSameTogether) {
		if (!useGroups)
			return;
		
		//List<ImageResourceModelData> workingList = imageResourceModelPreparationList;
		
		if (imagesListView.getStore().size() == 0)
			return;
		
		List<ImageResourceModelData> listGroupModels = new ArrayList<>();
		if (groupSameTogether) {
			for (ImageResourceModelData model : imagesListView.getStore().getAll())
				if (model.isGroupModel())
					listGroupModels.add(model);
		}
		
		imagesListView.getStore().clearSortInfo();
		imagesListStore.clearSortInfo();
		
		int index = 0;
		ImageResourceModelData lastGroupModel = null;		
		ImageResourceModelData model = imagesListView.getStore().get(index);

		while (model != null) {
			 boolean hasGroup = model.getGroupName() == null ? false : !model.getGroupName().isEmpty();
			 boolean prepareEmptyGroup = (lastGroupModel != null) && (!hasGroup); 	
			 //boolean prepareGroup =  
			 
			 if ((hasGroup || prepareEmptyGroup) && !model.isGroupModel()) {
				 //group separator not exists, so need to create it
				 boolean createGroup = false;
				 if (groupSameTogether && hasGroup) {
					 createGroup = true;
					 for (ImageResourceModelData modelGroup : listGroupModels) {
						 if (model.getGroupName().equals(modelGroup.getGroupName())) {
						     //group already exists, not create it, just move model to that group	 						 
							 createGroup = false;
							 if (modelGroup.equals(lastGroupModel))
								 break;
							 
							 imagesListView.getStore().remove(model);
							 int nextGroupindex = listGroupModels.indexOf(modelGroup) + 1;
							 int moveIndex = -1;
							 if (nextGroupindex < listGroupModels.size()) {
								moveIndex = imagesListView.getStore().indexOf(listGroupModels.get(nextGroupindex));
							 } else {	
								moveIndex = imagesListView.getStore().size();
							 }
							 List<ImageResourceModelData> listModel = new ArrayList<>();
							 listModel.add(model);
							 imagesListStore.addAll(moveIndex, listModel);
							 index--;
							 break;
						 }
					 }
				 } else if (lastGroupModel == null || !lastGroupModel.getGroupName().equals(model.getGroupName())) {
					 createGroup = true;
				 }
				 
				 if (createGroup) {
					 ImageResourceModelData modelGroup = new ImageResourceModelData();
					 String uniqueId = DOM.createUniqueId();
					 modelGroup.setUniqueId(uniqueId);
					 modelGroup.setImageHeight(32);
					 modelGroup.setImageWidth(32);
					 modelGroup.setContainerSize(thumbnailSize);
					 modelGroup.setHeight(50);
					 modelGroup.setCoverImage(modelGroup.getImageWidth() > modelGroup.getImageHeight() * coverWidthHeightCoefficient);					
					 modelGroup.setClassName("thumbnailBreak");
					 modelGroup.setGroupModel(true);
					 modelGroup.setBreakModel(true);
					 
					 ImageLoader imageLoader = new ImageLoader();
					 //imageLoader.setSrc(ImageResourceModelData.WATERMARK_SRC);
					 imageLoader.setDefaultSrc(ImageResourceModelData.GROUP_ICON_SRC);				
					 imageLoader.setImageElementId("thumbnailImage-" + uniqueId);
					 imageLoader.addImageLoaderListener(modelGroup);
					 imageLoader.addImageLoaderListener(imageLoadingChain);
					 imageLoader.addImageLoaderListener(ThumbnailPanel.this);
					 modelGroup.setImageLoader(imageLoader);
					 
					 if (prepareEmptyGroup) {
						 lastGroupModel = null;						 
						 modelGroup.setInfo("");
						 modelGroup.setGroupName("");
						 modelGroup.setSelectiveInfo("", false);						 
					 } else {
						 lastGroupModel = modelGroup;
						 modelGroup.setGroupName(model.getGroupName());
						 modelGroup.setInfo(model.getGroupName());
						 modelGroup.setSelectiveInfo(model.getGroupName(), false);						 
					 }
					 
					 modelGroup.setPriority(model.getPriority());
					 List<ImageResourceModelData> listModel = new ArrayList<>();
					 listModel.add(modelGroup);
					 //workingList.addAll(index, listModel);
					 imagesListStore.addAll(index, listModel);
					 if (groupSameTogether)
						 listGroupModels.add(modelGroup); 					 
				 }
			 } else {
				 if (model.isGroupModel() && !prepareEmptyGroup)
					 lastGroupModel = model;
				 else 
					 lastGroupModel = null; 
			 }
			 
			 index++;
			 if (index < imagesListView.getStore().size())
				 model = imagesListView.getStore().get(index);
			 else
				 model = null;
		}	
	}
	
	/***************************** GmeDragAndDropView ***********************************/
	protected GmeDragAndDropSupport gmeDragAndDropSupport;
	private WorkbenchActionContext<TemplateBasedAction> workbenchActionContext;
	
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
	
	@Override
	public WorkbenchActionContext<TemplateBasedAction> getDragAndDropWorkbenchActionContext() {
		if (workbenchActionContext != null)
			return workbenchActionContext;
		
		workbenchActionContext = prepareWorkbenchActionContext();
		return workbenchActionContext;
	}

	public void setUseGroupTogether(boolean useGroupTogether) {
		this.useGroupTogether = useGroupTogether;
	}

	public boolean isUseGroups() {
		return useGroups;
	}

	public void setUseGroups(boolean useGroups) {
		this.useGroups = useGroups;
	}
	
}
