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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.AddToCollectionActionFolderContent;
import com.braintribe.gm.model.uiaction.ChangeInstanceActionFolderContent;
import com.braintribe.gm.model.uiaction.ClearCollectionActionFolderContent;
import com.braintribe.gm.model.uiaction.ClearPropertyToNullActionFolderContent;
import com.braintribe.gm.model.uiaction.CopyIdToClipboardActionFolderContent;
import com.braintribe.gm.model.uiaction.DeleteEntityActionFolderContent;
import com.braintribe.gm.model.uiaction.EditEntityActionFolderContent;
import com.braintribe.gm.model.uiaction.EntityDetailsActionFolderContent;
import com.braintribe.gm.model.uiaction.InstantiateEntityActionFolderContent;
import com.braintribe.gm.model.uiaction.RemoveFromCollectionActionFolderContent;
import com.braintribe.gm.model.uiaction.WorkWithEntityActionFolderContent;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.InstanceSelectionData;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.SelectionConfig;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.workbench.InstantiationAction;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.menu.Menu;

/**
 * The GmContentViewActionManager will connect to a GmContentView by adding itself to the GmContentView as GmSelectionListener
 * and it will also determine the full set of potential model actions for that view and set it on the View.
 * After this initialization the GmContentViewActionManager listens to the GmSelectionListener events and acts with visibility
 * and enablement of the actions that were initially passed to the view.
 * 
 * @author michel.docouto
 *
 */
@SuppressWarnings("unusable-by-js")
public class DefaultGmContentViewActionManager implements GmContentViewActionManager, Loader<Void>, GmSessionHandler {
	
	private Map<GmContentView, ActionGroup> actionsByView = new HashMap<>();
	private Map<GmContentView, Widget> actionMenusByView = new HashMap<>();
	private Map<GmContentView, List<Pair<String, ? extends Widget>>> externalComponentsByView = new HashMap<>();
	private boolean prepareKnownActions = true;
	private boolean prepareInstantiateEntityAction = false;
	private boolean prepareWorkWithEntityAction = true;
	private boolean prepareDeleteEntityAction = true;
	private ModelEnvironmentDrivenGmSession gmSession;
	private InstantiationActionHandler instantiationActionHandler;
	private Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> instanceSelectionFutureProvider;
	private boolean readOnlyMode = false;
	private boolean prepareGimaOpenerAction = true;
	private ModelEnvironmentDrivenGmSession workbenchSession;
	private List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>> externalActionProviders = null;
	private List<ActionPerformanceListener> actionPerformanceListeners = new ArrayList<>();
	private ActionMenuBuilder actionMenuBuilder;
	private Supplier<? extends ActionMenuBuilder> actionMenuBuilderSupplier;
	private boolean useInstantiateActionInContextMenu = true;
	private List<Loader<Folder>> folderLoaders;
	private Loader<Folder> folderLoader;
	private Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier;
	private List<GmContentViewContext> externalContentViewContexts;
	private Folder rootFolder;
	private Map<ActionTypeAndName, Boolean> actionAvailabilityMap;
	private Map<GmContentView, List<Pair<ActionTypeAndName, ModelAction>>> knownActionsByView = new HashMap<>();
	private Map<GmContentView, WorkWithEntityAction> workWithEntityActionsMap = new HashMap<>();
	private Function<EntityType<?>, Future<List<InstantiationAction>>> instantiationActionsProvider;
	private WorkWithEntityExpert workWithEntityExpert;
	private int folderLoaderCounter;
	
	/**
	 * Configures the required {@link ModelEnvironmentDrivenGmSession} used within the actions.
	 */
	@Required
	public void setPersistenceSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the required {@link Supplier} which provides a provider for instance selection.
	 */
	@Required
	public void setInstanceSelectionFutureProvider(
			Supplier<? extends Function<SelectionConfig, Future<InstanceSelectionData>>> instanceSelectionFutureProvider) {
		this.instanceSelectionFutureProvider = instanceSelectionFutureProvider;
	}
	
