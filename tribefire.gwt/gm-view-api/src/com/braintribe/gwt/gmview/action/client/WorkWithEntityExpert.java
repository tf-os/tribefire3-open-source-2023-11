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

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.braintribe.gm.model.uiaction.WorkWithEntityActionFolderContent;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ParentModelPathSupplier;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.workbench.ExclusiveWorkbenchAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class is responsible to get configured Exclusive Open Workbench Action
 * 
 */

public class WorkWithEntityExpert implements Loader<Void> {
	private Folder rootFolder;
	private Loader<Folder> folderLoader;
	private StandardMatcher matcher;
	private Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	private ModelPath rootModelPath;
	private boolean rootModelPathChecked;
	private PersistenceGmSession gmSession;
	private WorkbenchActionContext<WorkbenchAction> currentWorkbenchContext = null;
	private boolean selectModelPath = true;
	private ExclusiveWorkbenchAction exclusiveWorkbenchAction;
	private boolean exclusiveWorkbenchActionChecked = false;
		
	/**
	 * Configures a loader for loading folders.
	 */
	@Required
	public void setFolderLoader(Loader<Folder> folderLoader) {
		this.folderLoader = folderLoader;
	}	
	
	/**
	 * Configures the registry for {@link WorkbenchAction}s handlers.
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}
	
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	public Folder getWorkWithFolder() {
		return getWorkWithEntityActionFolder(rootFolder);
	}
	
	public WorkbenchActionContext<WorkbenchAction> getCurrentWorkbenchContext() {
		return this.currentWorkbenchContext;
	}
	
	public ExclusiveWorkbenchAction getExclusiveWorkbenchAction() {
		if (exclusiveWorkbenchActionChecked)
			return exclusiveWorkbenchAction;
		
		exclusiveWorkbenchActionChecked = true;
		exclusiveWorkbenchAction = null;
		
		Folder folder = getWorkWithEntityActionFolder(rootFolder);
		
		if (folder != null && folder.getContent() instanceof ExclusiveWorkbenchAction) {
			exclusiveWorkbenchAction = (ExclusiveWorkbenchAction) folder.getContent();
			return exclusiveWorkbenchAction;
		}
		
		return null;
	}
	
	private Folder getWorkWithEntityActionFolder(Folder rootFolder) {
		if (rootFolder == null)
			return null;
		
		return rootFolder.getSubFolders().stream().filter(folder -> isFolderWorkWithEntity(folder)).findFirst().orElse(null);
	}
	
	private boolean isFolderWorkWithEntity(Folder folder) {
		String folderName = folder.getName();
		if (KnownActions.WORK_WITH_ENTITY.getName().equals(folderName) || ("$" + KnownActions.WORK_WITH_ENTITY.getName()).equals(folderName))
			return true;
		
		if (folder.getContent() != null)
			return folder.getContent().entityType().equals(WorkWithEntityActionFolderContent.T);
		
		return false;
	}

	public boolean checkWorkWithAvailable(ModelPath modelPath, Object gmContentView, boolean useWorkWithEntityExpert) {
		if (useWorkWithEntityExpert) {
			WorkbenchAction actionToPerform = getActionToPerform(modelPath);
			if (actionToPerform != null) {
				ExclusiveWorkbenchAction exclusiveWorkbenchAction = getExclusiveWorkbenchAction();
				if (exclusiveWorkbenchAction != null && exclusiveWorkbenchAction.getParentActionHidden())
					return false;
			}
		}
		
		if (gmContentView != null) {
			WorkWithEntityActionListener listener = GMEUtil.getWorkWithEntityActionListener(gmContentView);
			if (listener != null)
				return listener.isWorkWithAvailable(modelPath);
		}
		
		return false;
	}
	
	public WorkbenchAction getActionToPerform(ModelPath modelPath) {
		ExclusiveWorkbenchAction exclusiveWorkbenchAction = getExclusiveWorkbenchAction();
		if (exclusiveWorkbenchAction == null || modelPath == null)
			return null;
		
		for (WorkbenchAction workbenchAction : exclusiveWorkbenchAction.getActions()) {
			StandardMatcher matcher = getMatcher();
			matcher.setCriterion(workbenchAction.getInplaceContextCriterion());
			if (matcher.matches(modelPath.asTraversingContext()))
				return workbenchAction;
		}
		
		return null;
	}	
	
	private StandardMatcher getMatcher() {
		if (matcher != null)
			return matcher;
		
		matcher = new StandardMatcher();
		matcher.setCheckOnlyProperties(false);
		
		return matcher;
	}	
	
	@Override
	public void load(AsyncCallback<Void> asyncCallback) {
		exclusiveWorkbenchActionChecked = false;
		folderLoader.load(AsyncCallbacks.of( //
				result -> {
					rootFolder = result;
					asyncCallback.onSuccess(null);
				}, e -> {
					e.printStackTrace();
					asyncCallback.onSuccess(null);
				}));
	}	
	
	public void performAction(ModelPath modelPath, WorkbenchAction actionToPerform, GmContentView gmContentView, boolean handleInNewTab) {
		ModelAction action = workbenchActionHandlerRegistry.apply(prepareWorkbenchActionContext(modelPath, actionToPerform, gmContentView, handleInNewTab));
		if (action != null)
			action.perform(null);
	}
	
	private ModelPath getRootModelPath(GmContentView lastGmContentView, Object currentView) {
		if (rootModelPath != null || rootModelPathChecked)
			return rootModelPath;
		
		if (currentView instanceof ParentModelPathSupplier) {
			rootModelPath = ((ParentModelPathSupplier) currentView).apply(lastGmContentView);
			rootModelPathChecked = true;
			return rootModelPath;
		}
		
		if (currentView instanceof GmContentView)
			lastGmContentView = (GmContentView) currentView;
		
		if (currentView instanceof Widget)
			return getRootModelPath(lastGmContentView, ((Widget) currentView).getParent());
		
		rootModelPathChecked = true;
		return null;
	}
	
	private WorkbenchActionContext<WorkbenchAction> prepareWorkbenchActionContext(ModelPath modelPath, WorkbenchAction actionToPerform, GmContentView gmContentView, boolean handleInNewTab) {
		currentWorkbenchContext = new WorkbenchActionContext<WorkbenchAction>() {
			@Override
			public GmSession getGmSession() {
				if (gmContentView == null)
					return gmSession;
				else	
					return gmContentView.getGmSession();
			}

			@Override
			public List<ModelPath> getModelPaths() {
				return Collections.singletonList(modelPath);
			}
			
			@Override
			public ModelPath getRootModelPath() {
				GmContentView view = (GmContentView) getPanel();
				return WorkWithEntityExpert.this.getRootModelPath(view, view);
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public WorkbenchAction getWorkbenchAction() {
				return actionToPerform;
			}

			@Override
			public Object getPanel() {
				//return gmContentView.getView();
				return gmContentView;
			}
			
			@Override
			@SuppressWarnings("unusable-by-js")
			public Folder getFolder() {
				return getWorkWithFolder();
			}
			
			@Override
			public boolean isHandleInNewTab() {
				return handleInNewTab;
			}
		};
		return currentWorkbenchContext;
	}

	public boolean getSelectModelPath() {
		return selectModelPath;
	}

	public void setSelectModelPath(boolean selectModelPath) {
		this.selectModelPath = selectModelPath;
	}
	
}
