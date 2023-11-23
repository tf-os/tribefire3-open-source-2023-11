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
package com.braintribe.gwt.modeller.client.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.modeller.client.GmModeller;
import com.braintribe.gwt.modeller.client.filter.GmModellerFilterPanel;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.modellerfilter.NegationRelationshipFilter;
import com.braintribe.model.modellerfilter.WildcardEntityTypeFilter;
import com.braintribe.model.modellerfilter.meta.DefaultModellerView;
import com.braintribe.model.modellerfilter.view.ExcludesFilterContext;
import com.braintribe.model.modellerfilter.view.IncludesFilterContext;
import com.braintribe.model.modellerfilter.view.ModellerSettings;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.modellerfilter.view.RelationshipKindFilterContext;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.GlobalIdFactory;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

public class GmModellerViewPanel extends FlowPanel implements InitializableBean {

	private boolean offline = UrlParameters.getInstance().getParameter("offline") != null ? 
			UrlParameters.getInstance().getParameter("offline").equalsIgnoreCase("true") : 
			false;
			
	private boolean isFetchingModellerViews = false;
	private GmModeller gmModeller;
	//private GmModellerFilterPanel filterPanel;
	// private PersistenceGmSession querySession;
	private PersistenceGmSession session;

	protected GmMetaModel gmMetaModel;
	protected ModelOracle modelOracle;
	public ModelMdResolver cmdResolver;
	protected ModelMetaDataEditor modelMetaDataEditor;
	
	private Map<ModellerView, GmModellerViewSection> viewCache = new HashMap<>();
	
	private FlowPanel mainPanel;

	private TextBox filter;
	private TextButton refresh;
	private TextButton addView;
	
	private ModellerView defaultView;
	private ModellerView currentView;
	
	private boolean readOnly = false;

	public GmModellerViewPanel() {
		
	}
	
	public void setGmMetaModel(GmMetaModel gmMetaModel) {
		this.gmMetaModel = gmMetaModel;	
		
		initMetaModelTools();
	}

	public void setModeller(GmModeller gmModeller) {
		this.gmModeller = gmModeller;
	}

	@SuppressWarnings("unused")
	public void setFilterPanel(GmModellerFilterPanel filterPanel) {
		//this.filterPanel = filterPanel;
	}

	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public FlowPanel getMainPanel() {
		if(mainPanel == null){
			mainPanel = new FlowPanel();
			
			mainPanel.addStyleName("gmModellerViewMainPanel");
		}
		return mainPanel;
	}

