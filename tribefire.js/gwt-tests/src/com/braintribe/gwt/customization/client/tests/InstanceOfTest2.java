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
package com.braintribe.gwt.customization.client.tests;

import com.braintribe.gwt.async.client.AsyncUtils;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodel.client.codec.api.DefaultDecodingContext;
import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.resource.Resource;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author peter.gazdik
 */
public class InstanceOfTest2 extends AbstractGwtTest {

	private static final String IS_NOT_ASSIGNABLE_TO_TYPE = " is not assignable to ";
	private static final String IS_ASSIGNABLE_TO_TYPE = " is assignable to ";

	@Override
	protected void tryRun() throws GmfException {
		// positives
		logSeparator();
		log("loading model");
		
		ProxyContext proxyContext = new ProxyContext();
		AsyncUtils.loadStringResource("model.xml").get(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String xml) {
				log("decoding model");
				GmXmlCodec<GmMetaModel> codec = new GmXmlCodec<>();
				Future<GmMetaModel> modelFuture = codec.decodeAsync(xml, new DefaultDecodingContext() {
					@Override
					public boolean isLenientDecode() {
						return true;
					}
					
					@Override
					public ProxyContext getProxyContext() {
						return proxyContext;
					}
				});
				
				modelFuture.get(new AsyncCallback<GmMetaModel>() {
					@Override
					public void onSuccess(GmMetaModel result) {
						log("weaving model");
						
						GMF.getTypeReflection().deploy(result, new com.braintribe.processing.async.api.AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void future) {
								try {
									tryRunTypeChecks();
								}
								catch (Exception e) {
									logError("error while testing types", e);
								}
								
							}
							
							@Override
							public void onFailure(Throwable t) {
								logError("error while weaving model", t);
							}
						});
					}
					
					@Override
					public void onFailure(Throwable caught) {
						logError("error while decoding model", caught);
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				logError("error while loading model", caught);
			}
		});
	}
	
	protected void tryRunTypeChecks() throws Exception {

		// positives
		logSeparator();
		log("checking positive cases for compile-time types");
		checkAssignability(GenericEntity.T, Resource.T, true);
		checkAssignability(StandardStringIdentifiable.T, Resource.T, true);
		checkAssignability(HasMetaData.T, GmMetaModel.T, true);
		checkAssignability(HasMetaData.T, GmEntityType.T, true);
		checkAssignability(HasMetaData.T, GmEnumType.T, true);
		
		// negatives
		logSeparator();
		log("checking negative cases for compile-time types");
		checkAssignability(StandardIdentifiable.T, StandardStringIdentifiable.T, false);
		checkAssignability(HasMetaData.T, Resource.T, false);
		checkAssignability(Resource.T, GmMetaModel.T, false);
		checkAssignability(Resource.T, GmEntityType.T, false);
		checkAssignability(Resource.T, GmEnumType.T, false);
		
		logSeparator();
		log("checking runtime-time types");
		EntityType<?> superEntityType = GMF.getTypeReflection().getType("com.braintribe.model.extensiondeployment.StateChangeProcessor");
		EntityType<?> subEntityType = GMF.getTypeReflection().getType("com.braintribe.model.cartridge.CartridgeStateChangeProcessor");
		
		log("checking positive cases for runtime types");
		checkAssignability(GenericEntity.T, subEntityType, true);
		checkAssignability(superEntityType, subEntityType, true);
	}

	private void checkAssignability(EntityType<?> type, EntityType<?> otherType, boolean expectedAssignability) {
		// type assignability
		boolean assignable = type.isAssignableFrom(otherType);
		String issue = expectedAssignability? IS_ASSIGNABLE_TO_TYPE: IS_NOT_ASSIGNABLE_TO_TYPE;
		
		if (assignable != expectedAssignability) {
			logError("  fail: type " + otherType.getShortName() + issue + type.getShortName());
		}
		else {
			log("  match: type " + otherType.getShortName() + issue + type.getShortName());
		}
		
		// instanceof
		if (!otherType.isAbstract()) {
			GenericEntity instance = otherType.create();
			boolean instanceOf = type.isInstance(instance);
			
			if (instanceOf != expectedAssignability) {
				logError("  fail: instance of " + instance.entityType().getShortName() + issue + type.getShortName());
			}
			else {
				log("  match: instance of " + instance.entityType().getShortName() + issue + type.getShortName());
			}
		}
		
	}
	
}
