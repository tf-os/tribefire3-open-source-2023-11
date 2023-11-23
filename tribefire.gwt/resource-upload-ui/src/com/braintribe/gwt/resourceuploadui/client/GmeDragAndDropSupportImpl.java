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
package com.braintribe.gwt.resourceuploadui.client;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.CanceledException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.fileapi.client.ProgressHandler;
import com.braintribe.gwt.gm.resource.api.client.ResourceBuilder;
import com.braintribe.gwt.gme.constellation.client.GlobalActionsToolBar;
import com.braintribe.gwt.gmview.action.client.WorkbenchActionSelectionHandler;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmeDragAndDropSupport;
import com.braintribe.gwt.gmview.client.GmeDragAndDropView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.TemplateSupport;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.InstantiationAction;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.resources.client.CssResource;
import com.sencha.gxt.widget.core.client.ProgressBar;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;

/**
 * Class containing operations for the Drag and Drop support feature.
 * @author michel.docouto
 *
 */
public class GmeDragAndDropSupportImpl implements GmeDragAndDropSupport {
	
	private ResourceBuilder resourceBuilder;
	private Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry;
	private GlobalActionsToolBar globalActionsToolBar;
	private Supplier<List<TemplateBasedAction>> templateActionsSupplier;
	private Function<EntityType<?>, Future<List<InstantiationAction>>> instantiationActionsSupplier;
	private Variable currentResourceVariable;
	private ProgressHandler progressHandler;
	private ProgressBar progressBar;
	private BorderLayoutContainer progressParentContainer;
	private Object previousDefaultValue;
	private WorkbenchActionSelectionHandler workbenchActionSelectionHandler;
	
	/**
	 * Configures the required {@link Supplier} which returns a list of {@link TemplateBasedAction} available
	 */
	@Required
	public void setTemplateActionsSupplier(Supplier<List<TemplateBasedAction>> templateActionsSupplier) {
		this.templateActionsSupplier = templateActionsSupplier;
	}
	
	/**
	 * Configures the required {@link Function} which returns a list of {@link InstantiationAction} for a given {@link EntityType}.
	 */
	@Required
	public void setInstantiationActionsSupplier(Function<EntityType<?>, Future<List<InstantiationAction>>> instantiationActionsSupplier) {
		this.instantiationActionsSupplier = instantiationActionsSupplier;
	}
	
	/**
	 * Configures the required {@link ResourceBuilder} for uploading files.
	 */
	@Required
	public void setResourceBuilder(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}
	
	/**
	 * Configures the registry which contains action experts to be performed depending on the action type.
	 */
	@Required
	public void setWorkbenchActionHandlerRegistry(Function<WorkbenchActionContext<?>, ModelAction> workbenchActionHandlerRegistry) {
		this.workbenchActionHandlerRegistry = workbenchActionHandlerRegistry;
	}
	
	/**
	 * Configures the expert for getting the context {@link EntityType} for the current view.
	 */
	@Required
	public void setGlobalActionsToolBar(GlobalActionsToolBar globalActionsToolBar) {
		this.globalActionsToolBar = globalActionsToolBar;
	}
	
	/**
	 * Configures the required parent container for the upload progress bar.
	 */
	@Required
	public void setProgressParentContainer(BorderLayoutContainer progressParentContainer) {
		this.progressParentContainer = progressParentContainer;
	}
	
	/**
	 * Configures the required {@link WorkbenchActionSelectionHandler} used for choosing which action to use while uploading.
	 */
	@Required
	public void setWorkbenchActionSelectionHandler(WorkbenchActionSelectionHandler workbenchActionSelectionHandler) {
		this.workbenchActionSelectionHandler = workbenchActionSelectionHandler;
	}
	
	/**
	 * Configures the css resource for default styling, which should be ensured to be injected 
	 */
	@Configurable
	public void setCssResource(CssResource cssResource) {
		cssResource.ensureInjected();
	}
	
