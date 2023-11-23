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

import java.util.Set;
import java.util.function.Function;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.TextLoader;
import com.braintribe.gwt.genericmodel.client.codec.api.GmDecodingContext;
import com.braintribe.gwt.genericmodel.client.codec.jse.JseCodec;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class JseReadTest extends AbstractGwtTest {
	@Override
	protected void tryRun() throws Exception {
		AbsenceInformation.T.create();
		tryRunNew();
		tryRunNew();
	}
	
	protected void tryRunNew() throws Exception {
		TextLoader loader = new TextLoader(GWT.getHostPageBaseURL() + "jseCortex.txt");
		loader.load(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				log("received data");
				JseCodec codec = new JseCodec();
				
				try {
					long s = System.currentTimeMillis();
					Object assembly = codec.decode(result, new GmDecodingContextImpl());
					
					long e = System.currentTimeMillis();
					
					long d = e - s;
					
					log("decoding new jse took " + d + "ms");
					
					log(String.valueOf(assembly));
				} catch (Exception e) {
					logError("error while decoding", e);
				}
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				logError("error while loading", caught);
				
			}
		});
	}


}


class GmDecodingContextImpl implements GmDecodingContext {
	private static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	@Override
	public GenericModelType resolveType(String type) {
		return typeReflection.getType(type);
	}
	
	@Override
	public boolean isLenientDecode() {
		return true;
	}
	
	@Override
	public void ensureTypes(Set<String> types) throws CodecException {
		// empty
	}
	
	@Override
	public void ensureTypes(Set<String> types, AsyncCallback<Void> callback) {
		callback.onSuccess(null);
	}

	@Override
	public ProxyContext getProxyContext() {
		return null;
	}

	@Override
	public Function<Object, GmMetaModel> getIntrinsicModelExtractor() {
		return null;
	}

	@Override
	public GenericEntity create(EntityType<?> entityType) {
		return null;
	}

}
