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
package com.braintribe.gwt.modeller.client.typesoverview;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog.QuickAccessResult;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField.QuickAccessTriggerFieldListener;
import com.braintribe.gwt.gme.workbench.client.resources.WorkbenchResources;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.modeller.client.GmModeller;
import com.braintribe.gwt.modeller.client.resources.ModellerModuleResources;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class GmModellerTypesOverviewPanel extends FlowPanel implements ManipulationListener, QuickAccessTriggerFieldListener, InitializableBean{
	
	GmMetaModel gmMetaModel;
	ModellerView modellerView;
	PersistenceGmSession session;
	GmModeller modeller;
	FlowPanel addSection;
	Label addDepTitle;	
	TextBox add;
	
	FlowPanel filterSection;
	Button expand;
	Label filterTitle;
	TextBox filter;
	
	FlowPanel typesOverviewWrapper;
	Map<GmMetaModel, TypeOverviewEntry> typeOverviewEntries;
	Map<GmType, TypeEntry> typeEntries = new HashMap<>();
//	Set<GenericEntity> excludes = new HashSet<>();
	QuickAccessDialog quickAccessDialog;
	Supplier<SpotlightPanel> quickAccessPanelProvider;
	
	boolean readOnly = false;
	
	public GmModellerTypesOverviewPanel() {
		
	}
	
	public void setGmMetaModel(GmMetaModel gmMetaModel) {
		this.gmMetaModel = gmMetaModel;
		adaptOverview(null);
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
//	public void setSession(PersistenceGmSession session) {		
//		this.session = session;
//		adapt();
//	}
	
	public void setModellerView(ModellerView modellerView) {
		this.modellerView = modellerView;
		setGmMetaModel(modellerView.getMetaModel());
		adaptOverview(null);
	}
	
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	@Override
	public void intializeBean() throws Exception {
		addStyleName("typeOverviewPanel");
		
		if(!readOnly) {
			addSection = new FlowPanel();
			addSection.addStyleName("typeOverviewAddSection");
			addSection.add(getAddDepTitle());
			addSection.add(getAdd());
			
			FlowPanel p = new FlowPanel();
			p.add(addSection);
			add(p);
		}
		
		filterSection = new FlowPanel();
		filterSection.addStyleName("typeOverviewFilterSection");
		
		FlowPanel filterTopSection = new FlowPanel();
		filterTopSection.add(getExpand());
		filterTopSection.add(getFilterTitle());
		
		filterSection.add(filterTopSection);
		filterSection.add(getFilter());
		
		FlowPanel p = new FlowPanel();
		p.add(filterSection);
		add(p);
		
		add(getTypesOverviewWrapper());
	}
	
	@Override
	public void onQuickAccessResult(QuickAccessResult result) {
		if(result != null) {
			GmMetaModel modelToAdd = (GmMetaModel) result.getObject();
			refresh(modelToAdd);
		}
		if(quickAccessDialog != null)
			quickAccessDialog.forceHide();
		getAdd().setText("");
		Scheduler.get().scheduleFixedDelay(() -> {
			modeller.getModellerPanel().setFocus(true);	
			return false;
		}, 50);
	}
	
	private void refresh(GmMetaModel model) {
		Object id = GmMetaModel.T.getIdProperty().get(model);		
		EntityQuery query = EntityQueryBuilder.from(GmMetaModel.T).where().property(GmMetaModel.T.getIdProperty().getName()).eq(id).tc().negation().joker().done();
		session.query().entities(query).result(Future.async(this::error, this::handleResult));
	}
	
	private void handleResult(EntityQueryResultConvenience conv) {
		GenericEntity entity = conv.first();
		if(GmMetaModel.T.isValueAssignable(entity)) {					
			gmMetaModel.getDependencies().add((GmMetaModel) entity);				
		}
	}
	
	private void error(Throwable t) {
		t.printStackTrace();
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		adaptOverview(null);
	}
	
	public void adaptOverview() {
		adaptOverview(null);
	}
	
	void adaptOverview(String filter) {
//		if(clear)
		getTypesOverviewWrapper().clear();
		if(gmMetaModel != null) {
			if(modeller.getOracle() != null) {
				modeller.getOracle().getDependencies().transitive().includeSelf().asGmMetaModels().forEach(model -> {
					addDep(model, filter);
				});
			}
		}
//		addDep(gmMetaModel);		
	}
	
	private void addDep(GmMetaModel model, String filter) {
		TypeOverviewEntry depTypeOverviewEntry = new TypeOverviewEntry(this, model, filter);
		if(depTypeOverviewEntry.hasTypes())
			getTypesOverviewWrapper().add(depTypeOverviewEntry);
//		for(GmMetaModel dep : model.getDependencies()) {
//			addDep(dep);
//		}
	}
	
	public FlowPanel getTypesOverviewWrapper() {
		if(typesOverviewWrapper == null) {
			typesOverviewWrapper = new FlowPanel();
		}
		return typesOverviewWrapper;
	}
	
	public Label getAddDepTitle() {
		if(addDepTitle == null) {
			addDepTitle = new Label("Add Dependency");
		}
		return addDepTitle;
	}
	
	public Label getFilterTitle() {
		if(filterTitle == null) {
			filterTitle = new Label("Dependencies");
		}
		return filterTitle;
	}
	
	public Button getExpand() {
		if(expand == null) {
			String img = "<img src='"+ModellerModuleResources.INSTANCE.expanded().getSafeUri().asString()+"'>";
			expand = new Button(img, (ClickHandler) event -> {
				// TODO Auto-generated method stub
			});
			expand.addStyleName("modellerButton");
		}
		return expand;
	}
	
	public TextBox getFilter() {
		if(filter == null) {
			filter = new TextBox();
			filter.getElement().setAttribute("placeholder", "Type model or type name to filter...");
			filter.getElement().setAttribute("style", "margin-left: 30px; background: none; font-size: 12pt");
			filter.addKeyUpHandler(event -> adaptOverview(filter.getValue()));
		}
		return filter;
	}
	
	public TextBox getAdd() {
		if(add == null) {
			add = new TextBox();
			add.getElement().setAttribute("placeholder", "Type to filter models...");
			add.getElement().setAttribute("style", "background: none; font-size: 12pt");
			add.addKeyUpHandler(event -> {
				int keyCode = event.getNativeKeyCode();
				if (keyCode == KeyCodes.KEY_ESCAPE) {
					onQuickAccessResult(null);
					return;
				}
				
				try {
					if (keyCode != KeyCodes.KEY_MAC_FF_META && 
							keyCode != KeyCodes.KEY_ENTER && keyCode != KeyCodes.KEY_DOWN && keyCode != KeyCodes.KEY_UP && keyCode != KeyCodes.KEY_LEFT && keyCode != KeyCodes.KEY_RIGHT)
						showQuickAccess();
				} catch(Exception ex) {
					ErrorDialog.show("Error while providing quickAccessDialog", ex);
				}
			});
		}
		return add;
	}
	
	private void showQuickAccess() throws RuntimeException{
		QuickAccessDialog quickAccessDialog = getQuickAccessDialog();
		quickAccessDialog
				.getQuickAccessResult(quickAccessDialog.getQuickAccessPanel().prepareTypeCondition(GmMetaModel.T), getAdd(), getAdd().getText())
				.andThen(result -> onQuickAccessResult(result)) //
				.onError(e -> ErrorDialog.show("Error while providing showQuickAccess", e));
	}
	
	private QuickAccessDialog getQuickAccessDialog() throws RuntimeException {
		if(quickAccessDialog == null){
			quickAccessDialog = new QuickAccessDialog();
			quickAccessDialog.setShadow(false);
			quickAccessDialog.setUseApplyButton(false);
			quickAccessDialog.setUseNavigationButtons(false);
			quickAccessDialog.setInstantiateButtonLabel("Add");			
			quickAccessDialog.setFocusWidget(getAdd());
			
			quickAccessDialog.setQuickAccessPanelProvider(getQuickAccessPanelProvider());
			quickAccessDialog.addStyleName(WorkbenchResources.INSTANCE.css().border());
			try {
				quickAccessDialog.intializeBean();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return quickAccessDialog;
	}
	
	private Supplier<SpotlightPanel> getQuickAccessPanelProvider() {
		return () -> {
			SpotlightPanel spotlightPanel = quickAccessPanelProvider.get();
			spotlightPanel.setTextField(getAdd());
			spotlightPanel.setMinCharsForFilter(3);
			spotlightPanel.setUseApplyButton(false);
			spotlightPanel.configureUseQueryActions(false);
			spotlightPanel.setLoadTypes(false);
			spotlightPanel.setLoadExistingValues(true);
			return spotlightPanel;
		};
	}
		
	String getModelName(GmMetaModel model) {
		return model.getName().contains(":") ? model.getName().substring(model.getName().lastIndexOf(":")+1, model.getName().length()) : model.getName();
	}
	
	String getTypeName(GmType type) {
		if(type.getTypeSignature() != null)
			return type.getTypeSignature().substring(type.getTypeSignature().lastIndexOf(".")+1, type.getTypeSignature().length());
		else
			return "?";
	}
	
	class GmTypeComparator implements Comparator<GmType>{
		@Override
		public int compare(GmType o1, GmType o2) {
			try {
				if(getTypeName(o1).equalsIgnoreCase(getTypeName(o2)))
					return o1.getTypeSignature().compareTo(o2.getTypeSignature());
				else
					return getTypeName(o1).compareTo(getTypeName(o2));
			}catch(Exception ex) {
				return 0;
			}
		}
	}

	/*
	public Set<RelationshipFilter> getExcludes() {
		Set<RelationshipFilter> filters = new HashSet<>();
		excludes.forEach(entity -> {
			if(entity instanceof GmMetaModel) {
				ModelFilter mf = ModelFilter.T.create();
				mf.setModel((GmMetaModel) entity);
				filters.add(mf);
			}else if(entity instanceof GmEntityType) {
				EntityTypeFilter tf = EntityTypeFilter.T.create();
				tf.setEntityType((GmEntityType) entity);
				filters.add(tf);
			}
		});
		return filters;
	}
	*/

	public void adaptType(GmType type) {
		TypeEntry te = typeEntries.get(type);
		if(te != null) {
			te.adaptTypeSig();
		}
	}

}