	/**
	 * Look for available actions after the files were dropped.
	 */
	@Override
	public void handleDropFileList(FileList fileList, GmeDragAndDropView view) {
		WorkbenchActionContext<TemplateBasedAction> workbenchActionContext = view.getDragAndDropWorkbenchActionContext();
		List<ModelPath> modelPaths = workbenchActionContext.getModelPaths();
		ModelPath dropTargetModelPath = modelPaths == null || modelPaths.isEmpty() ? null : modelPaths.get(0);
		
		boolean isCollection = fileList.getLength() > 1;
		if (dropTargetModelPath != null) {
			List<TemplateBasedAction> actions = templateActionsSupplier.get();
			getAvailableResourceBasedAction(actions, workbenchActionContext, isCollection) //
					.andThen(action -> {
						if (action == null) {
							showNoActionAvailableMessage(isCollection);
							return;
						}

						uploadFiles(fileList, view, workbenchActionContext);
					});
			return;
		}
		
		EntityType<?> entityType;
		if (workbenchActionContext.getPanel() instanceof GmContentView)
			entityType = globalActionsToolBar.getViewContextEntityType((GmContentView) workbenchActionContext.getPanel());
		else
			entityType = null;
		
		if (entityType == null) {
			showNoActionAvailableMessage(isCollection);
			return;
		}
		
		instantiationActionsSupplier.apply(entityType).andThen(result -> {
			if (result == null || result.isEmpty()) {
				uploadFiles(fileList, view, null);
				return;
			}
			
			List<TemplateBasedAction> actions = new ArrayList<>();
			for (InstantiationAction action : result) {
				if (action instanceof TemplateBasedAction)
					actions.add((TemplateBasedAction) action);
			}

			getAvailableResourceBasedAction(actions, workbenchActionContext, isCollection).andThen(action -> {
				if (action == null && !Resource.T.equals(entityType)) {
					showNoActionAvailableMessage(isCollection);
					return;
				}
				
				uploadFiles(fileList, view, action == null ? null : workbenchActionContext);
			});
		}).onError(caught -> {
			GlobalState.showError("Error while getting the list of instantiation actions.", caught);
			caught.printStackTrace();
		});
	}
	
	@Override
	public Supplier<List<TemplateBasedAction>> getTemplateActionsSupplier() {
		return templateActionsSupplier;
	}
	
	private Variable getResourceVariable(List<Variable> variables, boolean handlingMultipleFiles) {
		for (Variable variable : variables) {
			String varTypeSignature = variable.getTypeSignature();
			if (handlingMultipleFiles) {
				if (varTypeSignature.contains(Resource.T.getTypeSignature()) && !varTypeSignature.equals(Resource.T.getTypeSignature()))
					return variable;
			} else if (varTypeSignature.contains(Resource.T.getTypeSignature()))
				return variable;
		}
		
		return null;
	}
	
	private void prepareDefaultValue(Variable resourceVariable, List<Resource> resources) {
		resourceVariable.detach();
		previousDefaultValue = resourceVariable.getDefaultValue();
		
		String typeSignature = resourceVariable.getTypeSignature();
		if (typeSignature.equals(Resource.T.getTypeSignature()))
			resourceVariable.setDefaultValue(resources.get(0));
		else if (typeSignature.contains("list<"))
			resourceVariable.setDefaultValue(resources);
		else if (typeSignature.contains("set<"))
			resourceVariable.setDefaultValue(new HashSet<>(resources));
	}
	
	private Future<TemplateBasedAction> getAvailableResourceBasedAction(List<TemplateBasedAction> actions,
			WorkbenchActionContext<TemplateBasedAction> workbenchActionContext, boolean isCollection) {
		currentResourceVariable = null;
		List<TemplateBasedAction> availableActions = new ArrayList<>();
		for (TemplateBasedAction templateAction : actions) {
			Template template = templateAction.getTemplate();

			Set<Variable> variables = newSet(TemplateSupport.findVariables(templateAction));
			List<Variable> visibleVariables = TemplateSupport.extractVisibleVariables(variables, template);
			
			Variable resourceVariable = getResourceVariable(visibleVariables, isCollection);
			if (resourceVariable != null)
				availableActions.add(templateAction);
		}
		
		Future<TemplateBasedAction> future = new Future<>();
		if (availableActions.isEmpty()) {
			Scheduler.get().scheduleDeferred(() -> future.onSuccess(null));
			return future;
		} else if (availableActions.size() == 1) {
			handleActionSelection(availableActions.get(0), workbenchActionContext, isCollection, future);
			return future;
		}
		
		Future<TemplateBasedAction> selectionFuture = new Future<>();
		selectionFuture.andThen(action -> {
			if (action == null) {
				future.onFailure(new CanceledException("Action selection was cancelled"));
				return;
			}
			
			handleActionSelection(action, workbenchActionContext, isCollection, future);
		}).onError(future::onFailure);
		workbenchActionSelectionHandler.handleActionSelection(availableActions, selectionFuture);
		
		return future;
	}
	