	/**
	 * Configures the workbench session used for preparing the {@link ActionMenuBuilder} given via {@link #setActionMenuBuilder(Supplier)}.
	 */
	@Required
	public void setWorkbenchSession(ModelEnvironmentDrivenGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configures the required {@link ActionMenuBuilder}.
	 */
	@Required
	public void setActionMenuBuilder(Supplier<? extends ActionMenuBuilder> actionMenuBuilderSupplier) {
		this.actionMenuBuilderSupplier = actionMenuBuilderSupplier;
	}
	
	/**
	 * Configures a loader for loading folders. Either this or {@link #setFolderLoaders(List)} is required.
	 */
	@Configurable
	public void setFolderLoader(Loader<Folder> folderLoader) {
		this.folderLoader = folderLoader;
	}
	
	/**
	 * Configures loaders for loading folders.
	 * The first one in the list which returns a valid folder is the one used.
	 * Either this or {@link #setFolderLoader(Loader)} is required.
	 */
	@Configurable
	public void setFolderLoaders(List<Loader<Folder>> folderLoaders) {
		this.folderLoaders = folderLoaders;
	}
	
	/**
	 * Configures the provider for {@link InstantiationAction}s.
	 * If {@link #setPrepareInstantiateEntityAction(boolean)} is set to true, then this is required.
	 */
	@Configurable
	public void setInstantiationActionsProvider(Function<EntityType<?>, Future<List<InstantiationAction>>> instantiationActionsProvider) {
		this.instantiationActionsProvider = instantiationActionsProvider;
	}
	
	/**
	 * Configures the InstantiationActionHandler.
	 * If {@link #setPrepareInstantiateEntityAction(boolean)} is set to true, then this is required.
	 */
	@Configurable
	public void setInstantiationActionHandler(InstantiationActionHandler instantiationActionHandler) {
		this.instantiationActionHandler = instantiationActionHandler;
	}
	
	/**
	 * Configures whether we should prepare the {@link InstantiateEntityAction}.
	 * Defaults to false. If {@link #setReadOnlyMode(boolean)} is set to true, then this property is ignored.
	 */
	@Configurable
	public void setPrepareInstantiateEntityAction(boolean prepareInstantiateEntityAction) {
		this.prepareInstantiateEntityAction = prepareInstantiateEntityAction;
	}
	
	/**
	 * Configures whether the {@link DeleteEntityAction} should be prepared.
	 * Defaults to true.
	 */
	@Configurable
	public void setPrepareDeleteEntityAction(boolean prepareDeleteEntityAction) {
		this.prepareDeleteEntityAction = prepareDeleteEntityAction;
	}
	
	public void setPrepareKnownActions(boolean prepareKnownActions) {
		this.prepareKnownActions = prepareKnownActions;
	}
	
	/**
	 * Configures if the actions will be used within a read only environment.
	 * If true, no actions will be prepared, except for the {@link GIMADialogOpenerAction}.
	 * @see #setPrepareGimaOpenerAction(boolean)
	 * Defaults to false.
	 */
	@Configurable
	public void setReadOnlyMode(boolean readOnlyMode) {
		this.readOnlyMode = readOnlyMode;
	}
	
	/**
	 * Configures whether we should prepare the {@link GIMADialogOpenerAction}.
	 * Defaults to true.
	 */
	@Configurable
	public void setPrepareGimaOpenerAction(boolean prepareGimaOpenerAction) {
		this.prepareGimaOpenerAction = prepareGimaOpenerAction;
	}
	
	/**
	 * Configures whether the {@link WorkWithEntityAction} is prepared.
	 * Defaults to true.
	 */
	@Configurable
	public void setPrepareWorkWithEntityAction(boolean prepareWorkWithEntityAction) {
		this.prepareWorkWithEntityAction = prepareWorkWithEntityAction;
	}
	
	/**
	 * Configures external action providers to be used within this manager.
	 */
	@Configurable
	public void setExternalActionProviders(List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>> externalActionProviders) {
		this.externalActionProviders = externalActionProviders;
	}
	
	/**
	 * Configures whether to use the instantiate action also in the context menu. Defaults to true.
	 * @see #setPrepareInstantiateEntityAction(boolean)
	 */
	@Configurable
	public void setUseInstantiateActionInContextMenu(boolean useInstantiateActionInContextMenu) {
		this.useInstantiateActionInContextMenu = useInstantiateActionInContextMenu;
	}
	
	/**
	 * Configures the view situation resolver. If using the work with action, then this is required. 
	 */
	@Configurable
	public void setViewSituationResolver(Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier) {
		this.viewSituationResolverSupplier = viewSituationResolverSupplier;
	}
	
	@Configurable
	public void setExternalContentViewContexts(List<GmContentViewContext> externalContentViewContexts) {
		this.externalContentViewContexts = externalContentViewContexts;
	}
	
	@Override
	public void load(final AsyncCallback<Void> asyncCallback) {
		ProfilingHandle ph = Profiling.start(DefaultGmContentViewActionManager.class, "Loading action folders", true);
		ActionMenuBuilder actionMenuBuilder = getActionMenuBuilder();
		actionMenuBuilder.setWorkbenchSession(workbenchSession);
		actionMenuBuilder.configureGmSession(gmSession);
		
		folderLoaderCounter = 0;
		if (folderLoaders != null)
			loadFolder(asyncCallback, folderLoaders.get(folderLoaderCounter), ph);
		else
			loadFolder(asyncCallback, folderLoader, ph);
	}
	
	private void loadFolder(AsyncCallback<Void> asyncCallback, Loader<Folder> loader, ProfilingHandle ph) {
		loader.load(AsyncCallbacks.of(result -> {
			if (result != null) {
				returnLoadedFolder(result, asyncCallback, ph);
				return;
			}
			
			folderLoaderCounter++;
			if (folderLoaders != null && folderLoaders.size() > folderLoaderCounter)
				loadFolder(asyncCallback, folderLoaders.get(folderLoaderCounter), ph);
			else
				returnLoadedFolder(null, asyncCallback, ph);
		}, e -> {
			e.printStackTrace();
			actionMenuBuilder.apply(null);
			ph.stop();
			asyncCallback.onSuccess(null);
		}));
	}
	
	private void returnLoadedFolder(Folder folder, AsyncCallback<Void> asyncCallback, ProfilingHandle ph) {
		if (actionAvailabilityMap != null)
			actionAvailabilityMap.clear();
		rootFolder = folder;
		actionMenuBuilder.apply(folder);
		ph.stop();
		asyncCallback.onSuccess(null);
	}
	
	@Override
	public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
		getActionMenuBuilder().onSelectionChanged(actionsByView.get(gmSelectionSupport), gmSelectionSupport);
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		if (gmSession instanceof ModelEnvironmentDrivenGmSession)
			this.gmSession = (ModelEnvironmentDrivenGmSession) gmSession;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}

