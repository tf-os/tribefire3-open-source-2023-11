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
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmeDragAndDropView;
import com.braintribe.gwt.gmview.client.ParentModelPathSupplier;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Class needed for being able to export the following classes via JsInterop: {@link GmListView},
 * {@link GmActionSupport}, {@link GmViewActionProvider}, {@link GmViewport} and {@link GmeDragAndDropView}.
 */
@JsType(name = "GmActionListViewportDragAndDrop", namespace = InteropConstants.VIEW_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class GmActionListViewportDragAndDropInterop extends GmActionListViewportInterop implements GmeDragAndDropViewInterfaceInterop {
	
	@JsConstructor
	public GmActionListViewportDragAndDropInterop() {
		super();
	}

	@Override
	@JsMethod
	public int getMaxAmountOfFilesToUpload() {
		return 0;
	}

	@Override
	@JsMethod
	public void handleDropFileList(FileList fileList) {
		//NOP
	}

	@Override
	@JsMethod
	public WorkbenchActionContext<TemplateBasedAction> getDragAndDropWorkbenchActionContext() {
		return null;
	}

	@Override
	@JsIgnore
	public void prepareDropTargetWidget(Widget dropTarget, int indexForSelection) {
		//NOP
	}

	@Override
	@JsIgnore
	public void prepareDropTarget(Element dropTarget, int indexForSelection) {
		//NOP
	}

	@Override
	@JsMethod
	public WorkbenchActionContext<TemplateBasedAction> prepareWorkbenchActionContext() {
		return null;
	}

	@Override
	@JsMethod
	public ParentModelPathSupplier getParentModelPathSupplier(Object view) {
		return null;
	}

	@Override
	@JsMethod
	public boolean isUploadingFolder(FileList fileList) {
		return false;
	}

}
