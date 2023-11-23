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

import java.util.List;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.dom.client.Element;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Class needed for being able to export {@link GmContentView} via JsInterop.
 */
@JsType(name = "GmContentView", namespace = InteropConstants.VIEW_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class GmContentViewInterop implements GmContentView {

	protected GmContentView viewWidget;

	@JsConstructor
	public GmContentViewInterop() {
		super();
	}

	/**
	 * Setter for the {@link GmContentView} which is related to this view.
	 */
	public void setViewWidget(GmContentView viewWidget) {
		this.viewWidget = viewWidget;
	}

	@Override
	@JsMethod
	public void addSelectionListener(GmSelectionListener sl) {
		// NOP
	}

	@Override
	@JsMethod
	public void removeSelectionListener(GmSelectionListener sl) {
		// NOP
	}

	@Override
	@JsMethod
	public ModelPath getFirstSelectedItem() {
		return null;
	}

	@Override
	@JsMethod
	public int getFirstSelectedIndex() {
		return -1;
	}

	@Override
	@JsMethod
	public List<ModelPath> getCurrentSelection() {
		return null;
	}

	@Override
	@JsMethod
	public boolean isSelected(Object element) {
		return false;
	}

	@Override
	@JsMethod
	public void select(int index, boolean keepExisting) {
		// NOP
	}

	@Override
	@JsIgnore
	public boolean select(Element element, boolean keepExisting) {
		return false;
	}

	@Override
	@JsMethod
	public void selectRoot(int index, boolean keepExisting) {
		// NOP
	}

	@Override
	@JsMethod
	public void deselectAll() {
		// NOP
	}

	@Override
	@JsMethod
	public GmContentView getView() {
		return this;
	}

	@Override
	@JsMethod
	public void configureGmSession(PersistenceGmSession gmSession) {
		// NOP
	}

	@Override
	@JsMethod
	public PersistenceGmSession getGmSession() {
		return null;
	}

	@Override
	@JsMethod
	public void configureUseCase(String useCase) {
		// NOP
	}

	@Override
	@JsMethod
	public String getUseCase() {
		return null;
	}

	@Override
	@JsMethod
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	@JsMethod
	public void setContent(ModelPath modelPath) {
		// NOP
	}

	@Override
	@JsMethod
	public Object getUxElement() {
		return null;
	}

	@Override
	@JsMethod
	public Object getUxWidget() {
		return null;
	}

	@Override
	@JsMethod
	public void detachUxElement() {
		// NOP
	}

	@Override
	@JsMethod
	public void setReadOnly(boolean readOnly) {
		// NOP
	}

	@Override
	@JsMethod
	public boolean isReadOnly() {
		return GmContentView.super.isReadOnly();
	}

	@Override
	@JsMethod
	public boolean isViewReady() {
		return GmContentView.super.isViewReady();
	}

	@Override
	@JsMethod
	public Future<Boolean> waitReply() {
		return GmContentView.super.waitReply();
	}
}