	@Override
	public void connect(final GmContentView view) {
		view.addSelectionListener(this);
		PersistenceGmSession session = view.getGmSession();
		if (session instanceof ModelEnvironmentDrivenGmSession)
			setPersistenceSession((ModelEnvironmentDrivenGmSession) session);
		
		if (prepareKnownActions) {
			ActionGroup actionGroup = getActionMenuBuilder().prepareActionGroup(getKnownActionsList(view), view);
			actionsByView.put(view, actionGroup);
			if (view instanceof GmActionSupport)
				((GmActionSupport) view).configureActionGroup(actionGroup);
		}
	}
	
	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getKnownActionsList(GmContentView view) {
		List<Pair<ActionTypeAndName, ModelAction>> modelActionList = knownActionsByView.get(view);
		if (modelActionList != null)
			return modelActionList;
		else
			modelActionList = new ArrayList<>();
		
		String workWithEntityName = KnownActions.WORK_WITH_ENTITY.getName();
		WorkWithEntityAction workWithEntityAction = new WorkWithEntityAction();
		workWithEntityAction.configureGmContentView(view);
		workWithEntityAction.configureViewSituationResolver(viewSituationResolverSupplier);
		workWithEntityAction.configureExternalContentViewContexts(externalContentViewContexts);
		workWithEntityAction.configureWorkWithEntityExpert(workWithEntityExpert);
		workWithEntityActionsMap.put(view, workWithEntityAction);
		if (!readOnlyMode && prepareWorkWithEntityAction) {
			ActionTypeAndName actionTypeAndName = new ActionTypeAndName(WorkWithEntityActionFolderContent.T, workWithEntityName);
			if (isActionAvailable(actionTypeAndName))
				modelActionList.add(new Pair<>(actionTypeAndName, workWithEntityAction));
		}
		
		String gimaOpenerName = KnownActions.GIMA_OPENER.getName();
		if (prepareGimaOpenerAction) {
			ActionTypeAndName actionTypeAndName = new ActionTypeAndName(EditEntityActionFolderContent.T, gimaOpenerName);
			if (isActionAvailable(actionTypeAndName)) {
				GIMADialogOpenerAction gimaDialogOpenerAction = new GIMADialogOpenerAction(true);
				gimaDialogOpenerAction.configureGmContentView(view);
				modelActionList.add(new Pair<>(actionTypeAndName, gimaDialogOpenerAction));
			}
		}
		
		String gimaOpenerForDetailsName = KnownActions.GIMA_OPENER_FOR_DETAILS.getName();
		ActionTypeAndName detailsActionTypeAndName = new ActionTypeAndName(EntityDetailsActionFolderContent.T, gimaOpenerForDetailsName);
		if (isActionAvailable(detailsActionTypeAndName)) {
			GIMADialogOpenerAction gimaDialogOpenerAction = new GIMADialogOpenerAction(false);
			gimaDialogOpenerAction.configureGmContentView(view);
			modelActionList.add(new Pair<>(detailsActionTypeAndName, gimaDialogOpenerAction));
		}
		
		if (readOnlyMode) {
			knownActionsByView.put(view, modelActionList);
			return modelActionList;
		}
		
		InstantiateEntityAction instantiateAction = null;
		String instantiateEntityName = KnownActions.INSTANTIATE_ENTITY.getName();
		if (prepareInstantiateEntityAction) {
			ActionTypeAndName actionTypeAndName = new ActionTypeAndName(InstantiateEntityActionFolderContent.T, instantiateEntityName);
			if (isActionAvailable(actionTypeAndName)) {
				final InstantiateEntityAction instantiateEntityAction = getNewInstantiateEntityAction(view);
				instantiateAction = instantiateEntityAction;
				instantiateEntityAction.configureGmContentView(view);
				modelActionList.add(new Pair<>(actionTypeAndName, instantiateEntityAction));
	
				if (view instanceof GmContentSupport) {
					((GmContentSupport) view).addGmContentViewListener(contentView -> {
						GenericModelType type = view.getContentPath().last().getType();
						EntityType<?> entityType = null;
						if (type.isEntity())
							entityType = (EntityType<?>) type;
						else if (type.isCollection()) {
							CollectionType collectionType = (CollectionType) type;
							switch (collectionType.getCollectionKind()) {
							case list:
							case set:
								if (collectionType.getCollectionElementType().isEntity())
									entityType = (EntityType<?>) collectionType.getCollectionElementType();
								break;
							case map:
								if (collectionType.getCollectionElementType().isEntity())
									entityType = (EntityType<?>) collectionType.getCollectionElementType();
								else if (collectionType.getParameterization()[1].isEntity())
									entityType = (EntityType<?>) collectionType.getParameterization()[1];
								break;
							}
						}
						
						instantiateEntityAction.configureEntityType(entityType);
					});
				}
			}
		}
		
		RemoveFromCollectionAction removeFromCollectionAction = null;
		ActionTypeAndName removeActionTypeAndName = new ActionTypeAndName(RemoveFromCollectionActionFolderContent.T,
				KnownActions.REMOVE_FROM_COLLECTION.getName());
		if (isActionAvailable(removeActionTypeAndName)) {
			removeFromCollectionAction = new RemoveFromCollectionAction();
			removeFromCollectionAction.configureGmContentView(view);
		}
		
		ClearPropertyToNullAction clearEntityToNullAction = null;
		ActionTypeAndName clearActionTypeAndName = new ActionTypeAndName(ClearPropertyToNullActionFolderContent.T,
				KnownActions.CLEAR_ENTITY_TO_NULL.getName());
		if (isActionAvailable(clearActionTypeAndName)) {
			clearEntityToNullAction = new ClearPropertyToNullAction();
			clearEntityToNullAction.configureGmContentView(view);
		}
		
		if (prepareDeleteEntityAction) {
			ActionTypeAndName actionTypeAndName = new ActionTypeAndName(DeleteEntityActionFolderContent.T, KnownActions.DELETE_ENTITY.getName());
			if (isActionAvailable(actionTypeAndName)) {
				DeleteEntityAction deleteEntityAction = new DeleteEntityAction(removeFromCollectionAction, clearEntityToNullAction);
				deleteEntityAction.configureGmContentView(view);
				deleteEntityAction.setActionPerformanceListeners(actionPerformanceListeners);
				modelActionList.add(new Pair<>(actionTypeAndName, deleteEntityAction));
			}
		}
		
		ActionTypeAndName changeActionTypeAndName = new ActionTypeAndName(ChangeInstanceActionFolderContent.T,
				KnownActions.CHANGE_INSTANCE.getName());
		if (isActionAvailable(changeActionTypeAndName)) {
			ChangeInstanceAction changeInstanceAction = new ChangeInstanceAction();
			changeInstanceAction.configureInstantiateEntityAction(instantiateAction);
			changeInstanceAction.setWorkbenchSession(workbenchSession);
			changeInstanceAction.configureGmContentView(view);
			changeInstanceAction.setInstanceSelectionFutureProvider(instanceSelectionFutureProvider);
			modelActionList.add(new Pair<>(changeActionTypeAndName, changeInstanceAction));
		}
		
		if (clearEntityToNullAction != null)
			modelActionList.add(new Pair<>(clearActionTypeAndName, clearEntityToNullAction));
		
		ActionTypeAndName addActionTypeAndName = new ActionTypeAndName(AddToCollectionActionFolderContent.T,
				KnownActions.ADD_TO_COLLECTION.getName());
		if (isActionAvailable(addActionTypeAndName)) {
			AddExistingEntitiesToCollectionAction addExistingEntitiesAction = new AddExistingEntitiesToCollectionAction();
			addExistingEntitiesAction.configureGmContentView(view);
			addExistingEntitiesAction.setEntitySelectionFutureProvider(instanceSelectionFutureProvider);
			addExistingEntitiesAction.configureInstantiateEntityAction(instantiateAction);
			addExistingEntitiesAction.setWorkbenchSession(workbenchSession);
			modelActionList.add(new Pair<>(addActionTypeAndName, addExistingEntitiesAction));
		}
		
		ActionTypeAndName insertActionTypeAndName = new ActionTypeAndName(InstantiateEntityActionFolderContent.T,
				KnownActions.INSERT_BEFORE_TO_LIST.getName());
		if (isActionAvailable(insertActionTypeAndName)) {
			InsertExistingBeforeToListAction insertExistingBeforeToListAction = new InsertExistingBeforeToListAction();
			insertExistingBeforeToListAction.configureInstantiateEntityAction(instantiateAction);
			insertExistingBeforeToListAction.setWorkbenchSession(workbenchSession);
			insertExistingBeforeToListAction.configureGmContentView(view);
			insertExistingBeforeToListAction.setEntitySelectionFutureProvider(instanceSelectionFutureProvider);
			modelActionList.add(new Pair<>(insertActionTypeAndName, insertExistingBeforeToListAction));
		}
		
		if (removeFromCollectionAction != null)
			modelActionList.add(new Pair<>(removeActionTypeAndName, removeFromCollectionAction));
		
		ActionTypeAndName clearCollectionActionTypeAndName = new ActionTypeAndName(ClearCollectionActionFolderContent.T,
				KnownActions.CLEAR_COLLECTION.getName());
		if (isActionAvailable(clearCollectionActionTypeAndName)) {
			ClearCollectionAction clearCollectionAction = new ClearCollectionAction();
			clearCollectionAction.configureGmContentView(view);
			modelActionList.add(new Pair<>(clearCollectionActionTypeAndName, clearCollectionAction));
		}

		ActionTypeAndName copyIdToClipboardActionTypeAndName = new ActionTypeAndName(CopyIdToClipboardActionFolderContent.T, KnownActions.COPY_ID_TO_CLIPBOARD.getName());
		if (isActionAvailable(copyIdToClipboardActionTypeAndName)) {
			CopyIdToClipboardAction copyIdToClipboardAction = new CopyIdToClipboardAction();
			copyIdToClipboardAction.configureGmContentView(view);
			modelActionList.add(new Pair<>(copyIdToClipboardActionTypeAndName, copyIdToClipboardAction));
		}	
		
		if (externalActionProviders != null) {
			for (Pair<ActionTypeAndName, Supplier<? extends ModelAction>> entry : externalActionProviders) {
				ActionTypeAndName actionTypeAndName = entry.getFirst();
				if (isActionAvailable(actionTypeAndName)) {
					ModelAction modelAction = entry.getSecond().get();
					modelAction.configureGmContentView(view);
					modelActionList.add(new Pair<>(actionTypeAndName, modelAction));
				}
			}
		}
		
		knownActionsByView.put(view, modelActionList);
		
		return modelActionList;
	}
	