	private void handleActionSelection(TemplateBasedAction action, WorkbenchActionContext<TemplateBasedAction> workbenchActionContext,
			boolean isCollection, Future<TemplateBasedAction> future) {
		workbenchActionContext.setWorkbenchAction(action);
		Template template = action.getTemplate();
		Set<Variable> variables = newSet(TemplateSupport.findVariables(action));
		List<Variable> visibleVariables = TemplateSupport.extractVisibleVariables(variables, template);
		Variable resourceVariable = getResourceVariable(visibleVariables, isCollection);
		List<Variable> mandatoryVariables = TemplateSupport.extractMandatoryVariables(visibleVariables, template);
		mandatoryVariables.remove(resourceVariable);
		workbenchActionContext.setUseForm(!mandatoryVariables.isEmpty());
		currentResourceVariable = resourceVariable;
		
		future.onSuccess(action);
	}
	
	private void showNoActionAvailableMessage(boolean isCollection) {
		if (isCollection)
			GlobalState.showProcess(ResourceUploadLocalizedText.INSTANCE.noActionAvailableForHandlingMultipleFiles());
		else
			GlobalState.showProcess(ResourceUploadLocalizedText.INSTANCE.noActionAvailableForHandlingFile());
	}
	
	private void uploadFiles(FileList fileList, GmeDragAndDropView view, WorkbenchActionContext<TemplateBasedAction> workbenchActionContext) {
		int amountOfFiles = fileList.getLength();
		if (workbenchActionContext != null && currentResourceVariable.getTypeSignature().equals(Resource.T.getTypeSignature()) && amountOfFiles > 1) {
			GlobalState.showProcess(ResourceUploadLocalizedText.INSTANCE.maxAmountOfFiles(1, amountOfFiles));
			return;
		}
		
		prepareUploadingMessage(amountOfFiles);
		
		resourceBuilder.configureGmSession(view.getGmSession());
		resourceBuilder.fromFiles().addFiles(fileList).withProgressHandler(getProgressHandler()).build().andThen(result -> {
			removeProgressBarFromParent();
			GlobalState.unmask();
			GlobalState.clearState();
			if (workbenchActionContext == null) {
				GlobalState.showSuccess(ResourceUploadLocalizedText.INSTANCE.resourceUploaded(result.size()));
				return;
			}
			
			boolean performed = handleAction(result, workbenchActionContext);
			if (!performed)
				GlobalState.showSuccess(ResourceUploadLocalizedText.INSTANCE.resourceUploaded(result.size()));
		}).onError(caught -> {
			removeProgressBarFromParent();
			GlobalState.unmask();
			GlobalState.showError("Error while uploading files.", caught);
			caught.printStackTrace();
		});
	}
	
	private boolean handleAction(List<Resource> resources, WorkbenchActionContext<TemplateBasedAction> workbenchActionContext) {
		prepareDefaultValue(currentResourceVariable, resources);
		
		ModelAction modelAction = workbenchActionHandlerRegistry.apply(workbenchActionContext);
		if (modelAction != null) {
			GmContentView view = workbenchActionContext.getPanel() instanceof GmContentView ? ((GmContentView) workbenchActionContext.getPanel()) : null;
			modelAction.configureGmContentView(view);
			modelAction.perform(null);
			
			currentResourceVariable.setDefaultValue(previousDefaultValue);
			return true;
		}
		
		currentResourceVariable.setDefaultValue(previousDefaultValue);
		return false;
	}
	
	private void prepareUploadingMessage(int ammountOfFiles) {
		Action cancelUploadAction = new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				removeProgressBarFromParent();
				resourceBuilder.abortUpload();
				GlobalState.unmask();
				GlobalState.showProcess(ResourceUploadLocalizedText.INSTANCE.uploadAborted());
			}
		};
		cancelUploadAction.setName(ResourceUploadLocalizedText.INSTANCE.abortUpload());
		
		GlobalState.showSuccess(ResourceUploadLocalizedText.INSTANCE.uploadingFiles(ammountOfFiles), cancelUploadAction);

		if (progressBar != null)
			progressBar.reset();
		
		ProgressBar progressBar = getProgressBar();
		progressBar.getCell().setProgressText(ResourceUploadLocalizedText.INSTANCE.progressText("{0}"));
		progressParentContainer.setSouthWidget(progressBar, new BorderLayoutData(18));
		progressParentContainer.forceLayout();
		GlobalState.mask();
	}
	
	private void removeProgressBarFromParent() {
		progressParentContainer.remove(progressBar);
		progressParentContainer.forceLayout();
	}
	
	private ProgressBar getProgressBar() {
		if (progressBar != null)
			return progressBar;
		
		progressBar = new ProgressBar();
		return progressBar;
	}
	
	private ProgressHandler getProgressHandler() {
		if (progressHandler != null)
			return progressHandler;
		
		progressHandler = event -> {
			double total = event.getTotal();
			double loaded = event.getLoaded();
			
			if (progressBar != null)
				progressBar.setValue(loaded / total);
		};
		
		return progressHandler;
	}
	
}
