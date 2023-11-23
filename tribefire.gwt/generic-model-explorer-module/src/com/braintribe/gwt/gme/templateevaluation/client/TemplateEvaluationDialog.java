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
package com.braintribe.gwt.gme.templateevaluation.client;

public class TemplateEvaluationDialog extends TemplateGIMADialog /*ClosableWindow implements InitializableBean, QueryProviderView<Template>*/ {
	
	@Override
	public void show() {
		applyButton.setText(LocalizedText.INSTANCE.execute());
		super.show();
	}
	
	/*private TemplateEvaluationPanel templateEvaluationPanel;
	private BorderLayoutContainer borderLayoutContainer;
	
	public TemplateEvaluationDialog() {
		setHeaderVisible(false);
		setClosable(false);
		setModal(true);
		setSize("800px", "600px");
		setOnEsc(true);
		setMaximizable(true);
		setMinWidth(560);
		setMinHeight(200);
		setBodyBorder(false);
		setBorders(false);
		
		borderLayoutContainer = new BorderLayoutContainer();
		setWidget(borderLayoutContainer);
	}
	
	@Override
	public void setOtherModeQueryProviderView(QueryProviderView<Template> otherModelQueryProviderView) {
		//Nothing to do
	}
	
	@Override
	public void modeQueryProviderViewChanged() {
		//Nothing to do
	}

	public void setTemplateEvaluationContext(TemplateEvaluationContext templateEvaluationContext) {
		this.templateEvaluationPanel.setTemplateEvaluationContext(templateEvaluationContext);
	}

	@Required
	public void setTemplateEvaluationPanel(TemplateEvaluationPanel templateEvaluationPanel) {
		this.templateEvaluationPanel = templateEvaluationPanel;
	}
	
	@Override
	public void show() {
		super.show();
		
		int currentHeight = getOffsetHeight();
		int computedHeight = Math.min(Document.get().getClientHeight(), currentHeight);
		if (computedHeight != currentHeight)
			setHeight(computedHeight);
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		//NOP
	}
	
	@Override
	public void intializeBean() throws Exception {
		borderLayoutContainer.setCenterWidget(templateEvaluationPanel);
		forceLayout();
	}
	
	public Future<Object> getEvaluatedPrototype(){
		Future<Object> result = templateEvaluationPanel.getEvaluatedPrototype();
		result.get(new AsyncCallback<Object>() {
			@Override
			public void onFailure(Throwable caught) {
				hide();
			}
			
			@Override
			public void onSuccess(Object result) {
				hide();
			}
		});
		return result;
	}

	@Override
	public Widget getWidget() {
		return this;
	}

	@Override
	public QueryProviderContext getQueryProviderContext() {
		return templateEvaluationPanel.getQueryProviderContext();
	}

	@Override
	public void notifyQueryPerformed(QueryResult queryResult, QueryProviderContext queryProviderContext) {
		templateEvaluationPanel.notifyQueryPerformed(queryResult, queryProviderContext)	;
	}

	@Override
	public void setEntityContent(Template entityContent) {
		templateEvaluationPanel.setEntityContent(entityContent);
	}

	@Override
	public void addQueryProviderViewListener(QueryProviderViewListener listener) {
		templateEvaluationPanel.addQueryProviderViewListener(listener);	
	}

	@Override
	public void removeQueryProviderViewListener(QueryProviderViewListener listener) {
		templateEvaluationPanel.removeQueryProviderViewListener(listener);
	}
	
	@Override
	public void focusEditor() {
		//NOP
	}*/

}