	@Override
	public void resetActions(GmContentView view) {
		if(prepareKnownActions) {
			ActionGroup actionGroup = getActionMenuBuilder().prepareActionGroup(getKnownActionsList(view), view);
			actionsByView.put(view, actionGroup);
		}
	}
	
	@Override
	public List<? extends Widget> addExternalActions(GmContentView view, List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		ActionGroup actionGroup = actionsByView.get(view);
		
		ActionMenuBuilder actionMenuBuilder = getActionMenuBuilder();
		Set<ActionTypeAndName> newActionSet = actionMenuBuilder.updateActionGroup(actionGroup, externalActions);
		
		Map<String, ModelAction> externalActionsMap = new FastMap<>();
		for (Pair<ActionTypeAndName, ModelAction> entry : externalActions)
			externalActionsMap.put(entry.getFirst().getActionName(), entry.getSecond());
		
		List<Pair<ActionTypeAndName, ModelAction>> newActionList = new ArrayList<>();
		for (ActionTypeAndName newAction : newActionSet)
			newActionList.add(new Pair<>(newAction, externalActionsMap.get(newAction.getActionName())));
		
		if (!newActionList.isEmpty())
			return actionMenuBuilder.updateMenu(view, actionMenusByView.get(view), newActionList, actionGroup);
		else
			return actionMenuBuilder.updateMenu(view, actionMenusByView.get(view), externalActions, actionGroup);
	}
		