	public TextBox getFilter() {
		if (filter == null) {
			filter = new TextBox();

			filter.getElement().setAttribute("style", "border:none; background:none; margin-right:15px; font-size:12pt; flex-grow:1; border-bottom:1px solid silver");
			filter.getElement().setAttribute("placeholder", "Type to search for views...");

			filter.addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					getMainPanel().clear();					
					if (filter.getValue() != null) {
						viewCache.entrySet().forEach((entry) -> {
							String lcValue = filter.getValue().trim().toLowerCase();
							String lcName = entry.getKey().getName().toString().toLowerCase();
							if (lcName.startsWith(lcValue) || lcName.endsWith(lcValue) || lcName.contains(lcValue) || lcName.equalsIgnoreCase(lcValue))
								getMainPanel().add(entry.getValue());
						});
					}
				}
			});
		}
		return filter;
	}

	public TextButton getRefresh() {
		if (refresh == null) {
			refresh = new TextButton();
			refresh.setToolTip("Refresh");
			refresh.setEnabled(true);
			refresh.setIcon(GmViewActionResources.INSTANCE.refresh());
			refresh.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					refresh();
				}
			});
		}
		return refresh;
	}

	public TextButton getAddView() {
		if(addView == null) {
			addView = new TextButton("+ Add view");
			
			addView.addSelectHandler(new SelectHandler() {
				
				@Override
				public void onSelect(SelectEvent event) {
					addView(gmMetaModel);
				}
			});
		}
		return addView;
	}
	
	@Override
	public void intializeBean() throws Exception {
		addStyleName("gmModellerViewPanel");
		
		FlowPanel northPanel = new FlowPanel();
		northPanel.addStyleName("gmModellerViewNorthPanel");
		northPanel.add(getFilter());
		northPanel.add(getRefresh());

		add(northPanel);

		add(getMainPanel());
		
		if(!readOnly) {
			FlowPanel southPanel = new FlowPanel();
			southPanel.addStyleName("gmModellerViewNorthPanel");
			southPanel.add(getAddView());
		
			add(southPanel);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible)
			refresh();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
	}

	public void initMetaModelTools(){
		modelOracle = new BasicModelOracle(this.gmMetaModel);
		cmdResolver = new CmdResolverImpl(modelOracle).getMetaData();
		
		modelMetaDataEditor = BasicModelMetaDataEditor.create(this.gmMetaModel).withSession(session).withGlobalIdFactory(GlobalIdFactory.noGlobalId).done();
		
		DefaultModellerView dmv = cmdResolver.meta(DefaultModellerView.T).exclusive();
		if(dmv != null)
			defaultView = dmv.getDefaultView();
	}

	public void fetchModellerViews() {
		if (!isFetchingModellerViews && gmMetaModel != null) {
			isFetchingModellerViews = true;
			
//			GmMetaModel gmMetaModel = gmModellerPanel.getGmMetaModel();
			EntityQuery query = EntityQueryBuilder.from(ModellerView.class).where().property("metaModel").eq()
					.entity(gmMetaModel)
					.tc().negation().joker()
					.done();

			getMainPanel().clear();
			//session.query().entities(query).result(Future.async(this::error, this::result));
			session.queryCache().entities(query).result(Future.async(this::error, this::result));
		}
	}
	
	public void result(EntityQueryResultConvenience future) {
		try {
			EntityQueryResult result = future.result();
			List<GenericEntity> entities = result.getEntities();
			
			for (GenericEntity entity : entities) {
				ModellerView view = (ModellerView) entity;
				GmModellerViewSection section = viewCache.get(view);
				if(section == null){
					section = new GmModellerViewSection(GmModellerViewPanel.this, session, view, readOnly);
					viewCache.put(view, section);
				}else{
					section.adapt();
				}
				getMainPanel().add(section);
			}
			
		} catch (GmSessionException e) {
			error(e);
		} finally {
			isFetchingModellerViews = false;
		}
	}
	
	private void error(Throwable t) {
		ErrorDialog.show("Error while fetching modeller views", t);
		isFetchingModellerViews = false;
	}

	public void removeView(ModellerView view) {
		GmModellerViewSection section = viewCache.get(view);
		getMainPanel().remove(section);
		
		NestedTransaction transaction = session.getTransaction().beginNestedTransaction();

		session.deleteEntity(view);
		transaction.commit();
		
		setCurrentView(defaultView, true);
	}

	public boolean isDefaultView(ModellerView candidate) {
		return candidate == defaultView;
	}
	
	public boolean isCurrentView(ModellerView candidate) {
		return candidate == currentView;
	}

	public void setCurrentView(ModellerView modellerView, boolean handle) {
		try {
			this.currentView = modellerView;
			if(handle)
				this.gmModeller.handleView(modellerView);
			adaptSections();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDefaultView(ModellerView view) {
		DefaultModellerView dmv = cmdResolver.meta(DefaultModellerView.T).exclusive();
		if(dmv != null)
			dmv.setDefaultView(view);
		
		defaultView = view;
	}
	
	private void addView(GmMetaModel gmMetaModel) {
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		ModellerView modellerView = session.create(ModellerView.T);
		
		String name = gmMetaModel.getName().contains(":") ? gmMetaModel.getName().substring(gmMetaModel.getName().indexOf(":")+1, gmMetaModel.getName().length()) :
			gmMetaModel.getName();
		modellerView.setName(name + "_view_" + (viewCache.size() + 1));
		
		modellerView.setMetaModel(gmMetaModel);
		
		modellerView.setExcludesFilterContext(session.create(ExcludesFilterContext.T));

		WildcardEntityTypeFilter genericEntityTypeFilter = session.create(WildcardEntityTypeFilter.T);
		genericEntityTypeFilter.setWildcardExpression("*com.braintribe.model.generic.GenericEntity*");
		NegationRelationshipFilter negatedGenericEntityTypeFilter = session.create(NegationRelationshipFilter.T);
		negatedGenericEntityTypeFilter.setOperand(genericEntityTypeFilter);
		
		modellerView.getExcludesFilterContext().getOperands().add(negatedGenericEntityTypeFilter);
		
		modellerView.setIncludesFilterContext(session.create(IncludesFilterContext.T));
		modellerView.getIncludesFilterContext().setAllIncludedTypes(true);
		modellerView.getIncludesFilterContext().setDeclaredTypes(true);
		modellerView.getIncludesFilterContext().setExplicitTypes(true);
		
		modellerView.setRelationshipKindFilterContext(session.create(RelationshipKindFilterContext.T));
		modellerView.getRelationshipKindFilterContext().setAggregation(true);
		modellerView.getRelationshipKindFilterContext().setGeneralization(true);
		
		modellerView.setSettings(session.create(ModellerSettings.T));		
		
		modellerView.getSettings().setMaxElements(8);
		modellerView.getSettings().setDepth(3);
			
		nt.commit();
		
		GmModellerViewSection section = new GmModellerViewSection(GmModellerViewPanel.this, session, modellerView, readOnly);
		viewCache.put(modellerView, section);
		getMainPanel().add(section);
		
	}
	
	public void refresh(){
		if (!offline)
			fetchModellerViews();
	}
	
	public void adaptSections(){
		viewCache.values().forEach((section) -> {
			section.adapt();
		});
	}

	public void showFilterPanel() {
		gmModeller.setActiveTabbedWidget("Filter");
	}

}
