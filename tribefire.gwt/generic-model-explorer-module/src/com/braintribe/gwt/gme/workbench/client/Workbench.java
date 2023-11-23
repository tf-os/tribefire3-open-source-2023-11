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
package com.braintribe.gwt.gme.workbench.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.TransientGmSession;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.workbench.client.resources.WorkbenchResources;
import com.braintribe.gwt.gmview.action.client.FieldDialogOpenerAction;
import com.braintribe.gwt.gmview.action.client.InstantiationActionHandler;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.actionbar.client.WorkbenchSuspension;
import com.braintribe.gwt.gmview.client.EditEntityActionListener;
import com.braintribe.gwt.gmview.client.EditEntityContext;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.query.Queryable;
import com.braintribe.model.path.GmModelPath;
import com.braintribe.model.path.GmModelPathElement;
import com.braintribe.model.path.GmRootPathElement;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.transaction.TransactionException;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.workbench.InstantiationAction;
import com.braintribe.model.workbench.KnownWorkenchPerspective;
import com.braintribe.model.workbench.ModelLinkAction;
import com.braintribe.model.workbench.SimpleInstantiationAction;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.core.shared.FastSet;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import tribefire.extension.js.model.deployment.JsUxComponentOpenerAction;
import tribefire.extension.js.model.deployment.ViewWithJsUxComponent;

/**
 * The workbench is able to display {@link Folder}s.
 * @author michel.docouto
 *
 */
public class Workbench extends BorderLayoutContainer implements InitializableBean, EditEntityActionListener, InstantiationActionHandler, WorkbenchSuspension, DisposableBean {
	protected static final String WORKBENCH_PERSPECTIVE_NAME = KnownWorkenchPerspective.root.toString();
	public static final String FAKE_FOLDER = "fakeFolder";
	public static final String COLLAPSED_FOLDER_TAG = "collapsed";
	private static final Logger logger = new Logger(Workbench.class);
	
	static {
		WorkbenchResources.INSTANCE.css().ensureInjected();
	}
	
	private WorkbenchTree workbenchTree;
	private List<WorkbenchListener> workbenchListeners;
	private ModelEnvironmentDrivenGmSession workbenchSession;
	protected ModelEnvironmentDrivenGmSession dataSession;
	private TransientGmSession transientSession;
	private WorkbenchMetaDataEnhancer workbenchMetaDataEnhancer;
	private List<? extends Action> externalActions;
	private boolean prepareQueryableOnly = true;
	private String rootFolderName;
	private boolean readOnlyMode = false;
	private Map<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>> fieldDialogOpenerActions;
	private Supplier<GIMADialog> gimaDialogProvider;
	private String useCase;
	protected boolean isSessionListenerSuspended = false;
	private Folder rootFolder;
	private String currentAccessId;
	private WorkbenchSaveExpert workbenchSaveExpert;
	
	public Workbench() {
		this.setBorders(false);
		this.addStyleName("gmeWorkbench");
	}
	