	@Override
	public void addExternalComponents(GmContentView view, List<Pair<String, ? extends Widget>> externalComponents) {
		if (externalComponents != null)
			externalComponentsByView.put(view, externalComponents);
	}		
	
	@Override
	public List<Pair<String, ? extends Widget>> getExternalComponentsForView(GmContentView view) {
		return externalComponentsByView.get(view);
	}	
	
	@Override
	public Widget getActionMenuByGroup(GmContentView view, ActionGroup actionGroup) {
		boolean filterExternal = true;
		
		if (view instanceof GmViewActionProvider) 
			filterExternal = ((GmViewActionProvider) view).isFilterExternalActions();

		List<Pair<String, ? extends Widget>> externalComponents = externalComponentsByView.get(view);		
		
		return getActionMenu(view, externalComponents, actionGroup, filterExternal);
	}
		
	@Override
	public Widget getActionMenu(GmContentView view, List<Pair<String, ? extends Widget>> externalComponents, boolean filterExternal) {		
		return getActionMenu(view, externalComponents, actionsByView.get(view), filterExternal);
	}
	
	private Menu getActionMenu(GmContentView view, List<Pair<String, ? extends Widget>> externalComponents, ActionGroup actionGroup, boolean filterExternal) {
		Menu actionMenu = getActionMenuBuilder().getContextMenu(view, externalComponents, actionGroup, filterExternal);		
		actionMenusByView.put(view, actionMenu);
		return actionMenu;
	}
	
