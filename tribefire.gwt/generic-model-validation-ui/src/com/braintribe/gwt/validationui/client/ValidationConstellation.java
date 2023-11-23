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
package com.braintribe.gwt.validationui.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.browserfeatures.client.Console;
import com.braintribe.gwt.gmview.client.GmAdditionalTextHandler;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.HasAdditionalText;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.ContentPanel;

public class ValidationConstellation extends ContentPanel implements GmListView, InitializableBean, HasAdditionalText, DisposableBean {

	private ValidationLogListPanel validationLogListPanel;
	private HTML emptyPanel;
	private PersistenceGmSession gmSession;
	private String useCase;
	private Supplier<ValidationLogRepresentation> validationLogListPanelProvider;
	private List<GmAdditionalTextHandler> listGmGmAdditionalTextHandler = new ArrayList<>();
	
	public ValidationConstellation() {
		setBodyBorder(false);
		setBorders(false);
		setHeaderVisible(false);
	}	
	
	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void addContent(ModelPath modelPath) {
		if (modelPath == null)
			return;
		
		Object entity = modelPath.last().getValue();
		if (entity!= null && entity instanceof ValidationLog)
			getValidationPanel().addValidationLog((ValidationLog) entity);
		
		updateCenterWidget();
		fireGmAdditionalTextHandler();
	}

	private void updateCenterWidget() {
		setWidget(getValidationPanel().isEmpty() ? getEmptyPanel()	: getValidationPanel());
		forceLayout();
	}
	
	@Override
	public void setContent(ModelPath modelPath) {
		getValidationPanel().clearValidationLog();
		addContent(modelPath);		
		fireGmAdditionalTextHandler();
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		//NOOP
	}


	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		//NOOP
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
		//NOOP		
	}


	@Override
	public GmContentView getView() {
		return this;
	}


	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;	
		getValidationPanel().configureGmSession(gmSession);
	}


	@Override
	public PersistenceGmSession getGmSession() {
		return this.gmSession;
	}


	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;		
		getValidationPanel().configureUseCase(useCase);
	}


	@Override
	public String getUseCase() {
		return useCase;
	}


	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		//NOOP
	}

	@Override
	public List<ModelPath> getAddedModelPaths() {
		return null;
	}
	
	@Override
	public void disposeBean() throws Exception {
		listGmGmAdditionalTextHandler.clear();
	}

	private HTML getEmptyPanel() {
		if (emptyPanel == null) {
			StringBuilder html = new StringBuilder();
			html.append("<div style='height: 100%; width: 100%; display: table;' class='emptyStyle'>");
			html.append("<div style='display: table-cell; vertical-align: middle'>").append(LocalizedText.INSTANCE.noValidations()).append("</div></div>");
			emptyPanel = new HTML(html.toString());
		}
		
		return emptyPanel;
	}

	private ValidationLogListPanel getValidationPanel() {		
		if (validationLogListPanel != null) 
			return validationLogListPanel;
		
		validationLogListPanel = (ValidationLogListPanel) validationLogListPanelProvider.get();
		validationLogListPanel.setValidationConstellation(this);
		return validationLogListPanel;			
	}
	
	public void clearValidationLog() {
		getValidationPanel().clearValidationLog();
		updateCenterWidget();
		fireGmAdditionalTextHandler();
	}

	public void setValidationLogListPanelProvider(Supplier<ValidationLogRepresentation> validationLogListPanelProvider) {
		this.validationLogListPanelProvider = validationLogListPanelProvider;
	}

	@Override
	public void intializeBean() throws Exception {
		updateCenterWidget();
	}

	@Override
	public String getAdditionalText() {
		return String.valueOf(validationLogListPanel.getAllPropertySize());
	}

	@Override
	public String getAdditionalClass() {
		return null;
	}

	@Override
	public boolean showAdditionalText() {
		return validationLogListPanel.getEntityEntrySize() > 0;
	}

	@Override
	public void addAdditionalListener(GmAdditionalTextHandler handler) {
		if (!listGmGmAdditionalTextHandler.contains(handler))
			listGmGmAdditionalTextHandler.add(handler);		
	}

	@Override
	public void removeAdditionalListener(GmAdditionalTextHandler handler) {
		listGmGmAdditionalTextHandler.remove(handler);
	}
	
	public void updateState() {
		updateCenterWidget();
		fireGmAdditionalTextHandler();	
	}
	
	private void fireGmAdditionalTextHandler() {
		Console.logWithTime("ValidationConstellation: fireGmAdditionalTextHandler");
		for (GmAdditionalTextHandler handler : listGmGmAdditionalTextHandler)
			handler.onUpdateAdditonalText(this);
	}	
}
