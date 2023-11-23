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
package com.braintribe.gwt.gmview.client.js.interop;

import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.gmview.client.GmeDragAndDropView;
import com.braintribe.gwt.gmview.client.ParentModelPathSupplier;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;

/**
 * Exposing the {@link GmeDragAndDropView} via JsInterop
 * @author michel.docouto
 *
 */
@SuppressWarnings("unusable-by-js")
public interface GmeDragAndDropViewInterfaceInterop extends GmeDragAndDropView {
	
	@Override
	@JsMethod
	int getMaxAmountOfFilesToUpload();
	
	@Override
	@JsMethod
	void handleDropFileList(FileList fileList);
	
	@Override
	@JsMethod
	PersistenceGmSession getGmSession();
	
	@Override
	@JsMethod
	WorkbenchActionContext<TemplateBasedAction> getDragAndDropWorkbenchActionContext();
	
	@Override
	@JsIgnore
	void prepareDropTargetWidget(Widget dropTarget, int indexForSelection);
	
	@Override
	@JsIgnore
	void prepareDropTarget(Element dropTarget, int indexForSelection);
	
	@Override
	@JsMethod
	WorkbenchActionContext<TemplateBasedAction> prepareWorkbenchActionContext();
	
	@Override
	@JsMethod
	ParentModelPathSupplier getParentModelPathSupplier(Object view);
	
	@Override
	@JsMethod
	boolean isUploadingFolder(FileList fileList);

}