	@Override
	public boolean isActionAvailable(ActionTypeAndName actionTypeAndName) {
		ActionTypeAndName actionCheck = new ActionTypeAndName(actionTypeAndName);
		String actionName = actionTypeAndName.getActionName();
		if (actionName != null && !actionName.startsWith("$"))
			actionCheck.setActionName("$" + actionName);
		
		Boolean available = actionAvailabilityMap != null ? actionAvailabilityMap.get(actionCheck) : null;
		if (available != null)
			return available;
		
		if (actionAvailabilityMap == null)
			actionAvailabilityMap = new HashMap<>();
		
		available = GMEUtil.isActionAvailable(actionCheck, rootFolder);
		actionAvailabilityMap.put(actionCheck, available);
		
		return available;
	}

	@Override
	public void addActionPeformanceListener(ActionPerformanceListener listener) {
		actionPerformanceListeners.add(listener);
	}
	
	@Override
	public void removeActionPeformanceListener(ActionPerformanceListener listener) {
		actionPerformanceListeners.remove(listener);
	}
	
	@Override
	public ModelAction getWorkWithEntityAction(GmContentView view) {
		return workWithEntityActionsMap.get(view);
	}
	
	private InstantiateEntityAction getNewInstantiateEntityAction(GmContentView view) {
		InstantiateEntityAction instantiateEntityAction = new InstantiateEntityAction();
		instantiateEntityAction.configureGmContentView(view);
		instantiateEntityAction.setUseInContextMenu(useInstantiateActionInContextMenu);
		instantiateEntityAction.setInstantiationActionHandler(instantiationActionHandler);
		instantiateEntityAction.setInstantiationActionsProvider(instantiationActionsProvider);
		
		return instantiateEntityAction;
	}
	
