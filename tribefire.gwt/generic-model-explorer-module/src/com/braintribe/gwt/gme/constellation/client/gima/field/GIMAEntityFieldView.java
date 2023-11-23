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
package com.braintribe.gwt.gme.constellation.client.gima.field;

import java.util.List;

import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.gima.GIMAView;
import com.braintribe.gwt.gmview.client.EntityFieldDialog;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

public abstract class GIMAEntityFieldView<T extends EntityFieldDialog<?>> extends SimpleContainer implements GIMAView {
	
	protected GIMADialog gimaDialog;
	
	public GIMAEntityFieldView(T entityFieldDialog, GIMADialog gimaDialog) {
		this.gimaDialog = gimaDialog;
		add(entityFieldDialog.getView());
	}
	
	@Override
	public boolean isApplyAllHandler() {
		return false;
	}
	
	public void configureGimaDialog(GIMADialog gimaDialog) {
		this.gimaDialog = gimaDialog;
	}
	
	/******************* GmContentView related methods ************************/
	
	@Override
	public GmContentView getView() {
		return this;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gimaDialog.getSessionForTransactionAndCMD();
	}
	
	@Override
	public String getUseCase() {
		return gimaDialog.getUseCase();
	}
	
	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		//NOP
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return null;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return null;
	}

	@Override
	public boolean isSelected(Object element) {
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		//NOP
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		//NOP
	}

	@Override
	public void configureUseCase(String useCase) {
		//NOP
	}

}