	/**
	 * Configures the session to be used within the Workbench.
	 */
	@Required
	public void setWorkbenchSession(ModelEnvironmentDrivenGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configures the required data session, used for checking metaData.
	 */
	@Required
	public void setDataSession(ModelEnvironmentDrivenGmSession dataSession) {
		this.dataSession = dataSession;
	}
	
	/**
	 * Configures the required transient session, used for checking transient metaData.
	 */
	@Required
	public void setTransientSession(TransientGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	/**
	 * Configures the required name of the root folder.
	 */
	@Required
	public void setRootFolderName(String rootFolderName) {
		this.rootFolderName = rootFolderName;
	}
	
	/**
	 * Configures the required provider for {@link GIMADialog}.
	 * If {@link #setReadOnlyMode(boolean)} is false (default), then this is required.
	 */
	@Configurable
	public void setGIMADialogProvider(Supplier<GIMADialog> gimaDialogProvider) {
		this.gimaDialogProvider = gimaDialogProvider;
	}
	
	/**
	 * Configures a {@link WorkbenchMetaDataEnhancer} instance. Defaults to the {@link WorkbenchMetaDataEnhancer} itself.
	 */
	@Configurable
	public void setWorkbenchMetaDataEnhancer(WorkbenchMetaDataEnhancer workbenchMetaDataEnhancer) {
		this.workbenchMetaDataEnhancer = workbenchMetaDataEnhancer;
	}
	
	/**
	 * Configures a list of external actions to be added to the Workbench context menu.
	 */
	@Configurable
	public void setExternalActions(List<? extends Action> externalActions) {
		this.externalActions = externalActions;
	}
	
	/**
	 * Configures whether only queryable entities should have a folder prepared.
	 * Defaults to true (only queryable entities are prepared).
	 */
	@Configurable
	public void setPrepareQueryableOnly(boolean prepareQueryableOnly) {
		this.prepareQueryableOnly = prepareQueryableOnly;
	}
	
	/**
	 * Configures whether editions are enabled or disabled within the {@link Workbench}.
	 * Defaults to false (edition enabled).
	 */
	@Configurable
	public void setReadOnlyMode(boolean readOnlyMode) {
		this.readOnlyMode = readOnlyMode;
	}
	
	/**
	 * Configures a map containing actions that are used instead of GIMA for the given entities.
	 */
	@Configurable
	public void setFieldDialogOpenerActions(Map<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>> fieldDialogOpenerActions) {
		this.fieldDialogOpenerActions = fieldDialogOpenerActions;
	}
	
	/**
	 * Adds a new {@link WorkbenchListener}.
	 */
	public void addWorkbenchListener(WorkbenchListener listener) {
		if (workbenchListeners == null)
			workbenchListeners = new ArrayList<>();
		workbenchListeners.add(listener);
	}
	
	/**
	 * Removes the given {@link WorkbenchListener}.
	 */
	public void removeWorkbenchListener(WorkbenchListener listener) {
		if (workbenchListeners != null) {
			workbenchListeners.remove(listener);
			if (workbenchListeners.isEmpty())
				workbenchListeners = null;
		}
	}
	
	public void configureModelEnvironment(ModelEnvironment modelEnvironment) {
		fireModelEnvironmentChanged(modelEnvironment);
	}
	
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	/**
	 * Prepares the workbench entries.
	 * @param modelChanged - true if the model environment has changed.
	 */
	public void prepareFolders(final boolean modelChanged) {
		boolean performedSuspend = false;
		if (!isSessionListenerSuspended) {
			performedSuspend = true;
			suspendHistoryListener();
		}
		
		boolean prepareFolders = false;
		if (modelChanged) {
			workbenchMetaDataEnhancer.enhanceWorkbenchModel(workbenchSession);
			
			if (currentAccessId == null || !currentAccessId.equals(dataSession.getAccessId())) {
				clearPreviousAutomaticFolders();
				prepareFolders = true;
			}
		}
		
		if (workbenchSession.getModelEnvironment().getDataAccessId() == null)
			fireWorkbenchRefreshed(false);
		
		rootFolder = null;
		boolean folderFound = false;
		boolean mergeCalled = false;
		List<WorkbenchPerspective> perspectives = dataSession.getModelEnvironment().getPerspectives();
		for (WorkbenchPerspective perspective : perspectives) {
			if (!WORKBENCH_PERSPECTIVE_NAME.equals(perspective.getName()))
				continue;
			List<Folder> folders = perspective.getFolders();
			if (!folders.isEmpty()) {
				mergeCalled = true;
				mergeFolder(prepareFolders, folders.get(0));
				folderFound = true;
			}
			
			break;
		}
		
		if (!folderFound)
			fireWorkbenchRefreshed(false);
		
		if (performedSuspend && !mergeCalled)
			resumeHistoryListener();
	}
	
	@Override
	public void handleInstantiationAction(InstantiationAction action, Widget callerView) {
		fireFolderSelected(prepareFolderForContent(action), callerView);
	}

	private void mergeFolder(boolean prepareFolders, Folder folder) {
		suspendHistoryListener();
		workbenchSession.merge().adoptUnexposed(true).suspendHistory(true).doFor(folder, AsyncCallback.of( //
				mergedFolder -> {
					resumeHistoryListener();
					if (prepareFolders) {
						currentAccessId = dataSession.getAccessId();
						prepareWorkbenchForFolder(mergedFolder);
					}
				}, e -> {
					logger.error("Error while merging and adopting folder.", e);
					resumeHistoryListener();
					if (prepareFolders) {
						currentAccessId = dataSession.getAccessId();
						prepareWorkbenchForFolder(folder);
					}
				}));
	}
	
	private void prepareWorkbenchForFolder(Folder folder) {
		addToTree(folder, ignoreRootFolder(folder));
		if (folder.getName() != null && folder.getName().equals(rootFolderName))
			rootFolder = folder;
		
		expandEntries();
		fireWorkbenchRefreshed(true);
		
		workbenchTree.addManipulationListeners(folder);
	}
	
	protected boolean ignoreRootFolder(Folder folder) {
		String folderName = folder.getName();
		return folder.getContent() == null && (folderName == null || folderName.equals(rootFolderName));
	}
	
	private void fireWorkbenchRefreshed(boolean containsData) {
		if (workbenchListeners != null)
			workbenchListeners.stream().forEach(listener -> listener.onWorkenchRefreshed(containsData));
	}
	
	/**
	 * Handles the result from a {@link SpotlightPanel}.
	 */
	public void handleQuickAccessValueOrTypeSelected(com.braintribe.gwt.gmview.action.client.ObjectAndType objectAndType, String useCase) {
		handleQuickAccessResult(objectAndType == null ? null : new QuickAccessResult(objectAndType, true, useCase));
	}
	
	/**
	 * Handles the result from a {@link SpotlightPanel}.
	 */
	public void handleQuickAccessResult(QuickAccessResult result) {
		if (result != null) {
			Folder folder = prepareFolderFromObjectAndType(result);
			if (folder != null)
				fireFolderSelected(folder, null);
		}
	}
	
	/**
	 * Returns the list of folders which are root folders.
	 */
	public List<Folder> getRootFolders() {
		List<Folder> list = new ArrayList<>();
		workbenchTree.getTree().getStore().getRootItems().stream().filter(node -> node.getFolder() != null).forEach(node -> list.add(node.getFolder()));
		return list;
	}

	/**
	 * Removes the given folder from the Workbench tree root.
	 * @return true iff the folder was present and removed.
	 */
	public boolean removeRootFolder(Folder folder) {
		TreeStore<BaseNode> treeStore = workbenchTree.getTree().getStore();
		for (BaseNode node : treeStore.getRootItems()) {
			if (node.getFolder() == folder)
				return treeStore.remove(node);
		}
		
		return false;
	}
	
	/**
	 * Expand all entries.
	 */
	public void expandEntries() {
		workbenchTree.getTree().expandAll();
		for (BaseNode node : workbenchTree.getTree().getStore().getAll()) {
			if (node.getFolder().getTags().contains(COLLAPSED_FOLDER_TAG))
				workbenchTree.getTree().setExpanded(node, false);
		}
	}
	
	public String getUseCase() {
		return useCase;
	}
	
	protected ModelEnvironmentDrivenGmSession getWorkbenchSession() {
		return workbenchSession;
	}
	
	/**
	 * Returns the root folder (if existing), of the folder with the name set via {@link #setRootFolderName(String)}.
	 */
	public Folder getRootFolder() {
		return rootFolder;
	}
	
	/**
	 * Returns the name of the root folder, if configured so via {@link #setRootFolderName(String)}.
	 */
	public String getRootFolderName() {
		return rootFolderName;
	}
	
	@Override
	public void intializeBean() throws Exception {
		workbenchTree = new WorkbenchTree(this);
		setCenterWidget(workbenchTree);
		
		if (externalActions != null && !externalActions.isEmpty())
			prepareContextMenu();
		
		if (workbenchMetaDataEnhancer == null)
			workbenchMetaDataEnhancer = new WorkbenchMetaDataEnhancer();
		
		if (!readOnlyMode) {
			workbenchSaveExpert = new WorkbenchSaveExpert();
			workbenchSaveExpert.configureWorkbench(this);
		}
	}
	
	@Override
	public boolean suspendHistoryListener() {
		boolean result = !isSessionListenerSuspended;
		isSessionListenerSuspended = true;
		return result;
	}
	
	@Override
	public boolean resumeHistoryListener() {
		boolean result = isSessionListenerSuspended;
		isSessionListenerSuspended = false;
		return result;
	}
	
	@Override
	public void onEditEntity(ModelPath modelPath, EditEntityContext editEntityContext) {
		displayGIMA(modelPath);
	}
	
	@Override
	public void onEditEntity(ModelPath modelPath) {
		onEditEntity(modelPath, null);
	}
	
	/**
	 * Updates the display info for the given folder.
	 */
	public void updateWorkbenchFolderDisplay(Object folderId, String replacement, String matchPattern) {
		BaseNode folderNode = findNode(folderId);
		if (folderNode == null)
			return;
		
		String newText;
		if (matchPattern == null)
			newText = replacement;
		else
			newText = folderNode.getName().replaceFirst(matchPattern, replacement);
		
		updateWorkbenchFolderDisplay(folderNode, newText);
	}
	
	private BaseNode findNode(Object folderId) {
		TreeStore<BaseNode> treeStore = workbenchTree.getTree().getStore();
		return treeStore.getAll().stream().filter(node -> node.getFolder().getId().equals(folderId)).findAny()
				.orElse(null);
	}
	
	private void updateWorkbenchFolderDisplay(BaseNode folderNode, String newText) {
		TreeStore<BaseNode> treeStore = workbenchTree.getTree().getStore();
		folderNode.setName(newText);
		treeStore.update(folderNode);
	}
	
	private void prepareContextMenu() {
		Menu contextMenu = new Menu();
		contextMenu.setMinWidth(210);
		if (externalActions != null && !externalActions.isEmpty()) {
			for (Action externalAction : externalActions) {
				MenuItem item = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem(externalAction, item);
				contextMenu.add(item);
			}
		}
		
		this.setContextMenu(contextMenu);
		
	}
	
	private void clearPreviousAutomaticFolders() {
		workbenchTree.removeManipulationListeners();
		workbenchTree.getTree().getStore().clear();
	}
	
	private BaseNode addToTree(Folder folder, boolean ignoreRoot) {
		if (!ignoreRoot) {
			BaseNode node = WorkbenchTree.prepareTreeNode(folder);
			workbenchTree.addToTreeWithChildren(null, node, -1);
			return node;
		}
		
		List<BaseNode> nodes = new ArrayList<>();
		List<Folder> subFolders = folder.getSubFolders();
		if (subFolders != null)
			subFolders.forEach(subFolder -> nodes.add(WorkbenchTree.prepareTreeNode(subFolder)));
		
		nodes.forEach(child -> workbenchTree.addToTreeWithChildren(null, child, -1));
		return null;
	}
	
	public void fireFolderSelected(Folder folder, Widget callerView) {
		if (workbenchListeners != null)
			workbenchListeners.forEach(listener -> listener.onFolderSelected(folder, callerView));
	}
	
	private void fireModelEnvironmentChanged(ModelEnvironment modelEnvironment) {
		if (workbenchListeners != null)
			workbenchListeners.forEach(listener -> listener.onModelEnvironmentChanged(modelEnvironment));
	}
	
	protected static LocalizedString prepareLocalizedString(String defaultValue) {
		Map<String, String> map = new FastMap<>();
		map.put("default", defaultValue);
		LocalizedString localizedString = LocalizedString.T.create();
		localizedString.setLocalizedValues(map);
		
		return localizedString;
	}
	
	private Folder prepareFolderFromObjectAndType(QuickAccessResult result) {
		String useCase = result.getUseCase();
		
		Object resultObject = result.getObject();
		if (resultObject == null && result.getType() instanceof GmEntityType)
			return prepareFolder(result, useCase);
	
		if (resultObject instanceof FolderContent)
			return prepareFolderForContent((FolderContent) resultObject);
		
		if (resultObject instanceof GenericEntity)
			return prepareModelLinkActionFolder(result.getObjectAndType(), useCase);
		
		return null;
	}
	
	private Folder prepareFolder(QuickAccessResult result, String useCase) {
		Folder folder = null;
		
		EntityType<?> entityType = GMF.getTypeReflection().getEntityType(result.getType().getTypeSignature());
		ObjectAndType objectAndType = result.getObjectAndType();
		if (objectAndType != null && objectAndType.isServiceRequest()) {
			ViewWithJsUxComponent viewWithJsUxComponent = transientSession.getModelAccessory().getMetaData().entityType(entityType).useCase(useCase)
					.meta(ViewWithJsUxComponent.T).exclusive();
			if (viewWithJsUxComponent != null)
				return createFolder(entityType, prepareJsUxComponentOpenerAction(viewWithJsUxComponent), useCase, true);
		}
		
		if (!result.isQuery())
			return createFolder(entityType, prepareInstantiationAction(entityType), useCase, false);
		
		EntityMdResolver entityMetaDataContextBuilder = dataSession.getModelAccessory().getMetaData()
				.entityType(entityType).useCase(useCase);
		
		boolean queryable = !prepareQueryableOnly ? true : entityMetaDataContextBuilder.is(Queryable.T);
		
		if (queryable)
			folder = createFolder(entityType, prepareSimpleQueryAction(entityType), useCase, false);
			
		return folder;
	}
	
	private Folder createFolder(EntityType<?> entityType, FolderContent content, String useCase, boolean useTransientSession) {
		ManagedGmSession theSession = useTransientSession ? transientSession : dataSession;
		String name = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, theSession.getModelAccessory().getMetaData().lenient(true), useCase);
		
		Folder folder = Folder.T.create();
		folder.setDisplayName(prepareLocalizedString(name));
		folder.setName(name);
		folder.setContent(content);
		folder.setIcon(getEntityTypeIcon(entityType, useCase, useTransientSession));
		
		Set<String> tags = new FastSet();
		tags.add(FAKE_FOLDER);
		folder.setTags(tags);
		
		return folder;
	}
	
	private Folder prepareFolderForContent(FolderContent folderContent) {
		Folder folder = Folder.T.create();
		folder.setDisplayName(folderContent.getDisplayName());
		folder.setContent(folderContent);
		folder.setIcon(folderContent.getIcon());
		
		Set<String> tags = new FastSet();
		tags.add(FAKE_FOLDER);
		folder.setTags(tags);
		
		return folder;
	}
	
	private Folder prepareModelLinkActionFolder(com.braintribe.gwt.gmview.action.client.ObjectAndType objectAndType, String useCase) {
		EntityType<?> entityType = GMF.getTypeReflection().getEntityType(objectAndType.getType().getTypeSignature());
		
		String name = SelectiveInformationResolver.resolve(entityType, (GenericEntity) objectAndType.getObject(),
				dataSession.getModelAccessory().getMetaData(), useCase);
		
		Folder folder = Folder.T.create();
		folder.setDisplayName(prepareLocalizedString(name));
		folder.setName(name);
		folder.setIcon(getEntityTypeIcon(entityType, useCase, false));
		folder.setContent(prepareModelLinkAction(objectAndType));
		
		Set<String> tags = new FastSet();
		tags.add(FAKE_FOLDER);
		folder.setTags(tags);
		
		return folder;
	}
	
	private SimpleQueryAction prepareSimpleQueryAction(EntityType<?> entityType) {
		SimpleQueryAction action = SimpleQueryAction.T.create();
		action.setTypeSignature(entityType.getTypeSignature());
		return action;
	}
	
	private SimpleInstantiationAction prepareInstantiationAction(EntityType<?> entityType) {
		SimpleInstantiationAction action = SimpleInstantiationAction.T.create();
		action.setTypeSignature(entityType.getTypeSignature());
		return action;
	}
	
	private JsUxComponentOpenerAction prepareJsUxComponentOpenerAction(ViewWithJsUxComponent viewWithJsUxComponent) {
		JsUxComponentOpenerAction action = JsUxComponentOpenerAction.T.create();
		action.setComponent(viewWithJsUxComponent.getComponent());
		action.setReadOnly(viewWithJsUxComponent.getReadOnly());
		return action;
	}
	
	private ModelLinkAction prepareModelLinkAction(com.braintribe.gwt.gmview.action.client.ObjectAndType objectAndType) {
		ModelLinkAction action = ModelLinkAction.T.create();
		
		GmModelPath modelPath = GmModelPath.T.create();
		GmRootPathElement rootPathElement = GmRootPathElement.T.create();
		rootPathElement.setTypeSignature(objectAndType.getType().getTypeSignature());
		rootPathElement.setValue(objectAndType.getObject());
		modelPath.setElements(Collections.singletonList((GmModelPathElement) rootPathElement));
		
		action.setPath(modelPath);
		
		return action;
	}
	
	private Icon getEntityTypeIcon(EntityType<?> entityType, String useCase, boolean useTransientSession) {
		ManagedGmSession theSession = useTransientSession ? transientSession : dataSession;
		com.braintribe.model.meta.data.display.Icon icon = theSession.getModelAccessory().getMetaData().lenient(true).entityType(entityType).useCase(useCase)
				.meta(com.braintribe.model.meta.data.display.Icon.T).exclusive();
		return icon == null ? null : icon.getIcon();
	}
	
	private void displayGIMA(final ModelPath modelPath) {
		ModelPathElement last = modelPath.last();
		FieldDialogOpenerAction<GenericEntity> fieldDialogOpenerAction = getFieldDialogOpenerAction(((EntityType<?>) last.getType()));
		if (fieldDialogOpenerAction != null) {
			fieldDialogOpenerAction.configureGmSession(workbenchSession);
			fieldDialogOpenerAction.configureEntityValue((GenericEntity) last.getValue());
			fieldDialogOpenerAction.perform(null);
			return;
		}
		
		suspendHistoryListener();
		final GIMADialog gimaDialog = gimaDialogProvider.get();
		gimaDialog.setGmSession(workbenchSession);
		gimaDialog.showForModelPathElement(modelPath).onError(caught -> resumeHistoryListener()).andThen(result -> {
			if (result) {
				resumeHistoryListener();
				workbenchSaveExpert.saveManipulations();
			} else {
				try {
					workbenchSession.getTransaction().undo(1);
				} catch (TransactionException e) {
					ErrorDialog.show(LocalizedText.INSTANCE.errorUndoingInstantiation(), e);
					e.printStackTrace();
				}
				resumeHistoryListener();
			}
		});
	}
	
	@SuppressWarnings({ "rawtypes" })
	private FieldDialogOpenerAction<GenericEntity> getFieldDialogOpenerAction(EntityType<?> type) {
		if (fieldDialogOpenerActions == null)
			return null;
		
		for (Map.Entry<EntityType<?>, Supplier<? extends FieldDialogOpenerAction<?>>> entry : fieldDialogOpenerActions.entrySet()) {
			if (entry.getKey().isAssignableFrom(type)) {
				Supplier<? extends FieldDialogOpenerAction<?>> supplier = entry.getValue();
				return supplier == null ? null : (FieldDialogOpenerAction) supplier.get();
			}
		}
		
		return null;
	}
	
	@Override
	public void disposeBean() throws Exception {
		workbenchTree.removeManipulationListeners();
	}	
}
