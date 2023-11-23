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
package com.braintribe.gwt.thumbnailpanel.client.js;

import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmViewportListener;
import com.braintribe.gwt.gmview.client.ParentModelPathSupplier;
import com.braintribe.gwt.gmview.client.js.interop.GmActionSupportInterfaceInterop;
import com.braintribe.gwt.gmview.client.js.interop.GmListViewInterfaceInterop;
import com.braintribe.gwt.gmview.client.js.interop.GmViewActionProviderInterfaceInterop;
import com.braintribe.gwt.gmview.client.js.interop.GmViewportInterfaceInterop;
import com.braintribe.gwt.gmview.client.js.interop.GmeDragAndDropViewInterfaceInterop;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.gwt.thumbnailpanel.client.ThumbnailPanel;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Class used for preparing the {@link ThumbnailPanel} for being used in the JS side.
 *
 */
@JsType (namespace = InteropConstants.VIEW_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class JsThumbnailPanel extends ThumbnailPanel implements GmListViewInterfaceInterop, GmViewActionProviderInterfaceInterop,
		GmActionSupportInterfaceInterop, GmViewportInterfaceInterop, GmeDragAndDropViewInterfaceInterop {
	
	@JsConstructor
	public JsThumbnailPanel() {
		super();
	}
	
	@JsMethod
	@Override
	public Object getUxElement() {
		//calling onAttach manually when using the TP as an external element so we have event handling enabled
		if (!isAttached())
			super.onAttach();
		
		return getElement();
	}
	
	@Override
	public Object getUxWidget() {
		return this;
	}
	
	@JsMethod
	@Override
	public void detachUxElement() {
		super.onDetach();
	}
	
	@JsIgnore
	@Override
	public boolean select(Element element, boolean keepExisting) {
		return false;
	}

	@JsMethod
	@Override
	public void selectRoot(int index, boolean keepExisting) {
		select(index, keepExisting);
	}
	
	@JsMethod
	@Override
	public boolean isReadOnly() {
		return super.isReadOnly();
	}
	
	@JsMethod
	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
	}
	
	@JsMethod
	@Override
	public boolean isViewReady() {
		return super.isViewReady();
	}
	
	@JsMethod
	@Override
	public ActionProviderConfiguration getActions() {
		return super.getActions();
	}
	
	@JsMethod
	@Override
	public boolean isFilterExternalActions() {
		return super.isFilterExternalActions();
	}
	
	@JsMethod
	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		super.setActionManager(actionManager);
	}
	
	@JsMethod
	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		return super.getGmContentViewActionManager();
	}
	
	@JsMethod
	@Override
	public void addGmViewportListener(GmViewportListener vl) {
		super.addGmViewportListener(vl);
	}
	
	@JsMethod
	@Override
	public void removeGmViewportListener(GmViewportListener vl) {
		super.removeGmViewportListener(vl);
	}
	
	@JsMethod
	@Override
	public boolean isWindowOverlappingFillingSensorArea() {
		return super.isWindowOverlappingFillingSensorArea();
	}
	
	@JsMethod
	@Override
	public WorkbenchActionContext<TemplateBasedAction> prepareWorkbenchActionContext() {
		return super.prepareWorkbenchActionContext();
	}
	
	@JsMethod
	@Override
	public boolean isUploadingFolder(FileList fileList) {
		return super.isUploadingFolder(fileList);
	}
	
	@JsMethod
	@Override
	public ParentModelPathSupplier getParentModelPathSupplier(Object view) {
		return super.getParentModelPathSupplier(view);
	}
	
	@JsIgnore
	@Override
	public void prepareDropTargetWidget(Widget dropTarget, int indexForSelection) {
		super.prepareDropTargetWidget(dropTarget, indexForSelection);
	}
	
	@JsIgnore
	@Override
	public void prepareDropTarget(Element dropTarget, int indexForSelection) {
		super.prepareDropTarget(dropTarget, indexForSelection);
	}
	
	@JsMethod
	@Override
	public void fireViewportListener(GmViewportListener listener) {
		listener.onWindowChanged(this);
	}

}
