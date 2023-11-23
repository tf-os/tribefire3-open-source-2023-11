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
package com.braintribe.gwt.gmview.client;

import java.util.List;

import com.braintribe.gwt.fileapi.client.File;
import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.fileapi.client.FilesTransfer;
import com.braintribe.gwt.gmview.action.client.LocalizedText;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface to be implemented by components which should handle file drag and drop.
 * {@link #prepareDropTargetWidget(Widget, int)} or {@link #prepareDropTarget(Element, int)} should be called to the area(s) where file drag and drop should be done.
 * 
 * @author michel.docouto
 *
 */
@SuppressWarnings("unusable-by-js")
public interface GmeDragAndDropView {
	
	public static String GME_ELEMENT_DROP_TARGET = "gmeElementDropTarget";
	
	public int getMaxAmountOfFilesToUpload();
	public void handleDropFileList(FileList fileList);
	public PersistenceGmSession getGmSession();
	public WorkbenchActionContext<TemplateBasedAction> getDragAndDropWorkbenchActionContext();

	/**
	 * Prepares the given target to be a D&D listener and handler.
	 * @param indexForSelection - if >= 0, then the given index will be selected in the view. If < 0, this means an empty space in the view.
	 * Of course, indexForSelection only is used if the view is a {@link GmSelectionSupport} view.
	 */
	public default void prepareDropTargetWidget(Widget dropTarget, int indexForSelection) {
		dropTarget.addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
			dropTarget.addStyleName("gmeWidgetDropTarget");
		}, DragOverEvent.getType());

		dropTarget.addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
			dropTarget.addStyleName("gmeWidgetDropTarget");
		}, DragEnterEvent.getType());

		dropTarget.addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
			dropTarget.removeStyleName("gmeWidgetDropTarget");
		}, DragLeaveEvent.getType());

		dropTarget.addDomHandler(event -> {
			event.stopPropagation();
			event.preventDefault();
			
			DataTransfer dataTransfer = event.getDataTransfer();
			if (GMEUtil.isUploadingFolder(dataTransfer)) {
				GlobalState.showProcess(LocalizedText.INSTANCE.cannotUploadFolders());
				dropTarget.removeStyleName("gmeWidgetDropTarget");
				return;
			}
			
			FilesTransfer filesTransfer = dataTransfer.cast();
			FileList fileList = filesTransfer.getFiles();
			if (fileList != null && fileList.getLength() > 0) {
				if (GmeDragAndDropView.this instanceof GmSelectionSupport) {
					GmSelectionSupport selectionSupport = (GmSelectionSupport) GmeDragAndDropView.this;
					if (indexForSelection >= 0)
						selectionSupport.select(indexForSelection, false);
					else
						selectionSupport.deselectAll();
				}
				
				handleDropFileList(fileList);
			}

			dropTarget.removeStyleName("gmeWidgetDropTarget");
		}, DropEvent.getType());
	}
	
	/**
	 * Prepares the given target to be a D&D listener and handler.
	 * @param indexForSelection - if >= 0, then the given index will be selected in the view. If < 0, then we try to check if the target element is selectable.
	 * If it is not, this means an empty space in the view was used for drop, so we select nothing.
	 * Of course, indexForSelection only is used if the view is a {@link GmSelectionSupport} view.
	 */
	public default void prepareDropTarget(Element dropTarget, int indexForSelection) {
		if (dropTarget == null)
			return;
		
		DOM.sinkBitlessEvent(dropTarget, DragOverEvent.getType().getName());
		DOM.sinkBitlessEvent(dropTarget, DragEnterEvent.getType().getName());
		DOM.sinkBitlessEvent(dropTarget, DragLeaveEvent.getType().getName());
		DOM.sinkBitlessEvent(dropTarget, DropEvent.getType().getName());
		DOM.setEventListener(dropTarget, event -> {
			String eventType = event.getType();
			if (eventType.equals(DragOverEvent.getType().getName()) || eventType.equals(DragEnterEvent.getType().getName())) {
				event.stopPropagation();
				event.preventDefault();
				dropTarget.addClassName(GME_ELEMENT_DROP_TARGET);
			} else if (eventType.equals(DragLeaveEvent.getType().getName())) {
				event.stopPropagation();
				event.preventDefault();
				dropTarget.removeClassName(GME_ELEMENT_DROP_TARGET);
			} else if (eventType.equals(DropEvent.getType().getName())) {
				event.stopPropagation();
				event.preventDefault();
				EventTarget currentEventTarget = event.getCurrentEventTarget();
				
				DataTransfer dataTransfer = event.getDataTransfer();
				if (GMEUtil.isUploadingFolder(dataTransfer)) {
					GlobalState.showProcess(LocalizedText.INSTANCE.cannotUploadFolders());
					dropTarget.removeClassName(GME_ELEMENT_DROP_TARGET);
					return;
				}
				
				FilesTransfer filesTransfer = dataTransfer.cast();
				FileList fileList = filesTransfer.getFiles();
				if (fileList != null && fileList.getLength() > 0) {
					if (isUploadingFolder(fileList))
						GlobalState.showProcess(LocalizedText.INSTANCE.cannotUploadFolders());
					else {
						if (GmeDragAndDropView.this instanceof GmSelectionSupport) {
							GmSelectionSupport selectionSupport = (GmSelectionSupport) GmeDragAndDropView.this;
							if (indexForSelection >= 0)
								selectionSupport.select(indexForSelection, false);
							else if (!Element.is(currentEventTarget) || !selectionSupport.select(Element.as(currentEventTarget), false))
								selectionSupport.deselectAll();
						}
						
						Scheduler.get().scheduleDeferred(() -> handleDropFileList(fileList));
					}
				}

				dropTarget.removeClassName(GME_ELEMENT_DROP_TARGET);
			}
		});
	}
	
	public default WorkbenchActionContext<TemplateBasedAction> prepareWorkbenchActionContext() {
		return new InnerWorkbenchActionContext(this);
	}
	
	default ParentModelPathSupplier getParentModelPathSupplier(Object view) {
		if (view instanceof ParentModelPathSupplier)
			return (ParentModelPathSupplier) view;
		
		if (view instanceof Widget)
			return getParentModelPathSupplier(((Widget) view).getParent());
		
		return null;
	}
	
	default boolean isUploadingFolder(FileList fileList) {
		for (int i = 0; i < fileList.getLength(); i++) {
			File item = fileList.item(i);
			if (item.size() == 0)
				return true;
		}
		
		return false;
	}

	@SuppressWarnings("unusable-by-js")
	public static class InnerWorkbenchActionContext implements WorkbenchActionContext<TemplateBasedAction> {
		private GmeDragAndDropView view;
		private TemplateBasedAction workbenchAction;
		private boolean useForm;
		
		public InnerWorkbenchActionContext(GmeDragAndDropView view) {
			this.view = view;
		}
		
		@Override
		public GmSession getGmSession() {
			return view.getGmSession();
		}

		@Override
		public List<ModelPath> getModelPaths() {
			if (view instanceof GmSelectionSupport)
				return ((GmSelectionSupport) view).getCurrentSelection();
			
			return null;
		}
		
		@Override
		public ModelPath getRootModelPath() {
			if (view instanceof GmContentView) {
				ParentModelPathSupplier parentModelPathSupplier = view.getParentModelPathSupplier(view);
				if (parentModelPathSupplier != null)
					return parentModelPathSupplier.apply(null);
			}
			
			return null;
		}
		
		@Override
		public void setWorkbenchAction(TemplateBasedAction workbenchAction) {
			this.workbenchAction = workbenchAction;
		}

		@Override
		public TemplateBasedAction getWorkbenchAction() {
			return workbenchAction;
		}

		@Override
		public Object getPanel() {
			return view;
		}

		@Override
		public Folder getFolder() {
			return null;
		}
		
		@Override
		public void setUseForm(boolean useForm) {
			this.useForm = useForm;
		}
		
		@Override
		public boolean isUseForm() {
			return useForm;
		}
	}

}