	@Override
	public void notifyDisposedView(GmContentView view) {
		if (actionsByView != null)
			actionsByView.remove(view);
		workWithEntityActionsMap.remove(view);
		knownActionsByView.remove(view);
		
		getActionMenuBuilder().notifyDisposedView(actionMenusByView.get(view));
		actionMenusByView.remove(view);
	}
	
	@Override
	public ActionMenuBuilder getActionMenuBuilder() {
		if (actionMenuBuilder != null)
			return actionMenuBuilder;
		
		actionMenuBuilder = actionMenuBuilderSupplier.get();
		return actionMenuBuilder;
	}

	public WorkWithEntityExpert getWorkWithEntityExpert() {
		return workWithEntityExpert;
	}

	public void setWorkWithEntityExpert(WorkWithEntityExpert workWithEntityExpert) {
		this.workWithEntityExpert = workWithEntityExpert;
	}
	
	@Override
	public ActionGroup getActionGroup(GmContentView view) {
		return getActionMenuBuilder().prepareActionGroup(getKnownActionsList(view), view);
	}
	
	@Override
	public List<Action> getActionsForView(GmContentView view) {
		if (actionsByView == null)
			return Collections.emptyList();
		
		ActionGroup actionGroup = actionsByView.get(view);
		if (actionGroup == null)
			return Collections.emptyList();
		
		List<Action> actionList = new ArrayList<>();
		fillActions(actionGroup, actionList);
		return actionList;
	}
	
	private void fillActions(ActionGroup actionGroup, List<Action> actionList) {
		if (actionGroup.getAction() != null)
			actionList.add(actionGroup.getAction());
		
		if (actionGroup.getActionList() != null)
			actionGroup.getActionList().forEach(childActionGroup -> fillActions(childActionGroup, actionList));
	}
}
