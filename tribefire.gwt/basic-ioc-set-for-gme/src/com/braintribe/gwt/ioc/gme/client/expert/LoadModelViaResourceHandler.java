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

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.genericmodel.client.codec.api.DefaultDecodingContext;
import com.braintribe.gwt.genericmodel.client.codec.api.GmAsyncCodec;
import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.gwt.genericmodel.client.codec.jse.JseCodec;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.modeller.client.standalone.StandAloneModeler;
import com.braintribe.gwt.notification.client.Notification;
import com.braintribe.gwt.notification.client.NotificationListener;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.service.api.result.Failure;
import com.google.gwt.xhr.client.XMLHttpRequest;
import com.sencha.gxt.core.shared.FastMap;

public class LoadModelViaResourceHandler implements NotificationListener<LoadModelViaResourceConfig> {
	
	private LoadModelViaResourceConfig config;
	private ModelEnvironmentDrivenGmSession session;
	private String defaultCodec = "gm/jse";
	private Supplier<StandAloneModeler> modelerSupplier;
	
	protected Map<String, GmAsyncCodec<?, ?>> marshallerRegistry;
	
	public Map<String, GmAsyncCodec<?, ?>> getMarshallerRegistry() {
		if(marshallerRegistry == null){
			marshallerRegistry = new FastMap<>();
			
			GmAsyncCodec<?, ?> xmlCodec = new GmXmlCodec<>(GenericEntity.class);
			marshallerRegistry.put("application/xml", xmlCodec);
			marshallerRegistry.put("text/xml", xmlCodec);
			marshallerRegistry.put("gm/xml", xmlCodec);
	
			GmAsyncCodec<?, ?> jseCodec = new JseCodec();
			marshallerRegistry.put("gm/jseh", jseCodec);
			marshallerRegistry.put("gm/jse", jseCodec);
		}
		return marshallerRegistry;
	}
	
	public void setSession(ModelEnvironmentDrivenGmSession session) {
		this.session = session;
	}
	
	@Override
	public void onNotificationReceived(Notification<LoadModelViaResourceConfig> notification) {
		config = notification.getData();
		tryParse();
	}
	
	public void onNotificationReceived(LoadModelViaResourceConfig config) {
		this.config = config;
		tryParse();
	}
	
	private void tryParse() {
		StandAloneModeler modeler = modelerSupplier.get();
		
		modeler.mask("Parsing model information");
		if (config == null)
			return;
		
		XMLHttpRequest request = XMLHttpRequest.create();
		request.open("get", config.getUrl());
		request.setRequestHeader("Accept", "gm/jse");
		
		request.setOnReadyStateChange(xhr -> {
			if (xhr.getReadyState() != XMLHttpRequest.DONE)
				return;
			
			if (xhr.getStatus() == 404) {
				modeler.unmask();
				ErrorDialog.showMessage("No resource was found for url '" + config.getUrl() + "'");
				return;
			}
			
			modeler.unmask();
			GmAsyncCodec<?,Object> codec = (GmAsyncCodec<?, Object>) getMarshallerRegistry().get(config.getCodec() != null ? config.getCodec() : defaultCodec);
			if (codec == null) {
				ErrorDialog.showMessage("No codec was found for '" + codec + "'");
				return;
			}
			
			try {				
				String resp = xhr.getResponseText();
				Object model = codec.decode(resp, new DefaultDecodingContext());

				if(model instanceof Failure) {
					Failure f = (Failure)model;
					ErrorDialog.showMessage("Failure: " + f.getMessage());
					return; 
				}
				
				if (!(model instanceof GmMetaModel)) {
					ErrorDialog.showMessage("Returned information is not a GmMetaModel: " + model.toString());
					return; 
				}
				
				ModelEnvironment modelEnvironment = ModelEnvironment.T.create();
				modelEnvironment.setDataModel((GmMetaModel) model);
				modelEnvironment.setDataAccessId("cortex");
				modelEnvironment.setMetaModelAccessId("cortex");
				session.configureModelEnvironment(modelEnvironment);
				
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(GmMetaModel.T, model));
				
				modeler.init(session);
				modeler.setContent(modelPath);
				
			} catch (CodecException e) {
				modeler.unmask();
				ErrorDialog.show("Error while decoding resource", e);
			}
		});
		
		request.send();
	}
	
	public void setModeler(Supplier<StandAloneModeler> modelerSupplier) {
		this.modelerSupplier = modelerSupplier;
	}
	
	public Supplier<StandAloneModeler> getModelerSupplier() {
		return modelerSupplier;
	}

}
