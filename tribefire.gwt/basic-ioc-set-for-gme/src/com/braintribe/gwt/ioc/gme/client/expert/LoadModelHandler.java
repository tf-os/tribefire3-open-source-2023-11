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
package com.braintribe.gwt.ioc.gme.client.expert;

import java.util.function.Supplier;

import com.braintribe.gm.model.persistence.reflection.api.GetModelAndWorkbenchEnvironment;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.modeller.client.standalone.StandAloneModeler;
import com.braintribe.gwt.notification.client.Notification;
import com.braintribe.gwt.notification.client.NotificationListener;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.processing.async.api.AsyncCallback;

public class LoadModelHandler implements NotificationListener<LoadModelConfig>{
	
	private PersistenceGmSession session;
	private EntityQuery query;
	private LoadModelConfig config;
	private boolean modelEnvSet = false;
	private boolean queryFired = false;
	private String errorMessage = "";
	private StandAloneModeler modeler;
	private Supplier<StandAloneModeler> modelerSupplier;
	private Loader<Void> sessionReadyLoader;
	//private LoadModelHandlerComponent component = LoadModelHandlerComponent.modeler;
	//private MasterDetailConstellation masterDetailConstellation;
	
	String typeName;
	
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public void setSessionReadyLoader(Loader<Void> sessionReadyLoader) {
		this.sessionReadyLoader = sessionReadyLoader;
	}
	
//	public void setMasterDetailConstellation(MasterDetailConstellation masterDetailConstellation) {
//		this.masterDetailConstellation = masterDetailConstellation;
//	}
	
	//public void setComponent(LoadModelHandlerComponent component) {
		//this.component = component;
	//}
	
	public void setModeler(Supplier<StandAloneModeler> modelerSupplier) {
		this.modelerSupplier = modelerSupplier;
	}
		
	public void onModelEnvironmentSet(Object me) {
		ModelEnvironmentDrivenGmSession meds = (ModelEnvironmentDrivenGmSession)session;
		meds.configureModelEnvironment((ModelEnvironment) me, AsyncCallback.of(v -> load(), this::error));
	}
	
	private void load() {
		sessionReadyLoader.load(AsyncCallbacks.of(v -> {
			GlobalState.unmask();
			modelEnvSet = true;
			tryQuery();
		}, this::error));
	}
	
	@Override
	public void onNotificationReceived(Notification<LoadModelConfig> notification) {
		if (modeler != null)
			modeler.setContent(null);
		config = notification.getData();
		String modelName = config.getModelName();
		String modelId = config.getModelId();
		String viewName = config.getViewName();
		typeName = config.getTypeName();
		
//		switch (component) {
//		case smartmapper:
//			pepareTypeQuery(typeName);
//			break;
//		case modeler:
//			prepareModelQuery(modelId, modelName);		
//			break;
//		default:
//			break;
//		}
		
		prepareModelQuery(viewName, modelId, modelName);
		if(modelEnvSet)
			tryQuery();
		else
			loadModelEnvironment();
	}
	
	private void loadModelEnvironment() {
		GlobalState.mask("Loading model environment");
		GetModelAndWorkbenchEnvironment r = GetModelAndWorkbenchEnvironment.T.create();
		r.setAccessId("cortex");
		session.eval(r).get(Future.async(this::error, this::onModelEnvironmentSet));
	}
	
	private void error(Throwable t) {
		GlobalState.unmask();
		t.printStackTrace();
	}
	
	/*private void pepareTypeQuery(String typeName){
		query = EntityQueryBuilder.from(GmEntityType.class).where().property("typeSignature").eq(typeName).tc().negation().joker().done();
		errorMessage = "No type found with name " + typeName;
	}*/
	
	private void prepareModelQuery(String viewName, String modelId, String modelName){
		if(viewName != null) {
			query = EntityQueryBuilder.from(ModellerView.class).where().property("name").eq(viewName).tc().negation().joker().done();
			errorMessage = "No view found with name '" + viewName + "'";
		}else if(modelId != null){
//			long id = 0;
//			try{
//				id = Long.parseLong(modelId);
				query = EntityQueryBuilder.from(GmMetaModel.class).where().property("id").eq(modelId).tc().negation().joker().done();
				errorMessage = "No meta model found with id " + modelId;
//			}catch(Exception ex){
//				//NOP
//			}
		}else if(modelName != null){
//			if(modelName.contains("-"))
//				modelName = modelName.replace("-", "#");
			query = EntityQueryBuilder.from(GmMetaModel.class).where().property("name").eq(modelName).tc().negation().joker().done();
			errorMessage = "No meta model found with name '" + modelName + "'";
		} else
			throw new RuntimeException("One of the following parameters required: par.viewName, par.modelId, par.modelName");
	}
	
	private void tryQuery(){
		if (query == null || !modelEnvSet || queryFired)
			return;
		
		GlobalState.mask("Loading model");
		queryFired = true;
		session.query().entities(query).result(AsyncCallback.of(future -> {
			GlobalState.unmask();
			try {
				queryFired = false;
				GenericEntity candidate = future.first();
				
				if (candidate == null) {
					handleQueryError(new RuntimeException(errorMessage));
					return;
				}
				
				ModelPath mp = new ModelPath();
				mp.add(new RootPathElement(candidate));
				getModeler().setContent(mp);								
			} catch (Exception e) {
				handleQueryError(e);
			}
		}, this::handleQueryError));
	}

	private void handleQueryError(Throwable e) {
		GlobalState.unmask();
		queryFired = false;
		e.printStackTrace();
		ErrorDialog.show("Error while loading model", e);
	}
	
	private StandAloneModeler getModeler() {
		if (modeler != null)
			return modeler;
		
		modeler = modelerSupplier.get();
		modeler.init(session);
		return modeler;
	}
	
	/*private GmEntityType getType(GmMetaModel model, String typeName) {
		if(typeName != null) {
			for(GmEntityType type : getTypes(model)){
				if(type.getTypeSignature().equalsIgnoreCase(typeName.toLowerCase()))
					return type;
			}
			errorMessage = "No type found for " + typeName;
			throw new RuntimeException("No type found for " + typeName);
		}else
			return null;		
	}
	
	private Set<GmEntityType> getTypes(GmMetaModel model){
		Set<GmEntityType> types = model.entityTypeSet();
		model.getDependencies().forEach(dep -> {
			types.addAll(getTypes(dep));
		});
		return types;
	}*/

}
